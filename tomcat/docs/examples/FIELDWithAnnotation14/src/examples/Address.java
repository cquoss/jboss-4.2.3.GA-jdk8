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

/**
 * @author Ben Wang
 * org.jboss.cache.aop.AopMarker
 * @@org.jboss.web.tomcat.service.session.AopMarker
 */
public class Address {
   protected String city;
   protected int zip;
   protected String street;

   public void setCity(String city)
   {
      this.city = city;
   }

   public String getCity()
   {
      return this.city;
   }

   public void setZip(int zip)
   {
      this.zip = zip;
   }

   public int getZip()
   {
      return zip;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   public String getStreet()
   {
      return this.street;
   }
   
   public String getSimpleAddress()
   {
      StringBuffer buf = new StringBuffer(street);      
      buf.append(" "  + city);
      if (zip > 0)
         buf.append("  " + zip);

      return buf.toString();
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("{City = " +city).append(" ,zip = " +zip).append(" ,street = " +street + "}\n");

      return buf.toString();
   }
}
