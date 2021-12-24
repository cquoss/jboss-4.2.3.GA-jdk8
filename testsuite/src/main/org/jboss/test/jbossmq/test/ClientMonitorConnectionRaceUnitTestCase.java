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

import org.jboss.mq.ConnectionToken;
import org.jboss.mq.il.ClientIL;
import org.jboss.mq.server.ClientMonitorInterceptor;
import org.jboss.test.jbossmq.JBossMQMicrocontainerTest;
import org.jboss.test.jbossmq.support.MockClientIL;

/**
 * ClientMonitorConnectionRaceUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClientMonitorConnectionRaceUnitTestCase extends JBossMQMicrocontainerTest 
{
   public ClientMonitorConnectionRaceUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testRace() throws Exception
   {
      ClientMonitorInterceptor monitor = (ClientMonitorInterceptor) getBean("ClientMonitorInterceptor");
      ClientIL client = new MockClientIL();
      
      // First ping without an id
      ConnectionToken original = new ConnectionToken(null, client, "session");
      monitor.ping(original, System.currentTimeMillis());
      assertNotNull(monitor.peekClientStats(original));

      // Ping with an id
      ConnectionToken withID = new ConnectionToken("test", client, "session");
      monitor.ping(withID, System.currentTimeMillis());
      assertNull("Token without id should be removed", monitor.peekClientStats(original));
      assertNotNull(monitor.peekClientStats(withID));
   }
}
