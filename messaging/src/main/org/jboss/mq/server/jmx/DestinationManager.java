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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.xa.Xid;

import org.jboss.mq.MessageStatistics;
import org.jboss.mq.pm.PersistenceManager;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.server.BasicQueueParameters;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.mq.server.MessageCache;
import org.jboss.mq.server.MessageCounter;
import org.jboss.mq.server.Receivers;
import org.jboss.mq.sm.StateManager;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.JBossStringBuilder;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.ThreadPool;

/**
 * JMX MBean implementation for JBossMQ.
 *
 * @jmx:mbean extends="org.jboss.mq.server.jmx.InterceptorMBean"
 * @author     Vincent Sheffer (vsheffer@telkel.com)
 * @author     <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version    $Revision: 66418 $
 */
public class DestinationManager extends InterceptorMBeanSupport implements DestinationManagerMBean
{
   public String jndiBindLocation = "java:/JBossMQServer";

   /** A proxy to the service controller. */
   private ServiceControllerMBean serviceController;
   /** The JMX ObjectName of this service MBean */
   private ObjectName mqService;
   /** The JMS server implementation */
   protected JMSDestinationManager jmsServer;
   /** The JMX ObjectName of the configured persistence manager */
   private ObjectName persistenceManager;
   /** The JMX ObjectName of the configured state manager */
   private ObjectName stateManager;
   /** The JMX ObjectName of the message cache */
   private ObjectName messageCache;
   /** The temporary topic/queue parameters */
   protected BasicQueueParameters tempParameters = new BasicQueueParameters();
   /** Default thread pool */
   private ObjectName threadPool;
   /** Default expiry destination */
   private ObjectName expiryDestination;
   /** Whether to do the redelivery update lazily */
   private boolean lazyRedeliveryUpdate = false;

   /**
    * @jmx:managed-attribute
    * @return the count of active clients
    */
   public int getClientCount()
   {
      if (jmsServer == null)
         return 0;
      return jmsServer.getClientCount();
   }
   /**
    * @jmx:managed-attribute
    * @return a Map<ConnectionToken, ClientConsumer> of current clients
    */
   public Map getClients()
   {
      if (jmsServer == null)
         return null;
      return jmsServer.getClients();
   }

   /**
    * Get the value of PersistenceManager.
    * 
    * @jmx:managed-attribute
    * @return value of PersistenceManager.
    */
   public ObjectName getPersistenceManager()
   {
      return persistenceManager;
   }

   /**
    * Set the value of PersistenceManager.
    * 
    * @jmx:managed-attribute
    * @param v  Value to assign to PersistenceManager.
    */
   public void setPersistenceManager(ObjectName objectName)
   {
      this.persistenceManager = objectName;
   }

   /**
    * Get the value of StateManager.
    * 
    * @jmx:managed-attribute
    * @return value of StateManager.
    */
   public ObjectName getStateManager()
   {
      return stateManager;
   }

   /**
    * Set the value of StateManager.
    * 
    * @jmx:managed-attribute
    * @param v  Value to assign to StateManager.
    */
   public void setStateManager(ObjectName objectName)
   {
      this.stateManager = objectName;
   }

   /**
    * Get the value of MessageCache.
    * 
    * @jmx:managed-attribute
    * @return value of MessageCache.
    */
   public ObjectName getMessageCache()
   {
      return messageCache;
   }

   /**
    * Set the value of MessageCache.
    * 
    * @jmx:managed-attribute
    * @param v  Value to assign to MessageCache.
    */
   public void setMessageCache(ObjectName objectName)
   {
      this.messageCache = objectName;
   }

   /**
    * Retrieve the temporary topic/queue max depth
    * @return the maximum depth
    * @jmx:managed-attribute
    */
   public int getTemporaryMaxDepth()
   {
      return tempParameters.maxDepth;
   }

   /**
    * Set the temporary topic/queue max depth
    * @param depth the maximum depth
    * @jmx:managed-attribute
    */
   public void setTemporaryMaxDepth(int depth)
   {
      tempParameters.maxDepth = depth;
   }

   /**
    * Retrieve the temporary topic/queue in memory mode
    * @return true for in memory
    * @jmx:managed-attribute
    */
   public boolean getTemporaryInMemory()
   {
      return tempParameters.inMemory;
   }

   /**
    * Set the temporary topic/queue in memory mode
    * @param mode true for in memory
    * @jmx:managed-attribute
    */
   public void setTemporaryInMemory(boolean mode)
   {
      tempParameters.inMemory = mode;
   }

   /**
    * Get the receivers implemenetation
    * 
    * @return the receivers implementation class
    * @jmx:managed-attribute
    */
   public Class getReceiversImpl()
   {
      return tempParameters.receiversImpl;
   }

   /**
    * Set the receivers implementation class
    * 
    * @param clazz the receivers implementation class
    * @jmx:managed-attribute
    */
   public void setReceiversImpl(Class clazz)
   {
      if (clazz != null && Receivers.class.isAssignableFrom(clazz) == false)
         throw new IllegalArgumentException("Class " + clazz.getName() + " is not a Receivers implementation");
      tempParameters.receiversImpl = clazz;
   }
   
   public int getRecoveryRetries()
   {
      return tempParameters.recoveryRetries;
   }

   public void setRecoveryRetries(int retries)
   {
      tempParameters.recoveryRetries = retries;
   }
   
   public ObjectName getThreadPool()
   {
      return this.threadPool;
   }

   public void setThreadPool(ObjectName threadPool)
   {
      this.threadPool = threadPool;
   }
   
   public ObjectName getExpiryDestination() 
   {
      return expiryDestination;
   }

   public void setExpiryDestination(ObjectName expiryDestination)
   {
      this.expiryDestination = expiryDestination;
   }

   /**
    * Get the lazyRedeliveryUpdate.
    * 
    * @return the lazyRedeliveryUpdate.
    */
   public boolean isLazyRedeliveryUpdate()
   {
      return lazyRedeliveryUpdate;
   }

   /**
    * Set the lazyRedeliveryUpdate.
    * 
    * @param lazyRedeliveryUpdate the lazyRedeliveryUpdate.
    */
   public void setLazyRedeliveryUpdate(boolean lazyRedeliveryUpdate)
   {
      this.lazyRedeliveryUpdate = lazyRedeliveryUpdate;
   }

   /**
    * @jmx:managed-operation
    */
   public void createQueue(String name) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Queue", getQueueObjectName(name), null);
   }

   /**
    * @jmx:managed-operation
    */
   public void createTopic(String name) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Topic", getTopicObjectName(name), null);
   }

   /**
    * @jmx:managed-operation
    */
   public void createQueue(String name, String jndiLocation) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Queue", getQueueObjectName(name), jndiLocation);
   }

   /**
    * @jmx:managed-operation
    */
   public void createTopic(String name, String jndiLocation) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Topic", getTopicObjectName(name), jndiLocation);
   }

   // TODO. Should we add any Kind of security configuration for these 
   // dynamicly created destination. For example en optional URL to
   // an xml config file.
   protected void createDestination(String type, ObjectName name, String jndiLocation) throws Exception
   {
      log.debug("Attempting to create destination: " + name + "; type=" + type);

      server.createMBean(type, name);
      server.setAttribute(name, new Attribute("DestinationManager", mqService));
      if (jndiLocation != null)
         server.setAttribute(name, new Attribute("JNDIName", jndiLocation));

      // This destination should be stopped when we are stopped
      ArrayList depends = new ArrayList();
      depends.add(serviceName);

      serviceController.create(name, depends);
      serviceController.start(name);
   }

   /**
    * @jmx:managed-operation
    */
   public void destroyQueue(String name) throws Exception
   {
      destroyDestination(getQueueObjectName(name));
   }

   /**
    * @jmx:managed-operation
    */
   public void destroyTopic(String name) throws Exception
   {
      destroyDestination(getTopicObjectName(name));
   }

   protected void destroyDestination(ObjectName name) throws Exception
   {
      if (log.isDebugEnabled())
      {
         log.debug("Attempting to destroy destination: " + name);
      }

      serviceController.stop(name);

      server.invoke(name, "removeAllMessages", new Object[] {
      }, new String[] {
      });
      serviceController.destroy(name);
      serviceController.remove(name);
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name) throws MalformedObjectNameException
   {
      // Save our object name to create destination names based on it
      mqService = name;
      return mqService;
   }

   protected ObjectName getTopicObjectName(String name) throws MalformedObjectNameException
   {
      return new ObjectName(mqService.getDomain() + ".destination:service=Topic,name=" + name);
   }

   protected ObjectName getQueueObjectName(String name) throws MalformedObjectNameException
   {
      return new ObjectName(mqService.getDomain() + ".destination:service=Queue,name=" + name);
   }

   protected ServiceControllerMBean getServiceController()
   {
      return serviceController;
   }

   /**
    * @see InterceptorMBean#getInterceptor()
    */
   public JMSServerInterceptor getInterceptor()
   {
      return jmsServer;
   }

   /**
    * @see ServiceMBeanSupport#createService()
    */
   protected void createService() throws Exception
   {
      super.createService();
      jmsServer = new JMSDestinationManager(tempParameters);
      jmsServer.setLazyRedeliveryUpdate(lazyRedeliveryUpdate);
   }

   protected void startService() throws Exception
   {
      // Get a proxy to the service controller
      serviceController =
         (ServiceControllerMBean) MBeanProxyExt.create(
            ServiceControllerMBean.class,
            ServiceControllerMBean.OBJECT_NAME,
            server);
      
      PersistenceManager pm = (PersistenceManager) server.getAttribute(persistenceManager, "Instance");
      jmsServer.setPersistenceManager(pm);

      StateManager sm = (StateManager) server.getAttribute(stateManager, "Instance");
      jmsServer.setStateManager(sm);

      // We were either told the message cache or we get it from the
      // persistence manager
      MessageCache mc;
      if (messageCache != null)
         mc = (MessageCache) server.getAttribute(messageCache, "Instance");
      else
         mc = pm.getMessageCacheInstance();
      jmsServer.setMessageCache(mc);

      ThreadPool tp;
      ThreadGroup tg;
      if (threadPool == null)
      {
         tg = new ThreadGroup("JBossMQ Server Threads");
         tp = new BasicThreadPool("JMSThread", tg);
      }
      else
      {
         tp = (ThreadPool) server.getAttribute(threadPool, "Instance");
         if (tp instanceof BasicThreadPool)
           tg = ((BasicThreadPool)tp).getThreadGroup();
         else
           tg = new ThreadGroup("JBossMQ Server Threads");
      }
      jmsServer.setThreadPool(tp);
      jmsServer.setThreadGroup(tg);

      jmsServer.startServer();

      super.startService();
   }

   protected void stopService()
   {
      jmsServer.stopServer();
   }

   protected void destroyService() throws Exception
   {
      super.destroyService();
      jmsServer = null;
   }

   /**
    * Sets the destination message counter history day limit
    * <0: unlimited, =0: disabled, > 0 maximum day count
    *
    * @param days  maximum day count
    * 
    * @jmx:managed-attribute
    */
   public void setMessageCounterHistoryDayLimit( int days )
   {
      if( days < -1 )
          days = -1;

      tempParameters.messageCounterHistoryDayLimit = days;

   }

   /**
    * Gets the destination message counter history day limit
    * @return  Maximum day count
    * 
    * @jmx:managed-attribute
    */
   public int getMessageCounterHistoryDayLimit()
   {
      return tempParameters.messageCounterHistoryDayLimit;
   }

   /**
    * get message counter of all configured destinations 
    * 
    * @jmx:managed-operation
    */
   public MessageCounter[] getMessageCounter() throws Exception
   {
      if (jmsServer == null)
         return null;
      return jmsServer.getMessageCounter();
   }

   /**
    * get message stats
    * 
    * @jmx:managed-operation
    */
   public MessageStatistics[] getMessageStatistics() throws Exception
   {
      if (jmsServer == null)
         return null;
      return MessageCounter.getMessageStatistics(jmsServer.getMessageCounter());
   }

   /**
    * List message counter of all configured destinations as HTML table
    *
    * @jmx:managed-operation
    */
   public String listMessageCounter() throws Exception
   {
      if (jmsServer == null)
         return null;
      MessageCounter[] counter = jmsServer.getMessageCounter();

      String ret =
         "<table width=\"100%\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">"
            + "<tr>"
            + "<th>Type</th>"
            + "<th>Name</th>"
            + "<th>Subscription</th>"
            + "<th>Durable</th>"
            + "<th>Count</th>"
            + "<th>CountDelta</th>"
            + "<th>Depth</th>"
            + "<th>DepthDelta</th>"
            + "<th>Last Add</th>"
            + "</tr>";

      String strNameLast = null;
      String strTypeLast = null;
      String strDestLast = null;

      String destData = "";
      int destCount = 0;

      int countTotal = 0;
      int countDeltaTotal = 0;
      int depthTotal = 0;
      int depthDeltaTotal = 0;

      int i = 0; // define outside of for statement, so variable
      // still exists after for loop, because it is
      // needed during output of last module data string

      for (i = 0; i < counter.length; i++)
      {
         // get counter data
         StringTokenizer tokens = new StringTokenizer(counter[i].getCounterAsString(), ",");

         String strType = tokens.nextToken();
         String strName = tokens.nextToken();
         String strSub = tokens.nextToken();
         String strDurable = tokens.nextToken();

         String strDest = strType + "-" + strName;

         String strCount = tokens.nextToken();
         String strCountDelta = tokens.nextToken();
         String strDepth = tokens.nextToken();
         String strDepthDelta = tokens.nextToken();
         String strDate = tokens.nextToken();

         // update total count / depth values
         countTotal += Integer.parseInt(strCount);
         depthTotal += Integer.parseInt(strDepth);

         countDeltaTotal += Integer.parseInt(strCountDelta);
         depthDeltaTotal += Integer.parseInt(strDepthDelta);

         if (strCountDelta.equalsIgnoreCase("0"))
            strCountDelta = "-"; // looks better

         if (strDepthDelta.equalsIgnoreCase("0"))
            strDepthDelta = "-"; // looks better

         // output destination counter data as HTML table row
         // ( for topics with multiple subscriptions output
         //   type + name field as rowspans, looks better )
         if (strDestLast != null && strDestLast.equals(strDest))
         {
            // still same destination -> append destination subscription data
            destData += "<tr bgcolor=\"#" + ((i % 2) == 0 ? "FFFFFF" : "F0F0F0") + "\">";
            destCount += 1;
         }
         else
         {
            // start new destination data
            if (strDestLast != null)
            {
               // store last destination data string
               ret += "<tr bgcolor=\"#"
                  + ((i % 2) == 0 ? "FFFFFF" : "F0F0F0")
                  + "\"><td rowspan=\""
                  + destCount
                  + "\">"
                  + strTypeLast
                  + "</td><td rowspan=\""
                  + destCount
                  + "\">"
                  + strNameLast
                  + "</td>"
                  + destData;

               destData = "";
            }

            destCount = 1;
         }

         // counter data row
         destData += "<td>"
            + strSub
            + "</td>"
            + "<td>"
            + strDurable
            + "</td>"
            + "<td>"
            + strCount
            + "</td>"
            + "<td>"
            + strCountDelta
            + "</td>"
            + "<td>"
            + strDepth
            + "</td>"
            + "<td>"
            + strDepthDelta
            + "</td>"
            + "<td>"
            + strDate
            + "</td>";

         // store current destination data for change detection
         strTypeLast = strType;
         strNameLast = strName;
         strDestLast = strDest;
      }

      if (strDestLast != null)
      {
         // store last module data string
         ret += "<tr bgcolor=\"#"
            + ((i % 2) == 0 ? "FFFFFF" : "F0F0F0")
            + "\"><td rowspan=\""
            + destCount
            + "\">"
            + strTypeLast
            + "</td><td rowspan=\""
            + destCount
            + "\">"
            + strNameLast
            + "</td>"
            + destData;
      }

      // append summation info
      ret += "<tr>"
         + "<td><![CDATA[ ]]></td><td><![CDATA[ ]]></td>"
         + "<td><![CDATA[ ]]></td><td><![CDATA[ ]]></td><td>"
         + countTotal
         + "</td><td>"
         + (countDeltaTotal == 0 ? "-" : Integer.toString(countDeltaTotal))
         + "</td><td>"
         + depthTotal
         + "</td><td>"
         + (depthDeltaTotal == 0 ? "-" : Integer.toString(depthDeltaTotal))
         + "</td><td>Total</td></tr></table>";

      return ret;
   }

   /**
    * Reset message counter of all configured destinations
    * 
    * @jmx:managed-operation
    */
   public void resetMessageCounter()
   {
      if (jmsServer == null)
         return;
      jmsServer.resetMessageCounter();
   }

   public Map retrievePreparedTransactions()
   {
      if (jmsServer == null)
         return null;
      Map map = jmsServer.getPersistenceManager().getTxManager().getPreparedTransactions();
      HashMap result = new HashMap();
      for (Iterator i = map.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         Xid xid = (Xid) entry.getKey();
         TxManager.PreparedInfo info = (TxManager.PreparedInfo) entry.getValue();
         if (xid != null && info != null)
            result.put(xid, Boolean.valueOf(info.isInDoubt()));
      }
      return result;
   }

   public String showPreparedTransactions()
   {
      if (jmsServer == null)
         return null;
      Map map = jmsServer.getPersistenceManager().getTxManager().getPreparedTransactions();
      JBossStringBuilder buffer = new JBossStringBuilder();
      buffer.append("<table width=\"100%\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">");
      buffer.append("<tr><th>Xid</th><th>In Doubt</th><th>Local TXIDs</th></tr>");
      for (Iterator i = map.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         Xid xid = (Xid) entry.getKey();
         TxManager.PreparedInfo info = (TxManager.PreparedInfo) entry.getValue();
         if (xid != null && info != null)
         {
            buffer.append("<tr><td>");
            buffer.append(xid);
            buffer.append("</td><td>");
            buffer.append(info.isInDoubt());
            buffer.append("</td><td>");
            buffer.append(info.getTxids());
            buffer.append("</td></tr>");
         }
      }
      buffer.append("</table>");
      return buffer.toString();
   }
}
