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
package org.jboss.test.cluster.web.aop;

/**
 * @author Brian Stansberry
 */
@org.jboss.cache.aop.annotation.PojoCacheable
public class Course {
   protected String title;
   protected String instructor;
   protected String room;

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getTitle()
   {
      return this.title;
   }

   public void setRoom(String room)
   {
      this.room = room;
   }

   public String getRoom()
   {
      return this.room;
   }
   
   public void setInstructor(String instructor)
   {
      this.instructor = instructor;
   }
   
   public String getInstructor()
   {
      return instructor;
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("{Title = " +title).append(", Instructor = " + instructor).append(", Room = " +room + "}\n");

      return buf.toString();
   }
   
   public boolean equals(Object other)
   {
      if (this == other)
         return true;
         
      if (other instanceof Course) {
         String otherTitle = ((Course) other).getTitle();
         return (title == otherTitle || (title != null && title.equals(otherTitle)));
      } 
      
      return false;
   }
   
   public int hashCode()
   {
      return title == null ? 0 : title.hashCode();
   }
}
