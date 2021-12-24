/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.fkmapping.ejb;

/**
 * Utility class for Department.
 */
public class DepartmentUtil
{
   private static  DepartmentLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for Department. Lookup using JNDI_NAME
    */
   public static  DepartmentLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = ( DepartmentLocalHome) initialContext.lookup( DepartmentLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}