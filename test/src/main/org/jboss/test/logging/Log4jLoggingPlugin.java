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

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jboss.logging.XLevel;

/**
 * A Log4jLoggingPlugin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public class Log4jLoggingPlugin extends LoggingPlugin
{
   public void enableTrace(String name)
   {
      Logger.getLogger(name).setLevel(XLevel.TRACE);
   }

   public void setUp() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String file = System.getProperty("log4j.configuration");
      
      // Look for a test log4j.xml (if not in the testsuite)
      if (file == null)
      {
         URL url = cl.getResource("test-log4j.xml");
         if (url != null)
         {
            System.setProperty("log4j.configuration", "test-log4j.xml");
            BasicConfigurator.configure();
            return;
         }
      }
      
      // Use any automatic configuration
      URL url = cl.getResource("log4j.xml");
      if (url != null)
         return;
      url = cl.getResource("log4j.properties");
      if (url != null)
         return;

      // Setup by hand
      BasicConfigurator.resetConfiguration();
      PatternLayout layout = new PatternLayout("%r %-5p [%c{1}] %m%n");
      ConsoleAppender appender = new ConsoleAppender(layout);
      BasicConfigurator.configure(appender);
      file = System.getProperty("org.jboss.test.logfile");
      if (file != null)
      {
         FileAppender fileAppender = new FileAppender(layout, file);
         BasicConfigurator.configure(fileAppender);
      }
   }
}
