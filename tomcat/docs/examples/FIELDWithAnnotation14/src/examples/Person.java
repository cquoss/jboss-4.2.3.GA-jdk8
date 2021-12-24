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

import java.util.List;

/**
 * @author Ben Wang
 * org.jboss.cache.aop.InstanceOfAopMarker
 * @@org.jboss.web.tomcat.service.session.InstanceOfAopMarker
 */
public class Person {
   protected String name;
   protected Address address;

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return this.name;
   }

   public void setAddress(Address address)
   {
      this.address = address;
   }

   public Address getAddress()
   {
      return this.address;
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("{Name = " +name);
      if (address != null)
         buf.append(", Address = " + address.getSimpleAddress());
      buf.append("}\n");

      return buf.toString();
   }

}
