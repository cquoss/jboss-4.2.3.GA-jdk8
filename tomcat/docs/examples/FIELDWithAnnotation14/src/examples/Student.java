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
package examples;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Ben Wang
 */
public class Student extends Person 
{
   protected String school;
   protected Set   courses = new LinkedHashSet();

   public void setSchool(String school)
   {
      this.school = school;
   }

   public String getSchool()
   {
      return this.school;
   }
   
   public void addCourse(Course course)
   {
      courses.add(course);
   }
   
   public void removeCourse(Course course)
   {
      courses.remove(course);
   }
   
   public Set getCourses()
   {
      return Collections.unmodifiableSet(courses);
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("{Name = " +name).append(", School = " +school);
      if (address != null)
         buf.append(", Address = " + address.getSimpleAddress());
      buf.append("}\n");
      buf.append("Courses:\n");
      for (Iterator iter = getCourses().iterator(); iter.hasNext(); )
         buf.append(iter.next());

      return buf.toString();
   }

}
