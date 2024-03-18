<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<t:block template="/WEB-INF/base-layout.jsp">
	<jsp:attribute name="title">
	 REM Meal Service - Lunch Order
	</jsp:attribute>
	<jsp:attribute name="main">
<div class="col">

<h2>Lunch Order &nbsp;->&nbsp; Balance: &nbsp;$<span id="lblBalance">${status.balance}</span>
&nbsp;&nbsp; Total: $<span id="lblTotal">${status.total}</span>
</h2>
<br />     
<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=LunchOrder" autocomplete="off">
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br />
     
	<div class="row">
		<div class="col-md-4">
		 	<label for="txtBarcode">Barcode</label>
		 	<input name="txtBarcode" id="txtBarcode" type="text" class="textbox" />
		 	<input name="memberCode" id="memberCode" type="hidden" value="${memberCode}" />
		 	<input name="mode" id="mode" type="hidden" value="${mode}" />
		</div>
		<div class="col-md-4">
			<input type="image" name="btnGO" id="btnGO" src="Images/GO.png"
							alt="GO" style="width: 60px; height: 60px;" />
			<input type="image" name="btnB0031" id="btnB0031"
							src="Images/B0031.png" alt="B0031"
							style="width: 60px; height: 60px;" />
			<input type="image" name="btnB0033" id="btnB0033"
							src="Images/B0033.png" alt="B0033"
							style="width: 60px; height: 60px;" />
			<input type="image" name="btnB0037" id="btnB0037"
							src="Images/B0037.png" alt="B0037"
							style="width: 60px; height: 60px;" />
			<input type="image" name="btnB0035" id="btnB0035"
							src="Images/B0035.png" alt="B0035"
							style="width: 60px; height: 60px;" />
			<input type="image" name="btnC" id="btnC" src="Images/C.png"
							alt="Checkout" style="width: 60px; height: 60px;" />
			<input type="submit" style="display: none" /> 
		</div>
	</div>  
 
 	<h2 style="color:red">${warning}</h2>
    <div>
 <c:if test="${not empty member}">
	<table class="table">
		<tr>
			<th scope="col">CustomerName</th>
			<th scope="col">EMAIL</th>
			<th scope="col">Balance</th>
			<th scope="col">OrderAmount</th>
		</tr>
		<tr>
			<td>${member.FIRST_NAME}&nbsp;${member.LAST_NAME}</td>
			<td>${member.familyEmail}</td>
			<td>${member.Balance}</td>
			<td>${orderAmount}</td>
		</tr>
	</table>
</c:if>
    </div>
    <br />
 
	<div>
<c:if test="${not empty sessionScope.miList}">
	<table class="table table-striped">
		<tr>
			<th scope="col">ID</th>
			<th scope="col">Code</th>
			<th scope="col">Description</th>
			<th scope="col">Price</th>
			<th scope="col">Quantity</th>
		</tr>
		<c:forEach items="${sessionScope.miList}" var="item">
		<tr>
			<td>${item.ItemID}</td>
			<td>${item.Itemcode}</td>
			<td>${item.ItemDescription}</td>
			<td>${item.ItemPrice}</td>
			<td>${item.quantity}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>
    </div>
     
<c:if test="${not empty sessionScope.orderList}">
     <table class="table table-striped">
		<tr>
			<th scope="col">OrderID</th>
			<th scope="col">Member_ID</th>
			<th scope="col">CREATE_DATE</th>
			<th scope="col">OrderAmount</th>
		</tr>
		<c:forEach items="${orderList}" var="item">
		<tr>
			<td>${item.OrderID}</td>
			<td>${item.Member_ID}</td>
			<td>${item.CREATE_DATE}</td>
			<td>${item.OrderAmount}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>
	
<c:if test="${not empty sessionScope.orderDetailList}">
     <table class="table table-striped">
		<tr>
			<th scope="col">OrderID</th>
			<th scope="col">ID</th>
			<th scope="col">Code</th>
			<th scope="col">Description</th>
			<th scope="col">Quantity</th>
			<th scope="col">Price</th>
		</tr>
		<c:forEach items="${orderDetailList}" var="item">
		<tr>
			<td>${item.OrderID}</td>
			<td>${item.ItemID}</td>
			<td>${item.Itemcode}</td>
			<td>${item.ItemDescription}</td>
			<td>${item.ItemQuantity}</td>
			<td>${item.ItemPrice}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>
	
	
</form>
<script>
	$(document).ready(function() {
		$("#txtBarcode").focus();
		$("#btnGO").click(function() {
			$("#form1").submit();
		})
	});
</script>

</div>
</jsp:attribute>
</t:block>