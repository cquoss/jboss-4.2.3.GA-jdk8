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
package org.jboss.web.tomcat.security.config;

//$Id: JBossContextConfig.java 57206 2006-09-26 12:25:30Z dimitris@jboss.org $

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Authenticator;
import org.apache.catalina.startup.ContextConfig;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 *  Extension of Catalina ContextConfig that will allow
 *  plugging custom authenticators at the host level
 *  in the least intrusive way to the tomcat layer.
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 6, 2006
 *  @version $Revision: 57206 $
 */
public class JBossContextConfig extends ContextConfig
{ 
   private static Logger log = Logger.getLogger(JBossContextConfig.class);
   
   private boolean isTrace = log.isTraceEnabled();
   
   private boolean DELEGATE_TO_PARENT = false; 
   
   /**
    * Create a new JBossContextConfig.
    */
   public JBossContextConfig()
   {
      super();  
      try
      {
         Map authMap = this.getAuthenticators();
         if(authMap.size() > 0)
            customAuthenticators = authMap; 
         this.DELEGATE_TO_PARENT = getDeleteWorkDir();
      }catch(Exception e)
      {
         log.error("Failed to customize authenticators::",e); 
      } 
   }   
    
   /**
    * Process a "destroy" event for this Context.
    */
   protected synchronized void destroy()
   { 
      if(log.isTraceEnabled())
         log.trace("destroy called with DELEGATE_TO_PARENT="
               + DELEGATE_TO_PARENT);
      if(DELEGATE_TO_PARENT)
        super.destroy();
   }



   /**
    * Map of Authenticators
    * @return
    * @throws Exception
    */
   private Map getAuthenticators() throws Exception
   {
      Map cmap = new HashMap();
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      
      Properties authProps = this.getAuthenticatorsFromJMX();
      if(authProps != null)
      {
         Set keys = authProps.keySet();
         Iterator iter = keys != null ? keys.iterator() : null;
         while(iter != null && iter.hasNext())
         {
            String key = (String)iter.next();
            String authenticatorStr = (String)authProps.get(key);
            Class authClass = tcl.loadClass(authenticatorStr);
            cmap.put(key, (Authenticator)authClass.newInstance()); 
         }
      }
      if(isTrace)
         log.trace("Authenticators plugged in::"+cmap);
      return cmap; 
   }
   
   /**
    * Get the key-pair of authenticators from the
    * TomcatAuthenticatorConfig MBean
    * 
    * @return
    * @throws JMException
    */
   private Properties getAuthenticatorsFromJMX() throws JMException
   {
      Properties props = null;
      MBeanServer server = MBeanServerLocator.locateJBoss();
      props = (Properties)server.getAttribute(new ObjectName("jboss.web:service=WebServer"),
                       "Authenticators"); 
      return props; 
   }
   
   /**
    * Get the flag that sets whether the work directory corresponding
    * to the context needs to be deleted (delegate to parent) or not
    * @return
    * @throws JMException
    */
   private boolean getDeleteWorkDir() throws JMException
   {
      Boolean flag = Boolean.FALSE;
      MBeanServer server = MBeanServerLocator.locateJBoss();
      flag = (Boolean)server.getAttribute(new ObjectName("jboss.web:service=WebServer"),
                       "DeleteWorkDirOnContextDestroy"); 
      return flag.booleanValue(); 
   }
}
