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

package org.jboss.web.jsf.integration.config;

import com.sun.faces.config.ConfigureListener;
import com.sun.faces.util.FacesLogger;
import java.util.logging.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.log4j.Level;
import org.jboss.logging.Logger;

/**
 * This ServletContextListener sets up a JBoss-specific environment for JSF
 * and then delegates the rest of the setup to the JSF RI.
 *
 * @author Stan Silvert
 */
public class JBossJSFConfigureListener extends ConfigureListener 
{
    private static final String WAR_BUNDLES_JSF_IMPL = "org.jboss.jbossfaces.WAR_BUNDLES_JSF_IMPL";
   
    private static Logger LOG = Logger.getLogger(JBossJSFConfigureListener.class);
    
    public static final String BASE_JSF_LOGGER = "javax.enterprise.resource.webcontainer.jsf";
    
    public static final String SHOULD_LOG_CONFIG_MESSAGES = "com.sun.faces.displayConfiguration";
    
    private ServletContext servletContext;
    
    public static boolean warBundlesJSFImpl(ServletContext servletContext)
    {
       String bundledJSFImpl = servletContext.getInitParameter(WAR_BUNDLES_JSF_IMPL);
       return (bundledJSFImpl != null) && bundledJSFImpl.equalsIgnoreCase("true");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) 
    {
        this.servletContext = event.getServletContext();
        if (warBundlesJSFImpl(this.servletContext)) return;
        
        // If the pluginClass is not set, assume Log4J
        if (System.getProperty("org.jboss.logging.Logger.pluginClass") == null) 
        {
            setLog4J();
        }

        checkForMyFaces();
        initializeJspRuntime();
        super.contextInitialized(event);
    }
    
    // This method accounts for a peculiar problem with Jasper that pops up from time
    // to time.  In some cases, if the JspRuntimeContext is not loaded then the JspFactory
    // will not be initialized for JSF.  This method assures that it will always be
    // be loaded before JSF is initialized.
    private static void initializeJspRuntime() 
    {

        try 
        {
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        }  
        catch (ClassNotFoundException cnfe) 
        {
            // do nothing 
        }
    }

    private void checkForMyFaces()
    {
        try
        {
            Thread.currentThread()
                  .getContextClassLoader()
                  .loadClass("org.apache.myfaces.webapp.StartupServletContextListener");
            LOG.warn("MyFaces JSF implementation found!  This version of JBoss AS ships with the java.net implementation of JSF.  There are known issues when mixing JSF implementations.  This warning does not apply to MyFaces component libraries such as Tomahawk.  However, myfaces-impl.jar and myfaces-api.jar should not be used without disabling the built-in JSF implementation.  See the JBoss wiki for more details.");
        }
        catch (ClassNotFoundException e)
        {
            // ignore - this is a good thing
        }
    }

    /**
     * For a given JSF julLogger find the logging level set to the
     * corresponding log4jLogger.  Then set the julLogger to the
     * same level as the log4jLogger.
     *
     * At this point we know that Log4J is being used.  So we can
     * reference a real Log4J logger instead of the JBoss one.
     */
    private void setLevel(java.util.logging.Logger julLogger)
    {
        org.apache.log4j.Logger log4jLogger = 
            org.apache.log4j.Logger.getLogger(julLogger.getName());

        julLogger.setLevel(java.util.logging.Level.OFF);

        if (log4jLogger.isEnabledFor(Level.FATAL))
            julLogger.setLevel(java.util.logging.Level.SEVERE);

        if (log4jLogger.isEnabledFor(Level.ERROR))
            julLogger.setLevel(java.util.logging.Level.SEVERE);

        if (log4jLogger.isEnabledFor(Level.WARN))
            julLogger.setLevel(java.util.logging.Level.WARNING);

        if (log4jLogger.isEnabledFor(Level.INFO))
            julLogger.setLevel(java.util.logging.Level.INFO);
 
        if (log4jLogger.isEnabledFor(Level.DEBUG)) 
            julLogger.setLevel(java.util.logging.Level.FINE);

        if (log4jLogger.isEnabledFor(Level.TRACE))
            julLogger.setLevel(java.util.logging.Level.FINEST);

        if (log4jLogger.isEnabledFor(Level.ALL))
            julLogger.setLevel(java.util.logging.Level.ALL);
    }

    /**
     * If Log4J is being used, set a filter that converts JSF RI java.util.logger
     * messages to Log4J messages.
     */
    private void setLog4J() 
    {
        Filter conversionFilter = new Log4JConversionFilter(logConfigMessages());

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(BASE_JSF_LOGGER);
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.APPLICATION.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.CONFIG.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.CONTEXT.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.LIFECYCLE.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);
        
        julLogger = FacesLogger.MANAGEDBEAN.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.RENDERKIT.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);

        julLogger = FacesLogger.TAGLIB.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);
        
        julLogger = FacesLogger.TIMING.getLogger();
        setLevel(julLogger);
        julLogger.setFilter(conversionFilter);
    }
    
    // should we log the configuration messages?
    private boolean logConfigMessages() 
    {
        String shouldLogConfigParam = this.servletContext.getInitParameter(SHOULD_LOG_CONFIG_MESSAGES);
        return (shouldLogConfigParam != null) && (shouldLogConfigParam.equalsIgnoreCase("true"));
    }
    
}
