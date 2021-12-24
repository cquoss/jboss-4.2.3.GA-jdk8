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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.mq.il.ClientILService;
import org.jboss.mq.il.ServerIL;
import org.jboss.util.UnreachableStatementException;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This class implements javax.jms.Connection.
 * 
 * <p>
 * It is also the gateway through wich all calls to the JMS server is done. To
 * do its work it needs a ServerIL to invoke (@see
 * org.jboss.mq.server.ServerIL).
 * </p>
 * 
 * <p>
 * The (new from february 2002) logic for clientID is the following: if logging
 * in with a user and passwork a preconfigured clientID may be automatically
 * delivered from the server.
 * </p>
 * 
 * <p>
 * If the client wants to set it's own clientID it must do so on a connection
 * wich does not have a prefonfigured clientID and it must do so before it
 * calls any other methods on the connection (even getClientID()). It is not
 * allowable to use a clientID that either looks like JBossMQ internal one
 * (beginning with ID) or a clientID that is allready in use by someone, or a
 * clientID that is already preconfigured in the server.
 * </p>
 * 
 * <p>
 * If a preconfigured ID is not get, or a valid one is not set, the server will
 * set an internal ID. This ID is NEVER possible to use for durable
 * subscriptions. If a prefconfigured ID or one manually set is possible to use
 * to create a durable subscriptions is governed by the security configuration
 * of JBossMQ. In the default setup, only preconfigured clientID's are possible
 * to use. If using a SecurityManager, permissions to create a surable
 * subscriptions is * the resiult of a combination of the following:
 * </p>
 * <p>- The clientID is not one of JBossMQ's internal.
 * </p>
 * <p>- The user is authenticated and has a role that has create set to true
 * in the security config of the destination.
 * </p>
 * 
 * <p>
 * Notes for JBossMQ developers: All calls, except close(), that is possible to
 * do on a connection must call checkClientID()
 * </p>
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="pra@tim.se">Peter Antman</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 61739 $
 */
public abstract class Connection implements Serializable, javax.jms.Connection
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 87938199839407082L;

   /** The threadGroup */
   private static ThreadGroup threadGroup = new ThreadGroup("JBossMQ Client Threads");

   /** The log */
   static Logger log = Logger.getLogger(Connection.class);

   /** Whether trace is enabled */
   static boolean trace = log.isTraceEnabled();

   /** Manages the thread that pings the connection to see if it is 'alive' */
   static protected ClockDaemon clockDaemon = new ClockDaemon();

   /** Maps a destination to a LinkedList of Subscriptions */
   public HashMap destinationSubscriptions = new HashMap();

   /** Maps a subscription id to a Subscription */
   public HashMap subscriptions = new HashMap();

   /** Is the connection stopped ? */
   public boolean modeStop;

   /** This is our connection to the JMS server */
   protected ServerIL serverIL;

   /** This is the clientID */
   protected String clientID;

   /** The connection token is used to identify our connection to the server. */
   protected ConnectionToken connectionToken;

   /** The object that sets up the client IL */
   protected ClientILService clientILService;

   /** How often to ping the connection */
   protected long pingPeriod = 1000 * 60;

   /** This field is reset when a ping is sent, set when ponged. */
   protected boolean ponged = true;

   /** This is used to know when the PingTask is running */
   Semaphore pingTaskSemaphore = new Semaphore(1);

   /** Identifies the PinkTask in the ClockDaemon */
   Object pingTaskId;

   /** Set a soon as close() is called on the connection. */
   private SynchronizedBoolean closing = new SynchronizedBoolean(false);

   /** Whether setClientId is Allowed */
   private volatile boolean setClientIdAllowed = true;

   /** LinkedList of all created sessions by this connection */
   HashSet createdSessions;

   /** Numbers subscriptions */
   int subscriptionCounter = Integer.MIN_VALUE;

   /** The lock for subscriptionCounter */
   Object subCountLock = new Object();

   /** Is the connection closed */
   private SynchronizedBoolean closed = new SynchronizedBoolean(false);

   /** Used to control tranactions */
   SpyXAResourceManager spyXAResourceManager;

   /** The class that created this connection */
   GenericConnectionFactory genericConnectionFactory;

   /** Last message ID returned */
   private int lastMessageID;

   /** the exceptionListener */
   private ExceptionListener exceptionListener;

   /** The exception listener lock */
   private Object elLock = new Object();
   
   /** The exception listener invocation thread */
   private Thread elThread;
   
   /** Used in message id generation */
   private StringBuffer sb = new StringBuffer();

   /** Used in message id generation */
   private char[] charStack = new char[22];

   /** The next session id */
   String sessionId;

   /** Temporary destinations created by this connection */
   protected HashSet temps = new HashSet();
   
   static
   {
      log.debug("Setting the clockDaemon's thread factory");
      clockDaemon.setThreadFactory(new ThreadFactory()
      {
         public Thread newThread(Runnable r)
         {
            Thread t = new Thread(getThreadGroup(), r, "Connection Monitor Thread");
            t.setDaemon(true);
            return t;
         }
      });
   }

   public static ThreadGroup getThreadGroup()
   {
      if (threadGroup.isDestroyed())
         threadGroup = new ThreadGroup("JBossMQ Client Threads");
      return threadGroup;
   }

   /**
	 * Create a new Connection
	 * 
	 * @param userName the username
	 * @param password the password
	 * @param genericConnectionFactory the constructing class
	 * @throws JMSException for any error
	 */
   Connection(String userName, String password, GenericConnectionFactory genericConnectionFactory) throws JMSException
   {
      //Set the attributes
      createdSessions = new HashSet();
      connectionToken = null;
      lastMessageID = 0;
      modeStop = true;

      if (trace)
         log.trace("Connection Initializing userName=" + userName + " " + this);
      this.genericConnectionFactory = genericConnectionFactory;
      genericConnectionFactory.initialise(this);

      // Connect to the server
      if (trace)
         log.trace("Getting the serverIL " + this);
      serverIL = genericConnectionFactory.createServerIL();
      if (trace)
         log.trace("serverIL=" + serverIL + " " + this);

      // Register ourselves as a client
      try
      {
         authenticate(userName, password);

         if (userName != null)
            askForAnID(userName, password);

         startILService();
      }
      catch (Throwable t)
      {
         // Client registeration failed, close the connection
         try
         {
            serverIL.connectionClosing(null);
         }
         catch (Throwable t2)
         {
            log.debug("Error closing the connection", t2);
         }

         SpyJMSException.rethrowAsJMSException("Failed to create connection", t);
      }

      // Finish constructing the connection
      try
      {
         if (trace)
            log.trace("Creating XAResourceManager " + this);

         // Setup the XA Resource manager,
         spyXAResourceManager = new SpyXAResourceManager(this);

         if (trace)
            log.trace("Starting the ping thread " + this);
         startPingThread();

         if (trace)
            log.trace("Connection establishment successful " + this);
      }
      catch (Throwable t)
      {
         // Could not complete the connection, tidy up
         // the server and client ILs.
         try
         {
            serverIL.connectionClosing(connectionToken);
         }
         catch (Throwable t2)
         {
            log.debug("Error closing the connection", t2);
         }
         try
         {
            stopILService();
         }
         catch (Throwable t2)
         {
            log.debug("Error stopping the client IL", t2);
         }

         SpyJMSException.rethrowAsJMSException("Failed to create connection", t);
      }
   }

   /**
	 * Create a new Connection
	 * 
	 * @param genericConnectionFactory the constructing class
	 * @throws JMSException for any error
	 */
   Connection(GenericConnectionFactory genericConnectionFactory) throws JMSException
   {
      this(null, null, genericConnectionFactory);
   }

   /**
	 * Gets the ServerIL attribute of the Connection object
	 * 
	 * @return The ServerIL value
	 */
   public ServerIL getServerIL()
   {
      return serverIL;
   }

   /**
	 * Notification from the server that the connection is closed
	 */
   public void asynchClose()
   {
      // If we receive a close and we did not initiate it, then fire the exception listener
      if (closing.get() == false)
         asynchFailure("Asynchronous close from server.", new IOException("Close request from the server or transport layer."));
   }

   /**
	 * Called by a TemporaryDestination which is going to be deleted()
	 * 
	 * @param dest the temporary destination
	 */
   public void asynchDeleteTemporaryDestination(SpyDestination dest)
   {
      if (trace)
         log.trace("Deleting temporary destination " + dest);
      try
      {
         deleteTemporaryDestination(dest);
      }
      catch (Throwable t)
      {
         asynchFailure("Error deleting temporary destination " + dest, t);
      }
   }

   /**
	 * Gets the first consumer that is listening to a destination.
	 * 
	 * @param requests the receive requests
	 */
   public void asynchDeliver(ReceiveRequest requests[])
   {
      // If we are closing the connection, the server will nack the messages
      if (closing.get())
         return;

      if (trace)
         log.trace("Async deliver requests=" + Arrays.asList(requests) + " " + this);
      
      try
      {
         for (int i = 0; i < requests.length; i++)
         {
            ReceiveRequest r = requests[i];
            if (trace)
               log.trace("Processing request=" + r + " " + this);
            
            SpyConsumer consumer = (SpyConsumer) subscriptions.get(r.subscriptionId);
            r.message.createAcknowledgementRequest(r.subscriptionId.intValue());

            if (consumer == null)
            {
               send(r.message.getAcknowledgementRequest(false));
               log.debug("WARNING: NACK issued due to non existent subscription " + r.message.header.messageId);
               continue;
            }

            if (trace)
               log.trace("Delivering messageid=" + r.message.header.messageId + " to consumer=" + consumer);
            
            consumer.addMessage(r.message);
         }
      }
      catch (Throwable t)
      {
         asynchFailure("Error during async delivery", t);
      }
   }
   /**
	 * Notification of a failure on this connection
	 * 
	 * @param reason the reason for the failure
	 * @param t the throwable
	 */
   public void asynchFailure(String reason, Throwable t)
   {
      if (trace)
         log.trace("Notified of failure reason=" + reason + " " + this, t);

      // Exceptions due to closing will be ignored.
      if (closing.get())
         return;

      JMSException excep = SpyJMSException.getAsJMSException(reason, t);

      synchronized (elLock)
      {
         ExceptionListener el = exceptionListener;
         if (el != null && elThread == null)
         {
            try
            {
               Runnable run = new ExceptionListenerRunnable(el, excep);
               elThread = new Thread(getThreadGroup(), run, "ExceptionListener " + this);
               elThread.setDaemon(false);
               elThread.start();
            }
            catch (Throwable t1)
            {
               log.warn("Connection failure: ", excep);
               log.warn("Unable to start exception listener thread: ", t1);
            }
         }
         else if (elThread != null)
            log.warn("Connection failure, already in the exception listener", excep);
         else
            log.warn("Connection failure, use javax.jms.Connection.setExceptionListener() to handle this error and reconnect", excep);
      }
   }

   /**
	 * Invoked when the server pong us
	 * 
	 * @param serverTime the server time
	 */
   public void asynchPong(long serverTime)
   {
      if (trace)
         log.trace("PONG serverTime=" + serverTime + " " + this);
      ponged = true;
   }

   /**
	 * Called by a TemporaryDestination which is going to be deleted
	 * 
	 * @param dest the temporary destination
	 * @exception JMSException for any error
	 */
   public void deleteTemporaryDestination(SpyDestination dest) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("DeleteDestination dest=" + dest + " " + this);
      try
      {
         //Ask the broker to delete() this TemporaryDestination
         serverIL.deleteTemporaryDestination(connectionToken, dest);

         //Remove it from the destinations list
         synchronized (subscriptions)
         {
            destinationSubscriptions.remove(dest);
         }

         // Remove it from the temps list
         synchronized (temps)
         {
            temps.remove(dest);
         }
      }
      catch (Throwable t)
      {
         
         SpyJMSException.rethrowAsJMSException("Cannot delete the TemporaryDestination", t);
      }
   }

   public void setClientID(String cID) throws JMSException
   {
      checkClosed();
      if (clientID != null)
         throw new IllegalStateException("The connection has already a clientID");
      if (setClientIdAllowed == false)
         throw new IllegalStateException("SetClientID was not called emediately after creation of connection");

      if (trace)
         log.trace("SetClientID clientID=" + clientID + " " + this);

      try
      {
         serverIL.checkID(cID);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot connect to the JMSServer", t);
      }

      clientID = cID;
      connectionToken.setClientID(clientID);
   }

   public String getClientID() throws JMSException
   {
      checkClosed();
      return clientID;
   }

   public ExceptionListener getExceptionListener() throws JMSException
   {
      checkClosed();
      checkClientID();
      return exceptionListener;
   }

   public void setExceptionListener(ExceptionListener listener) throws JMSException
   {
      checkClosed();
      checkClientID();

      exceptionListener = listener;
   }

   public ConnectionMetaData getMetaData() throws JMSException
   {
      checkClosed();
      checkClientID();

      return new SpyConnectionMetaData();
   }

   public synchronized void close() throws JMSException
   {
      if (closed.get())
         return;
      if (trace)
         log.trace("Closing connection " + this);
      
      closing.set(true);

      // We don't want to notify the exception listener
      exceptionListener = null;

      // The first exception
      JMSException exception = null;

      try
      {
         doStop();
      }
      catch (Throwable t)
      {
         log.trace("Error during stop", t);
      }
      
      if (trace)
         log.trace("Closing sessions " + this);
      Object[] vect = null;
      synchronized (createdSessions)
      {
         vect = createdSessions.toArray();
      }
      for (int i = 0; i < vect.length; i++)
      {
         SpySession session = (SpySession) vect[i];
         try
         {
            session.close();
         }
         catch (Throwable t)
         {
            if (trace)
               log.trace("Error closing session " + session, t);
         }
      }
      if (trace)
         log.trace("Closed sessions " + this);

      if (trace)
         log.trace("Notifying the server of close " + this);
      try
      {
         serverIL.connectionClosing(connectionToken);
      }
      catch (Throwable t)
      {
         log.trace("Cannot close properly the connection", t);
      }

      if (trace)
         log.trace("Stopping ping thread " + this);
      try
      {
         stopPingThread();
      }
      catch (Throwable t)
      {
         if (exception == null)
            exception = SpyJMSException.getAsJMSException("Cannot stop the ping thread", t);
      }

      if (trace)
         log.trace("Stopping the ClientIL service " + this);
      try
      {
         stopILService();
      }
      catch (Throwable t)
      {
         log.trace("Cannot stop the client il service", t);
      }

      // Only set the closed flag after all the objects that depend
      // on this connection have been closed.
      closed.set(true);

      if (trace)
         log.trace("Disconnected from server " + this);

      // Throw the first exception
      if (exception != null)
         throw exception;
   }

   public void start() throws JMSException
   {
      checkClosed();
      checkClientID();

      if (modeStop == false)
         return;
      modeStop = false;

      if (trace)
         log.trace("Starting connection " + this);

      try
      {
         serverIL.setEnabled(connectionToken, true);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot enable the connection with the JMS server", t);
      }
   }

   public void stop() throws JMSException
   {
      checkClosed();
      checkClientID();
      doStop();
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Connection@").append(System.identityHashCode(this));
      buffer.append('[');
      if (connectionToken != null)
         buffer.append("token=").append(connectionToken);
      else
         buffer.append("clientID=").append(clientID);
      if (closed.get())
         buffer.append(" CLOSED");
      else if (closing.get())
         buffer.append(" CLOSING");
      buffer.append(" rcvstate=");
      if (modeStop)
         buffer.append("STOPPED");
      else
         buffer.append("STARTED");
      buffer.append(']');
      return buffer.toString();
   }

   /**
	 * Get the next message id
	 * <p>
	 * 
	 * All longs are less than 22 digits long
	 * <p>
	 * 
	 * Note that in this routine we assume that System.currentTimeMillis() is
	 * non-negative always be non-negative (so don't set lastMessageID to a
	 * positive for a start).
	 * 
	 * @return the next message id
	 * @throws JMSException for any error
	 */
   String getNewMessageID() throws JMSException
   {
      checkClosed();
      synchronized (sb)
      {
         sb.setLength(0);
         sb.append(clientID);
         sb.append('-');
         long time = System.currentTimeMillis();
         int count = 0;
         do
         {
            charStack[count] = (char) ('0' + (time % 10));
            time = time / 10;
            ++count;
         }
         while (time != 0);
         --count;
         for (; count >= 0; --count)
         {
            sb.append(charStack[count]);
         }
         ++lastMessageID;
         //avoid having to deal with negative numbers.
         if (lastMessageID < 0)
         {
            lastMessageID = 0;
         }
         int id = lastMessageID;
         count = 0;
         do
         {
            charStack[count] = (char) ('0' + (id % 10));
            id = id / 10;
            ++count;
         }
         while (id != 0);
         --count;
         for (; count >= 0; --count)
         {
            sb.append(charStack[count]);
         }
         return sb.toString();
      }
   }

   /**
	 * A new Consumer has been created.
	 * <p>
	 * We have to handle security issues, a consumer may actually not be allowed
	 * to be created
	 * 
	 * @param consumer the consumer added
	 * @throws JMSException for any error
	 */
   void addConsumer(SpyConsumer consumer) throws JMSException
   {
      checkClosed();
      Subscription req = consumer.getSubscription();
      synchronized (subCountLock)
      {
         req.subscriptionId = subscriptionCounter++;
      }
      req.connectionToken = connectionToken;
      if (trace)
         log.trace("addConsumer sub=" + req);

      try
      {
         synchronized (subscriptions)
         {
            subscriptions.put(new Integer(req.subscriptionId), consumer);

            LinkedList ll = (LinkedList) destinationSubscriptions.get(req.destination);
            if (ll == null)
            {
               ll = new LinkedList();
               destinationSubscriptions.put(req.destination, ll);
            }

            ll.add(consumer);
         }

         serverIL.subscribe(connectionToken, req);
      }
      catch (JMSSecurityException ex)
      {
         removeConsumerInternal(consumer);
         throw ex;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot subscribe to this Destination: ", t);
      }
   }

   /**
	 * Browse a queue
	 * 
	 * @param queue the queue
	 * @param selector the selector
	 * @return an array of messages
	 * @exception JMSException for any error
	 */
   SpyMessage[] browse(Queue queue, String selector) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("Browsing queue=" + queue + " selector=" + selector + " " + this);

      try
      {
         return serverIL.browse(connectionToken, queue, selector);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot browse the Queue", t);
         throw new UnreachableStatementException();
      }
   }

   /**
	 * Ping the server
	 * 
	 * @param clientTime the start of the ping
	 * @throws JMSException for any error
	 */
   void pingServer(long clientTime) throws JMSException
   {
      checkClosed();
      trace = log.isTraceEnabled();
      if (trace)
         log.trace("PING " + clientTime + " " + this);

      try
      {
         serverIL.ping(connectionToken, clientTime);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot ping the JMS server", t);
      }
   }

   /**
	 * Receive a message
	 * 
	 * @param sub the subscription
	 * @param wait the wait time
	 * @return the message or null if there isn't one
	 * @throws JMSException for any error
	 */
   SpyMessage receive(Subscription sub, long wait) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("Receive subscription=" + sub + " wait=" + wait);

      try
      {
         SpyMessage message = serverIL.receive(connectionToken, sub.subscriptionId, wait);
         if (message != null)
            message.createAcknowledgementRequest(sub.subscriptionId);
         return message;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot receive ", t);
         throw new UnreachableStatementException();
      }
   }

   /**
    * Remove a consumer
    *
    * @param consumer the consumer
    * @throws JMSException for any error
    */
   void removeConsumer(SpyConsumer consumer) throws JMSException
   {
      checkClosed();
      Subscription req = consumer.getSubscription();
      if (trace)
         log.trace("removeConsumer req=" + req);

      try
      {
         serverIL.unsubscribe(connectionToken, req.subscriptionId);

         removeConsumerInternal(consumer);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot unsubscribe to this destination", t);
      }

   }

   /**
    * Send a message to the server
    *
    * @param mes the message
    * @throws JMSException for any error
    */
   void sendToServer(SpyMessage mes) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("SendToServer message=" + mes.header.jmsMessageID + " " + this);
      
      try
      {
         serverIL.addMessage(connectionToken, mes);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot send a message to the JMS server", t);
      }
   }

   /**
    * Closing a session
    *
    * @param who the session
    */
   void sessionClosing(SpySession who)
   {
      if (trace)
         log.trace("Closing session " + who);
      
      synchronized (createdSessions)
      {
         createdSessions.remove(who);
      }

      //This session should not be in the "destinations" object anymore.
      //We could check this, though
   }

   void unsubscribe(DurableSubscriptionID id) throws JMSException
   {
      if (trace)
         log.trace("Unsubscribe id=" + id + " " + this);
      
      try
      {
         serverIL.destroySubscription(connectionToken, id);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot destroy durable subscription " + id, t);
      }
   }

   /**
    * Check a tempoary destination
    *
    * @param destination the destination
    */
   void checkTemporary(Destination destination) throws JMSException
   {
      if (destination instanceof TemporaryQueue || destination instanceof TemporaryTopic)
      {
         synchronized (temps)
         {
            if (temps.contains(destination) == false)
               throw new JMSException("Cannot create a consumer for a temporary destination from a different session. " + destination);
         }
      }
   }

   /**
	 * Check that a clientID exists. If not get one from server.
	 * 
	 * Also sets the setClientIdAllowed to false.
	 * 
	 * Check clientId, must be called by all public methods on the
	 * jacax.jmx.Connection interface and its children.
	 * 
	 * @exception JMSException if clientID is null as post condition
	 */
   synchronized protected void checkClientID() throws JMSException
   {
      if (setClientIdAllowed == false)
         return;

      setClientIdAllowed = false;
      if (trace)
         log.trace("Checking clientID=" + clientID + " " + this);
      if (clientID == null)
      {
         askForAnID();//Request a random one
         if (clientID == null)
            throw new JMSException("Could not get a clientID");
         connectionToken.setClientID(clientID);

         if (trace)
            log.trace("ClientID established " + this);
      }
   }

   /**
	 * Ask the server for an id
	 * 
	 * @exception JMSException for any error
	 */
   protected void askForAnID() throws JMSException
   {
      if (trace)
         log.trace("Ask for an id " + this);
      
      try
      {
         if (clientID == null)
            clientID = serverIL.getID();
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot get a client ID", t);
      }
   }

   /**
	 * Ask the server for an id
	 * 
	 * @param userName the user
	 * @param password the password
	 * @exception JMSException for any error
	 */
   protected void askForAnID(String userName, String password) throws JMSException
   {
      if (trace)
         log.trace("Ask for an id user=" +  userName + " " + this);

      try
      {
         String configuredClientID = serverIL.checkUser(userName, password);
         if (configuredClientID != null)
            clientID = configuredClientID;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot get a client ID", t);
      }
   }

   /**
    * Authenticate a user
    *
    * @param userName the user
    * @param password the password
    * @throws JMSException for any error
    */
   protected void authenticate(String userName, String password) throws JMSException
   {
      if (trace)
         log.trace("Authenticating user " + userName + " " + this);
      try
      {
         sessionId = serverIL.authenticate(userName, password);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot authenticate user", t);
      }
   }

   // used to acknowledge a message
   /**
	 * Acknowledge/Nack a message
	 * 
	 * @param item the acknowledgement
	 * @exception JMSException for any error
	 */
   protected void send(AcknowledgementRequest item) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("Acknowledge item=" + item + " " + this);

      try
      {
         serverIL.acknowledge(connectionToken, item);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot acknowlege a message", t);
      }
   }

   /**
	 * Commit/rollback
	 * 
	 * @param transaction the transaction request
	 * @exception JMSException for any error
	 */
   protected void send(TransactionRequest transaction) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("Transact request=" + transaction + " " + this);

      try
      {
         serverIL.transact(connectionToken, transaction);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot process a transaction", t);
      }
   }

   /**
    * Recover
    * 
    * @param flags the flags
    * @throws JMSException for any error
    */
   protected Xid[] recover(int flags) throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("Recover flags=" + flags + " " + this);

      try
      {
         if (serverIL instanceof Recoverable)
         {
            Recoverable recoverableIL = (Recoverable) serverIL;
            return recoverableIL.recover(connectionToken, flags);
         }
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot recover", t);
      }
      
      log.warn(serverIL + " does not implement " + Recoverable.class.getName());
      return new Xid[0];
   }

   /**
	 * Start the il
	 * 
	 * @exception JMSException for any error
	 */
   protected void startILService() throws JMSException
   {
      if (trace)
         log.trace("Starting the client il " + this);
      try
      {
         clientILService = genericConnectionFactory.createClientILService(this);
         clientILService.start();
         if (trace)
            log.trace("Using client id " + clientILService + " " + this);
         connectionToken = new ConnectionToken(clientID, clientILService.getClientIL(), sessionId);
         serverIL.setConnectionToken(connectionToken);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot start a the client IL service", t);
      }
   }

   /**
	 * Stop the il
	 * 
	 * @exception JMSException for any error
	 */
   protected void stopILService() throws JMSException
   {
      try
      {
         clientILService.stop();
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot stop a the client IL service", t);
      }
   }
   
   /**
    * Stop delivery
    *
    * @param consumer the consumer
    */
   public void doStop() throws JMSException
   {
      if (modeStop)
         return;
      modeStop = true;

      if (trace)
         log.trace("Stopping connection " + this);

      try
      {
         serverIL.setEnabled(connectionToken, false);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot disable the connection with the JMS server", t);
      }
   }
   
   /**
    * Remove a consumer
    *
    * @param consumer the consumer
    */
   private void removeConsumerInternal(SpyConsumer consumer)
   {
      synchronized (subscriptions)
      {
         Subscription req = consumer.getSubscription();
         subscriptions.remove(new Integer(req.subscriptionId));

         LinkedList ll = (LinkedList) destinationSubscriptions.get(req.destination);
         if (ll != null)
         {
            ll.remove(consumer);
            if (ll.size() == 0)
            {
               destinationSubscriptions.remove(req.destination);
            }
         }
      }
   }
   
   /**
    * Check whether we are closed
    * 
    * @throws IllegalStateException when the session is closed
    */
   protected void checkClosed() throws IllegalStateException
   {
      if (closed.get())
         throw new IllegalStateException("The connection is closed");
   }

   /**
    * Start the ping thread
    */
   private void startPingThread()
   {
      // Ping thread does not need to be running if the ping period is 0.
      if (pingPeriod == 0)
         return;
      pingTaskId = clockDaemon.executePeriodically(pingPeriod, new PingTask(), true);
   }

   /**
    * Stop the ping thread
    */
   private void stopPingThread()
   {
      // Ping thread was not running if ping period is 0.
      if (pingPeriod == 0)
         return;

      ClockDaemon.cancel(pingTaskId);

      //Aquire the Semaphore to make sure the ping task is not running.
      try
      {
         pingTaskSemaphore.attempt(1000 * 10);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
   }

   /**
	 * The ping task
	 */
   class PingTask implements Runnable
   {
      public void run()
      {
         // Don't bother if we are closing
         if (closing.get())
            return;
         
         try
         {
            // If we can't aquire the semaphore then it
            // almost certainly means the close has got it
            // Try for 10 seconds to make sure the problem
            // is not just a long garbage collection that has suspended threads
            if (pingTaskSemaphore.attempt(1000 * 10) == false)
               return;
         }
         catch (InterruptedException e)
         {
            log.debug("Interrupted requesting ping semaphore");
            return;
         }
         try
         {
            if (ponged == false)
            {
               // Server did not pong use with in the timeout
               // period.. Assuming the connection is dead.
               throw new SpyJMSException("No pong received", new IOException("ping timeout."));
            }

            ponged = false;
            pingServer(System.currentTimeMillis());
         }
         catch (Throwable t)
         {
            asynchFailure("Unexpected ping failure", t);
         }
         finally
         {
            pingTaskSemaphore.release();
         }
      }
   }
   
   /**
    * The Exception listener runnable
    */
   class ExceptionListenerRunnable implements Runnable
   {
      ExceptionListener el;
      JMSException excep;
      
      /**
       * Create a new ExceptionListener runnable
       * 
       * @param el the exception exception
       * @param excep the jms exception
       */
      public ExceptionListenerRunnable(ExceptionListener el, JMSException excep)
      {
         this.el = el;
         this.excep = excep;
      }
      
      public void run()
      {
         try
         {
            synchronized (elLock)
            {
               el.onException(excep);
            }
         }
         catch (Throwable t)
         {
            log.warn("Connection failure: ", excep);
            log.warn("Exception listener ended abnormally: ", t);
         }
         
         synchronized (elLock)
         {
            elThread = null;
         }
      }
   }
}
