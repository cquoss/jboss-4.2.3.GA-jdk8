/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.ejbselect;

/**
 * Utility class for B.
 */
public class BUtil
{
   private static BLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for B. Lookup using JNDI_NAME
    */
   public static BLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = (BLocalHome) initialContext.lookup(BLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}