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

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/** 
 * Message Destination Reference Metadata
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>.
 * @version $Revision: 57209 $
 */
public class MessageDestinationRefMetaData extends MetaData
{
   // Constants -----------------------------------------------------

   public static final int CONSUMES = 1;
   public static final int PRODUCES = 2;
   public static final int CONSUMESPRODUCES = 3;
   
   // Attributes ----------------------------------------------------

   /** The reference name, it is unique by schema validation */
   private String refName;
   
   /** The type of destination */
   private String type;
   
   /** The usage of destination */
   private int usage;
   
   /** The link */
   private String link;
   
   /** The jndi name */
   private String jndiName;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public MessageDestinationRefMetaData()
   {
   }

   // Public --------------------------------------------------------

   public String getRefName()
   {
      return refName;
   }

   public String getType()
   {
      return type;
   }

   public int getUsage()
   {
      return usage;
   }

   public String getLink()
   {
      return link;
   }

   public String getJNDIName()
   {
      return jndiName;
   }

   public void importEjbJarXml(Element element) throws DeploymentException
   {
      refName = getElementContent(getUniqueChild(element, "message-destination-ref-name"));

      type = getElementContent(getUniqueChild(element, "message-destination-type"));

      String usageValue = getElementContent(getUniqueChild(element, "message-destination-usage"));
      usageValue = usageValue.trim();
      if (usageValue.equalsIgnoreCase("Consumes"))
         usage = CONSUMES;
      else if (usageValue.equalsIgnoreCase("Produces"))
         usage = PRODUCES;
      else if (usageValue.equalsIgnoreCase("ConsumesProduces"))
         usage = CONSUMESPRODUCES;
      else
         throw new DeploymentException("message-destination-usage should be one of Consumes, Produces, ConsumesProduces");

      Element child = getOptionalChild(element, "message-destination-link");
      if (child != null)
         link = getElementContent(child);
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      jndiName = getElementContent(getUniqueChild(element, "jndi-name"));
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
