<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.security.Principal" %>
<%@ page isErrorPage="true" %>
<html>
<head>
    <title>Error Page</title>
</head>
<body bgcolor="white">

Error caused by uri: <%= pageContext.getErrorData().getRequestURI() %><br>
Error in servlet=: <%= pageContext.getErrorData().getServletName() %><br>
Status code: <%= pageContext.getErrorData().getStatusCode() %><br>
Throwable : <%= pageContext.getErrorData().getRequestURI() %><br>
<%
    Principal userPrincipal = request.getUserPrincipal();
    String userPrincipalClass = userPrincipal != null ? userPrincipal.getClass().getName() : "none";
    response.addHeader("X-UserPrincipal", userPrincipal.toString());
    response.addHeader("X-UserPrincipalClass", userPrincipalClass);
%>
RequestPrincipal: <%= userPrincipal%><br>
RequestPrincipalClass: <%= userPrincipalClass%><br>
</body>
</html>
