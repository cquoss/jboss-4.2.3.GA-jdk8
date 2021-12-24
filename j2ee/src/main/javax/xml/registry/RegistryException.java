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
package javax.xml.registry;

import java.io.ObjectStreamField;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.xml.registry.infomodel.Key;
import javax.xml.namespace.QName;

import org.jboss.util.id.SerialVersion;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57196 $
 */
public class RegistryException
   extends JAXRException
{
   /** @since 4.0.2 */
   static final long serialVersionUID;
   static final int KEY_IDX = 0;
   private static final ObjectStreamField[] serialPersistentFields;
   static
   {
      if (SerialVersion.version == SerialVersion.LEGACY)
      {
         serialVersionUID = 5830899222180533586L;
         serialPersistentFields = new ObjectStreamField[] {
            new ObjectStreamField("key", QName.class),
         };
      }
      else
      {
         serialVersionUID = -2972094643801708304L;
         serialPersistentFields = new ObjectStreamField[] {
            new ObjectStreamField("errorObjectKey", QName.class),
         };
      }
   }

   private Key key;

	public RegistryException()
   {
   }
	public RegistryException(String msg)
   {
      this(msg, null);
   }
	public RegistryException(String msg, Throwable cause)
   {
      super(msg, cause);
   }

	public RegistryException(Throwable cause)
   {
      super(cause);
   }

	public Key getErrorObjectKey() throws JAXRException
   {
      return key;
   }
	public void setErrorObjectKey(Key key) throws JAXRException
   {
      this.key = key;
   }

   // Private -------------------------------------------------------
   private void readObject(ObjectInputStream ois)
      throws ClassNotFoundException, IOException
   {
      ObjectInputStream.GetField fields = ois.readFields();
      String name = serialPersistentFields[KEY_IDX].getName();
      this.key = (Key) fields.get(name, null);
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField fields =  oos.putFields();
      String name = serialPersistentFields[KEY_IDX].getName();
      fields.put(name, key);
      oos.writeFields();
   }
}
