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
package org.jboss.test.messaging;

import org.jboss.messaging.channel.plugins.handler.ChannelHandler;
import org.jboss.messaging.channel.plugins.handler.ExclusiveChannelHandler;
import org.jboss.messaging.interfaces.Consumer;
import org.jboss.messaging.memory.MemoryMessageSet;
import org.jboss.test.jms.BaseJMSTest;

/**
 * A basic test
 * 
 * @author <a href="adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ExclusiveChannelTestCase extends BaseJMSTest
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public ExclusiveChannelTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   public void testSomething()
      throws Exception
   {
      ChannelHandler handler = getExclusiveChannelHandler();
      Consumer consumer = new AcceptAllConsumer();
      TestMessageReference t1 = new TestMessageReference();
      handler.addMessage(t1);
      TestMessageReference t2 = new TestMessageReference();
      handler.addMessage(t2);
      TestMessageReference r = (TestMessageReference) handler.removeMessage(consumer);
      assertEquals(t1.getMessageID(), r.getMessageID());
      r = (TestMessageReference) handler.removeMessage(consumer);
      assertEquals(t2.getMessageID(), r.getMessageID());
   }

   // Protected ------------------------------------------------------

   protected ChannelHandler getExclusiveChannelHandler()
   {
      MemoryMessageSet mms = new MemoryMessageSet(new TestMessageReference.TestMessageReferenceComparator());
      return new ExclusiveChannelHandler(mms);
   }
   
   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------
}
