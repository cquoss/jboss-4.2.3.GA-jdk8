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
package org.jboss.mq;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * This Message class is used to send a non 'provider-optimized Message' over
 * the network [4.4.5]
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyEncapsulatedMessage extends SpyObjectMessage
{
   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   private final static long serialVersionUID = 3995327252678969050L;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // SpyMessage overrides ------------------------------------------
   
   public void setMessage(Message m) throws JMSException
   {
      this.setObject((Serializable) m);

      if (m.getJMSCorrelationID() != null)
         setJMSCorrelationID(m.getJMSCorrelationID());
      else if (m.getJMSCorrelationIDAsBytes() != null)
         setJMSCorrelationIDAsBytes(m.getJMSCorrelationIDAsBytes());
      setJMSReplyTo(m.getJMSReplyTo());
      setJMSType(m.getJMSType());
      setJMSDestination(m.getJMSDestination());
      setJMSDeliveryMode(m.getJMSDeliveryMode());
      setJMSExpiration(m.getJMSExpiration());
      setJMSPriority(m.getJMSPriority());
      setJMSMessageID(m.getJMSMessageID());
      setJMSTimestamp(m.getJMSTimestamp());

      Enumeration enumeration = m.getPropertyNames();
      while (enumeration.hasMoreElements())
      {
         String name = (String) enumeration.nextElement();
         Object o = m.getObjectProperty(name);
         setObjectProperty(name, o);
      }
   }

   public Message getMessage() throws JMSException
   {
      Message m = (Message) this.getObject();
      m.setJMSRedelivered(getJMSRedelivered());
      return m;
   }
   
   // SpyMessage overrides ------------------------------------------
   
   public SpyMessage myClone() throws JMSException
   {
      SpyEncapsulatedMessage result = MessagePool.getEncapsulatedMessage();
      result.copyProps(this);
      //HACK to get around read only problem
      boolean readOnly = result.header.msgReadOnly;
      result.header.msgReadOnly = false;
      result.setMessage(this.getMessage());
      result.header.msgReadOnly = readOnly;
      return result;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}