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
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import org.jboss.mx.util.ObjectNamePatternHelper;
import org.jboss.mx.util.ObjectNamePatternHelper.PropertyPattern;
import org.jboss.mx.util.QueryExpSupport;
import org.jboss.mx.util.Serialization;

/**
 * Object name represents the MBean reference.
 *
 * @see javax.management.MBeanServer
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020521 Adrian Brock:</b>
 * <ul>
 * <li>Allow *,* in the hashtable properties to signify a property pattern
 * </ul>
 * <p><b>20020710 Adrian Brock:</b>
 * <ul>
 * <li> Serialization
 * <p><b>20030114 Adrian Brock:</b>
 * <ul>
 * <li> Valid character changes for 1.2
 * <li> The new line character is not allowed anywhere in the
 *      ObjectName, because of a bodge in the relation service
 * <li> Domains can now contain = and ,
 * <li> Keys are unchanged except for the new line restriction
 * <li> For values, "?* are allowed providing they are proceeed
 *      by a \ and the escaping is in enclosed in a "" pair.
 *      A new line must be \n inside a "" pair.
 *      =,: are allowed inside a "" pair.
 * </ul>
 */
public class ObjectName
   implements java.io.Serializable, QueryExp
{

   // Attributes ----------------------------------------------------
   private transient boolean hasPattern = false;
   private transient boolean hasDomainPattern = false;
   private transient boolean hasPropertyPattern = false;
   private transient Hashtable propertiesHash = null;
   private transient ArrayList propertiesList = null;

   private transient String domain = null;
   // The key property list string. This string is independent of whether the ObjectName is a pattern.
   private transient String kProps = null;
   // The canonical key property list string. This string is independent of whether the ObjectName is a pattern.
   private transient String ckProps = null;
   // The canonical name.
   private transient String cName = null;
   // The properties as passed to the constructor
   private transient String constructorProps;

   private transient int hash;
   private transient PropertyPattern propertyPattern;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = -5467795090068647408L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("domain",             String.class),
            new ObjectStreamField("propertyList",       Hashtable.class),
            new ObjectStreamField("propertyListString", String.class),
            new ObjectStreamField("canonicalName"     , String.class),
            new ObjectStreamField("pattern"           , Boolean.TYPE),
            new ObjectStreamField("propertyPattern"   , Boolean.TYPE)
         };
         break;
      default:
         serialVersionUID = 1081892073854801359L;
         serialPersistentFields = new ObjectStreamField[0];
      }
   }

   /**
    * Return an instance of an ObjectName
    *
    * @todo Review: return common object names?
    * @param name a string representation of the object name
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null name
    */
   public static ObjectName getInstance(String name)
      throws MalformedObjectNameException, NullPointerException
   {
      return new ObjectName(name);
   }

   /**
    * Return an instance of an ObjectName
    *
    * @todo Review: return common object names?
    * @param domain the domain of the object name
    * @param key the key of the single property
    * @param value the value of the single property
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null parameter
    */
   public static ObjectName getInstance(String domain, String key, String value)
      throws MalformedObjectNameException, NullPointerException
   {
      return new ObjectName(domain, key, value);
   }

   /**
    * Return an instance of an ObjectName
    *
    * @todo Review: return common object names?
    * @param domain the domain of the object name
    * @param table of hashtable for key property pairs
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null parameter
    */
   public static ObjectName getInstance(String domain, Hashtable table)
      throws MalformedObjectNameException, NullPointerException
   {
      return new ObjectName(domain, table);
   }

   /** Return an instance of ObjectName that can be used anywhere the given
    * object can be used. The returned object may be of a subclass of
    * ObjectName. If name is of a subclass of ObjectName, it is not guaranteed
    * that the returned object will be of the same class.
    * 
    * The returned value may or may not be identical to name. Calling this
    * method twice with the same parameters may return the same object or two
    * equal but not identical objects.
    * 
    * Since ObjectName is immutable, it is not usually useful to make a copy
    * of an ObjectName. The principal use of this method is to guard against a
    * malicious caller who might pass an instance of a subclass with surprising
    * behaviour to sensitive code. Such code can call this method to obtain an
    * ObjectName that is known not to have surprising behaviour.
    * 
    * @param name - an instance of the ObjectName class or of a subclass 
    * @return an instance of ObjectName or a subclass that is known to have the
    * same semantics. If name respects the semantics of ObjectName, then the
    * returned object is equal (though not necessarily identical) to name. 
    * @throws NullPointerException
    */ 
   public static ObjectName getInstance(ObjectName name)
      throws NullPointerException
   {
      return name;
   }

   /**
    * Quotes the passed string suitable for use as a value in an
    * ObjectName.
    *
    * @param value the string to quote
    * @return the quoted string
    * @exception NullPointerException for a null string
    */
   public static String quote(String value)
      throws NullPointerException
   {
      if (value == null)
         throw new NullPointerException("null value");

      StringBuffer buffer = new StringBuffer(value.length() + 10);
      buffer.append('"');
      char[] chars = value.toCharArray();
      for (int i = 0; i < chars.length; i++)
      {
         switch (chars[i])
         {
         case '"':
         case '*':
         case '?':
         case '\\':
           buffer.append('\\').append(chars[i]);
           break;
         case '\n':
           buffer.append('\\').append('n');
           break;
         default:
           buffer.append(chars[i]);
         }
      }
      buffer.append('"');

      return buffer.toString();
   }

   /**
    * Unquotes a string, unquote(quote(s)).equals(s) is always true.
    *
    * @param value the string to unquote
    * @return the unquoted string
    * @exception IllegalArgumentException when the string is
    *            not of a form that can be unquoted.
    * @exception NullPointerException for a null string
    */
   public static String unquote(String value)
      throws IllegalArgumentException, NullPointerException
   {
      if (value == null)
         throw new NullPointerException("null value");
      if (value.length() == 0)
         throw new IllegalArgumentException("Empty value");

      StringBuffer buffer = new StringBuffer(value.length());

      char[] chars = value.toCharArray();

      boolean inQuote = false;
      boolean escape = false;

      for (int i = 0; i < chars.length; ++i)
      {
         // Valid escape character?
         char c = chars[i];
         if (escape)
         {
            switch (c)
            {
               case '\\':
               case '"':
               case '*':
               case '?':
                  escape = false;
                  buffer.append(c);
                  break;
               case 'n':
                  escape = false;
                  buffer.append('\n');
                  break;

               // Invalid escape character
               default:
                  throw new IllegalArgumentException(
                     "The value " + value + " contains an invalid escape sequence backslash " + c);
            }
         }

         // Starting a quote
         else if (inQuote == false && c == '"')
            inQuote = true;

         // Ending a quote
         else if (inQuote && c == '"')
            inQuote = false;

         // Escaping
         else if (inQuote && c == '\\')
            escape = true;

         // Inside a quote
         else if (inQuote)
         {
            switch (c)
            {
               case '"':
                  throw new IllegalArgumentException(
                     "The value " + value + " cannot contain quote inside a quote pair, use backslash quote");
               case '*':
               case '?':
                  throw new IllegalArgumentException(
                     "The value " + value + " cannot contain " + c + " inside a quote pair, use backslash " + c);
               case '\n':
                  throw new IllegalArgumentException(
                     "The value " + value + " cannot contain a new line inside a quote pair, use backslash n");
            }
            buffer.append(c);
         }

         // Plain text
         else
            throw new IllegalArgumentException(
               "The value " + value + " is not enclosed in quotes");
      }

      if (inQuote)
         throw new IllegalArgumentException(
            "Unterminated quote pair, missing quote");
      if (escape)
         throw new IllegalArgumentException(
            "Unterminated escape, missing one of backslash quote asterisk question mark or n");

      return buffer.toString();
   }

   // Constructors --------------------------------------------------

   /**
    *
    * @param name a string representation of the object name
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null name
    */
   public ObjectName(String name)
      throws MalformedObjectNameException, NullPointerException
   {
      init(name);
   }

   /**
    * Construct a new ObjectName
    *
    * @param domain the domain of the object name
    * @param key the key of the single property
    * @param value the value of the single property
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null parameter
    */
   public ObjectName(String domain, String key, String value)
      throws MalformedObjectNameException, NullPointerException
   {
      /**
       * According to the JMX 1.2 spec javadoc, should throw NullPointerException
       * if any of the parameters are null. -TME
       */
      if (null == key)
         throw new NullPointerException("properties key cannot be null");
      if (null == value)
         throw new NullPointerException("properties value cannot be null");

      if (key.length() == 0)
         throw new MalformedObjectNameException("properties key cannot be empty");
      if (value.length() == 0)
         throw new MalformedObjectNameException("properties value cannot be empty");

      initDomain(domain, null);

      Hashtable ptable = new Hashtable();
      ptable.put(key, value);

      initProperties(ptable, null);
   }

   /**
    * Construct a new ObjectName
    *
    * @param domain the domain of the object name
    * @param table of hashtable for key property pairs
    * @exception MalformedObjectNameException for an invalid object name
    * @exception NullPointerException for a null parameter
    */
   public ObjectName(String domain, Hashtable table)
      throws MalformedObjectNameException, NullPointerException
   {
      if (table == null)
         throw new NullPointerException("null table");

      initDomain(domain, null);

      if (table.size() < 1)
         throw new MalformedObjectNameException("empty table");

      initProperties((Hashtable) table.clone(), null);
   }

   // Public ------------------------------------------------------
   public boolean equals(Object object)
   {
      if (object == this)
         return true;
      if (object == null)
         return false;

      if (object instanceof ObjectName)
      {
         ObjectName oname = (ObjectName) object;
         return (oname.hash == hash && domain.equals(oname.domain) &&
            ckProps.equals(oname.ckProps));
      }

      return false;
   }

   public int hashCode()
   {
      return hash;
   }

   public String toString()
   {
      return getCanonicalName();
   }

   public boolean isPattern()
   {
      return hasPattern;
   }

   /**
    * The canonical form of the name is a String consisting of the domain part, a colon (:),
    * the canonical key property list, and a pattern indication.
    *
    * The canonical key property list is the same string as described for getCanonicalKeyPropertyListString().
    * The pattern indication is:
    * - empty for an ObjectName that is not a property pattern;
    * - an asterisk for an ObjectName that is a property pattern with no keys; or
    * - a comma and an asterisk (,*) for an ObjectName that is a property pattern with at least one key.
    */
   public String getCanonicalName()
   {
      if (cName == null)
      {
         cName = domain + ":" + ckProps;
         if (ckProps.length() == 0 && hasPropertyPattern)
         {
            cName += "*";
         }
         if (ckProps.length() > 0 && hasPropertyPattern)
         {
            cName += ",*";
         }
      }
      return cName;
   }

   public String getDomain()
   {
      return domain;
   }

   public String getKeyProperty(String property)
      throws NullPointerException
   {
      return (String) propertiesHash.get(property);
   }

   public Hashtable getKeyPropertyList()
   {
      return (Hashtable) propertiesHash.clone();
   }

   /**
    * Construct the key property list, ignoring patters
    * Note, it might not be the same order
    */
   public String getKeyPropertyListString()
   {
      if (constructorProps != null)
         return constructorProps;
      else if (kProps == null)
      {
         StringBuffer buffer = new StringBuffer();
         Iterator it = propertiesHash.keySet().iterator();

         // if the list of property keys is available, we use it
         if (propertiesList != null)
            it = propertiesList.iterator();

         for (int i=0; it.hasNext(); i++)
         {
            if (i > 0) buffer.append(",");
            String key  = (String) it.next();
            String val = (String)propertiesHash.get(key);
            buffer.append(key + "=" + val);
         }
         kProps = buffer.toString();
      }
      return kProps;
   }

   public String getCanonicalKeyPropertyListString()
   {
      if (ckProps == null)
      {
         StringBuffer buffer = new StringBuffer();
         ArrayList list = new ArrayList(propertiesHash.keySet());
         Collections.sort(list);
         Iterator it = list.iterator();
         for (int i=0; it.hasNext(); i++)
         {
            if (i > 0) buffer.append(",");
            String key  = (String) it.next();
            String val = (String)propertiesHash.get(key);
            buffer.append(key + "=" + val);
         }
         ckProps = buffer.toString();
      }
      return ckProps;
   }

   public boolean isPropertyPattern()
   {
      return hasPropertyPattern;
   }

   public boolean isDomainPattern()
   {
      return hasDomainPattern;
   }

   // QueryExp Implementation -------------------------------------

   public boolean apply(ObjectName name)
      throws NullPointerException
   {
       if (name.isPattern())
          return false;
       if (this == name)
          return true;

       if (ObjectNamePatternHelper.patternMatch(name.getDomain(), this.getDomain()))
       {
          if (propertyPattern == null)
             propertyPattern = new PropertyPattern(this);
          return propertyPattern.patternMatch(name);
       }
       return false;
   }

   public void setMBeanServer(MBeanServer server)
   {
      QueryExpSupport.server.set(server);
   }

   // Private -----------------------------------------------------

   /**
    * constructs an object name from a string
    */
   private void init(String name) throws MalformedObjectNameException
   {
      if (name == null)
         throw new NullPointerException("null name");

      if (name.length() == 0)
         name = "*:*";

      int domainSep = name.indexOf(':');

      if (-1 == domainSep)
         throw new MalformedObjectNameException("missing domain");

      initDomain(name.substring(0, domainSep), name);
      constructorProps = name.substring(domainSep + 1);
      initProperties(constructorProps, name);
      // Remove any pattern
      if (constructorProps.equals("*"))
         constructorProps = "";
      else if (constructorProps.startsWith("*,"))
         constructorProps = constructorProps.substring(2);
      else if (constructorProps.endsWith(",*"))
         constructorProps = constructorProps.substring(0, constructorProps.length()-2); 
   }

   /**
    * checks for domain patterns and illegal characters
    */
   private void initDomain(String dstring, String name) throws MalformedObjectNameException
   {
      if (null == dstring)
         throw new NullPointerException("null domain");

      checkIllegalDomain(dstring, name);

      if (dstring.indexOf('*') > -1 || dstring.indexOf('?') > -1)
      {
         this.hasPattern = true;
         this.hasDomainPattern = true;
      }

      this.domain = dstring;
   }

   /**
    * takes the properties string and breaks it up into key/value pairs for
    * insertion into a newly created hashtable.
    *
    * minimal validation is performed so that it doesn't blow up when
    * constructing the kvp strings.
    *
    * checks for duplicate keys
    *
    * detects property patterns
    *
    */
   private void initProperties(String properties, String name) throws MalformedObjectNameException
   {
      if (null == properties || properties.length() < 1)
         throw new MalformedObjectNameException(addDebugObjectName(name) +
            "null or empty properties");

      Hashtable ptable = new Hashtable();
      this.propertiesList = new ArrayList();

      char[] chars = properties.toCharArray();

      String key = null;
      int start = 0;
      boolean inKey = true;
      boolean inQuote = false;
      boolean escape = false;
      boolean endOrTerminate = false;

      for (int current = 0; current < chars.length; ++current)
      {
         char c = chars[current];

         // Previous character was the backslash escape
         if (escape)
            escape = false;

         // In a quote
         else if (inQuote)
         {
            // End the quote
            if (c == '"')
            {
               inQuote = false;
               endOrTerminate = true;
            }

            // Start escaping
            else if (c == '\\')
               escape = true;
         }

         // Processing the key
         else if (inKey)
         {
            // Found the terminator
            if (c == '=' || c == ',')
            {
               if (current == start)
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "Empty key");
               key = properties.substring(start, current);

               // Didn't get an equals so the only thing valid is asterisk
               if (c == ',')
               {
                  if (key.equals("*"))
                  {
                     if (hasPropertyPattern)
                        throw new MalformedObjectNameException(addDebugObjectName(name) +
                           "A property pattern may only contain one * property");
                     this.hasPropertyPattern = true;
                     this.hasPattern = true;
                  }
                  else
                     throw new MalformedObjectNameException(addDebugObjectName(name) +
                        "Invalid key/value data " + key);
               }
               else
               {
                  // Check for a duplicate key and process the value
                  if (ptable.containsKey(key))
                     throw new MalformedObjectNameException(addDebugObjectName(name) +
                        "Duplicate key " + key);
                  inKey = false;
               }
               start = current + 1;
            }
         }

         // Processing a value
         else
         {
            // Starting a quote
            if (c == '"')
               inQuote = true;

            // Found the terminator
            else if (c == ',')
            {
               // Add the key, value pair and process the next pair
               if (current == start)
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "Invalid key/value data " + key);
               else
               {
                  propertiesList.add(key);
                  ptable.put(key, properties.substring(start, current));
               }

               inKey = true;
               endOrTerminate = false;
               start = current + 1;
            }

            // expect a terminator after the closing quote
            else if (endOrTerminate)
            {
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "Invalid key/value data " + key);
            }
         }
      }

      // Fell off the end while processing a key
      if (inKey)
      {
         if (start == chars.length)
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "An ObjectName cannot end with a comma");

         // Might be ok if the remainder is an asterisk
         key = properties.substring(start, chars.length);
         if (key.equals("*"))
         {
            if (hasPropertyPattern)
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "A property pattern may only contain one * property");

            this.hasPropertyPattern = true;
            this.hasPattern = true;
         }
         else
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "Invalid key/value data " + key);
      }

      // Fell off the end while processing a value
      if (inKey == false)
      {
         // No value
         if (start == chars.length)
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "Invalid key/value data " + key);
         else
         {
            propertiesList.add(key);
            ptable.put(key, properties.substring(start, chars.length));
         }
      }

      initProperties(ptable, name);
   }

   /**
    * validates incoming properties hashtable
    *
    * builds canonical string
    *
    * precomputes the hashcode
    */
   private void initProperties(Hashtable properties, String name) throws MalformedObjectNameException
   {
      if (null == properties || (!this.hasPropertyPattern && properties.size() < 1))
         throw new MalformedObjectNameException(addDebugObjectName(name) +
            "null or empty properties");

      Iterator it = properties.keySet().iterator();
      ArrayList list = new ArrayList();

      while (it.hasNext())
      {
         String key = null;
         try
         {
            key = (String) it.next();
         }
         catch (ClassCastException e)
         {
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "key is not a string " + key);
         }

         String val = null;
         try
         {
            val = (String) properties.get(key);
         }
         catch (ClassCastException e)
         {
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "value is not a string " + val);
         }

         if (key.equals("*") && val.equals("*"))
         {
            it.remove();
            this.hasPropertyPattern = true;
            this.hasPattern = true;
            continue;
         }

         if (key.length() == 0)
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "key has no length =" + val);

         if (val.length() == 0)
            throw new MalformedObjectNameException(addDebugObjectName(name) +
               "value has no length =" + val);

         checkIllegalKey(key, name);
         checkIllegalValue(val, name);

         list.add(new String(key + "=" + val));
      }

      Collections.sort(list);
      StringBuffer strBuffer = new StringBuffer();

      it = list.iterator();
      while (it.hasNext())
      {
         strBuffer.append(it.next());
         if (it.hasNext())
         {
            strBuffer.append(',');
         }
      }

      if (this.hasPropertyPattern)
      {
         if (properties.size() > 0)
         {
            strBuffer.append(",*");
         }
         else
         {
            strBuffer.append("*");
         }
      }

      this.propertiesHash = properties;
      this.kProps = getKeyPropertyListString();
      this.ckProps = getCanonicalKeyPropertyListString();
      this.hash = getCanonicalName().hashCode();
   }

   private void checkIllegalKey(String key, String name)
      throws MalformedObjectNameException
   {
      char[] chars = key.toCharArray();

      for (int i = 0; i < chars.length; ++i)
      {
         switch (chars[i])
         {
            case ':':
            case ',':
            case '=':
            case '*':
            case '?':
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "The key " + key + " cannot contain a " + chars[i] + " character");
            case '\n':
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "The key " + key + " cannot contain a new line character");
         }
      }
   }

   private void checkIllegalValue(String value, String name)
      throws MalformedObjectNameException
   {
      char[] chars = value.toCharArray();

      boolean inQuote = false;
      boolean escape = false;

      for (int i = 0; i < chars.length; ++i)
      {
         // Valid escape character?
         char c = chars[i];
         if (escape)
         {
            switch (c)
            {
               case '\\':
               case 'n':
               case '"':
               case '*':
               case '?':
                  escape = false;
                  break;

               // Invalid escape character
               default:
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " contains an invalid escape sequence backslash " + c);
            }
         }

         // Starting a quote
         else if (inQuote == false && c == '"')
            inQuote = true;

         // Ending a quote
         else if (inQuote && c == '"')
            inQuote = false;

         // Escaping
         else if (inQuote && c == '\\')
            escape = true;

         // Inside a quote
         else if (inQuote)
         {
            switch (c)
            {
               case '"':
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " cannot contain quote inside a quote pair, use backslash quote");
               case '*':
               case '?':
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " cannot contain " + c + " inside a quote pair, use backslash " + c);
               case '\n':
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " cannot contain a new line inside a quote pair, use backslash n");
            }
         }

         // Plain text
         else
         {
            switch (c)
            {
               case ':':
               case ',':
               case '=':
               case '*':
               case '?':
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " cannot contain " + c + " use quote backslash " + c + " quote or ObjectName.quote(String)");
               case '\n':
                  throw new MalformedObjectNameException(addDebugObjectName(name) +
                     "The value " + value + " cannot contain a new line use quote backslash n quote or ObjectName.quote(String)");
            }
         }
      }

      if (inQuote)
         throw new MalformedObjectNameException(addDebugObjectName(name) +
            "Unterminated quote pair, missing quote");
      if (escape)
         throw new MalformedObjectNameException(addDebugObjectName(name) +
            "Unterminated escape, missing one of backslash quote asterisk question mark or n");
   }

   /**
    * returns true if the domain contains illegal characters
    */
   private void checkIllegalDomain(String dom, String name)
      throws MalformedObjectNameException
   {
      char[] chars = dom.toCharArray();

      for (int i = 0; i < chars.length; i++)
      {
         switch (chars[i])
         {
            case ':':
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "The domain " + dom + " cannot contain a : character");
            case '\n':
               throw new MalformedObjectNameException(addDebugObjectName(name) +
                  "The domain " + dom + " cannot contain a new line character");
         }
      }
   }

   private String addDebugObjectName(String name)
   {
      if (name == null)
         return "";
      else
         return name + " is not a valid ObjectName. ";
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectInputStream.GetField getField = ois.readFields();
         try
         {
            String name = (String) getField.get("canonicalName", null);
            if (name == null)
               throw new StreamCorruptedException("No canonical name for jmx1.0?");
            init(name);
         }
         catch (MalformedObjectNameException e)
         {
            throw new StreamCorruptedException(e.toString());
         }
         break;
      default:
         try
         {
            init((String) ois.readObject());
         }
         catch (MalformedObjectNameException e)
         {
            throw new StreamCorruptedException(e.toString());
         }
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectOutputStream.PutField putField = oos.putFields();
         putField.put("domain", domain);
         putField.put("propertyList", propertiesHash);
         putField.put("propertyListString", ckProps);
         putField.put("canonicalName", domain + ":" + ckProps);
         putField.put("pattern", hasPattern);
         putField.put("propertyPattern", hasPropertyPattern);
         oos.writeFields();
         break;
      default:
         String props = getKeyPropertyListString();
         StringBuffer buffer = new StringBuffer(domain.length() + props.length() + 3);
         buffer.append(domain);
         buffer.append(':');
         buffer.append(props);
         if (hasPropertyPattern)
         {
            if (propertiesHash.size() > 0)
               buffer.append(',');
            buffer.append('*');
         }
         oos.writeObject(buffer.toString());
      }
   }
}
