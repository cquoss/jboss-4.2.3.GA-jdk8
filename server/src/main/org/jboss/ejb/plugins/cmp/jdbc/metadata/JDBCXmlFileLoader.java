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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.net.URL;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.w3c.dom.Element;

/**
 * Immutable class which loads the JDBC application meta data from the jbosscmp-jdbc.xml files.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 57209 $
 */
public final class JDBCXmlFileLoader {
   private final ApplicationMetaData application;
   private final ClassLoader classLoader;
   private final ClassLoader localClassLoader;
   private final Logger log;
   
   /**
    * Constructs a JDBC XML file loader, which loads the JDBC application meta data from
    * the jbosscmp-xml files.
    *
    * @param application the application meta data loaded from the ejb-jar.xml file
    * @param classLoader the classLoader used to load all classes in the application
    * @param localClassLoader the classLoader used to load the jbosscmp-jdbc.xml file from the jar
    * @param log the log for this application
    */
   public JDBCXmlFileLoader(ApplicationMetaData application, ClassLoader classLoader, ClassLoader localClassLoader, Logger log) {
      this.application = application;
      this.classLoader = classLoader;
      this.localClassLoader = localClassLoader;
      this.log = log;
   }

   /**
    * Loads the application meta data from the jbosscmp-jdbc.xml file
    *
    * @return the jdbc application meta data loaded from the jbosscmp-jdbc.xml files
    */
   public JDBCApplicationMetaData load() throws DeploymentException {
      JDBCApplicationMetaData jamd = new JDBCApplicationMetaData(application, classLoader);
      
      // Load standardjbosscmp-jdbc.xml from the default classLoader
      // we always load defaults first
      URL stdJDBCUrl = classLoader.getResource("standardjbosscmp-jdbc.xml");   
      if(stdJDBCUrl == null) {
         throw new DeploymentException("No standardjbosscmp-jdbc.xml found");
      }

      boolean debug = log.isDebugEnabled();
      if (debug)
         log.debug("Loading standardjbosscmp-jdbc.xml : " + stdJDBCUrl.toString());
      Element stdJDBCElement = XmlFileLoader.getDocument(stdJDBCUrl, true).getDocumentElement();

      // first create the metadata
      jamd = new JDBCApplicationMetaData(stdJDBCElement, jamd);

      // Load jbosscmp-jdbc.xml if provided
      URL jdbcUrl = localClassLoader.getResource("META-INF/jbosscmp-jdbc.xml");
      if(jdbcUrl != null) {
         if (debug)
            log.debug(jdbcUrl.toString() + " found. Overriding defaults");
         Element jdbcElement = XmlFileLoader.getDocument(jdbcUrl, true).getDocumentElement();
         jamd = new JDBCApplicationMetaData(jdbcElement, jamd);
      }

      return jamd;
   }
}
