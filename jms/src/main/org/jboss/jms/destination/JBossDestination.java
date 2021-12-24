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
package org.jboss.jms.destination;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * A destination
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JBossDestination
   implements Destination, Referenceable, Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The name */
   private String name;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new destination
    * 
    * @param name the name
    */
   public JBossDestination(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      this.name = name;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieve the name
    * 
    * @return the name
    * @throws JMSExeption for any error
    */   
   public String getName()
      throws JMSException
   {
      return name;
   }

   // Destination implementation ------------------------------------

   // Referenceable implementation ----------------------------------
   
   public Reference getReference()
      throws NamingException
   {
      return new Reference
      (
         getClass().getName(),
         new StringRefAddr("name", name),
         JBossDestinationFactory.class.getName(),
         null
      );
   }

   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      return name;
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;
      if (getClass() != obj.getClass()) return false;
      JBossDestination other = (JBossDestination) obj;
      return name.equals(other.name);
   }

   public int hashCode()
   {
      return name.hashCode();
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
