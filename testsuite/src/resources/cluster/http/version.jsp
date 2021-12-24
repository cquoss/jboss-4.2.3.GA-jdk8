<%@page contentType="text/html"
   import="org.jboss.mx.util.*"
   import="javax.management.*"
%>
<%
   MBeanServer server = MBeanServerLocator.locateJBoss();
   ObjectName on = new ObjectName("jboss.cache:service=TomcatClusteringCache");
   String fqn = "/JSESSION/localhost" + request.getContextPath() + "/" + session.getId();
   Object[] params = { fqn, "VERSION" };
   String[] types = new String[] { "java.lang.String", "java.lang.Object" };
   Object version = server.invoke(on, "get", params, types);
%>
<%= version %>
