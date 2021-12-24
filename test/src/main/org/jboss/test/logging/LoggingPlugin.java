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
package org.jboss.test.logging;

/**
 * A LoggingPlugin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public abstract class LoggingPlugin
{
   /**
    * Get the logging plugin
    * 
    * @return the logging plugin
    * @throws Exception for any error
    */
   public static LoggingPlugin getInstance() throws Exception
   {
      String loggingClassName = System.getProperty("org.jboss.test.logging.LogginPlugin", "org.jboss.test.logging.Log4jLoggingPlugin");
      Class loggingClass  = Thread.currentThread().getContextClassLoader().loadClass(loggingClassName);
      return (LoggingPlugin) loggingClass.newInstance();
   }

   /**
    * Setup the logging
    * 
    * @throws Exception for any error
    */
   public void setUp() throws Exception
   {
   }

   /**
    * Teardown the logging
    * 
    * @throws Exception for any error
    */
   public void tearDown() throws Exception
   {
   }

   /**
    * Enable trace for a logger category
    * 
    * @param name the name of the category
    */
   public void enableTrace(String name)
   {
   }
}
