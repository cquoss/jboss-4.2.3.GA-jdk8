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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

/**
 * A numeric that is an arguement to a query.
 * 
 * @author  Adrian.Brock@jboss.com
 * @version $Revision: 57200 $
 */
class NumericValueExp extends QueryEval implements ValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -4679739485102359104L;
   private static final ObjectStreamField[] serialPersistentFields;

   // Attributes --------------------------------------------------

   private Number val;

   // Static  -----------------------------------------------------

   static
   {
      serialPersistentFields = new ObjectStreamField[]
      {
         new ObjectStreamField("val", Number.class),
      };
   }

   // Constructors ------------------------------------------------

   public NumericValueExp()
   {
      this.val = new Double(0.0);
   }
   
   public NumericValueExp(Number value)
   {
      this.val = value;
   }

   // Public ------------------------------------------------------

   public boolean isInteger()
   {
      return val instanceof Integer || val instanceof Long;
   }

   public double getLongValue()
   {
      return val.longValue();
   }

   public double getDoubleValue()
   {
      return val.doubleValue();
   }

   // ValueExp implementation -------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
             BadBinaryOpValueExpException,
             BadAttributeValueExpException,
             InvalidApplicationException
   {
      return this;
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return val.toString();
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
   }
   
   private void writeObject(ObjectOutputStream oos) throws IOException
   {
      oos.defaultWriteObject();
   }
}
