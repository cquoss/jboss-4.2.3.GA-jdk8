/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree785;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.RemoteBinding;

@Stateless
@RemoteBinding(jndiBinding = TesterBean.JNDI_NAME)
public class TesterBean implements Tester
{
   // Class Members
   public static final String JNDI_NAME = "TesterBean/remote";

   @EJB
   private MyStatelessLocal local;

   public String sayHiTo(String name)
   {
      return local.sayHiTo(name);
   }
}
