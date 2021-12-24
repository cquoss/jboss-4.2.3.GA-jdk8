package org.jboss.test.naming.restart;


public interface ObjectBinderMBean
{

   void setNamingService(RestartNamingServiceMBean naming);

   /**
    * Bind an object both in standard JNDI (to expose via HA-JNDI) and in our
    * injected NamingServer
    * 
    * @throws Exception
    */
   void start() throws Exception;

   /**
    * Undoes the bindings done in start().
    * 
    * @throws Exception
    */
   void stop() throws Exception;

}