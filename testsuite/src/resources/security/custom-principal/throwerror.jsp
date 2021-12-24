<%@page import="java.security.Principal" %>
<%@page errorPage="errorpage.jsp?debug=log" %>
<%
	if( true )
		throw new IllegalStateException("throwerror.jsp caused this");
%>
