package org.remchurch.mealservice.dao;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceSupport {
	private static final Logger logger = Logger.getLogger(DataSourceSupport.class.getName());
	private DataSource ds;

	public DataSourceSupport(String dsName) throws SQLException {
		this.ds = getDataSource(dsName);
	}

	public static DataSource getDataSource(String dsName) throws SQLException {
		try {
			Context ctx = (Context) (new InitialContext()).lookup("java:/comp/env");
			return (DataSource) ctx.lookup(dsName);
		} catch (NamingException e) {
			logger.log(Level.WARNING,"Could not obtain connection from JNDI for datasource name: " + dsName, e);
			throw new SQLException(e.getMessage());
		}
	}

	public Connection getConnection() throws SQLException {
		return this.ds.getConnection();
	}

	/**
	 * Convenience for executing sql update.
	 * 
	 * @param sql
	 * @param paramMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(String sql, Map<String, ?> paramMap, Connection conn) throws SQLException {
		try(NamedParameterStatement statement = new NamedParameterStatement(conn, sql)){
			for (Entry<String, ?> entry : paramMap.entrySet()) {
				statement.setObject(entry.getKey(), entry.getValue());
			}
			// Step 4: Execute query and return ResultSet
			return statement.executeUpdate();
		}
	}

	/**
	 * Convenience method for executing sql procedure
	 * sample sql: "{ call process_my_table(id := ?, name := ?, date := ?) }";
	 * it need to have parameter names.
	 * 
	 * @param sql
	 * @param paramMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static boolean executeCall(String sql, Map<String, ?> paramMap, Connection conn) throws SQLException {
		try(CallableStatement  callStmt = conn.prepareCall(sql)){
			for (Entry<String, ?> entry : paramMap.entrySet()) {
				callStmt.setObject(entry.getKey(), entry.getValue());
			}
			boolean hasResultSet = callStmt.execute();
			if(hasResultSet)
				return true;
			else
				return false;
		}
	}

	public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws SQLException {
		try(Connection conn = getConnection()){
			return queryForList(sql, paramMap, conn);
		}
	}

	/**
	 * Convenience method for executing sql query
	 * 
	 * @param sql
	 * @param paramMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap, Connection conn) throws SQLException {
		List<Map<String, Object>> ret = new ArrayList<>();
		try(NamedParameterStatement statement = new NamedParameterStatement(conn, sql)){
			if(paramMap!=null)
				for (Entry<String, ?> entry : paramMap.entrySet()) {
					statement.setObject(entry.getKey(), entry.getValue());
				}
			ResultSet resultSet = statement.executeQuery();
			int rowNum = 1;
			while (resultSet.next()) {
				ret.add(mapRow(resultSet, rowNum++));
			}
			resultSet.close();
		}
		return ret;
	}


	/**
	 * map a ResultSet row to Map
	 * 
	 * @param rs
	 * @param rowNum
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColumnValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String column = lookupColumnName(rsmd, i);
			mapOfColumnValues.putIfAbsent(getColumnKey(column), getColumnValue(rs, i));
		}
		return mapOfColumnValues;
	}

	/**
	 * Create a Map instance to be used as column map.
	 * <p>By default, a linked case-insensitive Map will be created.
	 * @param columnCount the column count, to be used as initial
	 * capacity for the Map
	 * @return the new Map instance
	 * @see org.springframework.util.LinkedCaseInsensitiveMap
	 */
	protected static Map<String, Object> createColumnMap(int columnCount) {
		return new LinkedHashMap<>(columnCount);
	}

	/**
	 * Determine the key to use for the given column in the column Map.
	 * <p>By default, the supplied column name will be returned unmodified.
	 * @param columnName the column name as returned by the ResultSet
	 * @return the column key to use
	 * @see java.sql.ResultSetMetaData#getColumnName
	 */
	protected static String getColumnKey(String columnName) {
		return columnName;
	}

	/**
	 * Retrieve a JDBC column value from a ResultSet, using the most appropriate
	 * value type. The returned value should be a detached value object, not having
	 * any ties to the active ResultSet: in particular, it should not be a Blob or
	 * Clob object but rather a byte array or String representation, respectively.
	 * <p>Uses the {@code getObject(index)} method, but includes additional "hacks"
	 * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
	 * datatype and a {@code java.sql.Date} for DATE columns leaving out the
	 * time portion: These columns will explicitly be extracted as standard
	 * {@code java.sql.Timestamp} object.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @return the value object
	 * @throws SQLException if thrown by the JDBC API
	 * @see java.sql.Blob
	 * @see java.sql.Clob
	 * @see java.sql.Timestamp
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
	 */
	protected static Object getColumnValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob blob) {
			obj = blob.getBytes(1, (int) blob.length());
		}
		else if (obj instanceof Clob clob) {
			obj = clob.getSubString(1, (int) clob.length());
		}
		else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
			obj = rs.getTimestamp(index);
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

	/**
	 * Determine the column name to use. The column name is determined based on a
	 * lookup using ResultSetMetaData.
	 * <p>This method implementation takes into account recent clarifications
	 * expressed in the JDBC 4.0 specification:
	 * <p><i>columnLabel - the label for the column specified with the SQL AS clause.
	 * If the SQL AS clause was not specified, then the label is the name of the column</i>.
	 * @param resultSetMetaData the current meta-data to use
	 * @param columnIndex the index of the column for the lookup
	 * @return the column name to use
	 * @throws SQLException in case of lookup failure
	 */
	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name!=null && name.length()>0) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

}

