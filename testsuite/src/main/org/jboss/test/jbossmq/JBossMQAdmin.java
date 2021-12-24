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
package org.jboss.test.jbossmq;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.jms.JBossASJMSTestAdmin;
import org.jboss.util.NestedRuntimeException;

/**
 * JBossMQAdmin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class JBossMQAdmin extends JBossASJMSTestAdmin
{
   private Logger log = Logger.getLogger(JBossMQAdmin.class);
   
   protected static final ObjectName destinationManager;
   protected static final ObjectName namingService;
   
   static
   {
      try
      {
         destinationManager = new ObjectName("jboss.mq:service=DestinationManager");
         namingService = new ObjectName("jboss:service=Naming");
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
   
   public JBossMQAdmin(Class clazz) throws Exception
   {
      super(clazz);
   }

   public void createQueue(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         try
         {
            server.invoke(destinationManager, "createQueue", new Object[] { name, name },  new String[] { String.class.getName(), String.class.getName() } );
         }
         catch (Exception ignored)
         {
            log.trace("Ignored", ignored);
         }
         ObjectName queueName = new ObjectName("jboss.mq.destination:service=Queue,name=" + name);
         server.invoke(queueName, "removeAllMessages", null, null);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteQueue(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName queueName = new ObjectName("jboss.mq.destination:service=Queue,name=" + name);
         server.invoke(queueName, "removeAllMessages", null, null);
         server.invoke(destinationManager, "destroyQueue", new Object[] { name },  new String[] { String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createTopic(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         try
         {
            server.invoke(destinationManager, "createTopic", new Object[] { name, name },  new String[] { String.class.getName(), String.class.getName() } );
         }
         catch (Exception ignored)
         {
            log.trace("Ignored", ignored);
         }
         ObjectName topicName = new ObjectName("jboss.mq.destination:service=Topic,name=" + name);
         server.invoke(topicName, "removeAllMessages", null, null);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteTopic(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName topicName = new ObjectName("jboss.mq.destination:service=Topic,name=" + name);
         server.invoke(topicName, "removeAllMessages", null, null);
         server.invoke(destinationManager, "destroyTopic", new Object[] { name },  new String[] { String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createConnectionFactory(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(namingService, "createAlias", new Object[] { name, "ConnectionFactory" },  new String[] { String.class.getName(), String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteConnectionFactory(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(namingService, "removeAlias", new Object[] { name },  new String[] { String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
