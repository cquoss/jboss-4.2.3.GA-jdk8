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
package javax.management.modelmbean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import org.jboss.mx.util.Serialization;

/**
 * Thrown when unrecognizable target object type is set to a Model MBean
 * instance
 *
 * @see javax.management.modelmbean.ModelMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020715 Adrian Brock:</b>
 * <ul>
 * <li> Serialization
 * </ul>
 */
public class InvalidTargetObjectTypeException
         extends Exception
{
   // Attributes ----------------------------------------------------
   private Exception exception = null;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = 3711724570458346634L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("msgStr",        String.class),
            new ObjectStreamField("relatedExcept", Exception.class),
         };
         break;
      default:
         serialVersionUID = 1190536278266811217L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("exception", Exception.class),
         };
      }
   }

   // Constructors --------------------------------------------------
   public InvalidTargetObjectTypeException()
   {
      super();
   }
   
   public InvalidTargetObjectTypeException(String s)
   {
      super(s);
   }
   
   public InvalidTargetObjectTypeException(Exception e, String s)
   {
      super(s);
      this.exception = e;
   }

   // Object overrides ----------------------------------------------

   /**
    * Returns a string representation of this exception. The returned string
    * contains this exception name, message and a string representation of the
    * target exception if it has been set.
    *
    * @return string representation of this exception
    */
   public String toString()
   {
      return "InvalidTargetObjectTypeException: " + getMessage() + 
        ((exception == null) ? "" : " Cause: " + exception.toString());
   }

   // Private -------------------------------------------------------

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectInputStream.GetField getField = ois.readFields();
         exception = (Exception) getField.get("relatedExcept", null);
         break;
      default:
         ois.defaultReadObject();
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectOutputStream.PutField putField = oos.putFields();
         putField.put("msgStr", getMessage());
         putField.put("relatedExcept", exception);
         oos.writeFields();
         break;
      default:
         oos.defaultWriteObject();
      }
   }
}




