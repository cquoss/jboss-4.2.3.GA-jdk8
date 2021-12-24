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
package org.jboss.mq.kernel;

import java.util.Hashtable;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.jboss.mq.SpyQueue;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSQueue;
import org.jboss.mq.SpyDestination;
import org.jboss.util.naming.Util;

/**
 * lite wrapper so that this can work in a dependency injection framework.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60195 $
 */
public class Queue extends org.jboss.mq.server.jmx.Queue
{
   protected JMSDestinationManager destinationManagerPojo;
   protected Hashtable initialContextProperties;


   public void setDestinationName(String name)
   {
      this.destinationName = name;
   }

   public String getDestinationName()
   {
      return destinationName;
   }

   protected void setupSecurityManager() throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      // todo
   }

   protected void teardownSecurityManager() throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      // todo
   }

   public void create() throws Exception
   {
      // Copy default values from the server when null or zero
      if (parameters.receiversImpl == null)
         parameters.receiversImpl = destinationManagerPojo.getParameters().receiversImpl;
      if (parameters.recoveryRetries == 0)
         parameters.recoveryRetries = destinationManagerPojo.getParameters().recoveryRetries;
   }

   public void start() throws Exception
   {
      // todo set up security manager.
      /*
      if (securityManager != null)
      {
         // Set securityConf at manager
         getServer().invoke(securityManager, "addDestination", new Object[]{spyDest.getName(), securityConf}, new String[]{"java.lang.String", "org.w3c.dom.Element"});
      }
      */
      if (destinationName == null || destinationName.length() == 0)
      {
         throw new javax.jms.IllegalStateException("QueueName was not set");
      }

      spyDest = new SpyQueue(destinationName);
      destination = new JMSQueue(spyDest, null, destinationManagerPojo, parameters);

      destinationManagerPojo.addDestination(destination);

      if (jndiName == null) {
         setJNDIName("queue/" + destinationName);
      }
      else {
         // in config phase, all we did was store the name, and not actually bind
         setJNDIName(jndiName);
      }
   }
   
   public void setExpiryDestinationJndi(String jndi) throws Exception
   {
      InitialContext ctx = new InitialContext();
      parameters.expiryDestination = (SpyDestination)ctx.lookup(jndi);
   }
   
   public void stop()
   {
      try
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

         if (destinationManagerPojo != null)
            destinationManagerPojo.closeDestination(spyDest);

         // TODO: need to remove from JMSServer
         /*
         if (securityManager != null)
         {
            // Set securityConf at manager
            getServer().invoke(securityManager, "removeDestination", new Object[]{spyDest.getName()}, new String[]{"java.lang.String"});
         }
         */}
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      catch (JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   protected InitialContext getInitialContext()
           throws NamingException
   {
      InitialContext ctx1 = null;
      if (initialContextProperties != null)
      {
         ctx1 = new InitialContext(initialContextProperties);
      }
      else ctx1 = new InitialContext();
      InitialContext ctx = ctx1;
      return ctx;
   }

   public void destroy()
   {
   }

   public Hashtable getInitialContextProperties()
   {
      return initialContextProperties;
   }

   public void setInitialContextProperties(Hashtable initialContextProperties)
   {
      this.initialContextProperties = initialContextProperties;
   }

   public JMSDestinationManager getDestinationManagerPojo()
   {
      return destinationManagerPojo;
   }

   public void setDestinationManagerPojo(JMSDestinationManager destinationManagerPojo)
   {
      this.destinationManagerPojo = destinationManagerPojo;
   }
}
