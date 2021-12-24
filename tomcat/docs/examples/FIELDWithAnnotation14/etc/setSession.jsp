<%@ page import="examples.Student"%>
<%@ page import="examples.Address"%>
<%@ page import="examples.Course"%>
<%@ page import="java.util.*"%>
<%@page contentType="text/html" %>

<%
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 


   Student mary = new Student();
   mary.setName("Mary Smith");

   Address address = new Address();
   address.setStreet("456 Oak Drive");
   address.setCity("Pleasantville, CA");
   address.setZip(94555);

   mary.setAddress(address);

   Student joe = new Student();
   joe.setName("Joe Smith");
   joe.setSchool("Engineering");

   // Mary and Joe have the same address
   joe.setAddress(address);

   Course foo = new Course();
   foo.setTitle("Intro to Foo");
   foo.setInstructor("Jones");

   joe.addCourse(foo);
   mary.addCourse(foo);

   session.setAttribute("joe", joe);
   session.setAttribute("mary", mary);
   mary.getAddress();

   Collection col = joe.getCourses();
   Course first = (Course)col.iterator().next();
   first.setInstructor("Black");
%>

<html>
<body>
<center>
<p>Session ID (postfixed by jvmRoute in each node): <%=session.getId()%></p>
<p>Setting Joe's instructor to:</p>
   <p><%=first.getInstructor()%></p>
   <p>and zip code to:</p>
   <p><%=joe.getAddress().getZip()%> </p>
</body>
</html>
