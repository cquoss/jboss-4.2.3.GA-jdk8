<%@page contentType="text/html"
   import="java.util.*, 
   javax.servlet.ServletContext, 
   org.jboss.test.cluster.web.aop.*"
%>

<% 
   // Note: The name are hard-coded in the test case as well!!!
   // POJO modify no need to do setAttribute again!
   Student ben = (Student)session.getAttribute("TEST_PERSON");
   ben.setName("Joe");
   ben.setAge(60);
   ben.getAddress().setZip(94086);

   Student jane = (Student)session.getAttribute("WIFE");
   if( jane.getAddress().getZip() != 94086 )
   {
      throw new RuntimeException("modifyAttribute(): address zip is not modified properly.");
   }

   List lang = new ArrayList();
   lang.add("English");
   lang.add("Holo");
   ben.setLanguages(lang);

   Collection col = ben.getCourses();
   Course first = (Course)col.iterator().next();
   first.setInstructor("White");
%>
