<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Add Operator
</jsp:attribute>
<jsp:attribute name="main">
<div class="col-md-6">

<form id="form1" method="post" action="${pageContext.request.contextPath}/Main?action=NewUser">
	<div class="row">
	<div class="col">
	<h2>Add Operator</h2> 
	<div class="form-group">
	<label for="member">Member Code:</label>
	<input name="member" type="text" id="member" class="form-control" placeholder="Scan member QR code" />
	</div>
	<div class="form-group">
	<label for="username">Operator Login ID:</label>
	<input name="username" type="text" id="username" class="form-control" placeholder="Enter user name less than 20 characters" />
	</div>
	<div class="form-group">
	<label for="txtPassword">Password:</label>
	<input name="txtPassword" type="password" id="txtPassword" class="form-control" placeholder="Enter password" />
	</div>
	<br/>
	<div class="form-group">
    <label for="role"> Role:  </label>
    <select name="role" id="role">
		<option value="MCOS_GUEST">MCOS_GUEST</option>
		<option value="MCOS_OPERATOR">MCOS_OPERATOR</option>
	</select>
	</div>
	<br/>
	<button id="NewUser" name="NewUser" class="btn btn-outline-primary">Add</button>
	</div> 
    </div>
	<br/>
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<span id="lblTime">Response time: ${status.time}</span><br/>
 </form>    

<script>
	$(document).ready(function() {
		$("#username").focus();
		$("#NewUser").click(function() {
			$("#form1").submit();
		})
		//Prevent enter key submit form
		$(window).keydown(function(event){
		   if(event.keyCode == 13) {
		     event.preventDefault();
		     return false;
		   }
		});
	});
</script>
</div>
</jsp:attribute>
</t:block>

