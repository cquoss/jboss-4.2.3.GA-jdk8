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

import org.jboss.deployment.DeploymentInfo;
import org.jboss.metadata.WebMetaData;

//$Id: AbstractDeployerHookJSE.java 3146 2007-05-18 22:55:26Z thomas.diesler@jboss.com $

/**
 * An abstract deployer for JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public abstract class AbstractDeployerHookJSE extends ArchiveDeployerHook
{
   public boolean isWebServiceDeployment(DeploymentInfo unit)
   {
      if ((unit.metaData instanceof WebMetaData) == false)
         return false;

      return true;
   }
   
   static class Servlet
   {
      String servletName;
      String servletClass;
      public Servlet(String servletName, String servletClass)
      {
         this.servletName = servletName;
         this.servletClass = servletClass;
      }
      public String getServletClass()
      {
         return servletClass;
      }
      public String getServletName()
      {
         return servletName;
      }
   }
}
