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
package org.jboss.ha.jndi;

import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;

import org.jboss.ha.framework.server.HARMIServerImpl;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jnp.interfaces.Naming;

/** Management Bean for HA-JNDI service for the legacy version that is coupled
 * to the RMI/JRMP protocol. The DetachedHANamingService should be used with
 * the approriate detached invoker service.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 65549 $
 */
public class HANamingService
   extends DetachedHANamingService
   implements HANamingServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   /** An optional custom client socket factory */
   protected RMIClientSocketFactory clientSocketFactory;
   /** An optional custom server socket factory */
   protected RMIServerSocketFactory serverSocketFactory;
   /** The class name of the optional custom client socket factory */
   protected String clientSocketFactoryName;
   /** The class name of the optional custom server socket factory */
   protected String serverSocketFactoryName;
   /** The class name of the load balancing policy */
   protected String loadBalancePolicy = RoundRobin.class.getName();
   /** The RMI port on which the Naming implementation will be exported. The
    default is 0 which means use any available port. */
   protected int rmiPort = 0;
   protected String replicantName = "HAJNDI";
   HARMIServerImpl rmiserver;

   // Public --------------------------------------------------------

   public HANamingService()
   {
      // for JMX
   }

   public void setRmiPort(int p)
   {
      rmiPort = p;
   }
   public int getRmiPort()
   {
      return rmiPort;
   }
   
   public String getClientSocketFactory()
   {
      return serverSocketFactoryName;
   }
   public void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.clientSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(clientSocketFactoryName);
      clientSocketFactory = (RMIClientSocketFactory) clazz.newInstance();
   }
   
   public String getServerSocketFactory()
   {
      return serverSocketFactoryName;
   }
   public void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.serverSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(serverSocketFactoryName);
      serverSocketFactory = (RMIServerSocketFactory) clazz.newInstance();
   }
   
   public String getLoadBalancePolicy()
   {
      return loadBalancePolicy;
   }
   public void setLoadBalancePolicy(String policyClassName)
   {
      loadBalancePolicy = policyClassName;
   }

   protected void stopService() throws Exception
   {
      super.stopService();
      // Unexport server
      log.debug("destroy ha rmiserver");
      this.rmiserver.destroy ();
   }

   protected Naming getNamingProxy() throws Exception
   {
      Class clazz;
      LoadBalancePolicy policy;
      
      rmiserver = new HARMIServerImpl(partition, replicantName, Naming.class,
         theServer, rmiPort, clientSocketFactory, serverSocketFactory, bindAddress);
         
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      clazz = cl.loadClass(loadBalancePolicy);
      policy = (LoadBalancePolicy)clazz.newInstance();
   
      Naming proxy = (Naming) rmiserver.createHAStub(policy);
      return proxy;
   }
}
