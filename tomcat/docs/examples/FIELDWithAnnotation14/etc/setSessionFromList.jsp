<%@ page import="examples.Student"%>
<%@ page import="examples.Address"%>
<%@ page import="examples.Course"%>
<%@ page import="examples.RandomString"%>
<%@ page import="java.util.*"%>
<%@page contentType="text/html" %>

<%

   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id);

   int NUM = 100;

   List list = new ArrayList();
   String str = null;
   int zip = 0;
      Address address = new Address();

      str = RandomString.randomstring(5, 25);
      address.setStreet(str);
      str = RandomString.randomstring(5, 25);
      address.setCity(str);
      zip = RandomString.rand(1000, 9999);
      address.setZip(zip);

      Student joe = new Student();
      str = RandomString.randomstring(5, 25);
      joe.setName(str);
      str = RandomString.randomstring(5, 25);
      joe.setSchool(str);

      // Mary and Joe have the same address
      joe.setAddress(address);

   for(int i=0; i < NUM; i++)
   {
      Course foo = new Course();
      str = RandomString.randomstring(5, 25);
      foo.setTitle(str);
      str = RandomString.randomstring(5, 25);
      foo.setInstructor(str);

      joe.addCourse(foo);
   }

   session.setAttribute("joe", joe);
%>

<html>
<body>
<center>
<p>Session ID (postfixed by jvmRoute in each node): <%=session.getId()%></p>
</body>
</html>
