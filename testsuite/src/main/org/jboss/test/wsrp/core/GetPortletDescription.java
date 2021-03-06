// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.wsrp.core;

import java.io.Serializable;


public class GetPortletDescription implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6639983285673924876L;
   protected RegistrationContext registrationContext;
   protected PortletContext portletContext;
   protected UserContext userContext;
   protected java.lang.String[] desiredLocales;

   public GetPortletDescription()
   {
   }

   public GetPortletDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, java.lang.String[] desiredLocales)
   {
      this.registrationContext = registrationContext;
      this.portletContext = portletContext;
      this.userContext = userContext;
      this.desiredLocales = desiredLocales;
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

   public java.lang.String[] getDesiredLocales()
   {
      return desiredLocales;
   }

   public void setDesiredLocales(java.lang.String[] desiredLocales)
   {
      this.desiredLocales = desiredLocales;
   }
}
