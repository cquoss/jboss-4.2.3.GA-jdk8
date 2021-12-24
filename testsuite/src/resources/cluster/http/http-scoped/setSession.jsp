<%@page contentType="text/html"
   import="java.util.*,org.jboss.test.cluster.web.Person"
%>

<html>
<center>
<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Person ben=new Person("Ben", 55);
   session.setAttribute("TEST_PERSON", ben);
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</html>
