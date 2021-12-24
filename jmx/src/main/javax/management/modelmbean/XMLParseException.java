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
 * Exceptions related to XML handling.
 *
 * @see javax.management.modelmbean.DescriptorSupport
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
 *   
 */
public class XMLParseException
         extends Exception
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = -7780049316655891976L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("msgStr", String.class)
         };
         break;
      default:
         serialVersionUID = 3176664577895105181L;
         serialPersistentFields = new ObjectStreamField[0];
      }
   }
   
   // Constructors --------------------------------------------------
   public XMLParseException()
   {
      super();
   }

   public XMLParseException(String s)
   {
      super(s);
   }

   public XMLParseException(Exception e, String s)
   {
      // REVIEW Adrian Brock: The exception doesn't seem to be serialized so
      // I'm including it in the constructed message.   
      super("XMLParseException: " + s + 
        ((e == null) ? "" : "\nCause: " + e.toString()));
   }

   // Object overrides ----------------------------------------------

   // Private -------------------------------------------------------

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
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
         oos.writeFields();
         break;
      default:
         oos.defaultWriteObject();
      }
   }
}




