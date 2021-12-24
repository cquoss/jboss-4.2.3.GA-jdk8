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
package org.jboss.jms.server.list.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jboss.jms.server.MessageReference;
import org.jboss.jms.server.list.MessageList;
import org.jboss.jms.server.list.StandardMessageComparator;

import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.SyncSet;

/**
 * An in memory implementation of the message list
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class MemoryMessageList
   implements MessageList
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The list */
   private SyncSet list;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public MemoryMessageList()
   {
      Comparator comparator = new StandardMessageComparator();
      TreeSet set = new TreeSet(comparator);
      list = new SyncSet(set, new FIFOReadWriteLock());  
   }

   // Public --------------------------------------------------------

   // MessageList implementation ------------------------------------

   public void add(MessageReference message)
   {
      list.add(message);
   }
   
   public List browse(String selector)
      throws Exception
   {
      ArrayList result = new ArrayList(list.size());
      for (Iterator i = list.iterator(); i.hasNext();)
      {
         MessageReference reference = (MessageReference) i.next();
         result.add(reference.getMessage());
      }
      return result;
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
