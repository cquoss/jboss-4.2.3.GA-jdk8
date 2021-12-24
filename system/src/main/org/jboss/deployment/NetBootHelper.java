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
package org.jboss.deployment;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.system.server.ServerConfig;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Static helper methods for NetBoot features
 *
 * @see org.jboss.deployment.NetBootFile
 * @see org.jboss.deployment.SARDeployer
 * @see org.jboss.deployment.scanner.HttpURLDeploymentScanner
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57205 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>7 novembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class NetBootHelper
{
   
   // Constants -----------------------------------------------------
   
   public final static String DEFAULT_NETBOOT_LISTING_URL = "jboss.netboot.listing.url";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   protected static org.jboss.logging.Logger log = 
      org.jboss.logging.Logger.getLogger(NetBootHelper.class);
   protected static boolean traceEnabled = log.isTraceEnabled ();
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   public static String buildDownloadUrlForFile (String baseUrl, String directory, String filename)       
   {
      String part = baseUrl;
      if (part.charAt (part.length ()-1) != '/')
         part=part + "/";
      
      part = part + directory;
      if (part.charAt (part.length ()-1) != '/')
         part=part + "/";
      
      part = part + filename;
      
      return part;
   }
   
   public static String getDefaultDownloadUrl ()
   {
      return System.getProperty(ServerConfig.SERVER_HOME_URL);
   }
   
   public static String getDefaultListUrl ()
      throws IllegalStateException
   {
      String defaultUrl = System.getProperty (NetBootHelper.DEFAULT_NETBOOT_LISTING_URL);
      if (defaultUrl == null)
      {
         // No default listing URL is provided!
         // We have a defaulting mode that can be used: if, on the server side, 
         // the JBossWeb NetBoot war is used, we can automatically use it
         // The jboss.netboot.use.jbossweb System property can be set to false to
         // disable this automatic behaviour
         //
         String autofallback = System.getProperty ("jboss.netboot.use.jbossweb");
         if (autofallback == null || !autofallback.equalsIgnoreCase ("false"))
         {
            if (traceEnabled) log.trace ("jboss.netboot.use.jbossweb not defined but fallback activated...");
            defaultUrl = System.getProperty(ServerConfig.HOME_URL);
            int cropSize = defaultUrl.length ();
            if (defaultUrl.endsWith ("/files"))
               cropSize-= "/files".length () - 1;
            else if (defaultUrl.endsWith ("/files/"))
               cropSize-= "/files/".length () - 1;
            else
               throw new IllegalStateException
                     ("No wildcard permitted in non-file URL deployment when jboss.netboot.listing.url not defined. " + 
                      "You must either use the JBossWeb NetBoot WAR extension, specify individual jars" +
                      ", use the URL:* notation or specify the jboss.netboot.listing.url system property");
            defaultUrl = System.getProperty(ServerConfig.HOME_URL).substring (0, cropSize) + "List?";               

            if (traceEnabled) log.trace ("...using: " + defaultUrl);
         }
         else
         {
            if (traceEnabled) log.trace ("jboss.netboot.use.jbossweb not defined and fallback explicitly deactivated");
            throw new IllegalStateException
                  ("No wildcard permitted in non-file URL deployment when jboss.netboot.listing.url not defined. " + 
                   "You must either specify individual jars" +
                   ", use the URL:* notation or specify the jboss.netboot.listing.url system property");
         }
      }
      return StringPropertyReplacer.replaceProperties (defaultUrl);
   }                   
   
   public static String buildListUrlForFolder (String baseUrl, String directory)
      throws IllegalStateException, UnsupportedEncodingException
   {
      String listUrl = null;
      
      if (baseUrl == null || "".equals (baseUrl))
      {
         // If not supplied, we provide the default URL
         //
         listUrl = getDefaultListUrl ();
      }
      else
      {
         listUrl = baseUrl;
      }
      
      return listUrl + "dir=" + java.net.URLEncoder.encode (directory, "UTF-8");
   }
   
   public static NetBootFile[] listAllFromDirectory (String lister)
      throws Exception
   {
      return listAllFromDirectory (lister, true, true);
   }
   
   public static NetBootFile[] listFilesFromDirectory (String lister)
      throws Exception
   {
      return listAllFromDirectory (lister, false, true);
   }
   
   public static NetBootFile[] listDirectoriesFromDirectory (String lister)
      throws Exception
   {
      return listAllFromDirectory (lister, true, false);
   }
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    *
    *   The is expected document we should receive in result:
    *
    *           <!ELEMENT directory  (file*, sub-directory*)>
    *           <!ELEMENT file  (name, modified, size)>
    *           <!ELEMENT sub-directory  (name, modified)>
    *           <!ELEMENT name (#PCDATA)>
    *           <!ELEMENT modified (#PCDATA)>
    *           <!ELEMENT size (#PCDATA)>
    *
    *       In this case we are only interested in file, not directories
    *
    */
   protected static NetBootFile[] listAllFromDirectory (String lister, boolean doDir, boolean doFiles)
      throws Exception
   {
      if (traceEnabled) log.trace ("Getting directory listing from: " + lister);
      
      ArrayList result = new ArrayList();
      // We now download the XML description from the remote URL
      //
      InputStream stream = new URL (lister).openStream ();
      InputSource is = new InputSource(stream);   

      DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      org.w3c.dom.Document doc = parser.parse(is);
      
      if (doFiles)
      {
         Iterator dirContentIter = getChildrenByTagName(doc.getDocumentElement(), "file");
         while (dirContentIter.hasNext ())
         {
            Element item = (Element)dirContentIter.next();
            String name = getUniqueChild (item, "name").getFirstChild().getNodeValue();
            long lastModified = Long.parseLong(getUniqueChild (item, "modified").getFirstChild().getNodeValue());
            long size = Long.parseLong(getUniqueChild (item, "size").getFirstChild().getNodeValue());

            result.add (new NetBootFile (name, size, lastModified, false, lister));
         }
      }

      if (doFiles)
      {
         Iterator dirContentIter = getChildrenByTagName(doc.getDocumentElement(), "directory");
         while (dirContentIter.hasNext ())
         {
            Element item = (Element)dirContentIter.next();
            String name = getUniqueChild (item, "name").getFirstChild().getNodeValue();
            long lastModified = Long.parseLong(getUniqueChild (item, "modified").getFirstChild().getNodeValue());
            long size = Long.parseLong(getUniqueChild (item, "size").getFirstChild().getNodeValue());

            result.add (new NetBootFile (name, size, lastModified, true, lister));
         }
      }
      
      return (NetBootFile[]) result.toArray (new NetBootFile[] {});
   }
   
   /**
    * from org.jboss.metadata.MetaData which is not in the System module 
    * (which is understandable)
    */
   protected static Element getUniqueChild(Element element, String tagName)
      throws DeploymentException
   {
      Iterator goodChildren = getChildrenByTagName(element, tagName);

      if (goodChildren != null && goodChildren.hasNext()) {
         Element child = (Element)goodChildren.next();
         if (goodChildren.hasNext()) {
            throw new DeploymentException
               ("expected only one " + tagName + " tag");
         }
         return child;
      } else {
         throw new DeploymentException
            ("expected one " + tagName + " tag");
      }
   }

   protected static Iterator getChildrenByTagName(Element element,
                                               String tagName)
   {
      if (element == null) return null;
      // getElementsByTagName gives the corresponding elements in the whole 
      // descendance. We want only children

      NodeList children = element.getChildNodes();
      ArrayList goodChildren = new ArrayList();
      for (int i=0; i<children.getLength(); i++) {
         Node currentChild = children.item(i);
         if (currentChild.getNodeType() == Node.ELEMENT_NODE && 
             ((Element)currentChild).getTagName().equals(tagName)) {
            goodChildren.add((Element)currentChild);
         }
      }
      return goodChildren.iterator();
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
