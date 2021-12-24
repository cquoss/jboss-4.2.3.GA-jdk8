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

import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.management.ObjectName;

/**
 * The open type for simple java classes. These are a fixed number of these.
 *
 * The open types are available as static constants from this class.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public final class SimpleType
   extends OpenType
   implements Serializable
{
   // Attributes ----------------------------------------------------

   /**
    * Cached hash code
    */
   private transient int cachedHashCode = 0;

   /**
    * Cached string representation
    */
   private transient String cachedToString = null;

   // Static --------------------------------------------------------

   private static final long serialVersionUID = 2215577471957694503L;

   /**
    * The simple type for java.math.BigDecimal
    */
   public static final SimpleType BIGDECIMAL;

   /**
    * The simple type for java.math.BigInteger
    */
   public static final SimpleType BIGINTEGER;

   /**
    * The simple type for java.lang.Boolean
    */
   public static final SimpleType BOOLEAN;

   /**
    * The simple type for java.lang.Byte
    */
   public static final SimpleType BYTE;

   /**
    * The simple type for java.lang.Character
    */
   public static final SimpleType CHARACTER;

   /**
    * The simple type for java.lang.Date
    */
   public static final SimpleType DATE;

   /**
    * The simple type for java.lang.Double
    */
   public static final SimpleType DOUBLE;

   /**
    * The simple type for java.lang.Float
    */
   public static final SimpleType FLOAT;

   /**
    * The simple type for java.lang.Integer
    */
   public static final SimpleType INTEGER;

   /**
    * The simple type for java.lang.Long
    */
   public static final SimpleType LONG;

   /**
    * The simple type for javax.management.ObjectName
    */
   public static final SimpleType OBJECTNAME;

   /**
    * The simple type for java.lang.Short
    */
   public static final SimpleType SHORT;

   /**
    * The simple type for java.lang.String
    */
   public static final SimpleType STRING;

   /**
    * The simple type for java.lang.Void
    */
   public static final SimpleType VOID;

   static
   {
      try
      {
         BIGDECIMAL = new SimpleType(BigDecimal.class.getName());
         BIGINTEGER = new SimpleType(BigInteger.class.getName());
         BOOLEAN = new SimpleType(Boolean.class.getName());
         BYTE = new SimpleType(Byte.class.getName());
         CHARACTER = new SimpleType(Character.class.getName());
         DATE = new SimpleType(Date.class.getName());
         DOUBLE = new SimpleType(Double.class.getName());
         FLOAT = new SimpleType(Float.class.getName());
         INTEGER = new SimpleType(Integer.class.getName());
         LONG = new SimpleType(Long.class.getName());
         OBJECTNAME = new SimpleType(ObjectName.class.getName());
         SHORT = new SimpleType(Short.class.getName());
         STRING = new SimpleType(String.class.getName());
         VOID = new SimpleType(Void.class.getName());
      }
      catch (OpenDataException e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Construct an SimpleType.<p>
    *
    * This constructor is used to construct the static simple types.
    *
    * @param className the name of the class implementing the open type
    */
   private SimpleType(String className)
      throws OpenDataException
   {
      super(className, className, className);
   }

   // OpenType Overrides---------------------------------------------

   public boolean isValue(Object obj)
   {
       return (obj != null && obj.getClass().getName().equals(getClassName()));
   }

   // Serializable Implementation -----------------------------------

   public Object readResolve()
      throws ObjectStreamException
   {
      String className = getClassName();
      if (className.equals(STRING.getClassName()))
         return STRING;
      if (className.equals(INTEGER.getClassName()))
         return INTEGER;
      if (className.equals(BOOLEAN.getClassName()))
         return BOOLEAN;
      if (className.equals(OBJECTNAME.getClassName()))
         return OBJECTNAME;
      if (className.equals(LONG.getClassName()))
         return LONG;
      if (className.equals(BYTE.getClassName()))
         return BYTE;
      if (className.equals(CHARACTER.getClassName()))
         return CHARACTER;
      if (className.equals(DOUBLE.getClassName()))
         return DOUBLE;
      if (className.equals(FLOAT.getClassName()))
         return FLOAT;
      if (className.equals(SHORT.getClassName()))
         return SHORT;
      if (className.equals(BIGDECIMAL.getClassName()))
         return BIGDECIMAL;
      if (className.equals(BIGINTEGER.getClassName()))
         return BIGINTEGER;
      if (className.equals(VOID.getClassName()))
         return VOID;
      if (className.equals(DATE.getClassName()))
         return DATE;
      throw new InvalidClassException(className);
   }

   // Object Overrides ----------------------------------------------

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null || !(obj instanceof SimpleType))
         return false;
      return (this.getClassName().equals(((SimpleType) obj).getClassName()));
   }

   public int hashCode()
   {
      if (cachedHashCode != 0)
         return cachedHashCode;
      cachedHashCode = getClassName().hashCode();
      return cachedHashCode;
   }

   public String toString()
   {
      if (cachedToString != null)
         return cachedToString;
      StringBuffer buffer = new StringBuffer(SimpleType.class.getName());
      buffer.append(":");
      buffer.append(getClassName());
      cachedToString = buffer.toString();
      return cachedToString;
   }

   // Private -------------------------------------------------------
}
