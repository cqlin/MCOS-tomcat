<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:block>
    <jsp:attribute name="title">Template Page</jsp:attribute>
    <jsp:attribute name="style">
        .footer { font-size: smaller; color: #aaa; }
        .content { margin: 2em; color: #009; }
        ${moreStyle}
    </jsp:attribute>
    <jsp:attribute name="footer">
        <div class="footer">
            Powered by the block tag
        </div>
    </jsp:attribute>
    <jsp:body>
	<!--test comment -->
	<%-- test comment --%>
        <html>
            <head>
                <title>${title}</title>
                <style>
                    ${style}
                </style>
            </head>
            <body>
                <h1>${title}</h1>
                <div class="content">
                    ${content}
                </div>
                ${footer}
            </body>
        </html>
    </jsp:body>
</t:block>
