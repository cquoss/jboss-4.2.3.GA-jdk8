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
package org.jboss.test.messagedriven.mbeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.Message;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Management of the test message driven bean 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class TestMessageDrivenManagement extends ServiceMBeanSupport implements TestMessageDrivenManagementMBean
{
   private static final Properties defaultProps = new Properties();
   
   private TransactionManager tm;
   
   static
   {
      defaultProps.put("destination", "NotSpecified");
      defaultProps.put("destinationType", "NotSpecified");
      defaultProps.put("transactionType", "Container");
      defaultProps.put("transactionAttribute", "Required");
      defaultProps.put("rollback", "None");
      defaultProps.put("DLQMaxResent", "5");
      defaultProps.put("DeliveryActive", "true");
      defaultProps.put("durability", "NonDurable");
      defaultProps.put("subscriptionName", "");
      defaultProps.put("user", "guest");
      defaultProps.put("password", "guest");
   }
   
   protected ArrayList messages = new ArrayList();
   
   public TestMessageDrivenManagement() throws Exception
   {
      tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
   }
   
   public void initProperties(Properties props)
   {
      setProperties(defaultProps);
      setProperties(props);
   }
   
   public void addMessage(Message message)
   {
      synchronized (messages)
      {
         messages.add(message);
      }
   }
   
   public ArrayList getMessages()
   {
      synchronized (messages)
      {
         ArrayList result = new ArrayList(messages);
         messages.clear();
         return result;
      }
   }
   
   public Transaction getTransaction()
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch (Throwable ignored)
      {
      }
      return tx;
   }
   
   protected void setProperties(Properties props)
   {
      for (Enumeration e = props.keys(); e.hasMoreElements();)
      {
         String key = (String) e.nextElement();
         System.setProperty("test.messagedriven." + key, props.getProperty(key));
      }
   }
}
