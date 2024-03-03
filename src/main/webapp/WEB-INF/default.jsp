<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Menu Items
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-8">
	<h2>Today's Menu Items</h2>
	<br/>
	<br/>
	<table class="table table-striped">
		<tbody>
			<tr>
				<th scope="col">ID</th>
				<th scope="col">Code</th>
				<th scope="col">Description</th>
				<th scope="col">UnitPrice</th>
				<th scope="col">Quantity</th>
				<th scope="col">Sub-total</th>
			</tr>
			<c:forEach items="${menuItems}" var="item">
			<tr>
				<td>${item.ItemID}</td>
				<td>${item.Itemcode}</td>
				<td>${item.ItemDescription}</td>
				<td>${item.ItemPrice}</td>
				<td>1</td>
				<td>${item.ItemPrice}</td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
</jsp:attribute>
</t:block>