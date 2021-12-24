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
package org.jboss.mq.server.jmx;

import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.Util;

import org.jboss.mq.MessageStatistics;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.server.BasicQueueParameters;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.MessageCounter;
import org.jboss.mq.server.Receivers;
import org.w3c.dom.Element;

/**
 * Super class for destination managers.
 *
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 60195 $
 */
abstract public class DestinationMBeanSupport extends ServiceMBeanSupport
   implements DestinationMBean, MBeanRegistration
{
   protected SpyDestination spyDest;
   protected String destinationName;
   protected String jndiName;
   protected boolean jndiBound;
   protected ObjectName jbossMQService;
   //String securityConf;
   protected Element securityConf;
   /** The basic parameters */
   protected BasicQueueParameters parameters = new BasicQueueParameters();
   /** Destination JNDI name */
   ObjectName expiryDestination;

   /**
    * A optional security manager. Must be set to use security conf.
    */
   protected ObjectName securityManager;

   /**
    * Get the value of JBossMQService.
    *
    * @return value of JBossMQService.
    */
   public ObjectName getDestinationManager()
   {
      return jbossMQService;
   }

   /**
    * Set the value of JBossMQService.
    *
    * @param v  Value to assign to JBossMQService.
    */
   public void setDestinationManager(ObjectName jbossMQService)
   {
      this.jbossMQService = jbossMQService;
   }

   protected SpyDestination getSpyDest()
   {
      return spyDest;
   }

   public void setSecurityConf(org.w3c.dom.Element securityConf) throws Exception
   {
      log.debug("Setting securityConf: " + securityConf);
      this.securityConf = securityConf;
   }

   protected Element getSecurityConf()
   {
      return securityConf;
   }

   public void setSecurityManager(ObjectName securityManager)
   {
      this.securityManager = securityManager;
   }

   protected ObjectName getSecurityManager()
   {
      return securityManager;
   }
   
   public void createService() throws Exception
   {
      // Copy default values from the server when null or zero
      if (parameters.receiversImpl == null)
         parameters.receiversImpl = (Class) server.getAttribute(jbossMQService, "ReceiversImpl");
      if (parameters.recoveryRetries == 0)
         parameters.recoveryRetries = ((Integer) server.getAttribute(jbossMQService, "RecoveryRetries")).intValue();
   }
   
   public void startService() throws Exception
   {

      setupSecurityManager();
      setupExpiryDestination();

   }

   protected void setupSecurityManager()
           throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      if (securityManager != null)
      {
         // Set securityConf at manager
         getServer().invoke(securityManager, "addDestination", new Object[]{spyDest.getName(), securityConf}, new String[]{"java.lang.String", "org.w3c.dom.Element"});
      }
   }

   protected void setupExpiryDestination()
           throws Exception
   {
      if (expiryDestination != null)
      {
         String jndi = (String)getServer().getAttribute(expiryDestination, "JNDIName");
         InitialContext ctx = new InitialContext();
         try
         {
            parameters.expiryDestination = (SpyDestination)ctx.lookup(jndi);
         }
         finally
         {
            ctx.close();
         }
      }
   }

   public void stopService() throws Exception
   {
      // unbind from JNDI
      if (jndiBound)
      {
         InitialContext ctx = getInitialContext();
         try
         {
            log.info("Unbinding JNDI name: " + jndiName);
            Util.unbind(ctx, jndiName);
         }
         finally
         {
            ctx.close();
         }
         jndiName = null;
         jndiBound = false;
      }

      JMSDestinationManager jmsServer = (JMSDestinationManager) server.getAttribute(jbossMQService, "Interceptor");
      if (jmsServer != null)
         jmsServer.closeDestination(spyDest);

      // TODO: need to remove from JMSServer
      teardownSecurityManager();
   }

   protected void teardownSecurityManager()
           throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      if (securityManager != null)
      {
         // Set securityConf at manager
         getServer().invoke(securityManager, "removeDestination", new Object[]{spyDest.getName()}, new String[]{"java.lang.String"});
      }
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      destinationName = name.getKeyProperty("name");
      if (destinationName == null || destinationName.length() == 0)
      {
         throw new MalformedObjectNameException("Property 'name' not provided");
      }

      // re-setup the logger with a more descriptive name
      log = Logger.getLogger(getClass().getName() + "." + destinationName);

      return name;
   }

   /**
    * Sets the JNDI name for this topic
    *
    * @param name Name to bind this topic to in the JNDI tree
    */
   public synchronized void setJNDIName(String name) throws Exception
   {
      if (spyDest == null)
      { // nothing to bind yet, startService will recall us
         jndiName = name;
         return;
      }

      if (name == null)
      {
         throw new InvalidAttributeValueException("Destination JNDI names can not be null");
      }

      InitialContext ic = getInitialContext();
      try
      {
         if (jndiName != null && jndiBound)
         {
            Util.unbind(ic, jndiName); //Remove old jndi name
            jndiName = null;
            jndiBound = false;
         }

         Util.rebind(ic, name, spyDest);
         jndiName = name;
         jndiBound = true;
      }
      finally
      {
         ic.close();
      }

      log.info("Bound to JNDI name: " + jndiName);
   }

   protected InitialContext getInitialContext()
           throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic;
   }

   /**
    * Gets the JNDI name use by this topic
    *
    * @return  The JNDI name currently in use
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   /**
    * Get destination message counter array
    * 
    * @return MessageCounter[]
    */
   abstract public MessageCounter[] getMessageCounter();

   /**
    * Get destination stats array
    * 
    * @return MessageStatistics[]
    * @throws Exception for any error
    */
   abstract public MessageStatistics[] getMessageStatistics() throws Exception;

   /**
    * List destination message counter as HTML table
    * 
    * @return String
    */
   public String listMessageCounter()
   {
      MessageCounter[] counter = getMessageCounter();
      if (counter == null)
         return null;
      
      String ret = "<table width=\"100%\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">"  +
                   "<tr>"                  +
                   "<th>Type</th>"         +
                   "<th>Name</th>"         +
                   "<th>Subscription</th>" +
                   "<th>Durable</th>"      +
                   "<th>Count</th>"        +
                   "<th>CountDelta</th>"   +
                   "<th>Depth</th>"        +
                   "<th>DepthDelta</th>"   +
                   "<th>Last Add</th>"     +
                   "</tr>";
      
      for( int i=0; i<counter.length; i++ )
      {
         String            data = counter[i].getCounterAsString();
         StringTokenizer   token = new StringTokenizer( data, ",");
         String            value;
         
         ret += "<tr bgcolor=\"#" + ( (i%2)==0 ? "FFFFFF" : "F0F0F0") + "\">";

         ret += "<td>" + token.nextToken() + "</td>"; // type
         ret += "<td>" + token.nextToken() + "</td>"; // name
         ret += "<td>" + token.nextToken() + "</td>"; // subscription
         ret += "<td>" + token.nextToken() + "</td>"; // durable

         ret += "<td>" + token.nextToken() + "</td>"; // count
         
         value = token.nextToken(); // countDelta

         if( value.equalsIgnoreCase("0") )
             value = "-";
             
         ret += "<td>" + value + "</td>";
         
         ret += "<td>" + token.nextToken() + "</td>"; // depth
         
         value = token.nextToken(); // depthDelta
         
         if( value.equalsIgnoreCase("0") )
             value = "-";
             
         ret += "<td>" + value + "</td>";

         ret += "<td>" + token.nextToken() + "</td>"; // date last add

         ret += "</tr>";
      }
      
      ret += "</table>";
      
      return ret;
   }      
   

   /**
    * Reset destination message counter
    */
   public void resetMessageCounter()
   {
      MessageCounter[] counter = getMessageCounter();
      if (counter == null)
         return;
      
      for( int i=0; i<counter.length; i++ )
      {
         counter[i].resetCounter();
      }
   }      
   
   /**
    * List destination message counter history as HTML table
    * 
    * @return String
    */
   public String listMessageCounterHistory()
   {
      MessageCounter[] counter = getMessageCounter();
      if (counter == null)
         return null;
      
      String           ret = "";
               
      for( int i=0; i<counter.length; i++ )
      {
         // destination name
         ret += ( counter[i].getDestinationTopic() ? "Topic '" : "Queue '" );
         ret += counter[i].getDestinationName() + "'";
         
         if( counter[i].getDestinationSubscription() != null )
            ret += "Subscription '" + counter[i].getDestinationSubscription() + "'";
            
                     
         // table header
         ret += "<table width=\"100%\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">"  +
                "<tr>"                  +
                "<th>Date</th>";

         for( int j = 0; j < 24; j++ )
            ret += "<th width=\"4%\">" + j + "</th>";

         ret += "<th>Total</th></tr>";

         // get history data as CSV string         
         StringTokenizer tokens = new StringTokenizer( counter[i].getHistoryAsString(), ",\n");
         
         // get history day count
         int days = Integer.parseInt( tokens.nextToken() );
         
         for( int j=0; j<days; j++ )
         {
            // next day counter row 
            ret += "<tr bgcolor=\"#" + ((j%2)==0 ? "FFFFFF" : "F0F0F0") + "\">";
         
            // date 
            ret += "<td>" + tokens.nextToken() + "</td>";
             
            // 24 hour counters
            int total = 0;
            
            for( int k=0; k<24; k++ )
            {
               int value = Integer.parseInt( tokens.nextToken().trim() );
            
               if( value == -1 )
               {
                    ret += "<td></td>";
               }  
               else
               {
                    ret += "<td>" + value + "</td>";
                    
                    total += value;
               } 
            }

            ret += "<td>" + total + "</td></tr>";
         }

         ret += "</table><br><br>";
      }

      return ret;
   }


   /**
    * Reset destination message counter history
    */
   public void resetMessageCounterHistory()
   {
      MessageCounter[] counter = getMessageCounter();
      if (counter == null)
         return;

      for( int i=0; i<counter.length; i++ )
      {
         counter[i].resetHistory();
      }
   }

   /**
    * Sets the destination message counter history day limit
    * <0: unlimited, =0: disabled, > 0 maximum day count
    *
    * @param days  maximum day count
    */
   public void setMessageCounterHistoryDayLimit( int days )
   {
      if( days < -1 )
          days = -1;
      
      parameters.messageCounterHistoryDayLimit = days;
      if (getState() != STARTED)
         return;

      MessageCounter[] counter = getMessageCounter();
      for (int i=0; i<counter.length; ++i)
         counter[i].setHistoryLimit(days);
   }

   /**
    * Gets the destination message counter history day limit
    * @return  Maximum day count
    */
   public int getMessageCounterHistoryDayLimit()
   {
      return parameters.messageCounterHistoryDayLimit;
   }

   public int getMaxDepth()
   {
      return parameters.maxDepth;
   }

   public void setMaxDepth(int depth)
   {
      parameters.maxDepth = depth;
   }

   public boolean getInMemory()
   {
      return parameters.inMemory;
   }

   public void setInMemory(boolean mode)
   {
      parameters.inMemory = mode;
   }

   /**
    * Returns the message redelivery limit; the number of redelivery attempts
    * before a message is moved to the DLQ.
    */
   public int getRedeliveryLimit()
   {
      return parameters.redeliveryLimit;
   }

   /**
    * Sets the redelivery limit.
    */
   public void setRedeliveryLimit(int limit)
   {
      if (limit < 0)
         throw new IllegalArgumentException("Negative redelivery limit: " + limit);
      
      parameters.redeliveryLimit = limit;
   }

   /**
    * Returns the message redelivery delay, the delay in milliseconds before a
    * rolled back or recovered message is redelivered
    */
   public long getRedeliveryDelay()
   {
      return parameters.redeliveryDelay;
   }


   /**
    * Sets the Message redelivery delay in milliseconds.
    */
   public void setRedeliveryDelay(long rDelay)
   {
      if (rDelay < 0)
         throw new IllegalArgumentException("Negative redelivery delay: " + rDelay);
      
      parameters.redeliveryDelay = rDelay;
   }

   public Class getReceiversImpl()
   {
      return parameters.receiversImpl;
   }

   public void setReceiversImpl(Class clazz)
   {
      if (clazz != null && Receivers.class.isAssignableFrom(clazz) == false)
         throw new IllegalArgumentException("Class " + clazz.getName() + " is not a Receivers implementation");
      parameters.receiversImpl = clazz;
   }
   
   public int getRecoveryRetries()
   {
      return parameters.recoveryRetries;
   }

   public void setRecoveryRetries(int retries)
   {
      parameters.recoveryRetries = retries;
   }

   public ObjectName getExpiryDestination() 
   {
      return expiryDestination;
   }

   public void setExpiryDestination(ObjectName expiryDestination)
   {
      this.expiryDestination = expiryDestination;
   }

}
