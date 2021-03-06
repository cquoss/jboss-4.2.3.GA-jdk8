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
package org.jboss.ejb3.test.enventry;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Remote;

import org.jboss.annotation.ejb.RemoteBinding;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision: 60233 $</tt>
 */
@Stateless(name="ExtendedTestEnvEntry")
@Remote(TestEnvEntry.class)
@RemoteBinding(jndiBinding="ExtendedTestEnvEntry")
public class ExtendedTestEnvEntryBean
   extends TestEnvEntryBean
{
   private static final Logger log = Logger.getLogger(ExtendedTestEnvEntryBean.class);
   
   @Resource(name="maxExceptions") private int maxExceptions = 3;
   
   @Resource private int numExceptions = 2;
   
   private int minExceptions = 0;
   
   public int getMaxExceptions()
   {
      return maxExceptions;
   }
   
   public int getNumExceptions()
   {
      return numExceptions;
   }
   
   public int getMinExceptions()
   {
      return minExceptions;
   }

}
