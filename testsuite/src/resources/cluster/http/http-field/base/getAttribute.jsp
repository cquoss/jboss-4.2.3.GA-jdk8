<%@page contentType="text/html"
   import="java.util.*, 
   javax.servlet.ServletContext,
   org.jboss.test.cluster.web.CacheHelper, 
   org.jboss.test.cluster.web.aop.*"
%>

<%
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
   
   Student ben = (Student)session.getAttribute("TEST_PERSON");
   String flag = ben != null ? "true" : "false";
   response.setHeader("X-SawTestHttpAttribute", flag);
   Address addr = (Address)ben.getAddress();
      flag = addr != null ? "true" : "false";
      response.setHeader("X-SawTestHttpAttribute", flag);

   Student jane = (Student)session.getAttribute("WIFE");
   Collection col = jane.getCourses();
   Course first = null;
   if(!(col == null || col.size() == 0))
   {
      first = (Course)col.iterator().next();
   } else
   {
      throw new RuntimeException("getAttribute(): Empty course from student jane.");
   }

   // Bind ben to the servlet context as well so it can be
   // accessed later without involving the session
   ServletContext ctx = getServletConfig().getServletContext();
   ctx.setAttribute("TEST_PERSON", ben);
%>

<%=ben.getName() %>
<%=addr.getZip() %>
<%=ben.getLanguages() %>
<%=first.getInstructor() %>
