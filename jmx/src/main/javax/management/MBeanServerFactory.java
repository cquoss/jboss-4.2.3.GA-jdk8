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
package javax.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.management.loading.ClassLoaderRepository;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.PropertyAccess;

/**
 * MBeanServerFactory is used to create instances of MBean servers.
 *
 * @see javax.management.MBeanServer
 * @see javax.management.MBeanServerBuilder
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20030806 Juha Lindfors:</b>
 * <ul>
 * <li>
 *   Attempts a reflected invoke to "releaseServer" method on the MBean server
 *   instance before letting go of the server reference. This allows the
 *   MBean server to do some clean up. Based on the patch submitted by
 *   Rod Burgett (Bug Tracker: #763378)
 * </ul>
 */
public class MBeanServerFactory
{

   // Constants -----------------------------------------------------

   private static final MBeanServerPermission CREATE =
      new MBeanServerPermission("createMBeanServer", null);
   private static final MBeanServerPermission FIND = 
      new MBeanServerPermission("findMBeanServer", null);
   private static final MBeanServerPermission NEW = 
      new MBeanServerPermission("newMBeanServer", null);
   private static final MBeanServerPermission RELEASE = 
      new MBeanServerPermission("releaseMBeanServer", null);


   // Attributes ----------------------------------------------------

   /**
    * Hold on to the server instances here (unless requested not to)
    */
   private static Map serverMap = new HashMap();


   // hide public constructor
   private MBeanServerFactory()
   {
   }

   // Public --------------------------------------------------------

   /**
    * Releases the reference to an MBean server. A security permission check
    * is made first if security manager has been installed.
    *
    * @param mbeanServer reference to the MBean server you want to release
    */
   public static void releaseMBeanServer(MBeanServer mbeanServer)
   {
      checkPermission(RELEASE);
      try
      {
         String agentID = null;
         /* Run the create using the jmx codebase priviledges since the
         MBeanServerPermission(releaseMBeanServer) has passed.
         */
         final MBeanServer theServer = mbeanServer;
         final ObjectName delegateName = new ObjectName(ServerConstants.MBEAN_SERVER_DELEGATE);
         try
         {
            agentID = (String) AccessController.doPrivileged(
               new PrivilegedExceptionAction()
               {
                  public Object run() throws Exception
                  {
                     return (String) theServer.getAttribute(delegateName,
                        "MBeanServerId");
                  }
               }
            );
         }
         catch(PrivilegedActionException e)
         {
            Exception ex = e.getException();
            if( ex instanceof JMException )
               throw (JMException) ex;
            else if( ex instanceof RuntimeException )
               throw (RuntimeException) ex;
            else
            {
               JMException jex = new JMException("Unknown exception during getAttribute(MBeanServerId)");
               jex.initCause(ex);
               throw jex;
            }
         }

         Object server = serverMap.remove(agentID);

         try
         {
            Method m = server.getClass().getMethod("releaseServer", null);
            m.invoke(server, null);
         }
         catch (Exception ignored)
         {
            // if the release fails, then it fails
         }

         if (server == null)
            throw new IllegalArgumentException("MBean server reference not found.");
      }
      catch (MalformedObjectNameException e)
      {
         throw new Error(e.toString());
      }
      catch (JMException e)
      {
         throw new Error("Cannot retrieve AgentID: " + e.toString());
      }
   }

   public static MBeanServer createMBeanServer()
   {
      return createMBeanServer(ServerConstants.DEFAULT_DOMAIN);
   }

   public static MBeanServer createMBeanServer(String domain)
   {
      checkPermission(CREATE);
      return createMBeanServer(domain, true);
   }

   public static MBeanServer newMBeanServer()
   {
      return newMBeanServer(ServerConstants.DEFAULT_DOMAIN);
   }

   public static MBeanServer newMBeanServer(String domain)
   {
      checkPermission(NEW);
      return createMBeanServer(domain, false);
   }

   public synchronized static ArrayList findMBeanServer(String agentId)
   {
      checkPermission(FIND);
      if (agentId != null)
      {
         ArrayList list = new ArrayList(1);
         Object server = serverMap.get(agentId);
         
         if (server != null)
            list.add(server);
            
         return list;
      }

      return new ArrayList(serverMap.values());
   }

   /**
    * Returns the classloader repository for an MBeanServer
    *
    * @param server the mbean server, pass null for the default loader repository
    *        shared by all mbeanservers
    * @return the loader repository
    */
   public static ClassLoaderRepository getClassLoaderRepository(MBeanServer server)
   {
      return server.getClassLoaderRepository();
   }


   // Private -------------------------------------------------------

   private static MBeanServer createMBeanServer(final String defaultDomain,
      boolean registerServer)
   {
      String builderClass = PropertyAccess.getProperty(
            ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY,
            ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS
      );

      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass(builderClass);
         Constructor constructor = clazz.getConstructor(new Class[0]);
         final MBeanServerBuilder builder = (MBeanServerBuilder)
            constructor.newInstance(new Object[0]);
         final MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
         /* Run the create using the jmx codebase priviledges since the
         MBeanServerPermission(createMBeanServer) has passed.
         */
         MBeanServer server = null;
         try
         {
            server = (MBeanServer) AccessController.doPrivileged(
               new PrivilegedExceptionAction()
               {
                  public Object run() throws Exception
                  {
                     return builder.newMBeanServer(defaultDomain, null, delegate);
                  }
               }
            );
         }
         catch(PrivilegedActionException e)
         {
            RuntimeException re = (RuntimeException) e.getException();
            throw re;
         }

         if (registerServer)
         {
            String agentID = delegate.getMBeanServerId();
            serverMap.put(agentID, server);
         }

         return server;
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException("The MBean server builder implementation class " + builderClass + " was not found: " + e.toString());
      }
      catch (NoSuchMethodException e) 
      {
         throw new IllegalArgumentException("The MBean server builder implementation class " + builderClass + " must contain a default constructor.");
      }
      catch (InstantiationException e) 
      {
         throw new IllegalArgumentException("Cannot instantiate class " + builderClass + ": " + e.toString());
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalArgumentException("Unable to create the MBean server builder instance. Illegal access to class " + builderClass + " constructor: " + e.toString());
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException("Unable to create the MBean server builder instance. Class " + builderClass + " has raised an exception in constructor: " + e.getTargetException().toString());
      }
   }

   private static void checkPermission(MBeanServerPermission permission)
   {
      SecurityManager security = System.getSecurityManager();
      if (security == null)
         return;
      security.checkPermission(permission);
   }

}

