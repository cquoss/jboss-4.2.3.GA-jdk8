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
package org.jboss.test.xml.choice;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class Choice1
{
   private String a;
   private String b;

   public Choice1()
   {
   }

   public Choice1(String a, String b)
   {
      this.a = a;
      this.b = b;
   }

   public String getA()
   {
      return a;
   }

   public void setA(String a)
   {
      this.a = a;
   }

   public String getB()
   {
      return b;
   }

   public void setB(String b)
   {
      this.b = b;
   }

   public String toString()
   {
      return "[a=" + a + ", b=" + b + "]";
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof Choice1))
      {
         return false;
      }

      final Choice1 choice1 = (Choice1)o;

      if(a != null ? !a.equals(choice1.a) : choice1.a != null)
      {
         return false;
      }
      if(b != null ? !b.equals(choice1.b) : choice1.b != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (a != null ? a.hashCode() : 0);
      result = 29 * result + (b != null ? b.hashCode() : 0);
      return result;
   }
}
