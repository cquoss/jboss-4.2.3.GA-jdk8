/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.wsf.container.jboss42;

//$Id: ModifyWebMetaDataDeployer.java 3772 2007-07-01 19:29:13Z thomas.diesler@jboss.com $

import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A deployer that modifies the web.xml meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ModifyWebMetaDataDeploymentAspect extends DeploymentAspect
{
   private WebXMLRewriterImpl webXMLRewriter;

   public void setWebXMLRewriter(WebXMLRewriterImpl serviceEndpointPublisher)
   {
      this.webXMLRewriter = serviceEndpointPublisher;
   }

   public void create(Deployment dep)
   {
      RewriteResults results = webXMLRewriter.rewriteWebXml(dep);

      // The endpoint may not have a target bean when 
      // <servlet-class> originally contained a javax.servlet.Servlet
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         if (ep.getTargetBeanName() == null)
         {
            String servletName = ep.getShortName();
            String beanClassName = results.sepTargetMap.get(servletName);
            if (beanClassName == null)
               throw new IllegalStateException("Cannot obtain target bean for: " + servletName);

            ep.setTargetBeanName(beanClassName);
         }
      }
   }
}