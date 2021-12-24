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


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class ImmutableChoice
{
   private final String choice1;
   private final Child1 child1;

   public ImmutableChoice(String choice1)
   {
      this.choice1 = choice1;
      this.child1 = null;
   }

   public ImmutableChoice(Child1 choice2)
   {
      this.choice1 = null;
      this.child1 = choice2;
   }

   public String getChoice1()
   {
      return choice1;
   }

   public Child1 getChild1()
   {
      return child1;
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof ImmutableChoice))
      {
         return false;
      }

      final ImmutableChoice immutableChoice = (ImmutableChoice)o;

      if(choice1 != null ? !choice1.equals(immutableChoice.choice1) : immutableChoice.choice1 != null)
      {
         return false;
      }
      if(child1 != null ? !child1.equals(immutableChoice.child1) : immutableChoice.child1 != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (choice1 != null ? choice1.hashCode() : 0);
      result = 29 * result + (child1 != null ? child1.hashCode() : 0);
      return result;
   }

   public String toString()
   {
      return "[choice1=" + choice1 + ", child1=" + child1 + "]";
   }
}
