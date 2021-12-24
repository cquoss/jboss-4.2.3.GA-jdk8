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
package org.jboss.test.wsrp;
 
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.webservice.WebserviceTestBase;
import org.xml.sax.InputSource;

//$Id: WSRPWSDLToEndpointTestCase.java 62137 2007-04-05 16:07:01Z fnasser@redhat.com $

/**
 *  Given a WSRP wsdl, derive the endpoint urls for the various
 *  wsrp services
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 5, 2006
 *  @version $Revision: 62137 $
 */
public class WSRPWSDLToEndpointTestCase extends WebserviceTestBase
{ 
   private static Logger log = Logger.getLogger(WSRPWSDLToEndpointTestCase.class); 

   /**
    * deploy the test archives
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(WSRPWSDLToEndpointTestCase.class, "wsrp.war");
   }
   
   
   public WSRPWSDLToEndpointTestCase(String name)
   {
      super(name); 
   }
   
   public void testEndpointGrab() throws Exception
   {
      String wsdl_url = "http://" + this.getServerHost() + ":8080/wsrp/MarkupService?wsdl";
      Definition def = getWSDLDefinition(new URL(wsdl_url));
      Service serve = def.getService(new QName("urn:oasis:names:tc:wsrp:v1:wsdl","WSRPService"));
      assertMarkupService(serve);
      assertPortletManagementService(serve);
      assertServiceDescriptionService(serve);
      assertRegistrationService(serve);
   }  
   
   //Requires wsdl4j
   private Definition getWSDLDefinition(URL url) throws Exception
   {   
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      javax.wsdl.xml.WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
      return wsdlReader.readWSDL(new WSDLLocatorImpl(url)); 
   }
   
   private void assertMarkupService(Service serve)
   {
      Port markupPort = serve.getPort("WSRPMarkupService");
      if(markupPort == null)
         markupPort = serve.getPort("WSRPBaseService");
      assertNotNull("MarkupService Port is not null", markupPort);
      String markupEndpoint = getLocation(markupPort);
      assertTrue("MarkupService endpoint is not null", 
            markupEndpoint != null && markupEndpoint.indexOf("MarkupService") > 0);
   }
   
   private void assertPortletManagementService(Service serve)
   {
      Port pmPort = serve.getPort("WSRPPortletManagementService"); 
      assertNotNull("WSRPPortletManagementService Port is not null", pmPort);
      String pmEndpoint = getLocation(pmPort);
      assertTrue("WSRPPortletManagementService endpoint is not null", 
            pmEndpoint != null && pmEndpoint.indexOf("PortletManagementService") > 0);
   }
   
   private void assertServiceDescriptionService(Service serve)
   {
      Port sdPort = serve.getPort("WSRPServiceDescriptionService"); 
      assertNotNull("WSRPServiceDescriptionService Port is not null", sdPort);
      String sdEndpoint = getLocation(sdPort);
      assertTrue("WSRPServiceDescriptionService endpoint is not null", 
            sdEndpoint != null && sdEndpoint.indexOf("ServiceDescriptionService") > 0);
   }
   
   private void assertRegistrationService(Service serve)
   {
      Port rsPort = serve.getPort("WSRPRegistrationService"); 
      assertNotNull("WSRPRegistrationService Port is not null", rsPort);
      String rsEndpoint = getLocation(rsPort);
      assertTrue("WSRPRegistrationService endpoint is not null", 
            rsEndpoint != null && rsEndpoint.indexOf("RegistrationService") > 0);
   }
   
   /* A WSDLLocator that can handle wsdl imports
    */
    public static class WSDLLocatorImpl implements WSDLLocator
    {
       private URL wsdlURL;
       private String latestImportURI;

       public WSDLLocatorImpl(URL wsdlFile)
       {
          if (wsdlFile == null)
             throw new IllegalArgumentException("WSDL file argument cannot be null");

          this.wsdlURL = wsdlFile;
       }

       public InputSource getBaseInputSource()
       {
          log.trace("getBaseInputSource [wsdlUrl=" + wsdlURL + "]");
          try
          {
             InputStream is = wsdlURL.openStream();
             if (is == null)
                throw new IllegalArgumentException("Cannot obtain wsdl from [" + wsdlURL + "]");

             return new InputSource(is);
          }
          catch (IOException e)
          {
             throw new RuntimeException("Cannot access wsdl from [" + wsdlURL + "], " + e.getMessage());
          }
       }

       public String getBaseURI()
       {
          return wsdlURL.toExternalForm();
       }

       public InputSource getImportInputSource(String parent, String resource)
       {
          log.trace("getImportInputSource [parent=" + parent + ",resource=" + resource + "]");

          URL parentURL = null;
          try
          {
             parentURL = new URL(parent);
          }
          catch (MalformedURLException e)
          {
             log.error("Not a valid URL: " + parent);
             return null;
          }

          String wsdlImport = null;
          String external = parentURL.toExternalForm();

          // An external URL
          if (resource.startsWith("http://") || resource.startsWith("https://"))
          {
             wsdlImport = resource;
          }

          // Absolute path
          else if (resource.startsWith("/"))
          {
             String beforePath = external.substring(0, external.indexOf(parentURL.getPath()));
             wsdlImport = beforePath + resource;
          }

          // A relative path
          else
          {
             String parentDir = external.substring(0, external.lastIndexOf("/"));

             // remove references to current dir
             while (resource.startsWith("./"))
                resource = resource.substring(2);

             // remove references to parentdir
             while (resource.startsWith("../"))
             {
                parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"));
                resource = resource.substring(3);
             }

             wsdlImport = parentDir + "/" + resource;
          }

          try
          {
             log.trace("Resolved to: " + wsdlImport);
             InputStream is = new URL(wsdlImport).openStream();
             if (is == null)
                throw new IllegalArgumentException("Cannot import wsdl from [" + wsdlImport + "]");

             latestImportURI = wsdlImport;
             return new InputSource(is);
          }
          catch (IOException e)
          {
             throw new RuntimeException("Cannot access imported wsdl [" + wsdlImport + "], " + e.getMessage());
          }
       }

       public String getLatestImportURI()
       {
          return latestImportURI;
       }

       public void close()
       {
       }
    }
    
    private String getLocation(Port port)
    {
       String loc = "";
       Iterator iter = port.getExtensibilityElements().iterator();
       while(iter.hasNext())
       {
          ExtensibilityElement ext = (ExtensibilityElement)iter.next(); 
          if(ext instanceof SOAPAddress)
          {
             SOAPAddress add = (SOAPAddress)ext;
             loc = add.getLocationURI();
          } 
       }
       return loc;
    }
}
