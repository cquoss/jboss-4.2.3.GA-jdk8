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
package org.jboss.test.messagedriven.support;

import javax.jms.Message;

/**
 * Check a message property
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class CheckJMSDestinationOperation extends Operation
{
   protected int msgNo;
   protected String destination;
   
   public CheckJMSDestinationOperation(BasicMessageDrivenUnitTest test, int msgNo, String destination)
   {
      super(test);
      this.msgNo = msgNo;
      this.destination = destination;
   }

   public void run() throws Exception
   {
      Message message = (Message) test.getMessages().get(msgNo);
      String actual = message.getJMSDestination().toString();
      if (actual == null || actual.startsWith(destination) == false)
         throw new Exception("For msgNo=" + msgNo + " destination=" + actual + " Expected=" + destination + " msg=" + message);
   }
}
