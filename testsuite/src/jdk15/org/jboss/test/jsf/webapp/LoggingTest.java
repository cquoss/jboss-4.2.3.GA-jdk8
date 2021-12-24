/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.jboss.test.jsf.webapp;

import com.sun.faces.util.FacesLogger;
import org.apache.log4j.*;
import java.io.ByteArrayOutputStream;

/**
 * This class simulates the kind of logging that the JSF implementation
 * does.  
 *
 * @author Stan Silvert
 */
public class LoggingTest {
    
   private ByteArrayOutputStream out = new ByteArrayOutputStream();

   /**
    * Constructor sets up an in-memory Log4J appender.  Log messages
    * logged with the java.util.logging package should end up in this
    * appender.
    */
   public LoggingTest()
   {
      Appender appender = new WriterAppender(new SimpleLayout(), out);
      Logger log4jLogger = Logger.getLogger("javax.enterprise.resource.webcontainer.jsf");
      log4jLogger.addAppender(appender);
   }

   /**
    * Send java.util.logging messages using the same java.util Loggers
    * that the JSF impl uses.
    * 
    * During the JUnit test, levels are set on the loggers so that
    * some messages will pass thorough to log4j and 
    * captured by the WriterAppender.  Others will not.
    */
   public void sendLogMessages()
   {
      java.util.logging.Logger julLogger = FacesLogger.RENDERKIT.getLogger();
      if (julLogger.isLoggable(java.util.logging.Level.SEVERE)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for SEVERE.");
      julLogger.severe("Logged SEVERE message in RENDERKIT_LOGGER<br/>"); // not logged 

      julLogger = FacesLogger.TAGLIB.getLogger();
      julLogger.severe("Logged SEVERE message in TAGLIB_LOGGER<br/>"); // not logged

      julLogger = FacesLogger.APPLICATION.getLogger();
      julLogger.severe("Logged SEVERE message in APPLICATION_LOGGER<br/>"); // logged
      if (julLogger.isLoggable(java.util.logging.Level.WARNING)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for WARNING.");
      julLogger.warning("Logged WARNING message in APPLICATION_LOGGER<br/>"); // not logged

      julLogger = FacesLogger.CONTEXT.getLogger();
      julLogger.warning("Logged WARNING message in CONTEXT_LOGGER<br/>"); // logged
      if (julLogger.isLoggable(java.util.logging.Level.INFO)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for INFO.");
      julLogger.info("Logged INFO message in CONTEXT_LOGGER<br/>"); // not logged

      julLogger = FacesLogger.CONFIG.getLogger();
      julLogger.info("Logged INFO message in CONFIG_LOGGER<br/>"); // logged 
      if (julLogger.isLoggable(java.util.logging.Level.FINE)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for FINE.");
      julLogger.fine("Logged FINE message in CONFIG_LOGGER<br/>"); // not logged

      julLogger = FacesLogger.LIFECYCLE.getLogger();
      julLogger.fine("Logged FINE message in LIFECYCLE_LOGGER<br/>"); // logged
      if (julLogger.isLoggable(java.util.logging.Level.FINER)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for FINER.");
      julLogger.finer("Logged FINER message in LIFECYCLE_LOGGER<br/>"); // not logged
      if (julLogger.isLoggable(java.util.logging.Level.FINEST)) 
         throw new RuntimeException(julLogger.getName() + " should not be loggable for FINEST.");
      julLogger.finest("Logged FINER message in LIFECYCLE_LOGGER<br/>"); // not logged

      julLogger = FacesLogger.TIMING.getLogger();
      julLogger.finer("Logged FINER message in TIMING_LOGGER<br/>"); // logged
      julLogger.finest("Logged FINEST message in TIMING_LOGGER<br/>"); // logged

      julLogger = FacesLogger.MANAGEDBEAN.getLogger();
      julLogger.finest("Logged FINEST message in MANAGEDBEAN_LOGGER Logger<br/>"); // logged
   }

   /**
    * Return the contents of the in-memory Log4J appender.
    */
   public String getLoggedMessages()
   {
      return out.toString();
   }
}
