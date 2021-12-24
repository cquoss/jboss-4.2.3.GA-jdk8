/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.naming;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class NamingEnumerationImpl<T extends NameClassPair> implements NamingEnumeration<T>
{
   private Iterator<T> iterator;
   
   NamingEnumerationImpl(Collection<T> set)
   {
      this.iterator = set.iterator();
   }

   /* (non-Javadoc)
    * @see javax.naming.NamingEnumeration#close()
    */
   public void close() throws NamingException
   {
      // do nothing
   }

   /* (non-Javadoc)
    * @see javax.naming.NamingEnumeration#hasMore()
    */
   public boolean hasMore() throws NamingException
   {
      return hasMoreElements();
   }

   /* (non-Javadoc)
    * @see javax.naming.NamingEnumeration#next()
    */
   public T next() throws NamingException
   {
      return nextElement();
   }

   /* (non-Javadoc)
    * @see java.util.Enumeration#hasMoreElements()
    */
   public boolean hasMoreElements()
   {
      return iterator.hasNext();
   }

   /* (non-Javadoc)
    * @see java.util.Enumeration#nextElement()
    */
   public T nextElement()
   {
      return iterator.next();
   }
}
