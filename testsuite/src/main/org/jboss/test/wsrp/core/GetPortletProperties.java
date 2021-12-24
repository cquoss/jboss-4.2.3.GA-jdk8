// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.wsrp.core;

import java.io.Serializable;


public class GetPortletProperties implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3259530502580808104L;
   protected RegistrationContext registrationContext;
   protected PortletContext portletContext;
   protected UserContext userContext;
   protected java.lang.String[] names;

   public GetPortletProperties()
   {
   }

   public GetPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, java.lang.String[] names)
   {
      this.registrationContext = registrationContext;
      this.portletContext = portletContext;
      this.userContext = userContext;
      this.names = names;
   }

   public RegistrationContext getRegistrationContext()
   {
      return registrationContext;
   }

   public void setRegistrationContext(RegistrationContext registrationContext)
   {
      this.registrationContext = registrationContext;
   }

   public PortletContext getPortletContext()
   {
      return portletContext;
   }

   public void setPortletContext(PortletContext portletContext)
   {
      this.portletContext = portletContext;
   }

   public UserContext getUserContext()
   {
      return userContext;
   }

   public void setUserContext(UserContext userContext)
   {
      this.userContext = userContext;
   }

   public java.lang.String[] getNames()
   {
      return names;
   }

   public void setNames(java.lang.String[] names)
   {
      this.names = names;
   }
}
