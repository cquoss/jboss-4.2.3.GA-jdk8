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
package org.jboss.mq.selectors;

import java.util.HashMap;
import java.util.Iterator;

import javax.jms.DeliveryMode;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.SpyMessage;
import org.jboss.util.NestedRuntimeException;

/**
 * This class implements a Message Selector.
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Juha Lindfors (jplindfo@helsinki.fi)
 * @author     <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author     Scott.Stark@jboss.org
 * @author     adrian@jboss.com
 * @version    $Revision: 57198 $
 */
public class Selector
{
   /** The logging interface */
   static Logger cat = Logger.getLogger(Selector.class);
   
   /** The ISelectorParser implementation class */
   private static Class parserClass = SelectorParser.class;

   /** The system property property for numeric delivery mode */
   public static final String USE_NUMERIC_DELIVERY_MODE = "org.jboss.mq.selectors.useNumericDeliveryMode"; 
   
   /** Backwards compatibility flag */
   private static boolean useNumericDeliveryMode;
   
   public String selector;

   public HashMap identifiers;
   
   public Object result;
   
   private Class resultType;

   static
   {
      try
      {
         String property = System.getProperty(USE_NUMERIC_DELIVERY_MODE, "false");
         useNumericDeliveryMode = Boolean.valueOf(property).booleanValue();
      }
      catch (Exception ignored)
      {
         cat.trace("Cannot get property " + USE_NUMERIC_DELIVERY_MODE, ignored);
      }
   }
   
   /**
    * Get the class that implements the ISelectorParser interface to be used by
    * Selector instances.
    */
   public static Class getSelectorParserClass()
   {
      return Selector.parserClass;
   }
   
   /**
    * Set the class that implements the ISelectorParser interface to be used by
    * Selector instances.
    * 
    * @param parserClass  the ISelectorParser implementation. This must have a
    *                     public no-arg constructor.
    */
   public static void setSelectorParserClass(Class parserClass)
   {
      Selector.parserClass = parserClass;
   }

   public Selector(String sel) throws InvalidSelectorException
   {
      selector = sel;
      identifiers = new HashMap();
      
      try
      {
         ISelectorParser bob = (ISelectorParser) parserClass.newInstance();
         result = bob.parse(sel, identifiers);
         resultType = result.getClass();
      }
      catch (Exception e)
      {
         InvalidSelectorException exception = new InvalidSelectorException("The selector is invalid: " + sel);
         exception.setLinkedException(e);
         throw exception;
      }
      catch (Error e)
      {
         InvalidSelectorException exception = new InvalidSelectorException("The selector is invalid: " + sel);
         exception.setLinkedException(new NestedRuntimeException(e));
         throw exception;
      }
   }

   public synchronized boolean test(SpyMessage.Header mes) throws JMSException
   {
      try
      {
         // Set the identifiers values
         Iterator i = identifiers.values().iterator();
         
         while (i.hasNext())
         {
            Identifier id = (Identifier) i.next();
            Object find = mes.jmsProperties.get(id.name);
            
            if (find == null)
               find = getHeaderFieldReferences(mes, id.name);
            
            if (find == null)
               id.value = null;
            else
            {
               Class type = find.getClass();
               if (type.equals(Boolean.class) ||
                  type.equals(String.class)  ||
                  type.equals(Double.class)  ||
                  type.equals(Float.class)   ||
                  type.equals(Integer.class) ||
                  type.equals(Long.class)    ||
                  type.equals(Short.class)   ||
                  type.equals(Byte.class))
                  id.value = find;
               else
                  throw new Exception("Bad property '" + id.name + "' type: " + type);
            }
         }
         
         // Compute the result of this operator
         Object res;
         
         if (resultType.equals(Identifier.class))
            res = ((Identifier)result).value;
         else if (resultType.equals(Operator.class))
         {
            Operator op = (Operator) result;
            res = op.apply();
         }
         else
            res = result;
         
         if (res == null)
            return false;
         
         if (!(res.getClass().equals(Boolean.class)))
            throw new Exception("Bad object type: " + res);
         
         return ((Boolean) res).booleanValue();
      }
      catch (Exception e)
      {
         cat.warn("Invalid selector: " + selector, e);
         return false;
      }
   }

   public boolean test(SpyMessage msg) throws JMSException
   {
      return test(msg.header);
   }
   
   // [JPL]
   private Object getHeaderFieldReferences(SpyMessage.Header header, String idName)
      throws JMSException
   {
      // JMS 3.8.1.1 -- Message header field references are restricted to:
      //                JMSDeliveryMode, JMSPriority, JMSMessageID,
      //                JMSTimeStamp, JMSCorrelationID and JMSType
      //
      if (idName.equals("JMSDeliveryMode"))
      {
         if (useNumericDeliveryMode)
            return new Integer(header.jmsDeliveryMode);
         // JMS 3.8.1.3 -- Use 'PERISTENT' and 'NON_PERSISTENT'
         else if (header.jmsDeliveryMode == DeliveryMode.NON_PERSISTENT)
            return "NON_PERSISTENT";
         else
            return "PERSISTENT";
      }
      else if (idName.equals("JMSPriority"))
         return new Integer(header.jmsPriority);
      else if (idName.equals("JMSMessageID"))
         return header.jmsMessageID;
      else if (idName.equals("JMSTimestamp"))
         return new Long(header.jmsTimeStamp);
      else if (idName.equals("JMSCorrelationID"))
         return header.jmsCorrelationIDString;
      else if (idName.equals("JMSType"))
         return header.jmsType;
      else
         return null;
   }
}
