<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Report
</jsp:attribute>
<jsp:attribute name="more_dependencies">
	<script src="${pageContext.request.contextPath}/js/csvExport.js" type="text/javascript"></script>
</jsp:attribute>
<jsp:attribute name="main">

<div class="col-md-6">

    <h2>Sunday Reports</h2> 
    <h3>Please select Report type and click the button to generate the report that you need.</h3><br/>
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br/>

<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=Report">
	<div class="row">
	<div class="col-md-3">
	<label for="startdate">Start Date:</label>
	<input type="date" class="form-control" id="startdate" name="startdate" value="${startdate}"/>
	</div>
	<div class="col-md-3">
	<label for="enddate">End Date:</label>
	<input type="date" class="form-control" id="enddate" name="enddate" value="${enddate}"/>
	</div>
	</div>
    <label for="reportType"> Report Type:  </label>
    <select name="ReportType" id="ReportType">
		<option value="order">Order List</option>
		<option value="orderDetail">Order Detail List</option>
		<option value="orderSummary">Order Summary List</option>
		<option value="deposit">Deposit List</option>
	</select> 
    <input type="submit" name="btnSubmit" value="Submit" id="btnSubmit" class="btn btn-outline-primary" /><br/><br/> 
</form>
<c:if test="${not empty reportCols}">
	<div>
	<button onclick="download_table_as_csv('reportTable');">Download as CSV</button>
	<table id="reportTable" class="table table-striped">
		<tr>
		<c:forEach items="${reportCols}" var="item">
			<th scope="col">${item}</th>
		</c:forEach>
		</tr>
		<c:forEach items="${reportValues}" var="row">
		<tr>
		<c:forEach items="${row}" var="o">
			<td>${o}</td>
		</c:forEach>
		</tr>
		</c:forEach>
	</table>
    </div>
</c:if>
	
	<!-- 	
    <input type="submit" name="btnAsst" value="Top off Assist Account" id="btnAsst" class="btn btn-outline-primary">
 	<span id="lblAsst"></span>
	 -->

</div>
</jsp:attribute>
</t:block>