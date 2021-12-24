/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.test.timer;

import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.logging.Logger;

/**
 * NOTE: We use a security domain without an unauthenticatedIdentity set.
 * 
 * @author carlo
 *
 */
@Stateless
@Remote(SecuredTimerTester.class)
@SecurityDomain("other")
public class SecuredTimerTesterBean extends BaseTimerTesterBean implements SecuredTimerTester
{
   private static final Logger log = Logger.getLogger(SecuredTimerTesterBean.class);
   
   protected static boolean getCallerPrincipalCalled = false;
   
   @Timeout
   @PermitAll
   public void timeoutHandler(Timer timer)
   {
      log.info("EJB TIMEOUT!!!!");
      timerCalled = true;

      // This should fail, because we don't have a principal (unauthenticatedIdentity is not set)
      try
      {
         log.info("caller principal = " + ctx.getCallerPrincipal());
         getCallerPrincipalCalled = true;
      }
      catch (Exception e){}
    
      //timer.cancel();
   }
   
   public boolean getCallerPrincipalCalled()
   {
      return getCallerPrincipalCalled;
   }
   
   protected void reset()
   {
      getCallerPrincipalCalled = false;
   }
}
