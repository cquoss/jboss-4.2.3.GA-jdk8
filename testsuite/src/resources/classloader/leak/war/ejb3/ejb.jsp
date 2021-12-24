<%@page contentType="text/html"%>
<%@page import="javax.naming.*"%>
<%@page import="org.jboss.test.classloader.leak.ejb3.*"%>
<%
  try
  {
      InitialContext ctx = new InitialContext();
      Ejb3StatelessSession ejb3slsb = (Ejb3StatelessSession) ctx.lookup("java:comp/env/ejb/ClassloaderLeakEJB3SLSB");
      ejb3slsb.log("EJB");
      Ejb3StatefulSession ejb3sfsb = (Ejb3StatefulSession) ctx.lookup("java:comp/env/ejb/ClassloaderLeakEJB3SFSB");
      ejb3sfsb.log("EJB");
      Ejb3StatelessSession tlpejb3slsb = (Ejb3StatelessSession) ctx.lookup("java:comp/env/ejb/TLPClassloaderLeakEJB3SLSB");
      tlpejb3slsb.log("EJB");
  }
  catch (Exception e)
  {
      throw new javax.servlet.ServletException(e);
  }
%>
EJB