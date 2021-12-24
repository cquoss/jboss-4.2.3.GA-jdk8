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
package org.jboss.mq.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;

import org.jboss.logging.Logger;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.mq.server.jmx.InterceptorMBeanSupport;

/**
 * The log interceptor.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class LogInterceptor
   extends InterceptorMBeanSupport
   implements InvocationHandler, LogInterceptorMBean
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(LogInterceptor.class);

   // Attributes ----------------------------------------------------

   private JMSServerInterceptor nextInterceptor;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // InvocationHandler implementation ------------------------------

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("setNext"))
      {
         nextInterceptor = (JMSServerInterceptor) args[0];
         return null;
      }
      
      StringBuffer buffer = new StringBuffer("invoke ");
      describe(buffer, method, args);
      log.debug(buffer);
      try
      {
         Object result = method.invoke(nextInterceptor, args);
         buffer = new StringBuffer("return (");
         dump(buffer, result);
         buffer.append(") ");
         describe(buffer, method, args);
         log.debug(buffer);
         return result;
      }
      catch (Throwable t)
      {
         if (t instanceof InvocationTargetException)
            t = ((InvocationTargetException) t).getTargetException();
         buffer = new StringBuffer("error ");
         describe(buffer, method, args);
         log.debug(buffer, t);
         throw t;
      }
   }

   public String getInterceptorClass() throws Exception
   {
      return null;
   }

   public void setInterceptorClass(String c) throws Exception
   {
   }

   public JMSServerInterceptor getInterceptor()
   {
      Class[] interfaces = new Class[] { JMSServerInterceptor.class };
      return (JMSServerInterceptor) Proxy.newProxyInstance(LogInterceptor.class.getClassLoader(), interfaces, this);
   }

   // Protected -----------------------------------------------------

   protected void describe(StringBuffer buffer, Method method, Object[] args)
      throws JMSException
   {
      buffer.append(method.getName());
      if (args == null)
         buffer.append("()");
      else
      {
         buffer.append("(");
         for (int i = 0; i < args.length; ++i)
         {
            if (i > 0)
               buffer.append(", ");
            dump(buffer, args[i]);
         }
         buffer.append(")");
      }
   }
   
   protected void dump(StringBuffer buffer, Object obj)
      throws JMSException
   {
      if (obj instanceof Message)
      {
         buffer.append("Message:");
         buffer.append(((Message) obj).getJMSMessageID());
      }
      else if (obj instanceof TransactionRequest)
      {
         TransactionRequest tr = (TransactionRequest) obj;
         buffer.append("Transaction Request xid=").append(tr.xid);
         buffer.append(" type=");
         switch (tr.requestType)
         {
            case TransactionRequest.ONE_PHASE_COMMIT_REQUEST: buffer.append("1Pcommit"); break;
            case TransactionRequest.TWO_PHASE_COMMIT_PREPARE_REQUEST: buffer.append("2Pprepare"); break;
            case TransactionRequest.TWO_PHASE_COMMIT_COMMIT_REQUEST: buffer.append("2Pcommit"); break;
            case TransactionRequest.TWO_PHASE_COMMIT_ROLLBACK_REQUEST: buffer.append("2Prollback"); break;
            default: buffer.append("UNKNOWN");
         }
         if (tr.messages != null && tr.messages.length != 0)
         {
            buffer.append(" msgs=");
            ArrayList msgs = new ArrayList(tr.messages.length);
            for (int i = 0; i < tr.messages.length; ++i)
                msgs.add(tr.messages[i].getJMSMessageID());
            buffer.append(msgs);
         }
         if (tr.acks != null && tr.acks.length != 0)
         {
            buffer.append(" acks=");
            ArrayList acks = new ArrayList(tr.acks.length);
            for (int i = 0; i < tr.acks.length; ++i)
                acks.add(tr.acks[i]);
            buffer.append(acks);
         }
      }
      else
         buffer.append(obj);
   }
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
