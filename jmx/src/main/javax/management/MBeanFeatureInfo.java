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
package javax.management;

import java.io.Serializable;

/**
 * General information for MBean descriptor objects.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 *
 * <p><b>Revisions:</b>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 *
 * @version $Revision: 57200 $
 */
public class MBeanFeatureInfo 
   implements Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 3952882688968447265L;

   // Attributes ----------------------------------------------------
   
   /**
    * Name of the MBean feature.
    */
   protected String name = null;
   
   /**
    * Human readable description string of the MBean feature.
    */
   protected String description = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   
   /**
    * Constructs an MBean feature info object.
    *
    * @param   name name of the MBean feature
    * @param   description human readable description string of the feature
    * @exception IllegalArgumentException if the name is not a valid java type
    */
   public MBeanFeatureInfo(String name, String description) throws IllegalArgumentException
   {
      /* This was removed in the jmx-1.2 mr
      - Illegal identifiers no longer produce exceptions (4839259)
      if (MetaDataUtil.isValidJavaType(name) == false)
         throw new IllegalArgumentException("name is not a valid java type (or is a reserved word): " + name);
      */
      this.name = name;
      this.description = description;
   }

   // Public --------------------------------------------------------
   
   /**
    * Returns the name of the MBean feature.
    *
    * @return  name string
    */
   public String getName()
   {
      return name;
   }

   /** 
    * Returns the description of the MBean feature.
    *
    * @return  a human readable description string
    */
   public String getDescription()
   {
      return description;
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanFeatureInfo) == false)
         return false;

      MBeanFeatureInfo other = (MBeanFeatureInfo) object;
      boolean equals = false;
      if( this.getName().equals(other.getName()) )
      {
         String thisDescription = getDescription();
         String otherDescription = other.getDescription();
         if( thisDescription == null )
            equals = thisDescription == otherDescription;
         else
            equals = thisDescription.equals(otherDescription);
      }

      return equals;
   }

   public int hashCode()
   {
      if (cacheHashCode == 0)
      {
         cacheHashCode =  getName().hashCode();
         if( getDescription() != null )
            cacheHashCode += getDescription().hashCode();
      }
      return cacheHashCode;
   }
 
   public String toString()
   {
      if (cacheString == null)
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append(getClass().getName()).append(":");
         buffer.append(" name=").append(getName());
         buffer.append(" description=").append(getDescription());
         cacheString = buffer.toString();
      }
      return cacheString;
   }
}
