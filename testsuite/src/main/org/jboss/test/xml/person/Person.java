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
package org.jboss.test.xml.person;

import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.TypeBinding;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class Person
{
   public static Person newInstance()
   {
      Person person = new Person();
      person.setFirstName("Vasiliy");
      person.setLastName("Poupkin");
      person.setDateOfBirth((java.util.Date)SimpleTypeBindings.JAVA_UTIL_DATE.unmarshal("1980-01-01"));
      person.setPhones(Arrays.asList(new Object[]{"01", "02"}));

      ArrayList list = new ArrayList();
      Address addr1 = new Address();
      addr1.setStreet("prosp. Rad. Ukr. 11A, 70");
      list.add(addr1);
      addr1 = new Address();
      addr1.setStreet("Sky 7");
      list.add(addr1);
      person.setAddresses(list);

      return person;
   }

   private String firstName;
   private String lastName;
   private java.util.Date dateOfBirth;
   private Collection phones = new ArrayList();
   private Collection addresses = new ArrayList();

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public java.util.Date getDateOfBirth()
   {
      return dateOfBirth;
   }

   public void setDateOfBirth(java.util.Date dateOfBirth)
   {
      this.dateOfBirth = dateOfBirth;
   }

   public Collection getPhones()
   {
      return phones;
   }

   public void setPhones(Collection phones)
   {
      this.phones = phones;
   }

   public Collection getAddresses()
   {
      return addresses;
   }

   public void setAddresses(Collection addresses)
   {
      this.addresses = addresses;
   }

   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(!(o instanceof Person)) return false;

      final Person person = (Person) o;

      if(addresses != null ? !addresses.equals(person.addresses) : person.addresses != null) return false;
      if(dateOfBirth != null ? !dateOfBirth.equals(person.dateOfBirth) : person.dateOfBirth != null) return false;
      if(firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
      if(lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
      if(phones != null ? !phones.equals(person.phones) : person.phones != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (firstName != null ? firstName.hashCode() : 0);
      result = 29 * result + (lastName != null ? lastName.hashCode() : 0);
      result = 29 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
      result = 29 * result + (phones != null ? phones.hashCode() : 0);
      result = 29 * result + (addresses != null ? addresses.hashCode() : 0);
      return result;
   }

   public String toString()
   {
      return "[firstName=" + firstName +
         ", lastName=" + lastName +
         ", dateOfBirth=" + dateOfBirth +
         ", phones=" + phones +
         ", addresses=" + addresses + "]";
   }
}
