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
package org.jboss.mq.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.jboss.mq.Subscription;

/**
 * Interface to be implemented by a receivers implementation.
 * The implementation should also have a default constructor.<p>
 * 
 * NOTE: There is no need to internally synchronize the caller
 * handles that.<p>
 * 
 * NOTE: This datastructure should have Set semantics. 
 *       i.e. attempts to add a subscriber that is already
 *       present should be ignored. Or more explicitly
 *       a subscriber should present zero or once. 
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public interface Receivers
{

   /**
    * @return Get the number of receivers
    */
   int size();
   
   /**
    * @return the subscriptions as an array list, this must be a
    * clone of any internal datastructure
    */
   ArrayList listReceivers();
   
   
   /**
    * Add a receiver, ignored if the receiver is already present.
    * 
    * @param sub the receiver to add
    */
   void add(Subscription sub);
   
   /**
    * Remove a receiver
    * 
    * @param sub the receiver to remove
    */
   void remove(Subscription sub);
   
   /**
    * Get an iterator to loop over all receivers
    * 
    * @return the iterator
    */
   Iterator iterator();
}
