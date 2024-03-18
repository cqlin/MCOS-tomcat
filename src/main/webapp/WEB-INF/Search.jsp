<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Search
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-6">


    <h2>Use Phone Number to Search Members</h2><br>

<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=Search" autocomplete="off">
	<div class="row">
	<div class="col">
    <label for="txtBarcode">Phone Number(10 digits) Or Email:</label> 
    <input name="txtBarcode" type="text" id="txtBarcode" />
    <input type="submit" name="btnSubmit" value="Submit" id="btnSubmit" class="btn btn-outline-primary" />
	</div> 
    </div>
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
 </form>    
    <div>
<c:if test="${not empty result}">
	<table class="table table-striped">
		<tr>
			<th scope="col">First Name</th>
			<th scope="col">Last Name</th>
			<th scope="col">Email</th>
			<th scope="col">MemberID</th>
			<th scope="col">MemberCode</th>
			<th scope="col">Balance</th>
		</tr>
		<c:forEach items="${result}" var="item">
		<tr>
			<td>${item.FIRST_NAME}</td>
			<td>${item.LAST_NAME}</td>
			<td>${item.familyEmail}</td>
			<td>${item.MEMBER_ID}</td>
			<td>${item.MemberCode}</td>
			<td>${item.Balance}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>
	</div>
    <br>
     
<script>
	$(document).ready(function() {
		$("#txtBarcode").focus();
	});
</script>
</div>
</jsp:attribute>
</t:block>