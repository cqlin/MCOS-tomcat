<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Deposit
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-6">

     <h2>Deposit: &nbsp;&nbsp;&nbsp;&nbsp; Message:<span id="lblStatus"></span> </h2><br/>
<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=NewDeposit">
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br/>

	<div class="row">
		<div class="col-md-4">
	 	<label for="txtBarcode">Scan Barcode Here:</label>
	 	<input name="txtBarcode" id="txtBarcode" type="text" class="textbox" />
	 	<input name="memberCode" id="memberCode" type="hidden" value="${memberCode}"/>
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
    <br />
	</div>
	<br/>
	<div class="row">
	<div>
    <label for="txtDepositAmount">Deposit Amount: </label> 
    <input name="txtDepositAmount" type="text" id="txtDepositAmount" pattern="[0-9.\-]+" title="dollar amount only"/>
    <select name="DepositType" id="DepositType">
		<option value="Cash">Cash</option>
		<option value="Check">Check</option>
		<option value="Zelle">Zelle</option>
	</select>
	<br/>
    <input type="submit" value="Add Deposit" id="btnAdd" class="btn btn-outline-primary" />
    </div>
    <br/> 
<c:if test="${not empty confirmMessage}">
    <h3><label>&nbsp;&nbsp;&nbsp;&nbsp; </label><span id="txtAfterDeposit">${confirmMessage}</span></h3><br> 
</c:if>
	</div>
	
<c:if test="${not empty member2}">
	<div>
	<span id="lblTime">After Deposit</span>
	<table class="table">
		<tr>
			<th scope="col">CustomerName</th>
			<th scope="col">EMAIL</th>
			<th scope="col">Balance</th>
		</tr>
		<tr>
			<td>${member2.FIRST_NAME}&nbsp;${member.LAST_NAME}</td>
			<td>${member2.familyEmail}</td>
			<td>${member2.Balance}</td>
		</tr>
	</table>
    </div>
</c:if>
    <br />
    <div class="row">
<c:if test="${not empty depositHistory}">
    <span id="lblDepositHistory">Deposit History: </span>
	<table class="table table-striped">
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
</c:if>
	</div>
 
 	<span id="lblTotal" style="color:Green;font-weight:bold">${lblTotal}</span>

</form>
<script>
	$(document).ready(function() {
		if(!$("#memberCode").val())
			$("#txtBarcode").focus();
		else
			$("#txtDepositAmount").focus();
	});
</script>
</div>
</jsp:attribute>
</t:block>