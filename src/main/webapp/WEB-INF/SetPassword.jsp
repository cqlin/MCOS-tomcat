<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Change Password
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-6">

<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=SetPassword">
	<div class="row">
	<div class="col">
		<h2 style="font-variant: normal">Change Password</h2><br>
		<div class="form-group">
			<label for="txtPasswordOld">Old Password:</label>
			<input name="txtPasswordOld" type="password" id="txtPasswordOld" class="form-control" placeholder="Enter password" />
		</div>
		<div class="form-group">
			<label for="txtPassword1">New Password:</label>
			<input name="txtPassword1" type="password" id="txtPassword1" class="form-control" placeholder="Enter password" />
		</div>
		<div class="form-group">
			<label for="txtPassword2">Confirm Password:</label>
			<input name="txtPassword2" type="password" id="_txtPassword2" class="form-control" placeholder="Enter password" />
		</div>
		<input type="submit" name="submit" value="Submit" id="submit" class="btn btn-outline-primary" />
	</div> 
    </div>
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br/>
 </form>    

<script>
	$(document).ready(function() {
		$("#txtPasswordOld").focus();
	});
</script>
</div>
</jsp:attribute>
</t:block>
