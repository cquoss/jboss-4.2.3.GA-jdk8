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
package org.jboss.test.xml.immutable;

import java.util.Collection;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class Parent
{
   private final Child1 child1;
   private final Collection child2;
   private final Collection otherChildren;
   private final Collection immutableChoice;

   public Parent(Child1 child1, Collection child2, Collection otherChildren, Collection immutableChoice)
   {
      this.child1 = child1;
      this.child2 = child2;
      this.otherChildren = otherChildren;
      this.immutableChoice = immutableChoice;
   }

   public Child1 getChild1()
   {
      return child1;
   }

   public Collection getChild2()
   {
      return child2;
   }

   public Collection getOtherChildren()
   {
      return otherChildren;
   }

   public Collection getImmutableChoice()
   {
      return immutableChoice;
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof Parent))
      {
         return false;
      }

      final Parent parent = (Parent)o;

      if(child1 != null ? !child1.equals(parent.child1) : parent.child1 != null)
      {
         return false;
      }
      if(child2 != null ? !child2.equals(parent.child2) : parent.child2 != null)
      {
         return false;
      }
      if(immutableChoice != null ? !immutableChoice.equals(parent.immutableChoice) : parent.immutableChoice != null)
      {
         return false;
      }
      if(otherChildren != null ? !otherChildren.equals(parent.otherChildren) : parent.otherChildren != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (child1 != null ? child1.hashCode() : 0);
      result = 29 * result + (child2 != null ? child2.hashCode() : 0);
      result = 29 * result + (otherChildren != null ? otherChildren.hashCode() : 0);
      result = 29 * result + (immutableChoice != null ? immutableChoice.hashCode() : 0);
      return result;
   }

   public String toString()
   {
      return "[child1=" +
         child1 +
         ", child2=" +
         child2 +
         ", otherChildren=" +
         otherChildren +
         ", immutableChoice=" +
         immutableChoice +
         "]";
   }
}
