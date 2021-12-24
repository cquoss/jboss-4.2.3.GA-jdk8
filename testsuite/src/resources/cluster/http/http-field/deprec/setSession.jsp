<%@page contentType="text/html"
   import="java.util.*, 
   javax.servlet.ServletContext, 
   org.jboss.test.cluster.web.aop.deprec.*"
%>

<html>
<center>
<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Student ben=new Student();
   Student jane=new Student();
   Address addr = new Address();
   addr.setZip(95123);
   addr.setCity("San Jose");
   ben.setAge(100);
   ben.setName("Ben");
   ben.setAddress(addr);
   jane.setAge(50);
   jane.setName("Jane");
   jane.setAddress(addr);

   Course foo = new Course();
   foo.setTitle("Intro to Foo");
   foo.setInstructor("Jones");

   ben.addCourse(foo);
   jane.addCourse(foo);

   session.setAttribute("TEST_PERSON", ben);
   session.setAttribute("WIFE", jane);

   Collection col = ben.getCourses();
   Course first = (Course)col.iterator().next();
   first.setInstructor("Black");

   // Bind ben to the servlet context as well so it can be
   // accessed without involving the session
   ServletContext ctx = getServletConfig().getServletContext();
   ctx.setAttribute("TEST_PERSON", ben);
   ctx.setAttribute("WIFE", jane);
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</html>
