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
package javax.management.openmbean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.management.ObjectName;

import org.jboss.mx.util.MetaDataUtil;

/**
 * A parent for all classes describing open types of open data values.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public abstract class OpenType
   implements Serializable
{
   // Constants -----------------------------------------------------------------

   private static final long serialVersionUID = -9195195325186646468L;
   private static final ObjectStreamField[] serialPersistentFields =
      new ObjectStreamField[]
      {
         new ObjectStreamField("className",   String.class),
         new ObjectStreamField("description", String.class),
         new ObjectStreamField("typeName",    String.class)
      };

   // Attributes ----------------------------------------------------

   /**
    * The open type's class name
    */
   private String className;

   /**
    * The type's description
    */
   private String description;

   /**
    * The type's name
    */
   private String typeName;

   /**
    * Whether the class is an array
    */
   private transient boolean array = false;

   // Static --------------------------------------------------------

   /**
    * The allowed classnames.<p>
    *
    * One of<br>
    * java.lang.Void<br>
    * java.lang.Boolean<br>
    * java.lang.Character<br>
    * java.lang.Byte<br>
    * java.lang.Short<br>
    * java.lang.Integer<br>
    * java.lang.Long<br>
    * java.lang.Float<br>
    * java.lang.Double<br>
    * java.lang.String<br>
    * java.lang.Date<br>
    * java.math.BigDecimal<br>
    * java.math.BigInteger<br>
    * javax.management.ObjectName<br>
    * {@link CompositeData}.class.getName()<br>
    * {@link TabularData}.class.getName()
    */
   public static final String[] ALLOWED_CLASSNAMES =
   {
      Void.class.getName(),
      Boolean.class.getName(),
      Character.class.getName(),
      Byte.class.getName(),
      Short.class.getName(),
      Integer.class.getName(),
      Long.class.getName(),
      Float.class.getName(),
      Double.class.getName(),
      String.class.getName(),
      Date.class.getName(),
      BigDecimal.class.getName(),
      BigInteger.class.getName(),
      ObjectName.class.getName(),
      CompositeData.class.getName(),
      TabularData.class.getName()
   };

   // Constructors --------------------------------------------------

   /**
    * Construct an OpenType.<p>
    *
    * The class name must be in {@link #ALLOWED_CLASSNAMES} or an
    * array of those classes.
    *
    * @param className the name of the class implementing the open type,
    *        cannot be null
    * @param typeName the name of the open type, cannot be null
    * @param description the human readable description of the type, cannot 
    *        be null
    * @exception OpenDataException when class name is not allowed class
    * @exception IllegalArgumentException for a null argument
    */
   protected OpenType(String className, String typeName, String description)
      throws OpenDataException
   {
      init(className, typeName, description);
   }

   // Public --------------------------------------------------------

   /**
    * Retrieve the class name of the open data values of this open data
    * type. It is one of those listed in ALLOWED_CLASSNAMES or
    * a (multi-dimensional) array of one of those classes.
    *
    * @return the class name
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Retrieve the name of the open data type
    *
    * @return the type name
    */
   public String getTypeName()
   {
      return typeName;
   }

   /**
    * Retrieve the description of the type
    *
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Retrieve whether the class name of the type is an array
    *
    * @return true when it is an array or false otherwise
    */
   public boolean isArray()
   {
      return array;
   }

   /**
    * Whether the passed value is one of those described by this open type.
    *
    * @param obj the object to test
    * @return true when it is value for this open type, false otherwise
    */
   public abstract boolean isValue(Object obj);

   // Serializable Implementation -----------------------------------

   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = in.readFields();
      String className = (String) getField.get("className", null);
      String typeName = (String) getField.get("typeName", null);
      String description = (String) getField.get("description", null);
      try
      {
         init(className, typeName, description);
      }
      catch (Exception e)
      {
         throw new StreamCorruptedException(e.toString());
      }
   }

   // Object Overrides ----------------------------------------------

   /**
    * Compares two object types for equality
    *
    * @return obj the object to test with this one
    * @return true when they are equal, false otherwise
    */
   public abstract boolean equals(Object obj);

   /**
    * Retrieve the hashCode for this OpenType
    *
    * @return the hash code
    */
   public abstract int hashCode();

   /**
    * Retrieve a string representation of this open type
    *
    * @return the string representation
    */
   public abstract String toString();

   // Private -------------------------------------------------------

   /**
    * Initialise the object
    *
    * @param className the name of the class implementing the open type,
    *        cannot be null or an empty
    * @param typeName the name of the open type, cannot be null or an empty 
    *        string
    * @param description the human readable description of the type, cannot 
    *        be null or an empty string
    * @exception OpenDataException when class name is not allowed class
    * @exception IllegalArgumentException for a null or empty argument
    */
   private void init(String className, String typeName, String description)
      throws OpenDataException
   {
      if (className == null || className.trim().equals(""))
         throw new IllegalArgumentException("null or empty class name");
      if (typeName == null || typeName.trim().equals(""))
         throw new IllegalArgumentException("null or empty type name");
      if (description == null || description.trim().equals(""))
         throw new IllegalArgumentException("null or empty description");

      // Calculate the underlying class and whether this is an array
      String testClassName = MetaDataUtil.getBaseClassName(className);
      if (testClassName == null)
         throw new OpenDataException("Invalid array declaration (see the javadocs for java.lang.Class): " + className);
      if (testClassName.equals(className) == false)
         array = true;

      // Check the underlying class
      boolean ok = false;
      for (int i = 0; i < ALLOWED_CLASSNAMES.length; i++)
         if (testClassName.equals(ALLOWED_CLASSNAMES[i]))
         {
            ok = true;
            break;
         }
      if (ok == false)
         throw new OpenDataException("Not an OpenType allowed class name: " + className);

      // Looks ok
      this.className = className;
      this.typeName = typeName;
      this.description = description;
   }
}

