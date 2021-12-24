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
 * A representation of an MBean attribute. It is a pair,
 * a {@link #getName() Name} and a {@link #getValue() Value}.<p>
 *
 * An Attribute is returned by a getter operation or passed to a
 * a setter operation.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020730 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 * <p><b>20020930 Juha Lindfors:</b>
 * <ul>
 * <li> Overrode hashCode() to return same hash for objects that are equal</li>
 * <li> Removed 'instanceof' from equals() as it tends to break the symmetricity
 *      requirement when subclasses are involved </li>
 * </ul> 
 *
 */
public class Attribute
   extends Object
   implements Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 2484220110589082382L;

   
   // Attributes --------------------------------------------------------

   /**
    * The name of the attribute.
    */
   private String name = null;
   /**
    * The value of the attribute.
    */
   private Object value = null;
   
   
   // Constructors --------------------------------------------------

   /**
    * Contruct a new attribute given a name and value.
    *
    * @param name the name of the attribute.
    * @param value the value of the attribute.
    */
   public Attribute(String name, Object value)
   {
      this.name = name;
      this.value = value;
   }

   
   // Public --------------------------------------------------------

   /**
    * Retrieves the name of the attribute.
    *
    * @return the name of the attribute.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Retrieves the value of the attribute.
    *
    * @return the value of the attribute.
    */
   public Object getValue()
   {
      return value;
   }

   
   // Object overrides ----------------------------------------------
   
   /**
    * Compares two attributes for equality.
    *
    * @return true when the name value objects are equal, false otherwise.
    */
    public boolean equals(Object object)
    {
       if (object == null)
          return false;
          
      if (!(object.getClass().equals(this.getClass())))
         return false;
        
      Attribute attr = (Attribute) object;
      
      return (name.equals(attr.getName()) && value.equals(attr.getValue()));
   }

   public int hashCode()
   {
      return name.hashCode();
   }
   
   /**
    * @return human readable string.
    */
    public String toString()
   {
      StringBuffer buffer = new StringBuffer(50);
      buffer.append(getClass().getName()).append(":");
      buffer.append(" name=").append(getName());
      buffer.append(" value=").append(getValue());
      return buffer.toString();
   }


}
