// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.wsrp.core;

import java.io.Serializable;


public class UserContext implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -3667824494806696290L;
   protected java.lang.String userContextKey;
   protected java.lang.String[] userCategories;
   protected UserProfile profile;
   protected Extension[] extensions;

   public UserContext()
   {
   }

   public UserContext(java.lang.String userContextKey, java.lang.String[] userCategories, UserProfile profile, Extension[] extensions)
   {
      this.userContextKey = userContextKey;
      this.userCategories = userCategories;
      this.profile = profile;
      this.extensions = extensions;
   }

   public java.lang.String getUserContextKey()
   {
      return userContextKey;
   }

   public void setUserContextKey(java.lang.String userContextKey)
   {
      this.userContextKey = userContextKey;
   }

   public java.lang.String[] getUserCategories()
   {
      return userCategories;
   }

   public void setUserCategories(java.lang.String[] userCategories)
   {
      this.userCategories = userCategories;
   }

   public UserProfile getProfile()
   {
      return profile;
   }

   public void setProfile(UserProfile profile)
   {
      this.profile = profile;
   }

   public Extension[] getExtensions()
   {
      return extensions;
   }

   public void setExtensions(Extension[] extensions)
   {
      this.extensions = extensions;
   }
}
