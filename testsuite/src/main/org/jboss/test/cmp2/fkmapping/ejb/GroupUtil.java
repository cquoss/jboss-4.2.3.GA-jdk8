/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.fkmapping.ejb;

/**
 * Utility class for Group.
 */
public class GroupUtil
{
   private static  GroupLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for Group. Lookup using JNDI_NAME
    */
   public static  GroupLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = ( GroupLocalHome) initialContext.lookup( GroupLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}