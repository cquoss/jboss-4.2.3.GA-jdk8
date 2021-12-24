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
package org.jboss.injection;

// $Id: WebServiceRefInjector.java 65717 2007-10-01 17:01:38Z thomas.diesler@jboss.com $

import java.lang.reflect.AnnotatedElement;
import java.net.URL;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;

import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.serviceref.ServiceRefDelegate;
import org.jboss.wsf.common.URLLoaderAdapter;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;

/**
 * Inject a jaxws web service ref.
 *
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 65717 $
 */
public class WebServiceRefInjector implements EncInjector
{
   private static final Logger log = Logger.getLogger(WebServiceRefInjector.class);

   private String name;
   private ServiceRefMetaData sref;

   public WebServiceRefInjector(String name, AnnotatedElement anElement, ServiceRefMetaData sref)
   {
      this.name = name;
      this.sref = sref;
      this.sref.setAnnotatedElement(anElement);
   }

   public void inject(InjectionContainer container)
   {
      try
      {
         Context encCtx = container.getEnc();
         EJBContainer ejbContainer = (EJBContainer)container;
         DeploymentUnit unit = ejbContainer.getDeploymentUnit();
         URL rootURL = unit.getUrl();

         ClassLoader loader = unit.getClassLoader();
         URLLoaderAdapter vfsRoot = new URLLoaderAdapter(rootURL);
         new ServiceRefDelegate().bindServiceRef(encCtx, name, vfsRoot, loader, sref);
         
         log.debug("@WebServiceRef bound [env=" + name + "]");
      }
      catch (Exception e)
      {
         throw new WebServiceException("Unable to bind @WebServiceRef [enc=" + name + "]", e);
      }
   }

   public String toString()
   {
      return super.toString() + "{enc=" + name + "}";
   }
}
