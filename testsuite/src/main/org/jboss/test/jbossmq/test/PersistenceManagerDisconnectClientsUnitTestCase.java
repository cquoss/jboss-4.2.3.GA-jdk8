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
package org.jboss.test.jbossmq.test;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * A test to make sure exception listeners are fired when the persistence manager service is stopped
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class PersistenceManagerDisconnectClientsUnitTestCase extends AbstractRestartDisconnectClientsTest
{
   static ObjectName PM = ObjectNameFactory.create("jboss.mq:service=PersistenceManager");

   public PersistenceManagerDisconnectClientsUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   protected ObjectName getRestartObjectName()
   {
      return PM;
   }
}
