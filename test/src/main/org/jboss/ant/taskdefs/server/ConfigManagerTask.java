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
package org.jboss.ant.taskdefs.server;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.test.util.server.Server;
import org.jboss.test.util.server.ServerManager;

/**
 * A ConfigManagerTask.  Delegates to the Server manager.
 * 
 * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
 * @version $Revision: 57204 $
 */
public class ConfigManagerTask extends Task
{
   /** The key for the project reference **/
   protected static final String MANAGER_REF = "serverManager";

   ServerManager manager = null;

   /**
    * Create a new ConfigManagerTask.
    * 
    * 
    */
   public ConfigManagerTask()
   {
       manager = new ServerManager();
   }
   
   /**
    * Create a server manager using the specified configuration.
    */
   public void execute() throws BuildException
   {
      if (getProject().getReference(MANAGER_REF) == null)
      {
         getProject().addReference(MANAGER_REF, manager);   
      }
   }

   /**
    * Add a server.
    */
   public void addServer(Server server)
   {
      manager.addServer(server);
   }

   /** 
    * JAVA_HOME to start jboss with.
    * @param javaHome
    */
   public void setJavaHome(String javaHome)
   {
      manager.setJavaHome(javaHome);
   }

   /**
    * JBoss dist to start.
    * @param jbossHome
    */
   public void setJbossHome(String jbossHome)
   {
      manager.setJbossHome(jbossHome);
   }

   /**
    * JVM command to use default is "java"
    * @param jvm
    */
   public void setJvm(String jvm)
   {
      manager.setJvm(jvm);
   }
   
   /**
    * The UDP group to pass to org.jboss.Main using
    * the -u option.
    */
   public void setUdpGroup(String udpGroup)
   {
      manager.setUdpGroup(udpGroup);
   }
}