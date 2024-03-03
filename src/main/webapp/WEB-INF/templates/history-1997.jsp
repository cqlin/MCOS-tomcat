<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:block template="history" title="Revised History Lesson">
    <jsp:attribute name="moreStyle">.revised { font-style: italic; }</jsp:attribute>
    <jsp:attribute name="shooter"><span class="revised">Greedo</span></jsp:attribute>
</t:block>