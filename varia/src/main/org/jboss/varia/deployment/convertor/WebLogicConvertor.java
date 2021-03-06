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
package org.jboss.varia.deployment.convertor;

import java.io.File;
import java.util.Properties;
import java.util.jar.JarFile;
import java.net.URL;

import javax.management.JMException;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.system.ServiceMBeanSupport;


/**
 * Converts WebLogic applications.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 57210 $
 *
 * <p><b>20020519 Andreas Schaefer:</b>
 * <ul>
 *    <li>Creation</li>
 * </ul>
 *
 * @jmx.mbean
 *    name="jboss.system:service=Convertor,type=WebLogic"
 *    extends="org.jboss.system.ServiceMBean"
 */
public class WebLogicConvertor
   extends ServiceMBeanSupport
   implements Convertor, WebLogicConvertorMBean
{
   // Attributes ---------------------------------------
   /** the deployer name this converter is registered with */
   private String deployerName;

   /** the version of xsl resources to apply */
   private String wlVersion;

   /** remove-table value */
   private String removeTable;

   /** datasource name that will be set up for converted bean */
   private String datasource;

   /** the datasource mapping for the datasource */
   private String datasourceMapping;

   /** xsl parameters used in transformations */
   private Properties xslParams;

   // WebLogicConverter implementation -----------------
   /**
    * @jmx.managed-attribute
    */
   public String getDeployer()
   {
      return deployerName;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDeployer( String name )
   {
      if( deployerName != null && name!= null && deployerName != name )
      {
         // Remove deployer
         try
         {
            server.invoke(
               new ObjectName( deployerName ),
               "removeConvertor",
               new Object[] { this },
               new String[] { this.getClass().getName() }
            );
         }
         catch( JMException jme ) { }
      }
      if( name != null ) deployerName = name;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getWlVersion()
   {
      return wlVersion;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setWlVersion( String wlVersion )
   {
      this.wlVersion = wlVersion;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getRemoveTable()
   {
      return removeTable;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setRemoveTable( String removeTable )
   {
      this.removeTable = removeTable;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getDatasource()
   {
      return datasource;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDatasource( String datasource )
   {
      this.datasource = datasource;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getDatasourceMapping()
   {
      return datasourceMapping;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDatasourceMapping( String datasourceMapping )
   {
      this.datasourceMapping = datasourceMapping;
   }

   // ServiceMBeanSupport overridding ------------------
   public void startService()
   {
      try
      {
         // init xsl params first
         initXslParams();

         server.invoke(
            new ObjectName(deployerName),
            "addConvertor",
            new Object[] { this },
            new String[] { Convertor.class.getName() }
         );
      }
      catch( JMException jme )
      {
         log.error( "Caught exception during startService()", jme );
      }
   }

   public void stopService()
   {
      if(deployerName != null)
      {
         // Remove deployer
         try {
            server.invoke(
               new ObjectName(deployerName),
               "removeConvertor",
               new Object[] { this },
               new String[] { this.getClass().getName() }
            );
         }
         catch( JMException jme )
         {
            // Ingore
         }
      }
   }

   // Converter implementation ----------------------------------------
   /**
    * Checks if the deployment can be converted to a JBoss deployment
    * by this converter.
    *
    * @param url The url of deployment to be converted
    * @return true if this converter is able to convert
    */
   public boolean accepts(URL url)
   {
      String stringUrl = url.toString();
      JarFile jarFile = null;
      boolean accepted = false;
      try
      {
         jarFile = new JarFile(url.getPath());
         accepted = (jarFile.getEntry("META-INF/weblogic-ejb-jar.xml" ) != null)
            && (stringUrl.endsWith(".wlar") || (stringUrl.endsWith(".wl")))
         || stringUrl.endsWith(".war.wl")
         || stringUrl.endsWith(".ear.wl") ;
         jarFile.close();
      }
      catch(Exception e)
      {
         log.debug("Couldn't create JarFile for " + url.getPath(), e);
         return false;
      }

      return accepted;
   }

   /**
    * Converts the necessary files to make the given deployment deployable
    * on JBoss
    *
    * @param di The deployment to be converted
    * @param path Path of the extracted deployment
    **/
   public void convert(DeploymentInfo di, File path)
      throws Exception
   {
      Properties xslParams = getXslParams();
      JarTransformer.transform(path, xslParams);
   }

   // Public -------------------------------------------
   /**
    * Returns the XSL parameters
    */
   public Properties getXslParams()
   {
      if(xslParams == null)
      {
         log.warn("xmlParams should have been initialized!");
         xslParams = initXslParams();
      }

      // xsl resources path
      xslParams.setProperty("resources_path", "resources/" + wlVersion + "/");

      // set remove-table
      xslParams.setProperty("remove-table", removeTable);

      // datasource
      xslParams.setProperty("datasource", datasource);

      // datasource-mapping
      xslParams.setProperty("datasource-mapping", datasourceMapping);

      return xslParams;
   }

   // Private -------------------------------------------------------
   /**
    * Initializes XSL parameters
    */
   private Properties initXslParams()
   {
      xslParams = new Properties();

      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      // path to standardjboss.xml
      URL url = cl.getResource( "standardjboss.xml" );
      if( url != null )
         xslParams.setProperty( "standardjboss",
            new File( url.getFile()).getAbsolutePath() );
      else log.debug( "standardjboss.xml not found." );

      // path to standardjbosscmp-jdbc.xml
      url = cl.getResource( "standardjbosscmp-jdbc.xml" );
      if( url != null )
         xslParams.setProperty( "standardjbosscmp-jdbc",
            new File( url.getFile()).getAbsolutePath() );
      else log.debug( "standardjbosscmp-jdbc.xml not found." );

      log.debug( "initialized xsl parameters: " + xslParams );

      return xslParams;
   }
}
