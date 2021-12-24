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
package org.jboss.mq.il.rmi;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.il.Invoker;
import org.jboss.mq.il.ServerIL;

/**
 * The JVM implementation of the ServerIL object
 *
 * @author    Hiram Chirino (Cojonudo14@hotmail.com)
 * @author    Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author    <a href="pra@tim.se">Peter Antman</a>
 * @version   $Revision: 57198 $
 * @created   August 16, 2001
 */
public class RMIServerIL extends java.rmi.server.UnicastRemoteObject implements RMIServerILRemote
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -7793119336262492395L;
   
   //The server implementation
   private transient Invoker server;

   /**
    * Constructor for the RMIServerIL object
    *
    * @param s                             Description of Parameter
    * @exception java.rmi.RemoteException  Description of Exception
    */
   public RMIServerIL(Invoker s)
          throws java.rmi.RemoteException
   {
      server = s;
   }

   /**
    * Sets the ConnectionToken attribute of the RMIServerIL object
    *
    * @param newConnectionToken  The new ConnectionToken value
    */
   public void setConnectionToken(ConnectionToken newConnectionToken)
   {
      // We cannot try to cache the token since this IL is stateless
   }

   /**
    * Sets the Enabled attribute of the RMIServerIL object
    *
    * @param dc             The new Enabled value
    * @param enabled        The new Enabled value
    * @exception Exception  Description of Exception
    */
   public void setEnabled(ConnectionToken dc, boolean enabled)
          throws Exception
   {
      server.setEnabled(dc, enabled);
   }

   /**
    * Gets the ID attribute of the RMIServerIL object
    *
    * @return                  The ID value
    * @exception JMSException  Description of Exception
    */
   public String getID()
          throws JMSException
   {
      return server.getID();
   }

   /**
    * Gets the TemporaryQueue attribute of the RMIServerIL object
    *
    * @param dc                Description of Parameter
    * @return                  The TemporaryQueue value
    * @exception JMSException  Description of Exception
    */
   public TemporaryQueue getTemporaryQueue(ConnectionToken dc)
          throws JMSException
   {
      return server.getTemporaryQueue(dc);
   }

   /**
    * Gets the TemporaryTopic attribute of the RMIServerIL object
    *
    * @param dc                Description of Parameter
    * @return                  The TemporaryTopic value
    * @exception JMSException  Description of Exception
    */
   public TemporaryTopic getTemporaryTopic(ConnectionToken dc)
          throws JMSException
   {
      return server.getTemporaryTopic(dc);
   }

   /**
    * No need to clone because there are no instance variables tha can get
    * clobbered. All Multiple connections can share the same JVMServerIL object
    *
    * @return   Description of the Returned Value
    */
   public ServerIL cloneServerIL()
   {
      return this;
   }

   /**
    * Adds a feature to the Message attribute of the RMIServerIL object
    *
    * @param dc                The feature to be added to the Message attribute
    * @param val               The feature to be added to the Message attribute
    * @exception JMSException  Description of Exception
    */
   public void addMessage(ConnectionToken dc, SpyMessage val)
          throws JMSException
   {
      server.addMessage(dc, val);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @param dest              Description of Parameter
    * @return                  Description of the Returned Value
    * @exception JMSException  Description of Exception
    */
   public Topic createTopic(ConnectionToken dc, String dest)
          throws JMSException
   {
      return server.createTopic(dc, dest);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @param dest              Description of Parameter
    * @return                  Description of the Returned Value
    * @exception JMSException  Description of Exception
    */
   public Queue createQueue(ConnectionToken dc, String dest)
          throws JMSException
   {
      return server.createQueue(dc, dest);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @param dest              Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest)
          throws JMSException
   {
      server.deleteTemporaryDestination(dc, dest);
   }

   /**
    * #Description of the Method
    *
    * @param ID                Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void checkID(String ID)
          throws JMSException
   {
      server.checkID(ID);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void connectionClosing(ConnectionToken dc)
          throws JMSException
   {
      server.connectionClosing(dc);
   }

   /**
    * #Description of the Method
    *
    * @param dc                            Description of Parameter
    * @param item                          Description of Parameter
    * @exception Exception                 Description of Exception
    * @exception java.rmi.RemoteException  Description of Exception
    */
   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item)
          throws Exception, java.rmi.RemoteException
   {
      server.acknowledge(dc, item);
   }

   /**
    * #Description of the Method
    *
    * @param dc             Description of Parameter
    * @param dest           Description of Parameter
    * @param selector       Description of Parameter
    * @return               Description of the Returned Value
    * @exception Exception  Description of Exception
    */
   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector)
          throws Exception
   {
      return server.browse(dc, dest, selector);
   }

   /**
    * #Description of the Method
    *
    * @param dc             Description of Parameter
    * @param subscriberId   Description of Parameter
    * @param wait           Description of Parameter
    * @return               Description of the Returned Value
    * @exception Exception  Description of Exception
    */
   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait)
          throws Exception
   {
      return server.receive(dc, subscriberId, wait);
   }

   /**
    * #Description of the Method
    *
    * @param dc              Description of Parameter
    * @param subscriptionId  Description of Parameter
    * @exception Exception   Description of Exception
    */
   public void unsubscribe(ConnectionToken dc, int subscriptionId)
          throws Exception
   {
      server.unsubscribe(dc, subscriptionId);
   }

   /**
    * #Description of the Method
    *
    * @param id             Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void destroySubscription(ConnectionToken dc,DurableSubscriptionID id)
          throws Exception
   {
      server.destroySubscription(dc,id);
   }

   /**
    * #Description of the Method
    *
    * @param userName          Description of Parameter
    * @param password          Description of Parameter
    * @return                  Description of the Returned Value
    * @exception JMSException  Description of Exception
    */
   public String checkUser(String userName, String password)
          throws JMSException
   {
      return server.checkUser(userName, password);
   }

   public String authenticate(String userName, String password)
      throws JMSException 
   {
      return server.authenticate(userName, password);
   }
   /**
    * #Description of the Method
    *
    * @param dc             Description of Parameter
    * @param s              Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void subscribe(ConnectionToken dc, org.jboss.mq.Subscription s)
          throws Exception
   {
      server.subscribe(dc, s);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @param t                 Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void transact(org.jboss.mq.ConnectionToken dc, TransactionRequest t)
          throws JMSException
   {
      server.transact(dc, t);
   }

   /**
    * #Description of the Method
    *
    * @param dc                Description of Parameter
    * @param clientTime        Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void ping(org.jboss.mq.ConnectionToken dc, long clientTime)
          throws JMSException
   {
      server.ping(dc, clientTime);
   }
}




