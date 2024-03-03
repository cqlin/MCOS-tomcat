<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html>
<meta charset="UTF-8">
<t:block>
	<jsp:attribute name="title">REM Meal Service</jsp:attribute>
	<jsp:attribute name="dependencies">
		<jsp:include page="/WEB-INF/head.jsp" />
	</jsp:attribute>
	<jsp:attribute name="more_dependencies"></jsp:attribute>
	<jsp:attribute name="header_fragment">
		<jsp:include page="/WEB-INF/header.jsp" />
	</jsp:attribute>
	<jsp:attribute name="footer_fragment">
		<jsp:include page="/WEB-INF/footer.jsp" />
	</jsp:attribute>
	<jsp:attribute name="content">
		<jsp:include page="/WEB-INF/content.jsp" />
	</jsp:attribute>
	<jsp:body>
		<head>
			<title>${title}</title>
			${dependencies}
			${more_dependencies}
		</head>
       <body>
       		${header_fragment}
			${content}
			${footer_fragment}
       </body>
    </jsp:body>
</t:block>
</html>