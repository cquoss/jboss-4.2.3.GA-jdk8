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
 * Information about an object registered in the MBeanServer. An 
 * <tt>ObjectInstance</tt> represents an MBean's object name and class name.
 * If the MBean is a Dynamic MBean the class name should be retrieved from the
 * <tt>MBeanInfo</tt> it provides.
 *
 * @see javax.management.ObjectName
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020710 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 * <p><b>20030220 Juha Lindfors:</b>
 * <ul>
 * <li> Added hashCode override.</li>
 * </ul>
 */
public class ObjectInstance 
   extends Object 
   implements Serializable
{

   // Attributes ----------------------------------------------------
   
   private ObjectName name  = null;
   private String className = null;

   
   // Static --------------------------------------------------------

   private static final long serialVersionUID = -4099952623687795850L;
   
   
   // Constructors --------------------------------------------------
   
   /**
    * Creates a new object instance with a given object name and a fully
    * qualified class name.
    *
    * @param   name        object name
    * @param   className   fully qualified class name
    *
    * @throws MalformedObjectNameException the object name string is invalid
    */
   public ObjectInstance(String name, String className)
      throws MalformedObjectNameException
   {
      if (name == null)
         throw new MalformedObjectNameException("Null name");
   
      this.name = new ObjectName(name);
      this.className = className;
   }

   /**
    * Creates a new object instance with a given object name and a fully
    * qualified class name.
    *
    * @param   name        object name
    * @param   className   fully qualified class name
    */
   public ObjectInstance(ObjectName name, String className)
   {
       this.name = name;
       this.className  = className;                  
   }

   
   // Public --------------------------------------------------------
   
   /**
    * Returns the object name of this instance.
    *
    * @return object name
    */
   public ObjectName getObjectName() 
   {
      return name;
   }

   /**
    * Returns the class name of this instance.
    *
    * @return class name
    */
   public String getClassName() 
   {
      return className;
   }
   
   
   // Object overrides ----------------------------------------------
   
   public boolean equals(Object object)
   {
      if (!(object instanceof ObjectInstance))
         return false;

      ObjectInstance oi = (ObjectInstance)object;
      return ( (name.equals(oi.getObjectName())) &&
               (className.equals(oi.getClassName())) );
   }

   public int hashCode()
   {
      return name.hashCode() + className.hashCode();
   }
   
   public String toString()
   {
      return "ObjectInstance[" + name + ", " + className + "]";
   }

}

