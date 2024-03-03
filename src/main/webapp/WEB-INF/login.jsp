<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
	if(request.getAttribute("target") == null){
		String targetURL = null;

		// this URL is stored in the header by container, this is specific for Tomcat 6.0 only
		String queryURL  = (String) request.getAttribute("javax.servlet.forward.request_uri");

		// Tomcat seems to separate the URI into base and parameters - append the parameters...
		String queryData = (String) request.getAttribute("javax.servlet.forward.query_string");

		if(null != queryURL )
			targetURL  =       queryURL ;
		if(null != queryData)
			targetURL += "?" + queryData;

		if(null == targetURL)
			targetURL = request.getContextPath();
		if(null == targetURL)
			targetURL = "/"; // web server ROOT
		request.setAttribute("target", targetURL);
	}
%>
<t:block template="/WEB-INF/base-layout.jsp" title="REM Meal Service Login">
<jsp:attribute name="content">
	<div class="row"
		style="background: radial-gradient(white, rgb(16, 160, 48)); background: -moz-radial-gradient(white, rgb(16, 160, 48))">
		<div class="col-sm-4"></div>
		<div class="col-sm-4" style="background-color: rgb(224, 255, 192)">
			<h3 style="font-variant: normal">${errorMsg}</h3>
			<h2 style="font-variant: normal">Please login</h2>
			<br />
			<form method="POST" action="login?action=login&target=${target}">
			<div class="form-group">
				<label for="j_username">User Name:</label>
				<input type="text" name="j_username" id="j_username" class="form-control"
					placeholder="Enter user name" />
			</div>
			<div class="form-group">
				<label for="j_password">Password:</label>
				<input type="password" name="j_password" id="j_password" class="form-control"
					placeholder="Enter password" />
			</div>
			<br />
			<input type="submit" name="Login" class="btn btn-default btn-lg"
				Style="font-weight: bold; margin-bottom: 30px; float: right; width: 120px; text-align: left" />
				</form>
		</div>
		<div class="col-sm-4"></div>
	</div>
</jsp:attribute>
</t:block>