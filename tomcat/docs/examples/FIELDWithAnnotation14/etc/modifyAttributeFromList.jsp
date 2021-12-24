<%@ page import="examples.Student"%>
<%@ page import="examples.Address"%>
<%@ page import="examples.Course"%>
<%@ page import="java.util.*"%>
<%@page contentType="text/html" %>

<% 
   // Note: The name are hard-coded in the test case as well!!!
   // POJO modify no need to do setAttribute again!

   Student joe = (Student)session.getAttribute("joe");
   joe.getAddress().setZip(94086);
   Collection col = joe.getCourses();
   Course first = (Course)col.iterator().next();
   first.setInstructor("White");
%>
<html>

<p>Session ID (postfixed by jvmRoute in each node): <%=session.getId()%></p>
<p>Modifying Joe's instructor to:</p>
   <p><%=first.getInstructor()%></p>
   <p>and zip code to:</p>
   <p><%=joe.getAddress().getZip()%> </p>
</html>
