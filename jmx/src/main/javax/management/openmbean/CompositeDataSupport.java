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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An implementation of CompositeData.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @author <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 *
 * @version $Revision: 57200 $
 */
public class CompositeDataSupport
   implements CompositeData, Serializable
{
   // Constants -----------------------------------------------------------------

   private static final long serialVersionUID = 8003518976613702244L;
   private static final ObjectStreamField[] serialPersistentFields =
      new ObjectStreamField[]
      {
         new ObjectStreamField("contents",      SortedMap.class),
         new ObjectStreamField("compositeType", CompositeType.class),
      };

   // Attributes ----------------------------------------------------

   /**
    * The contents of the composite data
    */
   private SortedMap contents;

   /**
    * The composite type of the composite data
    */
   private CompositeType compositeType;

   // cache the hashCode
   private int hashCode;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct Composite Data 
    *
    * @param compositeType the composite type of the data
    * @param itemNames the names of the values
    * @param itemValues the values
    * @exception IllegalArgumentException for a null or empty argument
    * @exception OpenDataException when the items do not match the
    *            CompositeType
    */
   public CompositeDataSupport(CompositeType compositeType,
                               String[] itemNames,
                               Object[] itemValues)
      throws OpenDataException
   {
      if (compositeType == null)
         throw new IllegalArgumentException("null compositeType");
      if (itemNames == null)
         throw new IllegalArgumentException("null itemNames");
      if (itemValues == null)
         throw new IllegalArgumentException("null itemValues");
      if (itemNames.length == 0)
         throw new IllegalArgumentException("empty itemNames");
      if (itemValues.length == 0)
         throw new IllegalArgumentException("empty itemValues");
      if (itemNames.length != itemValues.length)
         throw new IllegalArgumentException("itemNames has size " + itemNames.length +
            " but itemValues has size " + itemValues.length);
      int compositeNameSize = compositeType.keySet().size();
      if (itemNames.length != compositeNameSize)
         throw new OpenDataException("itemNames has size " + itemNames.length +
            " but composite type has size " + compositeNameSize);

      this.compositeType = compositeType;
      contents = new TreeMap();

      for (int i = 0; i < itemNames.length; i++)
      {
         if (itemNames[i] == null || itemNames[i].length() == 0)
            throw new IllegalArgumentException("Item name " + i + " is null or empty");
         if (contents.get(itemNames[i]) != null)
            throw new OpenDataException("duplicate item name " + itemNames[i]);
         OpenType openType = compositeType.getType(itemNames[i]);
         if (openType == null)
            throw new OpenDataException("item name not in composite type " + itemNames[i]);
         if (itemValues[i] != null && openType.isValue(itemValues[i]) == false)
            throw new OpenDataException("item value " + itemValues[i] + " for item name " +
               itemNames[i] + " is not a " + openType);
         contents.put(itemNames[i], itemValues[i]);
      }
   }

   /**
    * Construct Composite Data 
    *
    * @param compositeType the composite type of the data
    * @param items map of strings to values
    * @exception IllegalArgumentException for a null or empty argument
    * @exception OpenDataException when the items do not match the
    *            CompositeType
    * @exception ArrayStoreException when a key to the map is not a String
    */
   public CompositeDataSupport(CompositeType compositeType, Map items)
      throws OpenDataException
   {
      init(compositeType, items);
   }

   // Public --------------------------------------------------------

   // Composite Data Implementation ---------------------------------

   public CompositeType getCompositeType()
   {
      return compositeType;
   }

   public Object get(String key)
   {
      validateKey(key);
      return contents.get(key);
   }

   /**
    * Returns an array of the values of the items whose names are specified by keys, in the same order as keys.
    */
   public Object[] getAll(String[] keys)
   {
      if (keys == null)
         throw new IllegalArgumentException("Null keys");

      Object[] result = new Object[keys.length];
      for (int i = 0; i < keys.length; i++)
      {
         validateKey(keys[i]);
         result[i] = contents.get(keys[i]);
      }
      return result;
   }

   public boolean containsKey(String key)
   {
      if (key == null || key.length() == 0)
         return false;
      return contents.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return contents.containsValue(value);
   }

   public Collection values()
   {
      return Collections.unmodifiableCollection(contents.values());
   }

   // Serializable Implementation -----------------------------------

   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = in.readFields();
      SortedMap contents = (SortedMap) getField.get("contents", null);
      CompositeType compositeType = (CompositeType) getField.get("compositeType", null);
      try
      {
         init(compositeType, contents);
      }
      catch (Exception e)
      {
         throw new StreamCorruptedException(e.toString());
      }
   }

   // Object Overrides ----------------------------------------------

   public boolean equals(Object obj)
   {
      if (obj == null || (obj instanceof CompositeData) == false)
         return false;
      if (obj == this)
         return true;

      CompositeData other = (CompositeData) obj;
      if (compositeType.equals(other.getCompositeType()) == false)
         return false;
      if (values().size() != other.values().size())
         return false;

      for (Iterator i = contents.keySet().iterator(); i.hasNext();)
      {
         String key = (String) i.next();
         Object thisValue = this.get(key);
         Object otherValue = other.get(key);
         if ((thisValue == null && otherValue == null || thisValue != null && thisValue.equals(otherValue)) == false)
            return false;
      }
      return true;
   }

   /**
    * Returns the hash code value for this CompositeDataSupport instance.
    *
    * The hash code of a CompositeDataSupport instance is the sum of the hash codes of all elements of information used
    * in equals comparisons (ie: its composite type and all the item values).
    *
    * This ensures that t1.equals(t2) implies that t1.hashCode()==t2.hashCode() for any two CompositeDataSupport
    * instances t1 and t2, as required by the general contract of the method Object.hashCode .
    *
    * However, note that another instance of a class implementing the CompositeData interface may be equal to this
    * CompositeDataSupport instance as defined by equals(java.lang.Object), but may have a different hash code if it
    * is calculated differently.
    */
   public int hashCode()
   {
      if (hashCode != 0)
         return hashCode;

      hashCode = compositeType.hashCode();
      Iterator it = contents.values().iterator();
      while (it.hasNext())
      {
         Object value = it.next();
         if (value != null)
            hashCode += value.hashCode();
      }
      
      return hashCode;
   }

   /**
    * Returns a string representation of this CompositeDataSupport instance.
    *
    * The string representation consists of the name of this class (ie javax.management.openmbean.CompositeDataSupport),
    * the string representation of the composite type of this instance, and the string representation of the contents
    * (ie list the itemName=itemValue mappings).
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getName());
      buffer.append(": compositeType=[");
      buffer.append(getCompositeType());
      buffer.append("] mappings=[");
      Iterator keys = compositeType.keySet().iterator();
      while(keys.hasNext())
      {
         Object key = keys.next();
         buffer.append(key + "=" + contents.get(key));
         if (keys.hasNext())
            buffer.append(",");
      }
      buffer.append("]");
      return buffer.toString();
   }

   // Private -------------------------------------------------------

   /**
    * Initialise the composite data
    *
    * @param compositeType the composite type of the data
    * @param items map of strings to values
    * @exception IllegalArgumentException for a null or empty argument
    * @exception OpenDataException when the items do not match the
    *            CompositeType
    * @exception ArrayStoreException when a key to the map is not a String
    */
   private void init(CompositeType compositeType, Map items)
      throws OpenDataException
   {
      if (compositeType == null)
         throw new IllegalArgumentException("null compositeType");
      if (items == null)
         throw new IllegalArgumentException("null items");
      if (items.size() == 0)
         throw new IllegalArgumentException("empty items");
      int compositeNameSize = compositeType.keySet().size();
      if (items.size() != compositeNameSize)
         throw new OpenDataException("items has size " + items.size() +
            " but composite type has size " + compositeNameSize);

      this.compositeType = compositeType;
      contents = new TreeMap();

      for (Iterator i = items.keySet().iterator(); i.hasNext();)
      {
         Object next = i.next();
         if (next != null && (next instanceof String) == false)
            throw new ArrayStoreException("key is not a String " + next);
         String key = (String) next;
         if (key == null || key.length() == 0)
            throw new IllegalArgumentException("Key is null or empty");
         OpenType openType = compositeType.getType(key);
         if (openType == null)
            throw new OpenDataException("item name not in composite type " + key);
         Object value = items.get(key);
         if (value != null && openType.isValue(value) == false)
            throw new OpenDataException("item value " + value + " for item name " +
               key + " is not a " + openType);
         contents.put(key, value);
      }
   }

   /**
    * Validates the key against the composite type
    *
    * @param key the key to check
    * @exception IllegalArgumentException for a null or empty key
    * @exception InvalidKeyException if the key not a valid item name for the composite type
    */
   private void validateKey(String key)
      throws InvalidKeyException
   {
      if (key == null || key.length() == 0)
         throw new IllegalArgumentException("null or empty key");
      if (compositeType.containsKey(key) == false)
         throw new InvalidKeyException("no such item name " + key + " for composite type " +
            compositeType);
   }
}

