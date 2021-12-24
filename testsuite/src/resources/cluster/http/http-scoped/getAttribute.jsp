<%@page contentType="text/html"
   import="java.util.*,org.jboss.test.cluster.web.Person"
%>

<% Person joe = ((Person)session.getAttribute("TEST_PERSON"));
        String flag = joe != null ? "true" : "false";
        response.setHeader("X-SawTestHttpAttribute", flag);
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
%>
<%= ((Person)session.getAttribute("TEST_PERSON")).getName() %>
