/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.web.util;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.aop.PojoCache;
import org.jboss.metadata.WebMetaData;
import org.jboss.web.tomcat.service.session.JBossCacheManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A WebSessionTestUtil.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class WebSessionTestUtil
{
   private static final String CONFIG_LOCATION = "cluster/http/jboss-web-test-service.xml";
   
   public static final String JVM_ROUTE_0 = "node0";
   public static final String JVM_ROUTE_1 = "node1";
   
   public static JBossCacheManager createManager(String warName, int maxInactiveInterval,
                                                 boolean local, boolean totalReplication, 
                                                 boolean marshalling, String jvmRoute, 
                                                 Set<PojoCache> allCaches)
      throws Exception
   {
      PojoCache cache = createCache(local, totalReplication, marshalling, allCaches);
      return createManager(warName, maxInactiveInterval, cache, jvmRoute);
   }
   
   public static JBossCacheManager createManager(String warName, 
                                                 int maxInactiveInterval, 
                                                 PojoCache cache, 
                                                 String jvmRoute)
   {
      JBossCacheManager jbcm = new JBossCacheManager(cache);      
      jbcm.setSnapshotMode("instant");
      
      MockEngine engine = new MockEngine();
      engine.setJvmRoute(jvmRoute);
      MockHost host = new MockHost();
      engine.addChild(host);
      host.setName("localhost");
      StandardContext container = new StandardContext();
      container.setName(warName);
      host.addChild(container);
      container.setManager(jbcm);
      
      // Do this after assigning the manager to the container, or else
      // the container's setting will override ours
      // Can't just set the container as their config is per minute not per second
      jbcm.setMaxInactiveInterval(maxInactiveInterval);
      return jbcm;      
   }
   
   public static PojoCache createCache(boolean local, boolean totalReplication, 
         boolean marshalling, Set<PojoCache> allCaches) throws Exception
   {
      PojoCache cache =  new PojoCache();
      PropertyConfigurator config = new PropertyConfigurator();
      config.configure(cache, CONFIG_LOCATION);
      if (local)
         cache.setCacheMode("LOCAL");
      if (!totalReplication)
      {
         Element e = cache.getBuddyReplicationConfig();
         
         NodeList list = e.getChildNodes();
         for (int i = 0; i < list.getLength(); i++)
         {
            Node node = list.item(i);
            if (node.getNodeName().equals("buddyReplicationEnabled"))
            {
               node.setNodeValue("true");
               break;
            }
         }
         cache.setBuddyReplicationConfig(e);
      }   
      
      if (marshalling)
      {
         cache.setUseRegionBasedMarshalling(true);
         cache.setInactiveOnStartup(true);
      }  
      
      if (allCaches != null)
         allCaches.add(cache);
      
      cache.start();
      return cache;
   }
   
   public static void invokeRequest(Manager manager, RequestHandler handler, String sessionId)
      throws ServletException, IOException
   {
      Valve valve = setupPipeline(manager, handler);
      MockRequest request = new MockRequest();
      request.setRequestedSessionId(sessionId);
      request.setContext((Context) manager.getContainer());
      Response response = new Response();
      request.setResponse(response);
      valve.invoke(request, response);
      request.recycle();
   }
   
   public static Valve setupPipeline(Manager manager, RequestHandler requestHandler)
   {
      Pipeline pipeline = manager.getContainer().getPipeline();
      
      // Clean out any existing request handler
      Valve[] valves = pipeline.getValves();
      RequestHandlerValve mockValve = null;
      for (Valve valve: valves)
      {
         if (valve instanceof RequestHandlerValve)         
         {
            mockValve = (RequestHandlerValve) valve;
            break;
         }
      }
      
      if (mockValve == null)
      {
         mockValve = new RequestHandlerValve(requestHandler);
         pipeline.addValve(mockValve);
      }
      else
      {
         mockValve.setRequestHandler(requestHandler);
      }
      
      return pipeline.getFirst();
   }
   
   public static WebMetaData getWebMetaData(int granularity, int trigger, boolean batchMode, int maxUnreplicated)
   {
      WebMetaData metadata = new WebMetaData();
      metadata.setDistributable(true);
      metadata.setReplicationGranularity(granularity);
      metadata.setInvalidateSessionPolicy(trigger);
      metadata.setReplicationFieldBatchMode(batchMode);
      metadata.setMaxUnreplicatedInterval(maxUnreplicated);
      
      return metadata;
   }

   /**
    * Prevent external instantiation.
    * 
    */
   private WebSessionTestUtil()
   {      
   }

}
