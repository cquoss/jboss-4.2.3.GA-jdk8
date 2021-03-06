/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.cmrtree.ejb;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utility class for A.
 */
public class AUtil
{

   /** Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods). */
   private static ALocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for A. Lookup using JNDI_NAME
    */
   public static ALocalHome getLocalHome() throws NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         InitialContext initialContext = new InitialContext();
         try {
            cachedLocalHome = (ALocalHome) initialContext.lookup(ALocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}