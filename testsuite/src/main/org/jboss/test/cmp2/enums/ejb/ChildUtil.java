/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.enums.ejb;

/**
 * Utility class for Child.
 */
public class ChildUtil
{
   private static  ChildLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for Child. Lookup using JNDI_NAME
    */
   public static  ChildLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = ( ChildLocalHome) initialContext.lookup( ChildLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}