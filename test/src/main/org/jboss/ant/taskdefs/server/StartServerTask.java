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

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.test.util.server.ServerManager;

/**
 * A StopServerTask.
 * 
 * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
 * @version $Revision: 57204 $
 */
public class StartServerTask extends Task
{

   private String name;

   /**
    * Start the server.
    */

   public void execute() throws BuildException
   {
      ServerManager manager = (ServerManager) getProject().getReference(ConfigManagerTask.MANAGER_REF);
      try
      {
         manager.startServer(name);
      }
      catch (IOException e)
      {
         throw new BuildException("Error starting server \"" + name + "\": " + e.getMessage(), e);
      }
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
}