<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:block template="base" title="History Lesson">
    <jsp:attribute name="content" trim="false">
        <p>${shooter} shot first!</p>
        <p>${header}</p>
    </jsp:attribute>
</t:block>