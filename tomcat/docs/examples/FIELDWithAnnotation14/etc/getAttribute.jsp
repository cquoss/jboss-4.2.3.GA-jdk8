<%@ page import="examples.Student"%>
<%@ page import="examples.Address"%>
<%@ page import="examples.Course"%>
<%@ page import="java.util.*"%>
<%@page contentType="text/html" %>

<%
   Student mary = (Student)session.getAttribute("mary");
   Address addr = (Address)mary.getAddress();

   if( mary == null)
   {
      throw new RuntimeException("getAttribute(): null instance of Mary.");
   }

   Collection col = mary.getCourses();

   if( col == null || col.size() == 0)
   {
      throw new RuntimeException("getAttribute(): null instance of Courses.");
   }
   Course first = null;
   first = (Course)col.iterator().next();
%>

<html>
   <p>Session ID (postfixed by jvmRoute in each node): <%=session.getId()%></p>
   <p>Mary's instructor is:</p>
      <p><%=first.getInstructor()%></p>
      <p>and zip code is:</p>
      <p><%=mary.getAddress().getZip()%> </p>

</html>
