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
package test.implementation.util.support;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * Overrides and exposes java.lang.Object methods in the management
 * interface.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 */
public class ResourceOverride
{
   
   // Attributes ----------------------------------------------------
   
   private String state = null;
   
   
   // Constructors --------------------------------------------------
   public ResourceOverride(String state)
   {
      this.state = state;
   }
   
   public ResourceOverride() {}
   
   
   // Public --------------------------------------------------------
   
   public ModelMBeanInfo getMBeanInfo() 
   {
      ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[] 
      {
         new ModelMBeanAttributeInfo(
               "AttributeName", "java.lang.String", "description",
               false, true, false
         ),
         new ModelMBeanAttributeInfo(
               "AttributeName2", "java.lang.String", "description",
               true, true, false
         )
      };
      
      ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[]
      {
         new ModelMBeanOperationInfo(
               "doOperation", "description", null, "java.lang.Object", 1
         ),
         
         new ModelMBeanOperationInfo(
               "toString", "toString override", null, "java.lang.String", 1
         ),
         
         new ModelMBeanOperationInfo(
               "equals", "equals override",
               new MBeanParameterInfo[] 
               {
                  new MBeanParameterInfo("object", "java.lang.Object", "object to compare to")
               },
               "boolean", 1
         ),
         
         new ModelMBeanOperationInfo(
               "hashCode", "hashCode override in resource", null, Integer.TYPE.getName(), 1
         )
      };
      
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
            "test.implementation.util.support.Resource", "description",
            attributes, null, operations, null
      );
      
      return info;
   }


   public Object doOperation() 
   {
      return "tamppi";
   }

   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      return "Resource";
   }
   
   public boolean equals(Object o)
   {
      return true;
   }
   
   public int hashCode()
   {
      return 10;
   }
}
      



