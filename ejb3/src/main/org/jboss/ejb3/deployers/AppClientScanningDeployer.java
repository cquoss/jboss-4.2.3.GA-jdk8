/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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

import org.jboss.deployers.plugins.deployers.helpers.AbstractTypedDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.virtual.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Scan the main & super classes for annotations.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AppClientScanningDeployer extends AbstractTypedDeployer<ApplicationClientDD>
{

   public AppClientScanningDeployer()
   {
      super(ApplicationClientDD.class);
      
      setRelativeOrder(POSTPROCESS_CLASSLOADING_DEPLOYER);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.plugins.deployer.AbstractSimpleDeployer#deploy(org.jboss.deployers.spi.deployer.DeploymentUnit)
    */
   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      ApplicationClientDD dd = unit.getAttachment(getDeploymentType());
      // FIXME: implement metadata complete
//      if(dd != null && dd.getMetaDataComplete())
//         return;
      // for now the EJB3 client deployer handles all
      if(dd != null)
         return;
      
      try
      {
         String mainClassName = getMainClassName(unit);
         if(mainClassName == null)
            return;
         
         Class<?> mainClass = unit.getClassLoader().loadClass(mainClassName);
         
         log.info("mainClass = " + mainClass);
         
         if(hasAnnotations(mainClass))
         {
            // add a dummy application client dd to fire up the ejb3 client deployer
            dd = new ApplicationClientDD();
            unit.addAttachment(ApplicationClientDD.class, dd);
         }
      }
      catch(ClassNotFoundException e)
      {
         throw new DeploymentException(e);
      }
      catch(IOException e)
      {
         throw new DeploymentException(e);
      }
   }
   
   // TODO: integrate with Ejb3ClientDeployer.getMainClassName
   private String getMainClassName(DeploymentUnit unit) throws IOException
   {
      VirtualFile file = unit.getMetaDataFile("MANIFEST.MF");
      log.trace("parsing " + file);

      if(file == null)
      {
         return null;
      }

      try
      {
         // TODO - use VFSUtils.readManifest
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
         return className;
      }
      finally
      {
         file.close();
      }
   }
   
   // TODO: should we check for type of annotations?
   private boolean hasAnnotations(Class<?> cls)
   {
      if(cls == null)
         return false;
      
      // Note: this also returns true if super class has annotations
      if(cls.getAnnotations().length > 0)
         return true;
      
      for(Field f : cls.getDeclaredFields())
      {
         if(f.getAnnotations().length > 0)
            return true;
      }
      
      return hasAnnotations(cls.getSuperclass());
   }
}
