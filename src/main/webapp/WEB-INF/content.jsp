<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:block>
<jsp:attribute name="menu">
<div id="MenuContentPlaceHolder_Menu1">
	<ul class="level1">
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=default">
		<img src="Images/home-black.png" alt="" title="" class="icon" />Home</a></li>
	<% if(request.isUserInRole("MCOS_OPERATOR") || request.isUserInRole("MCOS_ADMIN")) { %>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=LunchOrder">
		<img src="Images/lunch-black.png" alt="" title="" class="icon" />Lunch Order</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=NewDeposit">
		<img src="Images/deposit-black.png" alt="" title="" class="icon" />Deposit</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=Search">
		<img src="Images/search-black.png" alt="" title="" class="icon" />Search Members</a></li>
	<% } %>
	<% if(request.isUserInRole("MCOS_ADMIN")) { %>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=Report">
		<img src="Images/report-black.png" alt="" title="" class="icon" />Sunday Reports</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=NewUser">
		<img src="Images/account-black.png" alt="" title="" class="icon" />Add Operator</a></li>
	<% } %>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=MyAccount">
		<img src="Images/account-black.png" alt="" title="" class="icon" />Account Status</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=SetPassword">
		<img src="Images/account-black.png" alt="" title="" class="icon" />Change Password</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/public/Registration">
		<img src="Images/account-black.png" alt="" title="" class="icon" />Registration</a></li>
		<li><a class="level1" href="${pageContext.request.contextPath}/Main?action=Logout">
		<img src="Images/logout-black.png" alt="" title="" class="icon" />Logout</a></li>
	</ul>
</div>
</jsp:attribute>
<jsp:attribute name="main">
<div>Empty page</div>
</jsp:attribute>
<jsp:body>
<div id="container" class="container-fluid">
	<div class="row">
		<div id="left_column" class="col-md-2">
		${menu}
		<div>
			<br /> 
			<img id="MenuContentPlaceHolder_imageFamily" src="m/F0000.jpg" style="height: 183px; width: 254px;" alt="family"/>
		</div>
	</div>

	<div id="right_column" class="col">
	    ${main}
	</div>

</div>
</div>
<script>
	$(document).ready(function() {
		//$("#left_column ul").width("100%");
	});
</script>
</jsp:body>
</t:block>