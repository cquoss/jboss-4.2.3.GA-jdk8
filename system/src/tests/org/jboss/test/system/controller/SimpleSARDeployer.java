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
package org.jboss.test.system.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;

/**
 * SimpleSARDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class SimpleSARDeployer
{
   private static Logger log = Logger.getLogger(SimpleSARDeployer.class);
   
   public static ObjectName classLoaderObjectName = ObjectNameFactory.create("test:classloader=test");
   
   private ServiceControllerMBean serviceController;

   private List deployed = new CopyOnWriteArrayList();

   private List tempDeployed = new CopyOnWriteArrayList();
   
   private DocumentBuilder parser;
   
   public SimpleSARDeployer(MBeanServer server, ServiceControllerMBean serviceController) throws Exception
   {
      this.serviceController = serviceController;
      
      TestClassLoader classLoader = new TestClassLoader();
      server.registerMBean(classLoader, classLoaderObjectName);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
   }

   public List deploy(URL url, boolean temporary) throws Exception
   {
      long start = System.currentTimeMillis();
      
      List result = doInstall(url, temporary);

      try
      {
         create(result);
         try
         {
            start(result);
         }
         catch (Throwable t)
         {
            stop(result);
            throw t;
         }
      }
      catch (Throwable t)
      {
         destroy(result);
         remove(result);
         DeploymentException.rethrowAsDeploymentException("Error", t);
      }
      
      log.debug("Deployed " + url + " took " + (System.currentTimeMillis() - start) + "ms");
      return result;
   }
   
   public void uninstall()
   {
      undeploy(deployed);
   }
   
   public void uninstallTemporary()
   {
      undeploy(tempDeployed);
   }
   
   public void undeploy(List objectNames)
   {
      stop(objectNames);
      destroy(objectNames);
      remove(objectNames);
   }
   
   public void create(List services) throws Exception
   {
      for (int i = 0; i < services.size(); ++i)
      {
         ObjectName name = (ObjectName) services.get(i);
         serviceController.create(name);
      }
   }
   
   public void start(List services) throws Exception
   {
      for (int i = 0; i < services.size(); ++i)
      {
         ObjectName name = (ObjectName) services.get(i);
         serviceController.start(name);
      }
   }
   
   public void stop(List services)
   {
      for (ListIterator i = services.listIterator(services.size()); i.hasPrevious();)
      {
         ObjectName name = (ObjectName) i.previous();
         try
         {
            serviceController.stop(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public void destroy(List services)
   {
      for (ListIterator i = services.listIterator(services.size()); i.hasPrevious();)
      {
         ObjectName name = (ObjectName) i.previous();
         try
         {
            serviceController.destroy(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public void remove(List services)
   {
      for (Iterator i = services.iterator(); i.hasNext();)
      {
         ObjectName name = (ObjectName) i.next();
         deployed.remove(name);
         tempDeployed.remove(name);
         try
         {
            serviceController.remove(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public List install(URL url) throws Exception
   {
      long start = System.currentTimeMillis();
      
      List result = doInstall(url, true);
      
      log.debug("Deployed " + url + " took " + (System.currentTimeMillis() - start) + "ms");
      return result;
   }
   
   public void uninstall(List services)
   {
      remove(services);
   }
   
   protected List doInstall(URL url, boolean temporary) throws Exception
   {
      List result = null;
      
      Element element = null;

      try
      {
         InputStream stream = url.openStream();
         try
         {
            InputSource is = new InputSource(stream);
            is.setSystemId(url.toString());
            parser.setEntityResolver(new JBossEntityResolver());

            Document document = parser.parse(is);
            element = document.getDocumentElement();
         }
         finally
         {
            stream.close();
         }

         result = serviceController.install(element, classLoaderObjectName);

         deployed.addAll(result);
         if (temporary)
            tempDeployed.addAll(result);
      }
      catch (Exception e)
      {
         log.debug("Error deploying: " + url + ": " + e);
         throw e;
      }
      
      return result;
   }
}
