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
package org.jboss.jms.server.list;

import java.util.Comparator;

import org.jboss.jms.server.MessageReference;

/**
 * A comparator that implements standard message ordering
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class StandardMessageComparator
   implements Comparator
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Comparator implementation -------------------------------------

   public int compare(Object o1, Object o2)
   {
      try
      {
         MessageReference r1 = (MessageReference) o1;
         MessageReference r2 = (MessageReference) o2;
         int p1 = r1.getPriority();
         int p2 = r2.getPriority();
         if (p1 != p2) return p2-p1;
         String l1 = r1.getMessageID();
         String l2 = r2.getMessageID();
         return l1.compareTo(l2);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error during comparison", e);
      }
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
