/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.test.regression.ejbthree376.unit;

import org.jboss.ejb3.test.regression.ejbthree376.StatelessRemote;
import org.jboss.ejb3.test.regression.ejbthree376.StatusRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: InterceptorTestCase.java 60233 2007-02-03 10:13:23Z wolfc $
 */

public class InterceptorTestCase
extends JBossTestCase
{
   org.apache.log4j.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public InterceptorTestCase(String name)
   {

      super(name);

   }

   public void testInterceptionWithNoSuperClassAroundInvoke() throws Exception
   {
      StatelessRemote slWithInterceptor = (StatelessRemote) getInitialContext().lookup("StatelessBean/remote");
      StatusRemote status = (StatusRemote)getInitialContext().lookup("StatusBean/remote");

      status.clear();
      slWithInterceptor.method();
      assertTrue(status.getInterceptorIntercepted());
      assertFalse(status.getBeanIntercepted());
      
      status.clear();
      slWithInterceptor.superMethod();
      assertTrue(status.getInterceptorIntercepted());
      assertFalse(status.getBeanIntercepted());
   }

   public void testInterceptionWithSuperClassAroundInvoke() throws Exception
   {
      StatelessRemote slWithInterceptorAndBean = (StatelessRemote) getInitialContext().lookup("StatelessWithBeanInterceptorBean/remote");
      StatusRemote status = (StatusRemote)getInitialContext().lookup("StatusBean/remote");

      status.clear();
      slWithInterceptorAndBean.method();
      assertTrue(status.getInterceptorIntercepted());
      assertTrue(status.getBeanIntercepted());
      
      status.clear();
      slWithInterceptorAndBean.superMethod();
      assertTrue(status.getInterceptorIntercepted());
      assertTrue(status.getBeanIntercepted());
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(InterceptorTestCase.class, "regression-ejbthree376.jar");
   }

}
