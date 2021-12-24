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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Server.
 * 
 * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
 * @version $Revision: 57419 $
 */
public class Server
{
   /** the handle for the server **/
   private String name;

   /** the config to start **/
   private String config;

   /** the arguments to pass to jboss **/
   private List arguments = new ArrayList();

   /** the server's process, if running **/
   private Process process;

   /** the arguments for the jvm **/
   private List jvmArguments = new ArrayList();

   /** system properties for the jvm **/
   private List sysProperties = new ArrayList();

   /** the port used to determine if jboss started **/
   private Integer httpPort = new Integer(8080);

   /** where to find the rmi port **/
   private Integer rmiPort = new Integer(1099);

   /** the name or IP address to bind to **/
   private String host = "localhost";

   /** used for global config info **/
   private ServerManager manager;

   /** the output log **/
   private PrintWriter outWriter;

   /** the error log **/
   private PrintWriter errorWriter;

   /** Is there a servlet engine? **/
   private boolean hasWebServer = true;
   
   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name.
    * 
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /** 
    * Set the manager.
    * @param manager
    */
   protected void setManager(ServerManager manager)
   {
      this.manager = manager;
   }

   /**
    * Add an argument.
    * 
    * @param arg
    */
   public void addArg(Argument arg)
   {
      arguments.add(arg);
   }

   /**
    * Get the arguments as a string for the command line.
    * 
    * @return
    */
   public String getArgs()
   {
      StringBuffer args = new StringBuffer();
      for (Iterator iter = arguments.iterator(); iter.hasNext();)
      {
         Argument argument = (Argument) iter.next();
         args.append(argument.getValue() + " ");
      }
      return args.toString();
   }

   /**
    * Add a jvm arg.
    * 
    * @param arg
    */
   public void addJvmArg(Argument arg)
   {
      jvmArguments.add(arg);
   }

   /**
    * Get the JVM args for the command line.
    * 
    * @return
    */
   public String getJvmArgs()
   {
      StringBuffer args = new StringBuffer();
      for (Iterator iter = jvmArguments.iterator(); iter.hasNext();)
      {
         Argument argument = (Argument) iter.next();
         args.append(argument.getValue() + " ");
      }
      return args.toString();
   }

   /**
    * Add a system property.
    * 
    * @param property
    */
   public void addSysProperty(Property property)
   {
      sysProperties.add(property);
   }

   /**
    * Get the system properties for the command line.
    * 
    * @return
    */
   public String getSysProperties()
   {
      StringBuffer args = new StringBuffer();
      for (Iterator iter = sysProperties.iterator(); iter.hasNext();)
      {
         Property property = (Property) iter.next();
         args.append("-D" + property.getKey() + "=" + property.getValue() + " ");
      }
      return args.toString();
   }

   /** 
    * The running process of this server.
    * @param process
    */
   public void setProcess(Process process)
   {
      this.process = process;
   }

   /**
    * Is the server actually running?
    * 
    * @return
    */
   public boolean isRunning()
   {
      if (isStopped())
      {
         return false;
      }
      else
      {
         try
         {
            //exitValue() only returns if process has ended.
            process.exitValue();
            return false;
         }
         catch (IllegalThreadStateException e)
         {
            return true;
         }
      }
   }

   /** 
    * Has the server been intentionally stopped?
    * @return
    */
   public boolean isStopped()
   {
      return process == null;
   }

   /**
    * Get the process.
    * @return
    */
   public Process getProcess()
   {
      return process;
   }

   /**
    * Where is the HTTP service listening?
    * @return
    * @throws MalformedURLException
    */
   public URL getHttpUrl() throws MalformedURLException
   {
      return new URL("http://" + host + ":" + httpPort);
   }

   /** 
    * The URl for the RMI listener.
    * @return
    */
   public String getRmiUrl()
   {
      return "jnp://" + host + ":" + rmiPort;
   }

   /**
    * Get the config. Defaults to the server name.
    * 
    * @return the config.
    */
   public String getConfig()
   {
      if (config != null)
      {
         return config;
      }
      else
      {
         return name;
      }
   }

   /**
    * Set the config.
    * 
    * @param config The config to set.
    */
   public void setConfig(String config)
   {
      this.config = config;
   }

   /**
    * Get the host.
    * 
    * @return the host.
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Set the host.
    * 
    * @param host The host to set.
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * Set the httpPort.
    * 
    * @param httpPort The httpPort to set.
    */
   public void setHttpPort(Integer httpPort)
   {
      this.httpPort = httpPort;
   }

   /**
    * Set the rmiPort.
    * 
    * @param rmiPort The rmiPort to set.
    */
   public void setRmiPort(Integer rmiPort)
   {
      this.rmiPort = rmiPort;
   }

   /**
    * Get the rmiPort
    * @return
    */
   public Integer getRmiPort()
   {
      return rmiPort;
   }
   /** 
    * Where should the server's std err log go?
    * @return
    */
   public File getErrorLog()
   {
      return new File(getLogDir(), "error.log");
   }

   /** 
    * Where should the servers's std out go?
    * @return
    */
   public File getOutputLog()
   {
      return new File(getLogDir(), "output.log");
   }

   /**
    * The server's log directory
    * 
    * @return
    */
   private File getLogDir()
   {
      return new File(getConfDir(), "log");
   }

   /**
    * The server's directory (ie, all, default)
    * 
    * @return
    */
   private File getConfDir()
   {
      return new File(manager.getJBossHome(), "server/" + getConfig());
   }

   /** 
    * Set the output log's writer
    * @param outlog
    */
   public void setOutWriter(PrintWriter outlog)
   {
      outWriter = outlog;
   }

   /**
    * The writer for the output log.
    * 
    * @return
    */
   public PrintWriter getOutWriter()
   {
      return outWriter;
   }

   /**
    * The error log's writer.
    * 
    * @return
    */
   public PrintWriter getErrorWriter()
   {
      return errorWriter;
   }

   /** 
    * Set the error writer.
    * @param errorlog
    */
   public void setErrorWriter(PrintWriter errorlog)
   {
      errorWriter = errorlog;
   }

   /**
    * Get the hasWebServer.
    * 
    * @return the hasWebServer.
    */
   public boolean hasWebServer()
   {
      return hasWebServer;
   }
   /**
    * Set the hasWebServer.
    * 
    * @param hasWebServer The hasWebServer to set.
    */
   public void setHasWebServer(boolean hasWebServer)
   {
      this.hasWebServer = hasWebServer;
   }

}