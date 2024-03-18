package org.remchurch.mealservice;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.remchurch.mealservice.dao.DataSourceSupport;
import org.remchurch.mealservice.dao.NamedParameterStatement;
import org.remchurch.mealservice.util.DateTimeBasedFormat;
import org.remchurch.mealservice.util.EmailService;
import org.remchurch.mealservice.util.XlsxRender;

public class MainService {
	private static final Logger logger = Logger.getLogger(MainService.class.getName());
	private DataSourceSupport ds  = null;
	private static List<Map<String, Object>> menuItems = null;
	private static Map<String, Map<String, Object>> menuMap = new HashMap<>();
	private static Map<String, Map<String, Object>> menuIdMap = new HashMap<>();
	private static Map<String, String> paramMap = new HashMap<>();
	private static MainService ms;
	private EmailService es;

	public static MainService getInstance(String dsName) throws SQLException {
		if(ms==null) {
			ms = new MainService(dsName);
			ms.es = new EmailService(ms.getParam("SMTPSRV"),
					ms.getParam("SMTPPORT"),
					"\""+ms.getParam("SMTPFROM_NAME")+"\"<"+ms.getParam("SMTPFROM")+">",
					ms.getParam("SMTPUSER"),
					ms.getParam("SMTPPASS")
					);
			//logger.info("Temp Dir:"+System.getProperty("java.io.tmpdir"));

			long initialDelaySeconds = getInitialDelaySeconds(DayOfWeek.SUNDAY.getValue(), 20, 0);
			ms.es.getExecutor().scheduleAtFixedRate(()->ms.sendWeeklyReport(), initialDelaySeconds, 7L*24*60*60, TimeUnit.SECONDS);
		}
		return ms;
	}

	private static long getInitialDelaySeconds(int dayOfWeek, int hour, int min) {
		LocalTime targetTime = LocalTime.of(hour, min);
		LocalTime currentTime = LocalTime.now();
		LocalDateTime currentDateTime = LocalDateTime.now();
		long secondsUntilTargetTime = currentTime.until(targetTime, ChronoUnit.SECONDS);
		long daysUntilNextSunday = dayOfWeek - currentDateTime.getDayOfWeek().getValue();
		if (daysUntilNextSunday <= 0 && secondsUntilTargetTime<=10) {
			daysUntilNextSunday += 7;
		}
		long initialDelaySeconds = TimeUnit.DAYS.toSeconds(daysUntilNextSunday) + secondsUntilTargetTime;
		return initialDelaySeconds;
	}

	private MainService(String dsName) throws SQLException {
		ds = new DataSourceSupport(dsName);
	}

	public EmailService getEmailService() {
		return es;
	}
	public List<Map<String, Object>> getMenuItems() {
		if(menuItems == null) try{
			String sql = "select * from MenuItem where ItemStartDate<getdate() and ItemEndDate>getdate() order by ItemID";
			menuItems =  ds.queryForList(sql,null);
			for(Map<String, Object> m: menuItems) {
				menuMap.put((String) m.get("Itemcode"), m);
				menuIdMap.put(m.get("ItemID").toString(), m);
			}
		}catch(Exception e) {
			logger.log(Level.SEVERE, "Error getting menu.", e);
		}
		return menuItems;
	}

	public String getParam(String name) {
		if(paramMap.isEmpty()) try {
			List<Map<String, Object>> m = ds.queryForList("select * from param", null);
			for(Map<String, Object> i:m) {
				paramMap.put((String)i.get("PARAM_NAME"), (String)i.get("PARAM_VALUE"));
			}
			//logger.info(paramMap.toString());
		}catch(Exception e) {
			logger.log(Level.SEVERE, "Error getting param.", e);
		}
		return paramMap.get(name);
	}

	public Map<String, Object> getMenuItem(String barcode) {
		if(menuMap.isEmpty())
			getMenuItems();
		return menuMap.get(barcode);
	}

	public Map<String, Map<String, Object>> getMenuMap(){
		if(menuMap.isEmpty())
			getMenuItems();
		return menuMap;
	}

	public Map<String, Map<String, Object>> getMenuIdMap(){
		if(menuIdMap.isEmpty())
			getMenuItems();
		return menuIdMap;
	}

	public List<Map<String, Object>> getFamily(int familyId) throws SQLException {
		String sql = "select m.MEMBER_ID, m.MemberCode, m.FAMILY_ID, m.LAST_NAME, m.FIRST_NAME, m.EMAIL memberEmail, "
				+"f.EMAIL familyEmail, f.Balance, f.FamilyPicture from member m join family f on f.family_id = m.family_id "
				+" where m.FAMILY_ID=:familyId";
		Map<String,Object> param = new HashMap<>();
		param.put("familyId", familyId);
		return ds.queryForList(sql,param);		
	}
	public List<Map<String, Object>> getMember(String barcode) throws SQLException {
		String sql = "select m.MEMBER_ID, m.MemberCode, m.FAMILY_ID, m.LAST_NAME, m.FIRST_NAME, m.EMAIL memberEmail, "
				+"f.EMAIL familyEmail, f.Balance, f.FamilyPicture from member m join family f on f.family_id = m.family_id "
				+" where m.membercode=:barcode";
		Map<String,String> param = new HashMap<>();
		param.put("barcode", barcode);
		return ds.queryForList(sql,param);		
	}
	public List<Map<String, Object>> getMemberDepositHistory(int familyId, int records) throws SQLException {
		String sql = "select top(:records) h.CREATE_DATE depositDate, * from DepositHistory h join Member m on h.Member_ID = m.Member_ID "
				+" where h.Family_ID = :familyId order by DepositID desc";
		Map<String,Object> param = new HashMap<>();
		param.put("familyId", familyId);
		param.put("records", records);
		List<Map<String, Object>> depositList = ds.queryForList(sql,param);
		return depositList;
	}
	public List<Map<String, Object>> getMemberOrderHistory(int familyId, int records) throws SQLException {
		String sql = "select top(:records) o.OrderID, o.Member_ID, m.first_name+' '+m.last_name MemberName, o.Create_Date OrderDate, o.OrderAmount "
				+ " from Orders o join member m on o.member_id = m.member_id "
				+ " where m.Family_ID = :familyId order by o.OrderID desc";
		Map<String,Object> param = new HashMap<>();
		param.put("familyId", familyId);
		param.put("records", records);
		List<Map<String, Object>> depositList = ds.queryForList(sql,param);
		return depositList;
	}
	public void insertDeposit(Map<String, Object> member, String depositType, Double depositAmount, Double balance, String name) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			NamedParameterStatement insertDeposit = new NamedParameterStatement(con, "insert into DepositHistory (DepositType,Family_ID,DepositAmount,USERNAME,CREATE_DATE,Balance,Member_ID) "
					+ "values (:depositType,:familyId,:depositAmount,:name,getdate(),:balance,:memberId)", Statement.RETURN_GENERATED_KEYS);
			NamedParameterStatement updateFamilyBalance = new NamedParameterStatement(con, "update family set balance=:balance,UPDATE_DATE=getdate() where family_id = :familyId");
			insertDeposit.setObject("memberId", member.get("MEMBER_ID"));
			insertDeposit.setObject("depositAmount", depositAmount);
			insertDeposit.setObject("depositType", depositType);
			insertDeposit.setObject("familyId", member.get("FAMILY_ID"));
			insertDeposit.setObject("balance", balance);
			insertDeposit.setObject("name", name);
			insertDeposit.executeUpdate();
			Integer depositId = 0;
			ResultSet result = insertDeposit.getGeneratedKeys();
			if(result.next())
				depositId = result.getInt(1);
			updateFamilyBalance.setObject("balance", balance);
			updateFamilyBalance.setObject("familyId", member.get("FAMILY_ID"));
			updateFamilyBalance.executeUpdate();
			con.commit();
			insertDeposit.close();
			updateFamilyBalance.close();
		}
	}
	public List<Map<String, Object>> getMemberOrderToday(String memberId) throws SQLException {
		String sql = "select * from orders o join orderDetail od on o.orderid = od.orderid where member_id = :memberId and CREATE_DATE>getdate()-0.5 order by od.orderid,od.orderdetailid";
		return getMemberOrder(memberId,sql);
	}
	public List<Map<String, Object>> getMemberOrder(String memberId, String sql) throws SQLException {
		Map<String,String> param = new HashMap<>();
		param.put("memberId", memberId);
		List<Map<String, Object>> temp = ds.queryForList(sql,param);
		List<Map<String, Object>> orderList = new ArrayList<>();
		Integer orderId = 0;
		Map<String, Object> order = null;
		List<Map<String, Object>> orderDetail = new ArrayList<>();
		for(Map<String, Object> r:temp) {
			if(!orderId.equals(r.get("OrderID"))) {
				orderId = (Integer) r.get("OrderID");
				order = new HashMap<>(r);
				orderList.add(order);
				orderDetail = new ArrayList<>();
				order.put("orderDetail", orderDetail);
			}
			orderDetail.add(r);
		}
		return orderList;
	}
	public int InsertOrderDetailList(Map<String, Object> member, List<Map<String, Object>> miList, double amount, double balance, String name) throws SQLException {

		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			NamedParameterStatement insertOrder = new NamedParameterStatement(con, "insert into orders (Member_ID,OrderDate,OrderAmount,USERNAME,CREATE_DATE,Balance) "
					+ "values (:memberId,:date,:amount,:name,getdate(),:balance)", Statement.RETURN_GENERATED_KEYS);
			NamedParameterStatement insertOrderDetail = new NamedParameterStatement(con, "insert into orderdetail (OrderID,ItemID,ItemQuantity) values (:orderId,:itemId,:itemQuantity)", new String[] { "OrderDetailID" });
			NamedParameterStatement updateFamilyBalance = new NamedParameterStatement(con, "update family set balance=:balance,UPDATE_DATE=getdate() where family_id = :familyId");
			insertOrder.setObject("memberId", member.get("MEMBER_ID"));
			insertOrder.setObject("date", new Date());
			insertOrder.setObject("amount", amount);
			insertOrder.setObject("balance", balance);
			insertOrder.setObject("name", name);
			insertOrder.executeUpdate();
			Integer orderId = 0;
			ResultSet result = insertOrder.getGeneratedKeys();
			if(result.next())
				orderId = result.getInt(1);
			for(Map<String, Object> mi:miList) {
				insertOrderDetail.setObject("orderId", orderId);
				insertOrderDetail.setObject("itemId", mi.get("ItemID"));
				insertOrderDetail.setObject("itemQuantity", mi.get("quantity"));
				insertOrderDetail.executeUpdate();
				result = insertOrderDetail.getGeneratedKeys();
				if(result.next()) {
					Integer orderDetailId = result.getInt("GENERATED_KEYS");//only support this column name
				}
			}
			updateFamilyBalance.setObject("balance", balance);
			updateFamilyBalance.setObject("familyId", member.get("FAMILY_ID"));
			updateFamilyBalance.executeUpdate();
			con.commit();
			insertOrderDetail.close();
			insertOrder.close();
			updateFamilyBalance.close();
			return orderId;
		}

	}
	public Map<String, Object> getCurrentDepositTotal(String name) throws SQLException {
		String sql = "select DepositType,sum(DepositAmount) total from DepositHistory where CREATE_DATE>getdate()-0.5 and USERNAME=:name group by DepositType";
		Map<String,String> param = new HashMap<>();
		param.put("name", name);
		List<Map<String, Object>> l = ds.queryForList(sql,param);
		Map<String, Object> ret = new HashMap<>();
		for(Map<String, Object> r:l)
			ret.put((String)r.get("DepositType"), r.get("total"));
		return ret;
	}

	public List<Map<String, Object>> getOrderReport(String startdate, String enddate) throws SQLException {
		String sql = "select o.OrderID, o.Member_ID, m.first_name+' '+m.last_name MemberName, o.Create_Date OrderDate, o.OrderAmount "
				+ " from Orders o join member m on o.member_id = m.member_id "
				+ " where o.create_date between convert(datetime, :startdate, 102) and convert(datetime, :enddate, 102)+1 "
				+ " order by o.OrderID";
		Map<String,Object> param = new HashMap<>();
		param.put("startdate", startdate);
		param.put("enddate", enddate);
		return ds.queryForList(sql,param);
	}
	public List<Map<String, Object>> getOrderDetailReport(String startdate, String enddate) throws SQLException {
		String sql = "select o.OrderID, o.Member_ID, m.first_name+' '+m.last_name MemberName, d.ItemID,mi.ItemDescription,mi.ItemPrice,d.ItemQuantity "
				+ " from Orders o join member m on o.member_id = m.member_id  join OrderDetail d on o.OrderId = d.OrderId join MenuItem mi on d.itemid=mi.itemid "
				+ "where o.create_date between convert(datetime, :startdate, 102) and convert(datetime, :enddate, 102)+1 "
				+ "order by o.OrderID, d.OrderDetailID";
		Map<String,Object> param = new HashMap<>();
		param.put("startdate", startdate);
		param.put("enddate", enddate);
		return ds.queryForList(sql,param);
	}
	public List<Map<String, Object>> getOrderSummaryReport(String startdate, String enddate) throws SQLException {
		String sql = "select d.ItemID,mi.ItemDescription,mi.ItemPrice, sum(d.ItemQuantity), sum(d.ItemQuantity*mi.ItemPrice) "
				+ " from Orders o join OrderDetail d on o.OrderId = d.OrderId join MenuItem mi on d.itemid=mi.itemid "
				+ "where o.create_date between convert(datetime, :startdate, 102) and convert(datetime, :enddate, 102)+1 "
				+ "group by d.ItemID,mi.ItemDescription,mi.ItemPrice order by d.ItemID;";
		Map<String,Object> param = new HashMap<>();
		param.put("startdate", startdate);
		param.put("enddate", enddate);
		return ds.queryForList(sql,param);
	}
	public List<Map<String, Object>> getDepositReport(String startdate, String enddate) throws SQLException {
		String sql = "select m.first_name+' '+m.last_name MemberName, d.USERNAME Cashier, d.Create_Date, d.DepositType, d.DepositAmount "
				+ "  from DepositHistory d join member m on d.member_id = m.member_id "
				+ " where d.CREATE_DATE between convert(datetime, :startdate, 102) and convert(datetime, :enddate, 102)+1 "
				+ " order by d.depositId";
		Map<String,Object> param = new HashMap<>();
		param.put("startdate", startdate);
		param.put("enddate", enddate);
		return ds.queryForList(sql,param);
	}
	public List<Map<String, Object>> searchMember(String val) throws SQLException {
		if(val==null || val.isBlank())
			return Collections.emptyList();
		String value = val.replace("(", "").replace(")", "").replace(" ", "");
		if(value.length()>0 && value.charAt(0)>='0' && value.charAt(0)<='9')
			value = value.replace("-", "");
		String sql = "select m.MEMBER_ID, m.MemberCode, m.FAMILY_ID, m.LAST_NAME, m.FIRST_NAME, m.EMAIL memberEmail, "
				+ "f.EMAIL familyEmail, f.Balance, f.FamilyPicture "
				+ "  from member m join family f on f.family_id = m.family_id where "
				+ " f.email = :value "
				+ " OR replace(replace(replace(replace(f.HOME_PHONE, '(',''),')',''),' ',''),'-','') = :value"
				+ " OR m.LAST_NAME = :value OR m.FIRST_NAME = :value";
		Map<String,Object> param = new HashMap<>();
		param.put("value", value);
		return ds.queryForList(sql,param);
	}
	public List<String> getRoles(String username, String password, List<String> userInfo) throws SQLException {
		if(username == null || username.isBlank())
			return null; //return null to indicate failed login
		username = username.toUpperCase();
		String sql = "select u.[MEMBER_ID] id,m.FIRST_NAME+' '+m.LAST_NAME name,[User_Name] username "
				+ "from [MCOS].[dbo].[users] u join [MCOS].[dbo].[Member] m on u.MEMBER_ID=m.MEMBER_ID "
				+ "where [User_Name]=:username and [Password]=HashBytes('SHA2_256', :password)";
		Map<String,Object> param = new HashMap<>();
		param.put("username", username);
		param.put("password", password);
		List<Map<String, Object>> result = ds.queryForList(sql,param);
		if(result.isEmpty())
			return null; //return null to indicate failed login
		userInfo.add((String) result.get(0).get("name"));
		userInfo.add(result.get(0).get("id").toString());
		userInfo.add(username);

		sql = "select [Role] from [MCOS].[dbo].[user_roles] where [User_Name]=:username";
		result = ds.queryForList(sql,param);
		List<String> ret = new ArrayList<>();
		for(Map<String, Object> r:result) {
			ret.add((String) r.get("Role"));
		}
		return ret;
	}
	public void addUser(String username, String password, String member, String role) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			NamedParameterStatement insertUser = new NamedParameterStatement(con, "insert into [MCOS].[dbo].[users] ([User_Name],[Password],[MEMBER_ID],[CREATE_DATE],[UPDATE_DATE]) "
					+ "values (:username,HashBytes('SHA2_256', :password),:member,GETDATE(),GETDATE())");
			insertUser.setObject("username", username);
			insertUser.setObject("password", password);
			insertUser.setObject("member", member);
			insertUser.executeUpdate();
			NamedParameterStatement insertRole = new NamedParameterStatement(con, "insert into [MCOS].[dbo].[user_roles] ([User_Name],[Role]) values (:username,:role)");
			insertRole.setObject("username", username);
			insertRole.setObject("role", role);
			insertRole.executeUpdate();
			con.commit();
			insertUser.close();
			insertRole.close();
		}
	}
	public boolean changePassword(String username, String oldPassword, String newPassword) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			NamedParameterStatement changePassword = new NamedParameterStatement(con, "update [MCOS].[dbo].[users] set [Password]=HashBytes('SHA2_256', :newPassword),UPDATE_DATE=getdate() "
					+ "where [User_Name]=:username and [Password]=HashBytes('SHA2_256', :oldPassword)");
			changePassword.setObject("username", username);
			changePassword.setObject("oldPassword", oldPassword);
			changePassword.setObject("newPassword", newPassword);
			int num = changePassword.executeUpdate();
			con.commit();
			changePassword.close();
			return num>0;
		}
	}
	public boolean updateFamily(int familyId, String email, String phone, String lastname, String firstname) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			NamedParameterStatement updateFamily = new NamedParameterStatement(con, "update family set email=:email, name=:name, HOME_PHONE=:phone, UPDATE_DATE=getdate() "
					+ "where family_id=:familyId");
			updateFamily.setObject("familyId", familyId);
			updateFamily.setObject("email", email);
			updateFamily.setObject("phone", phone);
			updateFamily.setObject("name",  lastname+", "+firstname);
			int num = updateFamily.executeUpdate();
			NamedParameterStatement updatePrimaryMember = new NamedParameterStatement(con, "update member set last_name=:lastname, first_name=:firstname, UPDATE_DATE=getdate() "
					+ "where family_id=:familyId");
			updatePrimaryMember.setObject("familyId", familyId);
			updatePrimaryMember.setObject("lastname", lastname);
			updatePrimaryMember.setObject("firstname", firstname);
			num = updatePrimaryMember.executeUpdate();
			con.commit();
			updatePrimaryMember.close();
			updateFamily.close();
			return num>0;
		}
	}
	public int insertFamily(String email, String phone, String lastname, String firstname) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			int familyId = (Integer)DataSourceSupport.queryForList("select max(family_id) familyId from family", null, con).get(0).get("familyId")+1;
			NamedParameterStatement insertFamily = new NamedParameterStatement(con, "insert into family (family_id, email, name, HOME_PHONE, Balance, CREATE_DATE) values (:familyId,:email,:name,:phone,0,getdate())");
			insertFamily.setObject("familyId", familyId);
			insertFamily.setObject("email", email);
			insertFamily.setObject("phone", phone);
			insertFamily.setObject("name",  lastname+", "+firstname);
			int num = insertFamily.executeUpdate();
			con.commit();
			insertFamily.close();
			return familyId;
		}
	}
	public int insertMember(int familyId, String lastname, String firstname) throws SQLException {
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			int memberId = (Integer)DataSourceSupport.queryForList("select max(member_id) memberId from member", null, con).get(0).get("memberId")+1;
			NamedParameterStatement insertMember = new NamedParameterStatement(con, "insert into member (member_id, family_id, MemberCode, last_name, first_name, CREATE_DATE) "+
					"values (:memberId, :familyId, 'M'+RIGHT(REPLACE(STR(:memberId),' ','0'),5)+LEFT(dbo.[STRIP_CHARACTERS](UPPER(SUBSTRING(master.dbo.fn_varbintohexstr(HashBytes('MD5', STR(:memberId))), 3, 32)),'^a-z'),2), :lastname, :firstname, getdate())");
			insertMember.setObject("memberId", memberId);
			insertMember.setObject("familyId", familyId);
			insertMember.setObject("lastname", lastname);
			insertMember.setObject("firstname", firstname);
			int num = insertMember.executeUpdate();
			con.commit();
			insertMember.close();
			return memberId;
		}
	}

	public void sendWeeklyReport() {
		XlsxRender render = new XlsxRender();
		String today = DateTimeBasedFormat.getCurrentDate();
		//today = "2024-03-10";
		try {
			List<Map<String, Object>> r = this.getOrderReport(today,today);
			if(!r.isEmpty()) {
				List<String> cols = new ArrayList<>(r.get(0).keySet());
				render.renderReport(r, "OrderReport", cols);
			}
			r = this.getOrderDetailReport(today,today);
			if(!r.isEmpty()) {
				List<String> cols = new ArrayList<>(r.get(0).keySet());
				render.renderReport(r, "OrderDetailReport", cols);
			}
			r = this.getOrderSummaryReport(today,today);
			if(!r.isEmpty()) {
				List<String> cols = new ArrayList<>(r.get(0).keySet());
				render.renderReport(r, "OrderSummaryReport", cols);
			}
			r = this.getDepositReport(today,today);
			if(!r.isEmpty()) {
				List<String> cols = new ArrayList<>(r.get(0).keySet());
				render.renderReport(r, "DepositReport", cols);
			}
			File file = render.writeToFile(	System.getProperty("java.io.tmpdir")+"/Report-"+today+".xlsx");

			String subject = "REM lunch report "+today;
			String email = this.getParam("REPORT_EMAIL");
			String emailBody = subject;

			es.emailAsync(email,subject,emailBody,file.getPath());
		}catch (Exception e) {
			logger.log(Level.SEVERE, "error sending weekly report:",e);
		}
	}

}
