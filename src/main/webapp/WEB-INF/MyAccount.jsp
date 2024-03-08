<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Account
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-6">

    <h2>Deposit Balance and Recent Transactions</h2>
<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=MyAccount">
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br/>

	<div class="row">
		<div class="col-md-4">
	 	<label for="txtBarcode">Scan Barcode Here:</label>
	 	<input name="txtBarcode" id="txtBarcode" type="text" class="textbox" />
		<input type="submit" style="display: none" /> 
		</div>
 		<br/>  
 	<h2 style="color:red">${warning}</h2>
<c:if test="${not empty member}">
	<div>
	<span id="lblTime">Before Deposit</span>
	<table class="table">
		<tr>
			<th scope="col">CustomerName</th>
			<th scope="col">EMAIL</th>
			<th scope="col">Balance</th>
		</tr>
		<tr>
			<td>${member.FIRST_NAME}&nbsp;${member.LAST_NAME}</td>
			<td>${member.familyEmail}</td>
			<td>${member.Balance}</td>
		</tr>
	</table>
    </div>
</c:if>
	</div>
</form>  
 
<c:if test="${not empty depositHistory}">
    <h3>Recent Deposit History</h3>
    <div class="row">
	<table class="table table-striped">
		<caption>Recent Deposit History</caption>
		<tr>
			<th scope="col">NAME</th>
			<th scope="col">Date</th>
			<th scope="col">DepositType</th>
			<th scope="col">DepositAmount</th>
			<th scope="col">Balance</th>
		</tr>
		<c:forEach items="${depositHistory}" var="item">
		<tr>
			<td>${item.FIRST_NAME}&nbsp;${item.LAST_NAME}</td>
			<td>${item.depositDate}</td>
			<td>${item.DepositType}</td>
			<td>${item.DepositAmount}</td>
			<td>${item.Balance}</td>
		</tr>
		</c:forEach>
	</table>
	</div>
</c:if>

<c:if test="${not empty orderHistory}">
    <h3>Recent Transactions</h3>
    <div class="row">
	<table class="table table-striped">
		<caption>Recent Transactions</caption>
		<tr>
			<th scope="col">OrderID</th>
			<th scope="col">NAME</th>
			<th scope="col">Date</th>
			<th scope="col">OrderAmount</th>
		</tr>
		<c:forEach items="${orderHistory}" var="item">
		<tr>
			<td>${item.OrderID}&nbsp;${item.LAST_NAME}</td>
			<td>${item.MemberName}</td>
			<td>${item.OrderDate}</td>
			<td>${item.OrderAmount}</td>
		</tr>
		</c:forEach>
	</table>
	</div>
</c:if>

<script>
	$(document).ready(function() {
		$("#txtBarcode").focus();
	});
</script>
</div>
</jsp:attribute>
</t:block>