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
package org.jboss.web;

import java.net.URL;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBeanSupport;

/** A container service used to introduce war dependencies. This service is
 created by the AbstractWebContainer during the create(DeploymentInfo) call
 and registered under the name "jboss.web.deployment:war="+di.shortName
 This name is stored in the di.context under the key AbstractWebContainer.WEB_MODULE

 When the jboss-web.xml dependencies are satisfied, this service is started
 and this triggers the AbstractWebDeployer.start. Likewise, a stop on this
 service triggers the AbstractWebDeployer.stop.
 
 @see AbstractWebContainer
 
 @author Scott.Stark@jboss.org
 @version $Revison:$
 */
public class WebModule extends ServiceMBeanSupport
   implements WebModuleMBean
{
   private DeploymentInfo di;
   private AbstractWebContainer container;
   private AbstractWebDeployer deployer;

   public WebModule(DeploymentInfo di, AbstractWebContainer container,
      AbstractWebDeployer deployer)
   {
      this.di = di;
      this.container = container;
      this.deployer = deployer;
   }

   protected void startService() throws Exception
   {
      startModule();
   }

   protected void stopService() throws Exception
   {
      stopModule();
   }

   protected void destroyService()
   {
      this.di = null;
      this.container = null;
      this.deployer = null;      
   }

   /** Invokes the deployer start
    */
   public synchronized void startModule()
      throws DeploymentException
   {
      // Get the war URL
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      WebApplication webApp = deployer.start(di);
      di.context.put(AbstractWebContainer.WEB_APP, webApp);
      container.addDeployedApp(warURL, webApp);
   }

   /** Invokes the deployer stop
    */
   public synchronized void stopModule()
      throws DeploymentException
   {
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      String warUrl = warURL.toString();
      try
      {
         WebApplication webApp = container.removeDeployedApp(warURL);
         if( deployer != null && webApp != null )
         {
            deployer.stop(di);
         }
         else
         {
            log.debug("Failed to find deployer/deployment for war: "+warUrl);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error during stop", e);
      }
   }

}
