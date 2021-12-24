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
package org.jboss.services.binding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.system.server.ServerConfig;
import org.jboss.metadata.MetaData;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An implementation of the ServicesConfigDelegate
 * that transforms an xml file used by a service.
 *
 * It retrieves the file location from the service, 
 * tranforms the file and saves it on a temporary location,
 * which will be then applied to the service.
 *  
 * It excpects a delegate-config element of the following form: 
 *  
 * <delegate-config>
 *   <xslt-config configName="jmx_filename_attribute"><![CDATA[
 *     XSL document contents...
 *   ]]></xslt-config>
 * </delegate-config>
 * 
 * The configName attribute specifies the JMX attribute,
 * which defines the XML file to be transformed.
 *
 * @author wonne.keysers@realsoftware.be
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57210 $
 */
public class XSLTFileDelegate implements ServicesConfigDelegate
{
   private static Logger log = Logger.getLogger(XSLTFileDelegate.class);

   /** Transform the file specified in the given config, 
    * transform it, temporarily save the result and apply it onto the service
    * specified in the config using JMX via the given server.
    @param config, the service name and its config bindings
    @param server, the JMX server to use to apply the config
    */
   public void applyConfig(ServiceConfig config, MBeanServer server)
      throws Exception
   {
      Element delegateConfig =
         (Element) config.getServiceConfigDelegateConfig();
      if (delegateConfig == null)
      {
         throw new IllegalArgumentException("ServiceConfig.ServiceConfigDelegateConfig is null");
      }

      Element xslConfigElement =
         (Element) delegateConfig.getElementsByTagName("xslt-config").item(0);
      if (xslConfigElement == null)
      {
         throw new IllegalArgumentException("No valid xslt config found");
      }

      String configName = xslConfigElement.getAttribute("configName");
      log.debug("configName = " + configName);

      if (configName.length() == 0)
      {
         throw new IllegalArgumentException("No valid configName attribute found");
      }

      ObjectName serviceName = new ObjectName(config.getServiceName());
      log.debug("serviceName = " + serviceName);

      String oldValue = (String) server.getAttribute(serviceName, configName);
      log.debug("oldValue = " + oldValue);

      String tmpName = System.getProperty(ServerConfig.SERVER_TEMP_DIR);
      File tempDirectory = new File(tmpName);
      File targetFile = File.createTempFile("server", ".xml", tempDirectory);
      targetFile.deleteOnExit();
      log.debug("targetFile: " + targetFile.getCanonicalPath());

      ServiceBinding[] bindings = config.getBindings();
      if (bindings == null || bindings.length == 0)
      {
         throw new IllegalArgumentException("No port binding specified");
      }

      int port = bindings[0].getPort();
      String host = bindings[0].getHostName();

      try
      {
         String xslText = xslConfigElement.getFirstChild().getNodeValue();
         log.trace("XSL text:" + xslText);
         Source xslSource = new StreamSource(new StringReader(xslText));

         Source xmlSource =
            new StreamSource(getClass().getClassLoader().getResourceAsStream(oldValue));

         Result xmlResult =
            new StreamResult(new FileOutputStream(targetFile));

         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer(xslSource);

         transformer.setParameter("port", new Integer(port));
         log.debug("set port parameter to:"+port);
         if (host != null)
         {
            transformer.setParameter("host", host);
            log.debug("set host parameter to:"+host);
         }

         // Check for any arbitrary attributes
         NodeList attributes = delegateConfig.getElementsByTagName("xslt-param");
         // xslt-param are transform parameters
         for(int a = 0; a < attributes.getLength(); a ++)
         {
            Element attr = (Element) attributes.item(a);
            String name = attr.getAttribute("name");
            if( name.length() == 0 )
               throw new IllegalArgumentException("attribute element #"
                            +a+" has no name attribute");
            String attrExp = MetaData.getElementContent(attr);
            String attrValue = StringPropertyReplacer.replaceProperties(attrExp);
            transformer.setParameter(name, attrValue);

            log.debug("set "+name+" parameter to:"+attrValue);
         }

         transformer.transform(xmlSource, xmlResult);

         Attribute mbeanConfigAttr =
            new Attribute(configName, targetFile.getCanonicalPath());

         server.setAttribute(serviceName, mbeanConfigAttr);
      }
      catch (Exception ex)
      {
         log.error("Error while transforming xml", ex);
      }

   }

}
