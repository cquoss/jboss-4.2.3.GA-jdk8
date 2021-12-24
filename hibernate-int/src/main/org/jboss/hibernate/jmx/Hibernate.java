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
package org.jboss.hibernate.jmx;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.transaction.JBossTransactionManagerLookup;
import org.hibernate.transaction.JTATransactionFactory;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.hibernate.ListenerInjector;
import org.jboss.hibernate.cache.DeployedTreeCacheProvider;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.util.naming.Util;
import org.jboss.system.ServiceMBeanSupport;

/**
 * The {@link HibernateMBean} implementation.
 *
 * @version <tt>$Revision: 60195 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:gavin@hibernate.org">Gavin King</a>
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class Hibernate extends ServiceMBeanSupport implements HibernateMBean
{

   private static final Logger log = Logger.getLogger(Hibernate.class);

   public static final String SESSION_FACTORY_CREATE = "hibernate.sessionfactory.create";
   public static final String SESSION_FACTORY_DESTROY = "hibernate.sessionfactory.destroy";

   // Configuration attributes "passed through" to Hibernate
   private String datasourceName;
   private String dialect;
   private String defaultSchema;
   private String defaultCatalog;
   private Boolean sqlCommentsEnabled;
   private Integer maxFetchDepth;
   private Integer jdbcFetchSize;
   private Integer jdbcBatchSize;
   private Boolean batchVersionedDataEnabled;
   private Boolean jdbcScrollableResultSetEnabled;
   private Boolean getGeneratedKeysEnabled;
   private Boolean streamsForBinaryEnabled;
   private String hbm2ddlAuto;
   private String querySubstitutions;
   private Boolean showSqlEnabled;
   private String username;
   private String password;
   private Boolean secondLevelCacheEnabled = Boolean.TRUE;
   private Boolean queryCacheEnabled;
   private String cacheProviderClass;
   private ObjectName deployedTreeCacheObjectName;
   private Boolean minimalPutsEnabled;
   private String cacheRegionPrefix;
   private Boolean structuredCacheEntriesEnabled;
   private Boolean statGenerationEnabled;
   private Boolean reflectionOptimizationEnabled;

   // Configuration attributes used by the MBean
   private String sessionFactoryName;
   private String sessionFactoryInterceptor;
   private String listenerInjector;
   private URL harUrl;
   private boolean scanForMappingsEnabled = false;
   private HashSet archiveClasspathUrls = new HashSet();
   private HashSet directoryClasspathUrls = new HashSet();

   // Internal state
   private boolean dirty = false;
   private SessionFactory sessionFactory;
   private Date runningSince;
   private ObjectName hibernateStatisticsServiceName;

    protected void createService() throws Exception
    {
        log.trace( "forcing bytecode provider -> javassist" );
        // todo : really need a much better solution for this...
        System.setProperty( Environment.BYTECODE_PROVIDER, "javassist" );
    }

    /**
    * Configure Hibernate and bind the <tt>SessionFactory</tt> to JNDI.
    */
   public void startService() throws Exception
   {
      log.debug("Hibernate MBean starting; " + this);

      // be defensive...
      if (sessionFactory != null)
      {
         destroySessionFactory();
      }

      harUrl = determineHarUrl();

      if (harUrl != null)
      {
         log.trace("starting in har deployment mode");
         // we are part of a har deployment...
         if (scanForMappingsEnabled)
         {
            log.trace("scan for mappings was enabled");
            scanForMappings();
         }
      }
      else
      {
         // we are not contained within a har deployment...
         log.trace("starting in non-har deployment mode");
         scanForMappings();
      }

      buildSessionFactory();
   }

   /**
    * Close the <tt>SessionFactory</tt>.
    */
   public void stopService() throws Exception
   {
      destroySessionFactory();
      archiveClasspathUrls.clear();
      directoryClasspathUrls.clear();
   }

   private URL determineHarUrl() throws Exception
   {
      log.trace("Attempting to determine HarUrl...");
      DeploymentInfo deploymentInfo = getDeploymentInfo();
      if (deploymentInfo == null)
      {
         log.warn("Unable to locate deployment info [" + getServiceName() + "]");
         return null;
      }

      String urlStr = deploymentInfo.url.getFile();
      log.trace("checking our deployment unit [" + urlStr + "]");
      if (urlStr.endsWith(".har") || urlStr.endsWith(".har/"))
      {
         return deploymentInfo.url;
      }
      else
      {
         return null;
      }
   }	

   
	private final Configuration buildConfiguration() throws Exception{
	      log.debug("Building SessionFactory; " + this);

	      Configuration cfg = new Configuration();
	      cfg.getProperties().clear(); // avoid reading hibernate.properties and Sys-props

	      // Handle custom listeners....
	      ListenerInjector listenerInjector = generateListenerInjectorInstance();
	      if (listenerInjector != null)
	      {
	         listenerInjector.injectListeners(getServiceName(), cfg);
	      }

	      // Handle config settings....
	      transferSettings(cfg.getProperties());

	      // Handle mappings....
	      handleMappings(cfg);

	      // Handle interceptor....
	      Interceptor interceptorInstance = generateInterceptorInstance();
	      if (interceptorInstance != null)
	      {
	         cfg.setInterceptor(interceptorInstance);
	      }
	      
	      return cfg;
	}
   
   /**
    * Centralize the logic needed for starting/binding the SessionFactory.
    *
    * @throws Exception
    */
   private void buildSessionFactory() throws Exception
   {
	   
	   Configuration cfg = buildConfiguration();

      // Generate sf....
      sessionFactory = cfg.buildSessionFactory();

      try
      {
         // Handle stat-mbean creation/registration....
         if (sessionFactory.getStatistics() != null && sessionFactory.getStatistics().isStatisticsEnabled())
         {
			String serviceName = getServiceName().toString();
			if( serviceName.indexOf("type=service") != -1 )
			{
				serviceName = serviceName.replaceAll("type=service","type=stats");
			}
			else
			{
				serviceName = serviceName + ",type=stats";
			}
            hibernateStatisticsServiceName = new ObjectName( serviceName );
            StatisticsService hibernateStatisticsService = new StatisticsService();
            hibernateStatisticsService.setSessionFactory(sessionFactory);
            getServer().registerMBean(hibernateStatisticsService, hibernateStatisticsServiceName);
         }

         // Handle JNDI binding....
         bind();
      }
      catch (Exception e)
      {
         forceCleanup();
         throw e;
      }

      dirty = false;

      sendNotification(new Notification(SESSION_FACTORY_CREATE, getServiceName(), getNextNotificationSequenceNumber()));

      runningSince = new Date();

      log.info("SessionFactory successfully built and bound into JNDI [" + sessionFactoryName + "]");
   }

   /**
    * Centralize the logic needed to unbind/close a SessionFactory.
    *
    * @throws Exception
    */
   private void destroySessionFactory() throws Exception
   {
      if (sessionFactory != null)
      {
         // TODO : exact situations where we need to clear the 2nd-lvl cache?
         // (to allow clean release of the classloaders)
         // Most likely, if custom classes are directly cached (UserTypes); anything else?
         unbind();
         sessionFactory.close();
         sessionFactory = null;
         runningSince = null;

         if (hibernateStatisticsServiceName != null)
         {
            try
            {
               getServer().unregisterMBean(hibernateStatisticsServiceName);
            }
            catch (Throwable t)
            {
               // just log it
               log.warn("unable to cleanup statistics mbean", t);
            }
         }

         sendNotification(new Notification(SESSION_FACTORY_DESTROY, getServiceName(), getNextNotificationSequenceNumber()));
      }
   }

   private void handleMappings(Configuration cfg)
   {
	   //if scanForMappingsEnabled=true we dont want to add the xyz.har file
	   //as scanForMappingsEnabled=true has already added it to archiveClasspathUrls
      if (harUrl != null && !scanForMappingsEnabled )
      {
         final File file = new File(harUrl.getFile());
         if (file.isDirectory())
         {
            cfg.addDirectory(file);
         }
         else
         {
            cfg.addJar(file);
         }
      }

      Iterator itr = archiveClasspathUrls.iterator();
      while (itr.hasNext())
      {
         final File archive = (File)itr.next();
         log.debug("Passing archive [" + archive + "] to Hibernate Configration");
         cfg.addJar(archive);
      }

      itr = directoryClasspathUrls.iterator();
      while (itr.hasNext())
      {
         final File directory = (File)itr.next();
         log.debug("Passing directory [" + directory + "] to Hibernate Configration");
         cfg.addDirectory(directory);
      }
   }

   /**
    * Scan the current context's classloader to locate any potential sources of Hibernate mapping files.
    *
    * @throws DeploymentException
    */
   private void scanForMappings() throws DeploymentException
   {
      // Won't this cause problems if start() is called from say the console?
      // a way around is to locate our DeploymentInfo and grab its ucl attribute
      // for use here.
      URL[] urls = null;
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl instanceof RepositoryClassLoader)
      {
         urls = ((RepositoryClassLoader)cl).getClasspath();
      }
      else if (cl instanceof URLClassLoader)
      {
         urls = ((URLClassLoader)cl).getURLs();
      }
      else
      {
         throw new DeploymentException("Unable to determine urls from classloader [" + cl + "]");
      }

      // Search the urls for each of the classpath entries for any containing
      // hibernate mapping files or archives
      for (int i = 0; i < urls.length; i++)
      {
         final File entry = new File(urls[i].getFile());
         log.trace("checking classpath entry [" + entry + "]");
         if (!entry.exists())
         {
            continue;
         }

         if (!entry.isDirectory())
         {
            // This entry is not a directory, meaning it is a file of
            // some sort.  If it is an archive, we are interested in it...
            if (isArchive(entry))
            {
               log.trace("classpath entry was an archive file...");
               archiveClasspathUrls.add(entry);
            }
            else
            {
               log.trace("classpath entry was a non-archive file...");
            }
         }
         else
         {
            log.trace("classpath entry was a directory...");

            // we have a directory, add it to the list of directory classpath urls
            directoryClasspathUrls.add(entry);
         }
      }
   }

   /**
    * Simple helper method to determine whether a given File instance represents an archive which complies with the JAR
    * specification.
    *
    * @param file The file to test.
    *
    * @return True if the incoming file for certain represents an archive; false otherwise.
    */
   private boolean isArchive(File file)
   {
      try
      {
         new JarFile(file);
         return true;
      }
      catch (Throwable t)
      {
         return false;
      }
   }
   
	public void createSchema() throws Exception{
		new SchemaExport( buildConfiguration() ).create(false, true);
	}   
	
	public void dropSchema() throws Exception{
		new SchemaExport( buildConfiguration() ).drop(false, true);
	}


   
   /**
    * Transfer the state represented by our current attribute values into the given Properties instance, translating our
    * attributes into the appropriate Hibernate settings.
    *
    * @param settings The Properties instance to which to add our state.
    */
   private void transferSettings(Properties settings)
   {
      if (cacheProviderClass == null)
      {
         cacheProviderClass = "org.hibernate.cache.HashtableCacheProvider";
      }

      log.debug("Using JDBC batch size : " + jdbcBatchSize);

      setUnlessNull(settings, Environment.DATASOURCE, datasourceName);
      setUnlessNull(settings, Environment.DIALECT, dialect);
      setUnlessNull(settings, Environment.CACHE_PROVIDER, cacheProviderClass);
      setUnlessNull(settings, Environment.CACHE_REGION_PREFIX, cacheRegionPrefix);
      setUnlessNull(settings, Environment.USE_MINIMAL_PUTS, minimalPutsEnabled);
      setUnlessNull(settings, Environment.HBM2DDL_AUTO, hbm2ddlAuto);
      setUnlessNull(settings, Environment.DEFAULT_SCHEMA, defaultSchema);
      setUnlessNull(settings, Environment.STATEMENT_BATCH_SIZE, jdbcBatchSize);
      setUnlessNull(settings, Environment.USE_SQL_COMMENTS, sqlCommentsEnabled);

      setUnlessNull(settings, Environment.STATEMENT_FETCH_SIZE, jdbcFetchSize);
      setUnlessNull(settings, Environment.USE_SCROLLABLE_RESULTSET, jdbcScrollableResultSetEnabled);
      setUnlessNull(settings, Environment.USE_QUERY_CACHE, queryCacheEnabled);
      setUnlessNull(settings, Environment.USE_STRUCTURED_CACHE, structuredCacheEntriesEnabled);
      setUnlessNull(settings, Environment.QUERY_SUBSTITUTIONS, querySubstitutions);
      setUnlessNull(settings, Environment.MAX_FETCH_DEPTH, maxFetchDepth);
      setUnlessNull(settings, Environment.SHOW_SQL, showSqlEnabled);
      setUnlessNull(settings, Environment.USE_GET_GENERATED_KEYS, getGeneratedKeysEnabled);
      setUnlessNull(settings, Environment.USER, username);
      setUnlessNull(settings, Environment.PASS, password);
      setUnlessNull(settings, Environment.BATCH_VERSIONED_DATA, batchVersionedDataEnabled);
      setUnlessNull(settings, Environment.USE_STREAMS_FOR_BINARY, streamsForBinaryEnabled);
      setUnlessNull(settings, Environment.USE_REFLECTION_OPTIMIZER, reflectionOptimizationEnabled);
      setUnlessNull(settings, Environment.GENERATE_STATISTICS, statGenerationEnabled);

      setUnlessNull(settings, Environment.TRANSACTION_MANAGER_STRATEGY, JBossTransactionManagerLookup.class.getName());
      setUnlessNull(settings, Environment.TRANSACTION_STRATEGY, JTATransactionFactory.class.getName());

      if (deployedTreeCacheObjectName != null)
      {
         String objNameString = deployedTreeCacheObjectName.toString();
         if (objNameString != null && !"".equals(objNameString))
         {
            settings.setProperty(DeployedTreeCacheProvider.OBJECT_NAME_PROP, objNameString);
         }
      }

      settings.setProperty(Environment.FLUSH_BEFORE_COMPLETION, "true");
      settings.setProperty(Environment.AUTO_CLOSE_SESSION, "true");

      // This is really H3-version-specific:
      // in 3.0.3 and later, this should be the ConnectionReleaseMode enum;
      // in 3.0.2, this is a true/false setting;
      // in 3.0 -> 3.0.1, there is no such setting
      //
      // so we just set them both :)
      settings.setProperty("hibernate.connection.agressive_release", "true");
      settings.setProperty("hibernate.connection.release_mode", "after_statement");
   }

   /**
    * Simple helper method for transferring individual settings to a properties
    * instance only if the setting's value is not null.
    *
    * @param props The properties instance into which to transfer the setting
    * @param key The key under which to transfer the setting
    * @param value The value of the setting.
    */
   private void setUnlessNull(Properties props, String key, Object value)
   {
      if (value != null)
      {
         props.setProperty(key, value.toString());
      }
   }

   private ListenerInjector generateListenerInjectorInstance()
   {
      if (listenerInjector == null)
      {
         return null;
      }

      log.info("attempting to use listener injector [" + listenerInjector + "]");
      try
      {
         return (ListenerInjector)Thread.currentThread()
                 .getContextClassLoader()
                 .loadClass(listenerInjector)
                 .newInstance();
      }
      catch (Throwable t)
      {
         log.warn("Unable to generate specified listener injector", t);
      }

      return null;
   }

   private Interceptor generateInterceptorInstance()
   {
      if (sessionFactoryInterceptor == null)
      {
         return null;
      }

      log.info("Generating session factory interceptor instance [" + sessionFactoryInterceptor + "]");
      try
      {
         return (Interceptor)Thread.currentThread()
                 .getContextClassLoader()
                 .loadClass(sessionFactoryInterceptor)
                 .newInstance();
      }
      catch (Throwable t)
      {
         log.warn("Unable to generate session factory interceptor instance", t);
      }

      return null;
   }

   /**
    * Perform the steps necessary to bind the managed SessionFactory into JNDI.
    *
    * @throws HibernateException
    */
   private void bind() throws HibernateException
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         Util.bind(ctx, sessionFactoryName, sessionFactory);
      }
      catch (NamingException e)
      {
         throw new HibernateException("Unable to bind SessionFactory into JNDI", e);
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (Throwable ignore)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Perform the steps necessary to unbind the managed SessionFactory from JNDI.
    *
    * @throws HibernateException
    */
   private void unbind() throws HibernateException
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         Util.unbind(ctx, sessionFactoryName);
      }
      catch (NamingException e)
      {
         throw new HibernateException("Unable to unbind SessionFactory from JNDI", e);
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (Throwable ignore)
            {
               // ignore
            }
         }
      }
   }

   private void forceCleanup()
   {
      try
      {
         sessionFactory.close();
         sessionFactory = null;
      }
      catch (Throwable ignore)
      {
         // ignore
      }
   }

   public String toString()
   {
      return super.toString() + " [ServiceName=" + serviceName + ", JNDI=" + sessionFactoryName + "]";
   }


   // Managed operations ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public void rebuildSessionFactory() throws Exception
   {
      destroySessionFactory();
      buildSessionFactory();
   }


   // RO managed attributes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isSessionFactoryRunning()
   {
      return sessionFactory != null;
   }

   public String getVersion()
   {
      return Environment.VERSION;
   }

   public SessionFactory getInstance()
   {
      return sessionFactory;
   }

   public URL getHarUrl()
   {
      return harUrl;
   }

   public ObjectName getStatisticsServiceName()
   {
      return hibernateStatisticsServiceName;
   }

   public Date getRunningSince()
   {
      return runningSince;
   }


   // R/W managed attributes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public String getSessionFactoryName()
   {
      return sessionFactoryName;
   }

   public void setSessionFactoryName(String sessionFactoryName)
   {
      this.sessionFactoryName = sessionFactoryName;
      dirty = true;
   }

   public String getDatasourceName()
   {
      return datasourceName;
   }

   public void setDatasourceName(String datasourceName)
   {
      this.datasourceName = datasourceName;
      dirty = true;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
      dirty = true;
   }

   public void setPassword(String password)
   {
      this.password = password;
      dirty = true;
   }

   public String getDefaultSchema()
   {
      return defaultSchema;
   }

   public void setDefaultSchema(String defaultSchema)
   {
      this.defaultSchema = defaultSchema;
      dirty = true;
   }

   public String getDefaultCatalog()
   {
      return defaultCatalog;
   }

   public void setDefaultCatalog(String defaultCatalog)
   {
      this.defaultCatalog = defaultCatalog;
   }

   public String getHbm2ddlAuto()
   {
      return hbm2ddlAuto;
   }

   public void setHbm2ddlAuto(String hbm2ddlAuto)
   {
      this.hbm2ddlAuto = hbm2ddlAuto;
      dirty = true;
   }

   public String getDialect()
   {
      return dialect;
   }

   public void setDialect(String dialect)
   {
      this.dialect = dialect;
      dirty = true;
   }

   public Integer getMaxFetchDepth()
   {
      return maxFetchDepth;
   }

   public void setMaxFetchDepth(Integer maxFetchDepth)
   {
      this.maxFetchDepth = maxFetchDepth;
      dirty = true;
   }

   public Integer getJdbcBatchSize()
   {
      return jdbcBatchSize;
   }

   public void setJdbcBatchSize(Integer jdbcBatchSize)
   {
      this.jdbcBatchSize = jdbcBatchSize;
      dirty = true;
   }

   public Integer getJdbcFetchSize()
   {
      return jdbcFetchSize;
   }

   public void setJdbcFetchSize(Integer jdbcFetchSize)
   {
      this.jdbcFetchSize = jdbcFetchSize;
      dirty = true;
   }

   public Boolean getJdbcScrollableResultSetEnabled()
   {
      return jdbcScrollableResultSetEnabled;
   }

   public void setJdbcScrollableResultSetEnabled(Boolean jdbcScrollableResultSetEnabled)
   {
      this.jdbcScrollableResultSetEnabled = jdbcScrollableResultSetEnabled;
      dirty = true;
   }

   public Boolean getGetGeneratedKeysEnabled()
   {
      return getGeneratedKeysEnabled;
   }

   public void setGetGeneratedKeysEnabled(Boolean getGeneratedKeysEnabled)
   {
      this.getGeneratedKeysEnabled = getGeneratedKeysEnabled;
      dirty = true;
   }

   public String getQuerySubstitutions()
   {
      return querySubstitutions;
   }

   public void setQuerySubstitutions(String querySubstitutions)
   {
      this.querySubstitutions = querySubstitutions;
      dirty = true;
   }

   public Boolean getSecondLevelCacheEnabled()
   {
      return secondLevelCacheEnabled;
   }

   public void setSecondLevelCacheEnabled(Boolean secondLevelCacheEnabled)
   {
      this.secondLevelCacheEnabled = secondLevelCacheEnabled;
      dirty = true;
   }

   public Boolean getQueryCacheEnabled()
   {
      return queryCacheEnabled;
   }

   public void setQueryCacheEnabled(Boolean queryCacheEnabled)
   {
      this.queryCacheEnabled = queryCacheEnabled;
      dirty = true;
   }

   public String getCacheProviderClass()
   {
      return cacheProviderClass;
   }

   public void setCacheProviderClass(String cacheProviderClass)
   {
      this.cacheProviderClass = cacheProviderClass;
      dirty = true;
   }

   public String getCacheRegionPrefix()
   {
      return cacheRegionPrefix;
   }

   public void setCacheRegionPrefix(String cacheRegionPrefix)
   {
      this.cacheRegionPrefix = cacheRegionPrefix;
      dirty = true;
   }

   public Boolean getMinimalPutsEnabled()
   {
      return minimalPutsEnabled;
   }

   public void setMinimalPutsEnabled(Boolean minimalPutsEnabled)
   {
      this.minimalPutsEnabled = minimalPutsEnabled;
      dirty = true;
   }

   public Boolean getUseStructuredCacheEntriesEnabled()
   {
      return structuredCacheEntriesEnabled;
   }

   public void setUseStructuredCacheEntriesEnabled(Boolean structuredCacheEntriesEnabled)
   {
      this.structuredCacheEntriesEnabled = structuredCacheEntriesEnabled;
   }

   public Boolean getShowSqlEnabled()
   {
      return showSqlEnabled;
   }

   public void setShowSqlEnabled(Boolean showSqlEnabled)
   {
      this.showSqlEnabled = showSqlEnabled;
      dirty = true;
   }

   public Boolean getSqlCommentsEnabled()
   {
      return sqlCommentsEnabled;
   }

   public void setSqlCommentsEnabled(Boolean commentsEnabled)
   {
      this.sqlCommentsEnabled = commentsEnabled;
   }

   public String getSessionFactoryInterceptor()
   {
      return sessionFactoryInterceptor;
   }

   public void setSessionFactoryInterceptor(String sessionFactoryInterceptor)
   {
      this.sessionFactoryInterceptor = sessionFactoryInterceptor;
      dirty = true;
   }

   public String getListenerInjector()
   {
      return listenerInjector;
   }

   public void setListenerInjector(String listenerInjector)
   {
      this.listenerInjector = listenerInjector;
   }

   public ObjectName getDeployedTreeCacheObjectName()
   {
      return deployedTreeCacheObjectName;
   }

   public void setDeployedTreeCacheObjectName(ObjectName deployedTreeCacheObjectName)
   {
      this.deployedTreeCacheObjectName = deployedTreeCacheObjectName;
   }

   public Boolean getBatchVersionedDataEnabled()
   {
      return batchVersionedDataEnabled;
   }

   public void setBatchVersionedDataEnabled(Boolean batchVersionedDataEnabled)
   {
      this.batchVersionedDataEnabled = batchVersionedDataEnabled;
      this.dirty = true;
   }

   public Boolean getStreamsForBinaryEnabled()
   {
      return streamsForBinaryEnabled;
   }

   public void setStreamsForBinaryEnabled(Boolean streamsForBinaryEnabled)
   {
      this.streamsForBinaryEnabled = streamsForBinaryEnabled;
      this.dirty = true;
   }

   public Boolean getReflectionOptimizationEnabled()
   {
      return reflectionOptimizationEnabled;
   }

   public void setReflectionOptimizationEnabled(Boolean reflectionOptimizationEnabled)
   {
      this.reflectionOptimizationEnabled = reflectionOptimizationEnabled;
      this.dirty = true;
   }

   public Boolean getStatGenerationEnabled()
   {
      return statGenerationEnabled;
   }

   public void setStatGenerationEnabled(Boolean statGenerationEnabled)
   {
      this.statGenerationEnabled = statGenerationEnabled;
   }

   public boolean isScanForMappingsEnabled()
   {
      return scanForMappingsEnabled;
   }

   public void setScanForMappingsEnabled(boolean scanForMappingsEnabled)
   {
      this.scanForMappingsEnabled = scanForMappingsEnabled;
   }
}
