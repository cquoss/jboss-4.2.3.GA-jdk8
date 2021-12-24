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
package org.jboss.test.util.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A ServerManager.
 * 
 * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
 * @version $Revision: 61205 $
 */
public class ServerManager
{

   /** jboss root **/
   private String jbossHome;

   /** the jvm executable **/
   private String jvm = "java";

   /** the java home to use**/
   private String javaHome;

   private String udpGroup;
   
   /** list of all configured servers **/
   private List servers = new ArrayList();

   /**
    * Create a new ServerManager.  Make sure all servers are killed on shutdown.
    * 
    */
   public ServerManager()
   {
      Thread cleanupThread = new Thread()
      {
         public void run()
         {
            for (Iterator iter = servers.iterator(); iter.hasNext();)
            {
               Server server = (Server) iter.next();
               if (server.isRunning())
               {
                  System.err.println("Found server \""+server.getName()+"\" still running; stopping it.");
                  try
                  {
                     ServerController.stopServer(server, ServerManager.this);
                  }
                  catch (IOException e)
                  {
                     System.err.println("Failed to stop server(s) on shutdown.");
                     e.printStackTrace(System.err);
                  }
               }
            }
         }
      };

      Runtime.getRuntime().addShutdownHook(cleanupThread);

   }

   /**
    * Add a server
    * 
    * @param newServer
    */
   public void addServer(Server newServer)
   {
      newServer.setManager(this);
      servers.add(newServer);
   }

   /**
    * Get the server by name.  Can't use a hashmap because of 
    * Ant.
    * 
    * @param name
    * @return
    */
   public Server getServer(String name)
   {
      for (Iterator iter = servers.iterator(); iter.hasNext();)
      {
         Server server = (Server) iter.next();
         if (server.getName().equals(name))
         {
            return server;
         }
      }
      throw new IllegalArgumentException("There is no server named: " + name);
   }

   /**
    * Start the named server.
    * 
    * @param serverName
    * @throws IOException
    */
   public void startServer(String serverName) throws IOException
   {
      ServerController.startServer(getServer(serverName), this);
   }

   /** 
    * Stop the named server 
    *
    * @param name
    * @throws IOException
    */
   public void stopServer(String name) throws IOException
   {
      ServerController.stopServer(getServer(name), this);
   }

   /**
    * Get the jvm.
    * 
    * @return the jvm.
    * @throws IOException
    */
   public String getJavaExecutable() throws IOException
   {
      return new File(javaHome + File.separator + "bin",jvm).getCanonicalPath();
   }

   /**
    * Set the jvm.
    * 
    * @param jvm The jvm to set.
    */
   public void setJvm(String jvm)
   {
      this.jvm = jvm;
   }

   /**
    * Set the javaHome.
    * 
    * @param javaHome The javaHome to set.
    */
   public void setJavaHome(String javaHome)
   {
      this.javaHome = javaHome;
   }

   /**
    * @param jbossHome The jBossHome to set.
    */
   public void setJbossHome(String jbossHome)
   {
      this.jbossHome = jbossHome;
   }

   /** 
    * The classpath to run the server
    * @return
    */
   protected String getStartClasspath()
   {
      File runjar = new File(jbossHome + "/bin/run.jar");
      File javaJar = new File(javaHome + "/lib/tools.jar");
      return runjar.toString() + File.pathSeparator + javaJar.toString();
   }

   /**
    * The classpath used to stop the server.
    * 
    * @return the string to put on the classpath
    */
   protected String getStopClasspath()
   {
      File shutdownJar = new File(jbossHome + "/bin/shutdown.jar");
      File clientJar = new File(jbossHome + "/client/jbossall-client.jar");
      return shutdownJar.toString() + File.pathSeparator + clientJar.toString();
   }

   /** 
    * Get the jboss home.
    * @return
    */
   public String getJBossHome()
   {
      return jbossHome;
   }

   /** 
    * How long to wait (in seconds) for a server to shutdown
    * @return
    */
   public int getShutdownTimeout()
   {
      // set this property in env.ANT_OPTS
      return Integer.parseInt(System.getProperty("jbossas.startup.timeout", "120"));
   }

   /** 
    * How long to wait (in seconds) for a server to startup
    * @return
    */
   public int getStartupTimeout()
   {
      return Integer.parseInt(System.getProperty("jbossas.startup.timeout", "120"));
   }

   /**
    * Get the udpGroup.
    * 
    * @return the udpGroup.
    */
   protected String getUdpGroup()
   {
      return udpGroup;
   }

   /**
    * Set the udpGroup.
    * 
    * @param udpGroup The udpGroup to set.
    */
   public void setUdpGroup(String udpGroup)
   {
      this.udpGroup = udpGroup;
   }
   
}
