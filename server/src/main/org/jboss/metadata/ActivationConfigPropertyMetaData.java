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
package org.jboss.metadata;

import org.jboss.deployment.DeploymentException;
import org.jboss.util.Strings;
import org.w3c.dom.Element;

/**
 * Parse the activation-config-property element used in message driven bean.
 * It is a name/value pair
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>.
 * @version $Revision: 57209 $
 */
public class ActivationConfigPropertyMetaData extends MetaData
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The property name */
   private String name;

   /** The property value */
   private String value;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new Activation Config Property MetaData object
    */
   public ActivationConfigPropertyMetaData()
   {
   }

   /**
    * Create a new Activation Config Property MetaData object
    *
    * @param name the name
    * @param value the value 
    */
   public ActivationConfigPropertyMetaData(String name, String value)
   {
      this.name = name;
      this.value = value;
   }
   
   // Public --------------------------------------------------------

   /**
    * Retrieve the property name
    */
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Retrieve the property value
    */
   public String getValue()
   {
      return value;
   }
   
   public void setValue(String value)
   {
      this.value = value;
   }

   public void importXml(Element element) throws DeploymentException
   {
      name = getElementContent(getUniqueChild(element, "activation-config-property-name"));
      value = getElementContent(getUniqueChild(element, "activation-config-property-value"));
      if (name == null || name.trim().length() == 0)
         throw new DeploymentException("activation-config-property doesn't have a name");
      if (Strings.isValidJavaIdentifier(name) == false)
         throw new DeploymentException("activation-config-property '" + name + "' is not a valid java identifier");
   }

   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      return "ActivationConfigProperty(" + name + "=" + value + ")";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
