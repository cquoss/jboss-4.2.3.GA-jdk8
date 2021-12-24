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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

import org.jboss.dom4j.Attribute;
import org.jboss.dom4j.Document;
import org.jboss.dom4j.DocumentException;
import org.jboss.dom4j.DocumentHelper;
import org.jboss.dom4j.Element;
import org.jboss.dom4j.io.OutputFormat;
import org.jboss.dom4j.io.SAXReader;
import org.jboss.dom4j.io.XMLWriter;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;
import org.jboss.util.xml.JBossEntityResolver;

/**
 * Support class for creating descriptors.
 *
 * @see javax.management.Descriptor
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a> * 
 * @version $Revision: 57200 $
 */
public class DescriptorSupport
   implements Descriptor
{

   // TODO: the spec doesn't define equality for descriptors
   //       we should override equals to match descriptor field, value pairs
   //       this does not appear to be the case with the 1.0 RI though
   
   // Attributes ----------------------------------------------------
   
   /**
    * Map for the descriptor field -> value.
    */
   private Map fieldMap;

   // Static --------------------------------------------------------

   private static final int DEFAULT_SIZE = 20;

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = 8071560848919417985L;
         break;
      default:
         serialVersionUID = -6292969195866300415L;
      }
      serialPersistentFields = new ObjectStreamField[]
      {
         new ObjectStreamField("descriptor", HashMap.class)
      };
   }

   
   // Constructors --------------------------------------------------
   /**
    * Default constructor.
    */
   public DescriptorSupport()
   {
      fieldMap = Collections.synchronizedMap(new HashMap(DEFAULT_SIZE));
   }

   /**
    * Creates descriptor instance with a given initial size.
    *
    * @param   initialSize initial size of the descriptor
    * @throws  MBeanException this exception is never thrown but is declared here
    *          for Sun RI API compatibility
    * @throws  RuntimeOperationsException if the <tt>initialSize</tt> is zero or negative. The target
    *          exception wrapped by this exception is an instace of <tt>IllegalArgumentException</tt> class.
    */
   public DescriptorSupport(int initialSize) throws MBeanException
   { 
      if (initialSize <= 0)
         // required by RI javadoc
         throw new RuntimeOperationsException(new IllegalArgumentException("initialSize <= 0"));
         
      fieldMap = Collections.synchronizedMap(new HashMap(initialSize));
   }

   /**
    * Copy constructor.
    *
    * @param   descriptor the descriptor to be copied
    * @throws  RuntimeOperationsException if descriptor is null. The target exception wrapped by this
    *          exception is an instance of <tt>IllegalArgumentException</tt> class.
    */
   public DescriptorSupport(DescriptorSupport descriptor)
   {
      if (descriptor != null)
      {
         String[] fieldNames = descriptor.getFieldNames();
         fieldMap = Collections.synchronizedMap(new HashMap(fieldNames.length));
         this.setFields(fieldNames, descriptor.getFieldValues(fieldNames));
      }
      else
      {
         fieldMap = Collections.synchronizedMap(new HashMap(DEFAULT_SIZE));
      }
   }

   /**
    * Creates descriptor instance with given field names and values.if both field names and field
    * values array contain empty arrays, an empty descriptor is created.
    * None of the name entries in the field names array can be a <tt>null</tt> reference.
    * Field values may contain <tt>null</tt> references.
    *
    * @param   fieldNames  Contains names for the descriptor fields. This array cannot contain
    *                      <tt>null</tt> references. If both <tt>fieldNames</tt> and <tt>fieldValues</tt>
    *                      arguments contain <tt>null</tt> or empty array references then an empty descriptor
    *                      is created. The size of the <tt>fieldNames</tt> array must match the size of
    *                      the <tt>fieldValues</tt> array.
    * @param   fieldValues Contains values for the descriptor fields. Null references are allowed.
    *
    * @throws RuntimeOperationsException if array sizes don't match
    */
   public DescriptorSupport(String[] fieldNames, Object[] fieldValues) 
      throws RuntimeOperationsException
   {
      fieldMap = Collections.synchronizedMap(new HashMap(DEFAULT_SIZE));
      setFields(fieldNames, fieldValues);
   }

   public DescriptorSupport(String[] fields)
   {
      if (fields == null)
      {
         fieldMap = Collections.synchronizedMap(new HashMap(DEFAULT_SIZE));
         return;
      }

      int j = 0;
      for (int i = 0; i < fields.length; ++i)
      {
         if (fields[i] != null && fields[i].length() != 0)
         {
            ++j;
         }
      }

      fieldMap = Collections.synchronizedMap(new HashMap(j));
      String[] names = new String[j];
      String[] values = new String[j];

      j = 0;
      for (int i = 0; i < fields.length; ++i)
      {
         if (fields[i] == null || fields[i].length() == 0)
            continue;

         try
         {
            int index = fields[i].indexOf('=');
            if (index == -1)
               throw new IllegalArgumentException("Invalid field " + fields[i]);

            names[j] = fields[i].substring(0, index);
            if (index == fields[i].length()-1)
               values[j] = null;
            else
               values[j] = fields[i].substring(index + 1, fields[i].length());
            ++j;
         }
         catch (RuntimeException e)
         {
            throw new RuntimeOperationsException(e, "Error in field " + i);
         }
      }

      setFields(names, values);
   }

   /**
    * Descriptor constructor taking an XML String.
    * In this implementation, all field values will be created as Strings.
    * If the field values are not Strings, the programmer will have to reset or convert these fields correctly.
    */
   public DescriptorSupport(String xmlString)
      throws MBeanException, RuntimeOperationsException, XMLParseException
   {
      if (xmlString == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null xmlString"));

      fieldMap = Collections.synchronizedMap(new HashMap(DEFAULT_SIZE));
      try
      {
         SAXReader saxReader = new SAXReader();
         saxReader.setEntityResolver(new JBossEntityResolver());

         Document document = saxReader.read(new StringReader(xmlString));
         Element root = document.getRootElement();
         String rootName = root.getName();
         if (rootName.equalsIgnoreCase("Descriptor"))
         {
            // iterate through child elements of root
            for (Iterator i = root.elementIterator(); i.hasNext();)
            {
               Element element = (Element) i.next();
               if (element.getName().equals("field"))
               {
                  Attribute attr = element.attribute("name");
                  if (attr != null)
                  {
                     String name = attr.getText();
                     String value = element.getTextTrim();
                     setField(name, value);
                  }
                  else
                  {
                     throw new XMLParseException("Cannot find attribute 'name' in " + element);
                  }
               }
            }
         }
         else
         {
            RuntimeException ex = new IllegalArgumentException(
               "Root element must be Descriptor, saw: "+rootName);
            throw new RuntimeOperationsException(ex);
         }
      }
      catch (DocumentException e)
      {
         throw new XMLParseException(e, "Cannot parse XML string: " + xmlString);
      }
   }

   // Public --------------------------------------------------------
   public Object getFieldValue(String inFieldName)
   {
      try
      {
         checkFieldName(inFieldName);
         return fieldMap.get(new FieldName(inFieldName));
      }
      catch (RuntimeException e)
      {
         throw new RuntimeOperationsException(e, e.toString());
      }
   }

   public void setField(String inFieldName, Object fieldValue)
   {
      try
      {
         checkFieldName(inFieldName);
         validateField(inFieldName, fieldValue);
         fieldMap.put(new FieldName(inFieldName), fieldValue);
      }
      catch (RuntimeException e)
      {
         throw new RuntimeOperationsException(e, e.toString());
      }
   }

   /**
    * Returns String array of fields in the format fieldName=fieldValue.
    * If there are no fields in the descriptor, then an empty String array is returned.
    * If a fieldValue is not a String then the toString() method is called on it and its returned value is used as
    * the value for the field enclosed in parenthesis.
    */
   public String[] getFields()
   {
      String[] fieldStrings = new String[fieldMap.size()];
      Iterator it = fieldMap.keySet().iterator();
      synchronized (fieldMap)
      {
         for (int i = 0; i < fieldMap.size(); ++i)
         {
            FieldName key = (FieldName)it.next();
            Object value = fieldMap.get(key);
            if (value != null)
            {
               if (value instanceof String)
                  fieldStrings[i] = key + "=" + value;
               else
                  fieldStrings[i] = key + "=(" + value + ")";
            }
            else
            {
               fieldStrings[i] = key + "=";
            }
         }
      }

      return fieldStrings;
   }

   /**
    * Returns string array of fields names. If the descriptor is empty, you will get an empty array.
    */
   public String[] getFieldNames()
   {
      String[] fields = new String[fieldMap.size()];
      Iterator it = fieldMap.keySet().iterator();
      synchronized (fieldMap)
      {
         for (int i = 0; i < fieldMap.size(); ++i)
         {
            FieldName key = (FieldName)it.next();
            fields[i] = key.getName();
         }
      }
      return fields;
   }

   /**
    * Returns all the field values in the descriptor as an array of Objects.
    * The returned values are in the same order as the fieldNames String array parameter.
    */
   public Object[] getFieldValues(String[] fieldNames)
   {
      if (fieldMap.size() == 0)
         return new Object[0];

      Object[] values = null;
      if (fieldNames == null)
      {
         values = new Object[fieldMap.size()];
         Iterator it = fieldMap.values().iterator();
         synchronized (fieldMap)
         {
            for (int i = 0; i < fieldMap.size(); ++i)
               values[i] = it.next();
         }
      }
      else
      {
         values = new Object[fieldNames.length];
         for (int i = 0; i < fieldNames.length; ++i)
         {
            if (fieldNames[i] == null || fieldNames[i].equals(""))
               values[i] = null;
            else
               values[i] = fieldMap.get(new FieldName(fieldNames[i]));
         }
      }
         
      return values;
   }

   /**
    * Sets all Fields in the list to the new value in with the same index in the fieldValue array.
    * Array sizes must match. The field value will be validated before it is set (by calling the method isValid)
    * If it is not valid, then an exception will be thrown. If the arrays are empty, then no change will take effect.
    */
   public void setFields(String[] fieldNames, Object[] fieldValues)
   {
      if (fieldNames == null || fieldValues == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("fieldNames or fieldValues was null."));

      if (fieldNames.length == 0 && fieldValues.length == 0)
         return;

      if (fieldNames.length != fieldValues.length)
         throw new RuntimeOperationsException(new IllegalArgumentException("fieldNames and fieldValues array size must match."));

      try
      {
         for (int i = 0; i < fieldNames.length; ++i)
         {
            String name = fieldNames[i];
            checkFieldName(name);
            validateField(name, fieldValues[i]);
            fieldMap.put(new FieldName(name), fieldValues[i]);
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeOperationsException(e);
      }
   }

   public synchronized Object clone()
   {
      try
      {
         DescriptorSupport clone = (DescriptorSupport)super.clone();
      
         clone.fieldMap = Collections.synchronizedMap(new HashMap(this.fieldMap));

         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         // Descriptor interface won't allow me to throw CNSE
         throw new RuntimeOperationsException(new RuntimeException(e.getMessage()), e.toString());
      }
   }

   public void removeField(String fieldName)
   {
      if (fieldName == null || fieldName.equals(""))
         return;

      fieldMap.remove(new FieldName(fieldName));
   }

   /**
    * Returns true if all of the fields have legal values given their names.
    *
    *  This implementation does not support interopreating with a directory or lookup service.
    * Thus, conforming to the specification, no checking is done on the "export" field.
    *
    * Otherwise this implementation returns false if:
    * - name and descriptorType fieldNames are not defined, or null, or empty, or not String
    * - class, role, getMethod, setMethod fieldNames, if defined, are null or not String
    * - persistPeriod, currencyTimeLimit, lastUpdatedTimeStamp, lastReturnedTimeStamp if defined, are null, or not a Numeric String or not a Numeric Value >= -1
    * - log fieldName, if defined, is null, or not a Boolean or not a String with value "t", "f", "true", "false". These String values must not be case sensitive.
    * - visibility fieldName, if defined, is null, or not a Numeric String or a not Numeric Value >= 1 and <= 4
    * - severity fieldName, if defined, is null, or not a Numeric String or not a Numeric Value >= 1 and <= 5
    * - persistPolicy fieldName, if defined, is null, or not a following String :
    * - "OnUpdate", "OnTimer", "NoMoreOftenThan", "Always", "Never". These String values must not be case sensitive.
    *
    * @return true if the values are legal.
    * @throws RuntimeOperationsException If the validity checking fails for any reason, this exception will be thrown.

    */
   public boolean isValid()
      throws RuntimeOperationsException
   {
      try
      {
         validateString(ModelMBeanConstants.NAME, true);
         validateString(ModelMBeanConstants.DESCRIPTOR_TYPE, true);

         synchronized (fieldMap)
         {
            for (Iterator i = fieldMap.entrySet().iterator(); i.hasNext(); )
            {
               Map.Entry entry = (Map.Entry) i.next();
               FieldName name = (FieldName) entry.getKey();
               Object value = entry.getValue();
               validateField(name.getName(), value);
            }
         }
      }
      catch (RuntimeException e)
      {
         return false;
      }

      return true;
   }

   /**
    * Returns an XML String representing the descriptor.
    */
   public String toXMLString()
      throws RuntimeOperationsException
   {
      // Return the javadoc specified empty representation
      if( fieldMap.size() == 0 )
         return "<Descriptor></Descriptor>";

      /* Build the non-empty rep
         <Descriptor name='...' field='...' />
      */
      try
      {
         Document document = DocumentHelper.createDocument();
         Element root = document.addElement("Descriptor");
         String[] names = getFieldNames();
         for (int i = 0; i < names.length; i++)
         {
            String name = names[i];
            Object value = getFieldValue(name);
            Element field = root.addElement("field");
            field.addAttribute("name", name);
            field.addText(value.toString());
         }

         StringWriter sw = new StringWriter();
         OutputFormat format = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(sw, format);
         writer.write(document);
         writer.close();
         return sw.toString();
      }
      catch (IOException e)
      {
         throw new RuntimeOperationsException(new RuntimeException(e),
            "Cannot get XML representation");
      }
   }

   // Object overrides ----------------------------------------------

   public String toString()
   {
      String[] names  = getFieldNames();
      Object[] values = getFieldValues(names);
      
      StringBuffer sbuf = new StringBuffer(500);
      sbuf.append(getClass()).append('@').append(System.identityHashCode(this)).append('[');
      
      if (names.length == 0)
         return "<empty descriptor>";
      else
      {
         for (int i = 0; i < values.length; ++i)
         {
            sbuf.append(names[i]);
            sbuf.append("=");
            sbuf.append(values[i]);
            if (i < values.length - 1)
               sbuf.append(",");
         }
      }
      
      sbuf.append(']');
      
      return sbuf.toString();
   }

   // Private -----------------------------------------------------

   private void checkFieldName(String inFieldName)
   {
      if (inFieldName == null || inFieldName.equals(""))
         throw new IllegalArgumentException("null or empty field name");
   }

   private void validateField(String inFieldName, Object value)
   {
      String fieldName = inFieldName;
      if (fieldName.equalsIgnoreCase(ModelMBeanConstants.NAME))
         validateString(inFieldName, value, true);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.DESCRIPTOR_TYPE))
         validateString(inFieldName, value, true);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.CLASS))
         validateString(inFieldName, value, false);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.ROLE))
         validateString(inFieldName, value, false);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.GET_METHOD))
         validateString(inFieldName, value, false);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.SET_METHOD))
         validateString(inFieldName, value, false);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.PERSIST_PERIOD))
         validateNumeric(inFieldName, value);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.CURRENCY_TIME_LIMIT))
         validateNumeric(inFieldName, value);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.LAST_UPDATED_TIME_STAMP))
         validateNumeric(inFieldName, value);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.LAST_UPDATED_TIME_STAMP2))
        validateNumeric(inFieldName, value);      
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.LAST_RETURNED_TIME_STAMP))
         validateNumeric(inFieldName, value);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.LOG))
         validateBoolean(inFieldName, value);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.VISIBILITY))
         validateNumeric(inFieldName, value, 1, 4);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.SEVERITY))
         validateNumeric(inFieldName, value, 1, 6);
      else if (fieldName.equalsIgnoreCase(ModelMBeanConstants.PERSIST_POLICY))
         validatePersistPolicy(inFieldName, value);
   }

   private void validateString(String fieldName, boolean mandatory)
   {
      Object value = fieldMap.get(new FieldName(fieldName));
      validateString(fieldName, value, mandatory);
   }

   private void validateString(String fieldName, Object value, boolean mandatory)
   {
      if (value == null && mandatory == true)
         throw new IllegalArgumentException("Expected a value for mandatory field '" + fieldName + "'");
      else if (value == null)
         throw new IllegalArgumentException("Expected a value for field '" + fieldName + "'");
      if ((value instanceof String) == false)
         throw new IllegalArgumentException("Expected a String for field '" + fieldName + "'");
      String string = (String) value;
      if (string.length() == 0)
         throw new IllegalArgumentException("Empty value for field '" + fieldName + "'");
   }

   private void validatePersistPolicy(String fieldName, Object value)
   {
      validateString(fieldName, value, false);
      String string = ((String) value);
      String[] policies = ModelMBeanConstants.PERSIST_POLICIES;
      for (int i = 0; i < policies.length; ++i)
      {
         if (policies[i].equalsIgnoreCase(string))
            return;
      }
      throw new IllegalArgumentException("Invalid value " + value + " for field '" + fieldName +
                                         "' expected one of " + Arrays.asList(policies));
   }

   private void validateBoolean(String fieldName, Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("Expected a value for field '" + fieldName + "'");
      if (value instanceof String)
      {
         String string = ((String) value);
         if (string.equalsIgnoreCase("T") || string.equalsIgnoreCase("F"))
            return;
         if (string.equalsIgnoreCase("TRUE") || string.equalsIgnoreCase("FALSE"))
            return;
      }
      else if (value instanceof Boolean)
         return;
      throw new IllegalArgumentException("Invalid value " + value + " for field '" + fieldName + "'");
   }

   private long validateNumeric(String fieldName, Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("Expected a value for field '" + fieldName + "'");

      Long number = null;
      if (value instanceof String)
         number = new Long((String) value);
      else if (value instanceof Number)
         number = new Long(((Number) value).longValue());
      if (number != null && number.longValue() >= -1)
         return number.longValue();

      throw new IllegalArgumentException("Invalid value " + value + " for field '" + fieldName + "'");
   }

   private void validateNumeric(String fieldName, Object value, int min, int max)
   {
      long result = validateNumeric(fieldName, value);
      if (result >= min && result <= max)
         return;
      throw new IllegalArgumentException("Invalid value " + value + " for field '" + fieldName + "'");
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      HashMap serMap = (HashMap) getField.get("descriptor", null);
      if (serMap == null)
         throw new StreamCorruptedException("Null descriptor?");

      // replace the keys with FieldName objects
      fieldMap = Collections.synchronizedMap(new HashMap());
      Iterator it = serMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         FieldName key = new FieldName((String)entry.getKey());
         fieldMap.put(key, entry.getValue());
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      /* Since non-Serializable values can be put into the descriptor
         just remove them when writing out the serialized form
      */
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream tstOOS = new ObjectOutputStream(baos);

      // replace the keys with strings
      HashMap serMap = new HashMap();
      Iterator it = fieldMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         String key = ((FieldName)entry.getKey()).name;
         Object value = entry.getValue();
         if( value instanceof Serializable )
         {
            // Validate that the object's references are serializable
            try
            {
               baos.reset();
               tstOOS.writeObject(value);
               serMap.put(key, value);
            }
            catch(Exception ignore)
            {
            }
         }
      }
      baos.close();
      tstOOS.close();

      putField.put("descriptor", serMap);
      oos.writeFields();
   }

   /**
    * Provides case insensitive hashCode, equals.
    */
   private class FieldName implements Serializable 
   {
      static final long serialVersionUID = 2645619836053638810L;
      private String name;
      private int hashCode;

      public FieldName(String aName)
      {
         if (aName == null)
            throw new IllegalArgumentException("null name");
         this.name = aName;
      }

      public String getName()
      {
         return name;
      }

      public int hashCode()
      {
         if (hashCode == 0)
            return hashCode = name.toLowerCase().hashCode();
         else
            return hashCode;
      }

      public boolean equals(Object obj)
      {
         if (obj == null) return false;
         if (obj == this) return true;
         if (obj instanceof FieldName)
            return name.equalsIgnoreCase(((FieldName) obj).name);
         if (obj instanceof String)
            return name.equalsIgnoreCase((String) obj);
         return false;
      }

      public String toString()
      {
         return name;
      }

   }
}
