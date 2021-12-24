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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.MarshalledObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.net.ServerSocketFactory;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigUtil;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BasicThreadPoolMBean;
import org.jboss.util.threadpool.ThreadPool;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;

/**
 * Management Bean for the protocol independent HA-JNDI service. This allows the
 * naming service transport layer to be provided by a detached invoker service
 * like JRMPInvokerHA + ProxyFactoryHA.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57188 $
 */
public class DetachedHANamingService
   extends ServiceMBeanSupport
   implements DetachedHANamingServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   /**
    * The jnp server socket through which the HAJNDI stub is vended
    */
   protected ServerSocket bootstrapSocket;

   /**
    * The Naming interface server implementation
    */
   protected HAJNDI theServer;
   /**
    * The mapping from the long method hash to the Naming Method
    */
   protected Map marshalledInvocationMapping;
   /**
    * The protocol stub returned to clients by the bootstrap lookup
    */
   protected Naming stub;
   /**
    * The HAPartition used for the state transfer service
    */
   protected HAPartition partition;
   /**
    * The ClusterPartition with which we are associated.
    */
   protected ClusterPartitionMBean clusterPartition;
   /**
    * The partition name used to lookup the HAPartition binding
    */
   protected String partitionName = ServerConfigUtil.getDefaultPartitionName();
   /**
    * The proxy factory service that generates the Naming stub
    */
   private ObjectName proxyFactory;

   /**
    * The interface to bind to. This is useful for multi-homed hosts that want
    * control over which interfaces accept connections.
    */
   protected InetAddress bindAddress;
   /**
    * The bootstrapSocket listen queue depth
    */
   protected int backlog = 50;
   /**
    * The jnp protocol listening port. The default is 1100, the same as the RMI
    * registry default port.
    */
   protected int port = 1100;

   /**
    * The autodiscovery multicast group
    */
   protected String adGroupAddress = NamingContext.DEFAULT_DISCOVERY_GROUP_ADDRESS;
   /**
    * The autodiscovery port
    */
   protected int adGroupPort = NamingContext.DEFAULT_DISCOVERY_GROUP_PORT;
   /**
    * The interface to bind the Multicast socket for autodiscovery to
    */
   protected InetAddress discoveryBindAddress;
   /** The runable task for discovery request packets */
   protected AutomaticDiscovery autoDiscovery = null;
   /** A flag indicating if autodiscovery should be disabled */
   protected boolean discoveryDisabled = false;
   /** The autodiscovery Multicast reply TTL */
   protected int autoDiscoveryTTL = 16;
   /**
    * An optional custom server socket factory for the bootstrap lookup
    */
   protected ServerSocketFactory jnpServerSocketFactory;
   /**
    * The class name of the optional custom JNP server socket factory
    */
   protected String jnpServerSocketFactoryName;

   /**
    * The thread pool used to handle jnp stub lookup requests
    */
   protected ThreadPool lookupPool;

   // Public --------------------------------------------------------

   public DetachedHANamingService()
   {
      // for JMX
   }

   /**
    * Expose the Naming service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the Naming interface
    * @jmx:managed-attribute
    */
   public Map getMethodMap()
   {
      return marshalledInvocationMapping;
   }

   public ClusterPartitionMBean getClusterPartition()
   {
      return clusterPartition;
   }

   public void setClusterPartition(ClusterPartitionMBean clusterPartition)
   {
      this.clusterPartition = clusterPartition;
   }

   public String getPartitionName()
   {
      return partitionName;
   }

   public void setPartitionName(final String partitionName)
   {
      this.partitionName = partitionName;
   }   

   public ObjectName getProxyFactoryObjectName()
   {
      return proxyFactory;
   }

   public void setProxyFactoryObjectName(ObjectName proxyFactory)
   {
      this.proxyFactory = proxyFactory;
   }

   public void setPort(int p)
   {
      port = p;
   }

   public int getPort()
   {
      return port;
   }

   public String getBindAddress()
   {
      String address = null;
      if (bindAddress != null)
         address = bindAddress.getHostAddress();
      return address;
   }

   public void setBindAddress(String host) throws java.net.UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }

   public int getBacklog()
   {
      return backlog;
   }

   public void setBacklog(int backlog)
   {
      if (backlog <= 0)
         backlog = 50;
      this.backlog = backlog;
   }

   public void setDiscoveryDisabled(boolean disable)
   {
      this.discoveryDisabled = disable;
   }

   public boolean getDiscoveryDisabled()
   {
      return this.discoveryDisabled;
   }

   public String getAutoDiscoveryAddress()
   {
      return this.adGroupAddress;
   }

   public void setAutoDiscoveryAddress(String adAddress)
   {
      this.adGroupAddress = adAddress;
   }

   public int getAutoDiscoveryGroup()
   {
      return this.adGroupPort;
   }
   public void setAutoDiscoveryGroup(int adGroup)
   {
      this.adGroupPort = adGroup;
   }

   public String getAutoDiscoveryBindAddress()
   {
      String address = null;
      if (discoveryBindAddress != null)
         address = discoveryBindAddress.getHostAddress();
      return address;      
   }
   public void setAutoDiscoveryBindAddress(String address)
      throws UnknownHostException
   {
      discoveryBindAddress = InetAddress.getByName(address);
   }

   public int getAutoDiscoveryTTL()
   {
      return autoDiscoveryTTL;
   }

   public void setAutoDiscoveryTTL(int ttl)
   {
      autoDiscoveryTTL = ttl;
   }

   public void setJNPServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.jnpServerSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(jnpServerSocketFactoryName);
      jnpServerSocketFactory = (ServerSocketFactory) clazz.newInstance();
   }

   public void setLookupPool(BasicThreadPoolMBean poolMBean)
   {
      lookupPool = poolMBean.getInstance();
   }

   public void startService(HAPartition haPartition)
      throws Exception
   {
      this.partition = haPartition;
      this.startService();
   }

   protected void createService()
      throws Exception
   {
      boolean debug = log.isDebugEnabled();

      if (this.clusterPartition == null)
      {
         partition = findHAPartitionWithName(partitionName);
      }
      else 
      {
         partition = clusterPartition.getHAPartition();
         partitionName = partition.getPartitionName();
      }
      
      if (partition == null)
         throw new IllegalStateException("Cannot find partition '" + partitionName + "'");

      if (debug)
         log.debug("Initializing HAJNDI server on partition: " + partitionName);
      
      // Start HAJNDI service
      theServer = new HAJNDI(partition);
      log.debug("initialize HAJNDI");
      theServer.init();

      // Build the Naming interface method map
      HashMap tmpMap = new HashMap(13);
      Method[] methods = Naming.class.getMethods();
      for (int m = 0; m < methods.length; m++)
      {
         Method method = methods[m];
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         tmpMap.put(hash, method);
      }
      marshalledInvocationMapping = Collections.unmodifiableMap(tmpMap);
      
      // share instance for in-vm discovery
      NamingContext.setHANamingServerForPartition(partitionName, theServer);
   }

   protected void startService()
      throws Exception
   {
      log.debug("Obtaining the transport proxy");
      stub = this.getNamingProxy();
      this.theServer.setHAStub(stub);
      if (port >= 0)
      {
         log.debug("Starting HAJNDI bootstrap listener");
         initBootstrapListener();
      }

      // Automatic Discovery for unconfigured clients
      if (adGroupAddress != null && discoveryDisabled == false)
      {
         try
         {
            autoDiscovery = new AutomaticDiscovery();
            autoDiscovery.start();
            lookupPool.run(autoDiscovery);
         }
         catch (Exception e)
         {
            log.warn("Failed to start AutomaticDiscovery", e);
         }
      }
   }

   protected void stopService() throws Exception
   {
      // un-share instance for in-vm discovery
      NamingContext.removeHANamingServerForPartition(partitionName);

      // Stop listener
      ServerSocket s = bootstrapSocket;
      bootstrapSocket = null;
      if (s != null)
      {
         log.debug("Closing the bootstrap listener");
         s.close();
      }

      // Stop HAJNDI service
      log.debug("Stopping the HAJNDI service");
      theServer.stop();

      log.debug("Stopping AutomaticDiscovery");
      if (autoDiscovery != null && discoveryDisabled == false)
         autoDiscovery.stop();
   }
   
   protected void destroyService() throws Exception
   {
      log.debug("Destroying the HAJNDI service");
      theServer.destroy();
   }

   /**
    * Expose the Naming service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.
    * @jmx:managed-operation
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      // Set the method hash to Method mapping
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      // Invoke the Naming method via reflection
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();
      Object value = null;
      try
      {
         value = method.invoke(theServer, args);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if (t instanceof Exception)
            throw (Exception) t;
         else
            throw new UndeclaredThrowableException(t, method.toString());
      }

      return value;
   }

   /**
    * Bring up the bootstrap lookup port for obtaining the naming service proxy
    */
   protected void initBootstrapListener()
   {
      // Start listener
      try
      {
         // Get the default ServerSocketFactory is one was not specified
         if (jnpServerSocketFactory == null)
            jnpServerSocketFactory = ServerSocketFactory.getDefault();
         bootstrapSocket = jnpServerSocketFactory.createServerSocket(port, backlog, bindAddress);
         // If an anonymous port was specified get the actual port used
         if (port == 0)
            port = bootstrapSocket.getLocalPort();
         String msg = "Started ha-jndi bootstrap jnpPort=" + port
            + ", backlog=" + backlog + ", bindAddress=" + bindAddress;
         log.info(msg);
      }
      catch (IOException e)
      {
         log.error("Could not start on port " + port, e);
      }

      if (lookupPool == null)
         lookupPool = new BasicThreadPool("HANamingBootstrap Pool");
      AcceptHandler handler = new AcceptHandler();
      lookupPool.run(handler);
   }

   // Protected -----------------------------------------------------
   
   protected HAPartition findHAPartitionWithName(String name) throws Exception
   {
      HAPartition result = null;
      QueryExp exp = Query.and(Query.eq(Query.classattr(),
         Query.value(ClusterPartition.class.getName())),
         Query.match(Query.attr("PartitionName"),
            Query.value(name)));

      Set mbeans = this.getServer().queryMBeans(null, exp);
      if (mbeans != null && mbeans.size() > 0)
      {
         ObjectInstance inst = (ObjectInstance) (mbeans.iterator().next());
         ClusterPartitionMBean cp = (ClusterPartitionMBean) MBeanProxyExt.create(ClusterPartitionMBean.class,
            inst.getObjectName(),
            this.getServer());
         result = cp.getHAPartition();
      }

      return result;
   }

   /**
    * Get the Naming proxy for the transport. This version looks  up the
    * proxyFactory service Proxy attribute. Subclasses can override this to set
    * the proxy another way.
    * @return The Naming proxy for the protocol used with the HAJNDI service
    */
   protected Naming getNamingProxy() throws Exception
   {
      Naming proxy = (Naming) server.getAttribute(proxyFactory, "Proxy");
      return proxy;
   }

   // Private -------------------------------------------------------

   private class AutomaticDiscovery
      implements Runnable
   {
      protected Logger log = Logger.getLogger(AutomaticDiscovery.class);
      /** The socket for auto discovery requests */
      protected MulticastSocket socket = null;
      /** The ha-jndi addres + ':' + port string */
      protected byte[] ipAddress = null;
      /** The multicast group address */
      protected InetAddress group = null;
      protected boolean stopping = false;
      // Thread that is executing the run() method   
      protected Thread receiverThread = null;
      protected boolean receiverStopped = true;

      public AutomaticDiscovery() throws Exception
      {
      }

      public void start() throws Exception
      {
         stopping = false;
         // Use the jndi bind address if there is no discovery address
         if (discoveryBindAddress == null)
            discoveryBindAddress = bindAddress;
         socket = new MulticastSocket(adGroupPort);
         // If there is a bind address valid, set the socket interface to it
         if (discoveryBindAddress != null && discoveryBindAddress.isAnyLocalAddress() == false)
         {
            socket.setInterface(discoveryBindAddress);
         }
         socket.setTimeToLive(autoDiscoveryTTL);
         group = InetAddress.getByName(adGroupAddress);
         socket.joinGroup(group);

         String address = getBindAddress();
         /* An INADDR_ANY (0.0.0.0 || null) address is useless as the value
            sent to a remote client so check for this and use the local host
            address instead.
          */
         if (address == null || address.equals("0.0.0.0"))
         {
            address = InetAddress.getLocalHost().getHostAddress();
         }
         ipAddress = (address + ":" + port).getBytes();

         log.info("Listening on " + socket.getInterface() + ":" + socket.getLocalPort()
            + ", group=" + adGroupAddress
            + ", HA-JNDI address=" + new String(ipAddress));
      }

      public void stop()
      {
         try
         {
            stopping = true;
            
            // JBAS-2834 -- try to stop the receiverThread
            if (receiverThread != null 
                  && receiverThread != Thread.currentThread()
                  && receiverThread.isInterrupted() == false)
            {
               // Give it a moment to die on its own (unlikely)
               receiverThread.join(5);
               if (!receiverStopped)
                  receiverThread.interrupt(); // kill it
            }
            
            socket.leaveGroup(group);
            socket.close();
         }
         catch (Exception ex)
         {
            log.error("Stopping AutomaticDiscovery failed", ex);
         }
      }

      public void run()
      {
         boolean trace = log.isTraceEnabled();
         log.debug("Discovery request thread begin");
         
         // JBAS-2834 Cache a reference to this thread so stop()
         // can interrupt it if necessary
         receiverThread = Thread.currentThread();

         receiverStopped = false;
         
         // Wait for a datagram
         while (true)
         {
            // Stopped by normal means
            if (stopping)
               break;
            try
            {
               if (trace)
                  log.trace("HA-JNDI AutomaticDiscovery waiting for queries...");
               byte[] buf = new byte[256];
               DatagramPacket packet = new DatagramPacket(buf, buf.length);
               socket.receive(packet);
               if (trace)
                  log.trace("HA-JNDI AutomaticDiscovery Packet received.");

               // Queue the response to the thread pool
               DiscoveryRequestHandler handler = new DiscoveryRequestHandler(log,
                  packet, socket, ipAddress);
               lookupPool.run(handler);
               if (trace)
                  log.trace("Queued DiscoveryRequestHandler");
            }
            catch (Throwable t)
            {
               if (stopping == false)
                  log.warn("Ignored error while processing HAJNDI discovery request:", t);
            }
         }
         receiverStopped = true;
         log.debug("Discovery request thread end");
      }
   }

   /**
    * The class used as the runnable for writing the bootstrap stub
    */ 
   private class DiscoveryRequestHandler implements Runnable
   {
      private Logger log;
      private MulticastSocket socket;
      private DatagramPacket packet;
      private byte[] ipAddress;

      DiscoveryRequestHandler(Logger log, DatagramPacket packet,
         MulticastSocket socket, byte[] ipAddress)
      {
         this.log = log;
         this.packet = packet;
         this.socket = socket;
         this.ipAddress = ipAddress;
      }
      public void run()
      {
         boolean trace = log.isTraceEnabled();
         if( trace )
            log.trace("DiscoveryRequestHandler begin");
         // Return the naming server IP address and port to the client
         try
         {
            // See if the discovery is restricted to a particular parition
            String requestData = new String(packet.getData()).trim();
            if( trace )
               log.trace("RequestData: "+requestData);
            int colon = requestData.indexOf(':');
            if (colon > 0)
            {
               // Check the partition name
               String name = requestData.substring(colon + 1);
               if (name.equals(partitionName) == false)
               {
                  log.debug("Ignoring discovery request for partition: " + name);
                  if( trace )
                     log.trace("DiscoveryRequestHandler end");
                  return;
               }
            }
            DatagramPacket p = new DatagramPacket(ipAddress, ipAddress.length,
               packet.getAddress(), packet.getPort());
            if (trace)
               log.trace("Sending AutomaticDiscovery answer: " + new String(ipAddress));
            socket.send(p);
            if (trace)
               log.trace("AutomaticDiscovery answer sent.");
         }
         catch (IOException ex)
         {
            log.error("Error writing response", ex);
         }         
         if( trace )
            log.trace("DiscoveryRequestHandler end");
      }
   }

   /**
    * The class used as the runnable for the bootstrap lookup thread pool.
    */ 
   private class AcceptHandler implements Runnable
   {
      public void run()
      {
         boolean trace = log.isTraceEnabled();
         while (bootstrapSocket != null)
         {
            Socket socket = null;
            // Accept a connection
            try
            {
               socket = bootstrapSocket.accept();
               if( trace )
                  log.trace("Accepted bootstrap client: "+socket);
               BootstrapRequestHandler handler = new BootstrapRequestHandler(socket);
               lookupPool.run(handler);
            }
            catch (IOException e)
            {
               // Stopped by normal means
               if (bootstrapSocket == null)
                  return;
               log.error("Naming accept handler stopping", e);
            }
            catch(Throwable e)
            {
               log.error("Unexpected exception during accept", e);
            }
         }      
      }
   }

   /**
    * The class used as the runnable for writing the bootstrap stub
    */ 
   private class BootstrapRequestHandler implements Runnable
   {
      private Socket socket;
      BootstrapRequestHandler(Socket socket)
      {
         this.socket = socket;
      }
      public void run()
      {
         // Return the naming server stub
         try
         {
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            MarshalledObject replyStub = new MarshalledObject(stub);
            out.writeObject(replyStub);
            out.close();
         }
         catch (IOException ex)
         {
            log.debug("Error writing response to " + socket, ex);
         }
         finally
         {
            try
            {
               socket.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
}
