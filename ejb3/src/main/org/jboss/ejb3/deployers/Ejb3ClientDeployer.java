/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.deployers;

import org.jboss.deployers.plugins.deployers.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.KernelAbstraction;
import org.jboss.ejb3.MCKernelAbstraction;
import org.jboss.ejb3.clientmodule.ClientENCInjectionContainer;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;
import org.jboss.virtual.VirtualFile;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Deploys a client application jar.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class Ejb3ClientDeployer extends AbstractSimpleRealDeployer<ApplicationClientDD>
{
   private static final Logger log = Logger.getLogger(Ejb3ClientDeployer.class);

   private Kernel kernel;
   private MBeanServer server;

   public Ejb3ClientDeployer()
   {
      super(ApplicationClientDD.class);
      // make sure we run after EJB3 deployer
      setRelativeOrder(COMPONENT_DEPLOYER + 1);
   }

   @Override
   public void deploy(DeploymentUnit unit, ApplicationClientDD metaData) throws DeploymentException
   {
      log.debug("deploy " + unit.getName());

      String appClientName = getJndiName(unit, metaData);

      try
      {
         // I create the namespace here, because I destroy it in undeploy
         InitialContext iniCtx = InitialContextFactory.getInitialContext();
         Context encCtx = Util.createSubcontext(iniCtx, appClientName);
         log.debug("Creating client ENC binding under: " + appClientName);

         String mainClassName = getMainClassName(unit, true);

         Class<?> mainClass = loadClass(unit, mainClassName);

         ClientENCInjectionContainer container = new ClientENCInjectionContainer(unit, metaData, mainClass, appClientName, unit.getClassLoader(), encCtx);

         //di.deployedObject = container.getObjectName();
         unit.addAttachment(ClientENCInjectionContainer.class, container);
         getKernelAbstraction().install(container.getObjectName().getCanonicalName(), container.getDependencyPolicy(), container);
      }
      catch(Exception e)
      {
         log.error("Could not deploy " + unit.getName(), e);
         undeploy(unit, metaData);
         throw new DeploymentException("Could not deploy " + unit.getName(), e);
      }
   }

   /**
    * If there is no deployment descriptor, or it doesn't specify a JNDI name, then we make up one.
    * We use the basename from di.shortName.
    *
    * @param unit
    * @param dd
    * @return   a good JNDI name
    */
   private String getJndiName(DeploymentUnit unit, ApplicationClientDD dd)
   {
      String jndiName = dd.getJndiName();
      if(jndiName != null)
         return jndiName;

      String shortName = unit.getDeploymentContext().getRoot().getName();
      if(shortName.endsWith(".jar/"))
         jndiName = shortName.substring(0, shortName.length() - 5);
      else if(shortName.endsWith(".jar"))
         jndiName = shortName.substring(0, shortName.length() - 4);
      else
         throw new IllegalStateException("Expected either '.jar' or '.jar/' at the end of " + shortName);

      return jndiName;
   }

//   public Kernel getKernel()
//   {
//      return kernel;
//   }

   private KernelAbstraction getKernelAbstraction()
   {
      return new MCKernelAbstraction(kernel, server);
   }

   // TODO: move this method either to a utility class or to the scanning deployer
   protected String getMainClassName(DeploymentUnit unit, boolean fail) throws Exception
   {
      VirtualFile file = unit.getMetaDataFile("MANIFEST.MF");
      log.trace("parsing " + file);
      // Default to the jboss client main
      String mainClassName = "org.jboss.client.AppClientMain";

      if (file != null)
      {
         try
         {
            // TODO - use VFSUtils.readManifest .. once VFS lib is updated
            InputStream is = file.openStream();
            Manifest mf;
            try
            {
               mf = new Manifest(is);
            }
            finally
            {
               is.close();
            }
            Attributes attrs = mf.getMainAttributes();
            String className = attrs.getValue(Attributes.Name.MAIN_CLASS);
            if (className != null)
            {
               mainClassName = className;
            }
         }
         finally
         {
            file.close();
         }
      }
      return mainClassName;
   }

   private Class<?> loadClass(DeploymentUnit unit, String className) throws ClassNotFoundException
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(unit.getClassLoader());
         return Thread.currentThread().getContextClassLoader().loadClass(className);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public void setMbeanServer(MBeanServer server)
   {
      this.server = server;
   }

   @Override
   public void undeploy(DeploymentUnit unit, ApplicationClientDD metaData)
   {
      log.debug("undeploy " + unit.getName());

      ClientENCInjectionContainer container = unit.getAttachment(ClientENCInjectionContainer.class);
      if(container != null)
         getKernelAbstraction().uninstall(container.getObjectName().getCanonicalName());

      String jndiName = getJndiName(unit, metaData);
      log.debug("Removing client ENC from: " + jndiName);
      try
      {
         InitialContext iniCtx = InitialContextFactory.getInitialContext();
         Util.unbind(iniCtx, jndiName);
      }
      catch(NameNotFoundException e)
      {
         // make sure stop doesn't fail for no reason
         log.debug("Could not find client ENC");
      }
      catch (NamingException e)
      {
         log.error("Failed to remove client ENC", e);
      }
   }

}
