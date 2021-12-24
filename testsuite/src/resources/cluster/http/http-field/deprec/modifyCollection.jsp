<%@page contentType="text/html"
   import="java.util.*, 
   javax.servlet.ServletContext, 
   org.jboss.test.cluster.web.aop.deprec.*"
%>

<% 
   // Note: The name are hard-coded in the test case as well!!!
   // POJO modify no need to do setAttribute again!
   Student ben = (Student)session.getAttribute("TEST_PERSON");
   List languages = ben.getLanguages();
   languages.add("JAPANESE");
%>
