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
package org.jboss.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

/**
 * Derived implementation of JBossTestServices for cluster testing.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 65061 $
 * @see org.jboss.test.JBossTestServices
 */
public class JBossTestClusteredServices extends JBossTestServices
{
   protected ArrayList adaptors = null;
   protected ArrayList servers = null;
   protected ArrayList namingURLs = null;
   protected ArrayList namingURLsHA = null;
   protected ArrayList httpURLs = null;
   
   // Constructors --------------------------------------------------
   
   public JBossTestClusteredServices(String className)
   {
      super(className);
   }

   int getServerCount()
   {
      return servers.size();
   }

   MBeanServerConnection[] getAdaptors() throws Exception
   {
      init();
      MBeanServerConnection[] tmp = new MBeanServerConnection[adaptors.size()];
      adaptors.toArray(tmp);
      return tmp;
   }
   MBeanServerConnection getAdaptor(int index) throws Exception
   {
      init();
      MBeanServerConnection adaptor = (MBeanServerConnection) adaptors.get(index);
      return adaptor;
   }

   String[] getServers() throws Exception
   {
      init();
      String[] tmp = new String[servers.size()];
      servers.toArray(tmp);
      return tmp;
   }
   String getServer(int index) throws Exception
   {
      init();
      String server = (String) servers.get(index);
      return server;
   }

   /** Get the JNDI provider urls for the cluster nodes
    * @return
    * @throws Exception
    */ 
   String[] getNamingURLs() throws Exception
   {
      init();
      String[] tmp = new String[namingURLs.size()];
      namingURLs.toArray(tmp);
      return tmp;
   }
   String getNamingURL(int index) throws Exception
   {
      init();
      String server = (String) namingURLs.get(index);
      return server;
   }

   /** Get the JNDI provider urls for the cluster nodes
    * @return
    * @throws Exception
    */ 
   String[] getHANamingURLs() throws Exception
   {
      init();
      String[] tmp = new String[namingURLsHA.size()];
      namingURLsHA.toArray(tmp);
      return tmp;
   }
   String getHANamingURL(int index) throws Exception
   {
      init();
      String server = (String) namingURLsHA.get(index);
      return server;
   }

   /** Get the default web container urls for the cluster nodes
    * @return
    * @throws Exception
    */ 
   String[] getHttpURLs() throws Exception
   {
      init();
      String[] tmp = new String[httpURLs.size()];
      httpURLs.toArray(tmp);
      return tmp;
   }
   String getHttpURL(int index) throws Exception
   {
      init();
      String server = (String) httpURLs.get(index);
      return server;
   }

   /**
    * Deploy a package on the given server with the main deployer. The supplied 
    * name is interpreted as a url, or as a filename in jbosstest.deploy.lib or 
    * ../lib.
    *
    * @param server         server on which the package should be deployed
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   public void deploy(MBeanServerConnection server, String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
      {
         log.debug("Skipping deployment of: " + name);
         return;
      }

      URL deployURL = getDeployURL(name);
      log.debug("Deploying " + name + ", url=" + deployURL  + " to " + server);
      invoke(server,
            getDeployerName(),
            "deploy",
            new Object[]{deployURL},
            new String[]{"java.net.URL"});
   }
   
   public void redeploy(MBeanServerConnection server, String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
      {
         log.debug("Skipping redeployment of: " + name);
         return;
      }

      URL deployURL = getDeployURL(name);
      log.debug("Deploying " + name + ", url=" + deployURL);
      invoke(server,
         getDeployerName(),
         "redeploy",
         new Object[]{deployURL},
         new String[]{"java.net.URL"});
   }

   /**
    * Undeploy a package from the given server with the main deployer. 
    * The supplied name is interpreted as a url, or as a filename in 
    * jbosstest.deploy.lib or ../lib.
    *
    * @param server         server on which the package should be deployed
    * @param name           filename/url of package to undeploy.
    * @exception Exception  Description of Exception
    */
   public void undeploy(MBeanServerConnection server, String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
         return;
      URL deployURL = getDeployURL(name);
      log.debug("Undeploying " + name + ", url=" + deployURL);
      Object[] args = {deployURL};
      String[] sig = {"java.net.URL"};
      invoke(server, getDeployerName(), "undeploy", args, sig);
   }

   /**
    * Override to invoke the operation on all servers
    *
    * @param name
    * @param method
    * @param args
    * @param sig
    * @return
    * @throws Exception
    */
   protected Object invoke(ObjectName name, String method, Object[] args,
      String[] sig)
      throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();

      Object result = null;
      for (int i = 0; i < adaptors.length; i++)
      {
         MBeanServerConnection adaptor = adaptors[i];
         log.debug("Using MBeanServerConnection: "+adaptor);
         result = invoke(adaptor, name, method, args, sig);
      }

      return result;

   }

   public void init() throws Exception
   {
      if (initialContext == null)
      {
         initialContext = new InitialContext();
      }
      if (adaptors == null)
      {
         adaptors = new ArrayList();
         servers = new ArrayList();
         namingURLs = new ArrayList();
         namingURLsHA = new ArrayList();
         httpURLs = new ArrayList();
         String adaptorName = System.getProperty("jbosstest.server.name");
         if (adaptorName == null)
            adaptorName = "jmx/invoker/RMIAdaptor";

         Hashtable env = new Hashtable();
         env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
         env.put("java.naming.factory.url.pkgs", "org.jnp.interfaces");

         // Look for jbosstest.cluster.nodeN properties for the server names
         String node = "jbosstest.cluster.node";
         int count = 0;
         while (count < 10)
         {
            String prop = node + count;
            String host = System.getProperty(prop);
            count++;
            if (host == null)
               break;
            log.info(prop + " = " + host);
            servers.add(host);
            // See if there is a jbosstest.cluster.nodeN.jndi.url
            String urlProp = prop + ".jndi.url";
            String urlDefault = "jnp://" + host + ":1099";
            String urlValue = System.getProperty(urlProp, urlDefault);
            log.debug("JNDI Url for node=" + count + " is:" + urlValue);
            namingURLs.add(urlValue);
            env.put("java.naming.provider.url", urlValue);
            // Lookup the adaptor
            InitialContext tmpCtx = new InitialContext(env);
            MBeanServerConnection adaptor = (MBeanServerConnection) tmpCtx.lookup(adaptorName);
            adaptors.add(adaptor);

            // See if there is a jbosstest.cluster.nodeN.hajndi.url
            urlProp = prop + ".hajndi.url";
            urlDefault = "jnp://" + host + ":1100";
            urlValue = System.getProperty(urlProp, urlDefault);
            log.debug("HA-JNDI Url for node=" + count + " is:" + urlValue);
            namingURLsHA.add(urlValue);
            
            // See if there is a jbosstest.cluster.nodeN.http.url
            urlProp = prop + ".http.url";
            urlDefault = "http://" + host + ":8080";
            urlValue = System.getProperty(urlProp, urlDefault);
            log.debug("Http Url for node=" + count + " is:" + urlValue);
            httpURLs.add(urlValue);
         }

         if (adaptors.size() == 0)
            throw new IllegalStateException("No jbosstest.cluster.node values found");
      }
   }

   /**
    * This method gives overriding testcases to set the cluster servernames
    */
   public void setServerNames(String[] snames)
   {
      if (snames == null) return;
      for (int i = 0; i < snames.length; i++)
      {
         servers.add(snames[i]);
      }
   }

}
