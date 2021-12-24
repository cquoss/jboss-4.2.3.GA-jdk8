/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.ejbselect;

/**
 * Utility class for A.
 */
public class AUtil
{

   private static ALocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for A. Lookup using JNDI_NAME
    */
   public static ALocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = (ALocalHome) initialContext.lookup(ALocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}