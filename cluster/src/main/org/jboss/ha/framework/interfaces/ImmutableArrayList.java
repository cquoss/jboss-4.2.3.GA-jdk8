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
package org.jboss.ha.framework.interfaces;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Subclasses ArrayList but throws an UnsupportedOperationException from
 * any method that would change the internal data members.  Any iterators
 * that are returned do the same.
 * 
 * All other methods are delegated to the ArrayList that is passed to the 
 * constructor, so creating an instance of this class does not result in
 * making a copy of the internal element array of the source array list.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class ImmutableArrayList extends ArrayList
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7080841600873898901L;
   
   private ArrayList delegate;

   ImmutableArrayList(ArrayList source)
   {
      this.delegate = source;
   }
   
   // Delegated Methods
   
   public Object clone()
   {
      return delegate.clone();
   }
   
   public boolean contains(Object elem)
   {
      return delegate.contains(elem);
   }
   
   public Object get(int index)
   {
      return delegate.get(index);
   }
   
   public int indexOf(Object elem)
   {
      return delegate.indexOf(elem);
   }
   
   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }
   
   public int lastIndexOf(Object elem)
   {
      return delegate.lastIndexOf(elem);
   }
   
   public int size()
   {
      return delegate.size();
   }
   
   public Object[] toArray()
   {
      return delegate.toArray();
   }
   
   public Object[] toArray(Object[] a)
   {
      return delegate.toArray(a);
   }
   
   public boolean equals(Object o)
   {
      return delegate.equals(o);
   }
   
   public int hashCode()
   {
      return delegate.hashCode();
   }
   
   public List subList(int fromIndex, int toIndex)
   {
      return delegate.subList(fromIndex, toIndex);
   }
   
   public boolean containsAll(Collection c)
   {
      return delegate.containsAll(c);
   }
   
   public String toString()
   {
      return delegate.toString();
   }
   
   // Immutable Methods

   public void add(int arg0, Object arg1)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public boolean add(Object arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public boolean addAll(Collection arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public boolean addAll(int arg0, Collection arg1)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public void clear()
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public void ensureCapacity(int arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public Object remove(int arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public boolean remove(Object arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   protected void removeRange(int arg0, int arg1)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public Object set(int arg0, Object arg1)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public void trimToSize()
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public Iterator iterator()
   {
      return new ImmutableArrayListIterator(super.listIterator());
   }
   
   public ListIterator listIterator()
   {
      return new ImmutableArrayListIterator(super.listIterator());
   }
   
   public ListIterator listIterator(int index)
   {
      return new ImmutableArrayListIterator(super.listIterator(index));
   }
   
   public boolean removeAll(Collection arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   public boolean retainAll(Collection arg0)
   {
      throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
   }
   
   // Serialization
   
   private Object writeReplace() throws ObjectStreamException
   {
      return delegate;
   }   
   
   // Inner Classes

   private class ImmutableArrayListIterator implements ListIterator
   {
      

      private ListIterator delegate;
      
      ImmutableArrayListIterator(ListIterator delegate)
      {
         this.delegate = delegate;
      }
   
      public boolean hasNext()
      {
         return delegate.hasNext();
      }
   
      public Object next()
      {
         return delegate.next();
      }
   
      public void remove()
      {
         throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");
         
      }
   
      public void add(Object o)
      {
         throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported"); 
      }
   
      public boolean hasPrevious()
      {
         return delegate.hasPrevious();
      }
   
      public int nextIndex()
      {
         return delegate.nextIndex();
      }
   
      public Object previous()
      {
         return delegate.previous();
      }
   
      public int previousIndex()
      {
         return delegate.previousIndex();
      }
   
      public void set(Object o)
      {
         throw new UnsupportedOperationException("Target list is immutable; mutator methods are not supported");         
      }
      
   }

}
