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
package org.jboss.jms.server.standard;

import java.util.List;

import org.jboss.jms.server.BrowserEndpoint;
import org.jboss.jms.server.list.MessageList;

/**
 * A queue browser endpoint
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class QueueBrowserEndpoint
   implements BrowserEndpoint
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The message list */
   private MessageList list;

   /** The selector */
   private String selector;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public QueueBrowserEndpoint(MessageList list, String selector)
   {
      this.list = list;
      this.selector = selector;
   }

   // Public --------------------------------------------------------

   // BrowserEndpoint implementation --------------------------------

   public List browse()
      throws Exception
   {
      return list.browse(selector);
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
