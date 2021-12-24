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
package org.jboss.ejb3.stateful;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJBObject;
import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;
import org.jboss.remoting.InvokerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision: 65844 $
 */
public class StatefulRemoteProxyFactory extends BaseStatefulProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatefulRemoteProxyFactory.class);
   
//   public static final String FACTORY_ATTRIBUTE = ",element=ProxyFactory";
   
   private RemoteBinding binding;
   private InvokerLocator locator;

   public void setRemoteBinding(RemoteBinding binding)
   {
      this.binding = binding;
   }

   protected Class<?>[] getInterfaces()
   {     
      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);

      boolean bindTogether = false;

      if (remoteHome != null && bindHomeAndBusinessTogether(statefulContainer))
         bindTogether = true;

      // Obtain all remote interfaces
      List<Class<?>> remoteInterfaces = new ArrayList<Class<?>>();
      remoteInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteInterfaces(statefulContainer)));

      // Add JBossProxy
      remoteInterfaces.add(JBossProxy.class);
      
      // If binding along w/ home, add home
      if (bindTogether)
      {
         remoteInterfaces.add(remoteHome.value());
      }

      // Return
      return remoteInterfaces.toArray(new Class<?>[]
      {});
   }
   
   protected boolean bindHomeAndBusinessTogether(StatefulContainer container)
   {
      return ProxyFactoryHelper.getHomeJndiName(container).equals(ProxyFactoryHelper.getRemoteJndiName(container));
   }

   protected void initializeJndiName()
   {
      jndiName = ProxyFactoryHelper.getRemoteJndiName(getContainer(), binding);
   }

   public void init() throws Exception
   {
      super.init();
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
   }

   public void start() throws Exception
   {
      init();

      super.start();
      Class[] interfaces = {ProxyFactory.class};
      String targetId = getTargetId();
      Object factoryProxy = createPojiProxy(targetId, interfaces, ProxyFactoryHelper.getClientBindUrl(binding));
      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, factoryProxy);
      }
      catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful remote proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      assert !Dispatcher.singleton.isRegistered(targetId) : targetId + " is already registered";
      Dispatcher.singleton.registerTarget(targetId, this);

      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()), homeProxy);
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      Dispatcher.singleton.unregisterTarget(getTargetId());
      
      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()));
      }
      super.stop();
   }


   public Object createHomeProxy(Class homeInterface)
   {
      try
      {
         Object containerId = getContainer().getObjectName().getCanonicalName();
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulHomeRemoteProxy proxy = new StatefulHomeRemoteProxy(getContainer(), stack.createInterceptors((Advisor) getContainer(), null), locator);


         setEjb21Objects(proxy);
         Class[] intfs = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(), intfs, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }
   public Object createProxy()
   {
      try
      {
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulRemoteProxy proxy = new StatefulRemoteProxy(getContainer(), stack.createInterceptors((Advisor) getContainer(), null), locator);


         setEjb21Objects(proxy);
         Object[] args = {proxy};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());  //To change body of catch statement use Options | File Templates.
      }
   }

   protected StatefulHandleImpl getHandle()
   {
      StatefulHandleImpl handle = new StatefulHandleImpl();
      RemoteBinding remoteBinding = (RemoteBinding) ((Advisor)getContainer()).resolveAnnotation(RemoteBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();

      return handle;
   }

   public Object createProxy(Object id)
   {
      try
      {
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatefulRemoteProxy proxy = new StatefulRemoteProxy(getContainer(), stack.createInterceptors((Advisor) getContainer(), null), locator, id);
         setEjb21Objects(proxy);
         Object[] args = {proxy};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());  //To change body of catch statement use Options | File Templates.
      }
   }
   
   /**
    * @return unique name for this proxy factory
    */
   protected String getTargetId()
   {  
      assert jndiName != null : "jndiName is null"; 
      return jndiName + PROXY_FACTORY_NAME;
   }
   
   protected Object createPojiProxy(Object oid, Class[] interfaces, String uri) throws Exception
   {
      InvokerLocator locator = new InvokerLocator(uri);
      Interceptor[] interceptors = {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy proxy = new PojiProxy(oid, locator, interceptors);
      return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, proxy);

   }


}
