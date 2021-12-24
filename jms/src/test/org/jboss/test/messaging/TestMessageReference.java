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

import java.util.Comparator;

import org.jboss.messaging.interfaces.MessageAddress;
import org.jboss.messaging.interfaces.MessageReference;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * A simple implementation of a message reference
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class TestMessageReference implements MessageReference
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   Long messageID;
   
   // Static --------------------------------------------------------

   private static final SynchronizedLong nextMessageID = new SynchronizedLong(0);

   // Constructors --------------------------------------------------
   
   public TestMessageReference()
   {
      messageID = new Long(nextMessageID.increment());
   }
   
   // Public --------------------------------------------------------
   
   // MessageReference implementation -------------------------------

   public Comparable getMessageID()
   {
      return messageID;
   }

   public void release()
   {
   }
   
   public MessageAddress getMessageAddress()
   {
      return null;
   }

   public int getMessagePriority()
   {
      return 0;
   }

   public boolean isGuaranteed()
   {
      return false;
   }
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------

   public static class TestMessageReferenceComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         Comparable m1 = ((TestMessageReference) o1).getMessageID();
         Comparable m2 = ((TestMessageReference) o2).getMessageID();
         return m1.compareTo(m2);
      }
   }
}
