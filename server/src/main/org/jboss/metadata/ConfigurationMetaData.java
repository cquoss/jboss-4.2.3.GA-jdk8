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
package org.jboss.metadata;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.ObjectNameFactory;
import org.w3c.dom.Element;

/** The configuration information for an EJB container.
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *   @author <a href="mailto:christoph.jung@infor.de">Christoph G. Jung</a>
 *   @version $Revision: 63202 $
 */
public class ConfigurationMetaData extends MetaData
{
   // Constants -----------------------------------------------------
   public static final String CMP_2x_13 = "Standard CMP 2.x EntityBean";
   public static final String CMP_1x_13 = "Standard CMP EntityBean";
   public static final String BMP_13 = "Standard BMP EntityBean";
   public static final String STATELESS_13 = "Standard Stateless SessionBean";
   public static final String STATEFUL_13 = "Standard Stateful SessionBean";
   public static final String MESSAGE_DRIVEN_13 = "Standard Message Driven Bean";
   public static final String MESSAGE_INFLOW_DRIVEN = "Standard Message Inflow Driven Bean";

   public static final String CLUSTERED_CMP_2x_13 = "Clustered CMP 2.x EntityBean";
   public static final String CLUSTERED_CMP_1x_13 = "Clustered CMP EntityBean";
   public static final String CLUSTERED_BMP_13 = "Clustered BMP EntityBean";
   public static final String CLUSTERED_STATEFUL_13 = "Clustered Stateful SessionBean";
   public static final String CLUSTERED_STATELESS_13 = "Clustered Stateless SessionBean";

   public static final byte A_COMMIT_OPTION = 0;
   public static final byte B_COMMIT_OPTION = 1;
   public static final byte C_COMMIT_OPTION = 2;
   /** D_COMMIT_OPTION is a lazy load option. By default it synchronizes every 30 seconds */
   public static final byte D_COMMIT_OPTION = 3;
   public static final String[] commitOptionStrings = {"A", "B", "C", "D"};

   // Attributes ----------------------------------------------------
   private String name;
   private String instancePool;
   private String instanceCache;
   private String persistenceManager;
   private String webClassLoader = "org.jboss.web.WebClassLoader";
   private String lockClass = "org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock";
   private byte commitOption;
   private long optionDRefreshRate = 30000;
   private boolean callLogging;
   private boolean syncOnCommitOnly = false;
   /** if true, INSERT will be issued after ejbPostCreate */
   private boolean insertAfterEjbPostCreate = false;
   /** The container level security domain */
   private String securityDomain;
   /** The container invoker binding names */
   private String[] invokerNames;
   /** The InstancePool configuration */
   private Element containerPoolConf;
   /** The InstanceCache configuration */
   private Element containerCacheConf;
   /** The ejb container interceptor stack configuration */
   private Element containerInterceptorsConf;
   /** The JMX object names for container level dependencies */
   private Collection depends = new LinkedList();
   /** The cluster-config element info */
   private ClusterConfigMetaData clusterConfig = null;
   /** By the spec ejbStore must be called even for clean instances. But not everyone agrees. */
   private boolean ejbStoreForClean;
   /** Whether dirty instances that couldn't be evicted from the cache after cache.flush()
       should or should not (to prevent potential data inconsistency) be stored*/
   private boolean storeNotFlushed = true;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ConfigurationMetaData(String name)
   {
      this.name = name;
   }

   // Public --------------------------------------------------------

   public String getName()
   {
      return name;
   }

   public String getInstancePool()
   {
      return instancePool;
   }

   public String getInstanceCache()
   {
      return instanceCache;
   }

   public String getPersistenceManager()
   {
      return persistenceManager;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public String[] getInvokers()
   {
      return invokerNames;
   }

   public String getWebClassLoader()
   {
      return webClassLoader;
   }

   public String getLockClass()
   {
      return lockClass;
   }

   public Element getContainerPoolConf()
   {
      return containerPoolConf;
   }

   public Element getContainerCacheConf()
   {
      return containerCacheConf;
   }

   public String getDefaultInvokerName()
   {
      if (invokerNames.length == 0)
         throw new IllegalStateException("No invokers defined");
      return invokerNames[0];
   }
   
   public Element getContainerInterceptorsConf()
   {
      return containerInterceptorsConf;
   }

   public boolean getCallLogging()
   {
      return callLogging;
   }

   public boolean getSyncOnCommitOnly()
   {
      return syncOnCommitOnly;
   }

   public boolean isInsertAfterEjbPostCreate()
   {
      return insertAfterEjbPostCreate;
   }

   public boolean isEjbStoreForClean()
   {
      return this.ejbStoreForClean;
   }

   public boolean isStoreNotFlushed()
   {
      return storeNotFlushed;
   }

   public byte getCommitOption()
   {
      return commitOption;
   }

   public long getOptionDRefreshRate()
   {
      return optionDRefreshRate;
   }

   public ClusterConfigMetaData getClusterConfigMetaData()
   {
      return this.clusterConfig;
   }

   public Collection getDepends()
   {
      return depends;
   }

   public void importJbossXml(Element element) throws DeploymentException
   {

      // everything is optional to allow jboss.xml to modify part of a configuration
      // defined in standardjboss.xml

      // set call logging
      callLogging = Boolean.valueOf(getElementContent(getOptionalChild(element, "call-logging"), String.valueOf(callLogging))).booleanValue();

      // set synchronize on commit only
      syncOnCommitOnly = Boolean.valueOf(getElementContent(getOptionalChild(element, "sync-on-commit-only"), String.valueOf(syncOnCommitOnly))).booleanValue();

      // set insert-after-ejb-post-create
      insertAfterEjbPostCreate = Boolean.valueOf(getElementContent(getOptionalChild(element, "insert-after-ejb-post-create"), String.valueOf(insertAfterEjbPostCreate))).booleanValue();

      // ejbStoreForClean
      ejbStoreForClean = Boolean.valueOf(getElementContent(getOptionalChild(element, "call-ejb-store-on-clean"), String.valueOf(ejbStoreForClean))).booleanValue();

      // store-not-flushed
      storeNotFlushed = Boolean.valueOf(getElementContent(getOptionalChild(element, "store-not-flushed"), String.valueOf(storeNotFlushed))).booleanValue();
      
      // set the instance pool
      instancePool = getElementContent(getOptionalChild(element, "instance-pool"), instancePool);

      // set the instance cache
      instanceCache = getElementContent(getOptionalChild(element, "instance-cache"), instanceCache);

      // set the persistence manager
      persistenceManager = getElementContent(getOptionalChild(element, "persistence-manager"), persistenceManager);

      // set the web classloader
      webClassLoader = getElementContent(getOptionalChild(element, "web-class-loader"), webClassLoader);

      // set the lock class
      lockClass = getElementContent(getOptionalChild(element, "locking-policy"), lockClass);

      // set the security domain
      securityDomain = getElementContent(getOptionalChild(element, "security-domain"), securityDomain);
   
      //Get configured invokers
      List invokers = new java.util.ArrayList();
      
      for (Iterator invokerElements = getChildrenByTagName(element, "invoker-proxy-binding-name"); invokerElements.hasNext();)
      {
         Element invokerElement = (Element) invokerElements.next();
         String invokerName = getElementContent(invokerElement);
         invokers.add(invokerName);
      } // end of for ()
      
      // JBAS-4444, override invokers only if they are specified
      if (invokers.isEmpty() == false)
      {
         invokerNames=(String[]) invokers.toArray(new String[invokers.size()]);
      }
      
      // set the commit option
      String commit = getElementContent(getOptionalChild(element, "commit-option"), commitOptionToString(commitOption));

      commitOption = stringToCommitOption(commit);

      //get the refresh rate for option D
      String refresh = getElementContent(getOptionalChild(element, "optiond-refresh-rate"),
            Long.toString(optionDRefreshRate / 1000));
      optionDRefreshRate = stringToRefreshRate(refresh);

      // the classes which can understand the following are dynamically loaded during deployment :
      // We save the Elements for them to use later

      // The configuration for the container interceptors
      containerInterceptorsConf = getOptionalChild(element, "container-interceptors", containerInterceptorsConf);

      // configuration for instance pool
      containerPoolConf = getOptionalChild(element, "container-pool-conf", containerPoolConf);

      // configuration for instance cache
      containerCacheConf = getOptionalChild(element, "container-cache-conf", containerCacheConf);

      //Get depends object names
      for (Iterator dependsElements = getChildrenByTagName(element, "depends"); dependsElements.hasNext();)
      {
         Element dependsElement = (Element) dependsElements.next();
         String dependsName = getElementContent(dependsElement);
         depends.add(ObjectNameFactory.create(dependsName));
      } // end of for ()

      // Check for clustering configuration
      Element clusterConfigElement = getOptionalChild(element, "cluster-config");
      if (clusterConfigElement != null)
      {
         clusterConfig = new ClusterConfigMetaData();
         clusterConfig.importJbossXml(clusterConfigElement);
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   private static String commitOptionToString(byte commitOption)
         throws DeploymentException
   {
      try
      {
         return commitOptionStrings[commitOption];
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new DeploymentException("Invalid commit option: " + commitOption);
      }
   }

   private static byte stringToCommitOption(String commitOption)
         throws DeploymentException
   {
      for (byte i = 0; i < commitOptionStrings.length; ++i)
         if (commitOptionStrings[i].equals(commitOption))
            return i;

      throw new DeploymentException("Invalid commit option: '" + commitOption + "'");
   }

   /** Parse the refresh rate string into a long
    * @param refreshRate in seconds
    * @return refresh rate in milliseconds suitable for use in Thread.sleep
    * @throws DeploymentException on failure to parse refreshRate as a long
    */
   private static long stringToRefreshRate(String refreshRate)
         throws DeploymentException
   {
      try
      {
         long rate = Long.parseLong(refreshRate);
         // Convert from seconds to milliseconds
         rate *= 1000;
         return rate;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Invalid optiond-refresh-rate '"
               + refreshRate + "'. Should be a number");
      }

   }

   // Inner classes -------------------------------------------------
}
