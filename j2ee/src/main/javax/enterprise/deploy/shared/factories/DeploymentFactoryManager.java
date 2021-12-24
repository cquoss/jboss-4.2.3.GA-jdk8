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
package javax.enterprise.deploy.shared.factories;

import org.jboss.logging.Logger;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The deployment factory manager.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57196 $
 */
public final class DeploymentFactoryManager
{
   // deployment logging
   private static final Logger log = Logger.getLogger(DeploymentFactoryManager.class);

   /** The instance */
   private static final DeploymentFactoryManager instance = new DeploymentFactoryManager();

   /** The deployment factories */
   private Set deploymentFactories = Collections.synchronizedSet(new HashSet());

   // Register an instance with the DeploymentFactoryManager
   static
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Class factoryClass = cl.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl");
         DeploymentFactory factory = (DeploymentFactory)factoryClass.newInstance();

         DeploymentFactoryManager dfManager = DeploymentFactoryManager.getInstance();
         dfManager.registerDeploymentFactory(factory);
      }
      catch (Exception e)
      {
         log.error("Cannot register DeploymentFactory: " + e.toString());
      }
   }

   // hide default constructor
   private DeploymentFactoryManager()
   {
   }

   /**
    * Retrieve the instance of the deployment factory manager
    *
    * @return the deployment factory manager
    */
   public static DeploymentFactoryManager getInstance()
   {
      return instance;
   }

   /**
    * Retrieve the deployment factories
    *
    * @return an array of deployment factories
    */
   public DeploymentFactory[] getDeploymentFactories()
   {
      DeploymentFactory[] arr = new DeploymentFactory[deploymentFactories.size()];
      return (DeploymentFactory[])deploymentFactories.toArray(arr);
   }

   /**
    * Get a connected deployment manager
    *
    * @param uri the uri of the deployment manager
    * @param userName the user name
    * @param password the password 
    * @return the deployment manager
    * @throws DeploymentManagerCreationException
    */
   public DeploymentManager getDeploymentManager(String uri, String userName, String password) throws DeploymentManagerCreationException
   {
      for (Iterator i = deploymentFactories.iterator(); i.hasNext();)
      {
         DeploymentFactory factory = (DeploymentFactory)i.next();
         if (factory.handlesURI(uri))
            return factory.getDeploymentManager(uri, userName, password);
      }
      throw new DeploymentManagerCreationException("No deployment manager for uri=" + uri);
   }

   /**
    * Register a deployment factory
    *
    * @param factory the deployment factory
    */
   public void registerDeploymentFactory(DeploymentFactory factory)
   {
      deploymentFactories.add(factory);
   }

   /**
    * Get a disconnected version of the deployment manager
    *
    * @param uri the uri to connect to
    * @return the disconnected deployment manager
    * @throws DeploymentManagerCreationException
    */
   public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException
   {
      for (Iterator i = deploymentFactories.iterator(); i.hasNext();)
      {
         DeploymentFactory factory = (DeploymentFactory)i.next();
         if (factory.handlesURI(uri))
            return factory.getDisconnectedDeploymentManager(uri);
      }
      throw new DeploymentManagerCreationException("No deployment manager for uri=" + uri);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
