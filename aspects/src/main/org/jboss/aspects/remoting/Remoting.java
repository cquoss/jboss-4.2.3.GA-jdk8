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
package org.jboss.aspects.remoting;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.InstanceAdvised;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.aspects.tx.ClientTxPropagationInterceptor;
import org.jboss.remoting.InvokerLocator;

import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57186 $
 */

public class Remoting
{
   public static ClassProxy createRemoteProxy(Object objectId, Class clazz, String uri)
   throws Exception
   {
      return createRemoteProxy(objectId, clazz, new InvokerLocator(uri));
   }

   public static ClassProxy createRemoteProxy(Object objectId, Class clazz, InvokerLocator locator)
   throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(clazz);
      makeRemotable(proxy, locator, objectId);

      return proxy;
   }


   /**
    * Does'nt propagate security/tx
    * @param oid
    * @param interfaces
    * @param uri
    * @return
    * @throws Exception
    */
   public static Object createPojiProxy(Object oid, Class[] interfaces, String uri) throws Exception
   {
      InvokerLocator locator = new InvokerLocator(uri);
      Interceptor[] interceptors = {IsLocalInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy proxy = new PojiProxy(oid, locator, interceptors);
      return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, proxy);

   }

   public static void makeRemotable(InstanceAdvised proxy, InvokerLocator locator, Object objectId)
   {
      InstanceAdvisor advisor = proxy._getInstanceAdvisor();
      advisor.insertInterceptor(IsLocalInterceptor.singleton);
      advisor.insertInterceptor(SecurityClientInterceptor.singleton);
      advisor.insertInterceptor(ClientTxPropagationInterceptor.singleton);
      advisor.insertInterceptor(MergeMetaDataInterceptor.singleton);
      advisor.insertInterceptor(InvokeRemoteInterceptor.singleton);
      advisor.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING,
      InvokeRemoteInterceptor.INVOKER_LOCATOR,
      locator,
      PayloadKey.AS_IS);
      advisor.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING,
      InvokeRemoteInterceptor.SUBSYSTEM,
      "AOP",
      PayloadKey.AS_IS);

      advisor.getMetaData().addMetaData(Dispatcher.DISPATCHER,
      Dispatcher.OID,
      objectId,
      PayloadKey.AS_IS);
   }


}
