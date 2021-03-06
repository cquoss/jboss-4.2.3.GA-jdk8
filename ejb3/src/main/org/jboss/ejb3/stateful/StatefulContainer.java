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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.Init;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.TimerService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.aop.AspectManager;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.SessionContainer;
import org.jboss.ejb3.cache.StatefulCache;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.proxy.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.handle.HomeHandleImpl;
import org.jboss.injection.Injector;
import org.jboss.injection.JndiPropertyInjector;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 65733 $
 */
public class StatefulContainer extends SessionContainer
{
   private static final Logger log = Logger.getLogger(StatefulContainer.class);

   protected StatefulCache cache;

   public StatefulContainer(ClassLoader cl, String beanClassName, String ejbName, AspectManager manager,
                            Hashtable ctxProperties, InterceptorInfoRepository interceptorRepository,
                            Ejb3Deployment deployment)
   {
      super(cl, beanClassName, ejbName, manager, ctxProperties, interceptorRepository, deployment);
      beanContextClass = StatefulBeanContext.class;
   }

   public void start() throws Exception
   {
      try
      {
         super.start();
         Cache cacheConfig = (Cache) resolveAnnotation(Cache.class);
         cache = (StatefulCache) cacheConfig.value().newInstance();
         cache.initialize(this);
         cache.start();
      }
      catch (Exception e)
      {
         try
         {
            stop();
         }
         catch (Exception ignore)
         {
            log.debug("Failed to cleanup after start() failure", ignore);
         }
         throw e;
      }

   }

   public void stop() throws Exception
   {
      if (cache != null) cache.stop();
      super.stop();
   }

   public StatefulCache getCache()
   {
      return cache;
   }

   /**
    * Performs a synchronous local invocation
    */
   public Object localInvoke(Object id, Method method, Object[] args)
           throws Throwable
   {
      return localInvoke(id, method, args, null);
   }

   /**
    * Performs a synchronous or asynchronous local invocation
    *
    */
   public Object localHomeInvoke(Method method, Object[] args) throws Throwable
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = (MethodInfo) methodInterceptors.get(hash);
         if (info == null)
         {
            throw new RuntimeException(
                    "Could not resolve beanClass method from proxy call: "
                            + method.toString());
         }
         return invokeLocalHomeMethod(info, args);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   /**
    * Performs a synchronous or asynchronous local invocation
    *
    * @param provider If null a synchronous invocation, otherwise an asynchronous
    */
   public Object localInvoke(Object id, Method method, Object[] args,
         FutureHolder provider) throws Throwable
   {
      long start = System.currentTimeMillis();

      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = (MethodInfo) methodInterceptors.get(hash);
         if (info == null)
         {
            throw new RuntimeException(
                  "Could not resolve beanClass method from proxy call: "
                  + method.toString());
         }
      
         Method unadvisedMethod = info.getUnadvisedMethod();
      
         try
         {
            invokeStats.callIn();
      
            if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               return invokeLocalHomeMethod(info, args);
            }
            else if (unadvisedMethod != null
                  && isEJBObjectMethod(unadvisedMethod))
            {
               return invokeEJBLocalObjectMethod(id, info, args);
            }
            
            StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info, id);
            nextInvocation.setAdvisor(this);
            nextInvocation.setArguments(args);
            
            ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
            
            invokedMethod.push(new InvokedMethod(true, method));
            return nextInvocation.invokeNext();
         }
         finally
         {
            if (unadvisedMethod != null)
            {
               long end = System.currentTimeMillis();
               long elapsed = end - start;
               invokeStats.updateStats(unadvisedMethod, elapsed);
            }
         
            invokeStats.callOut();
            
            invokedMethod.pop();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   /**
    * Create a stateful bean and return its oid.
    *
    * @return
    */
   public Object createSession()
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         return getCache().create().getId();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   /**
    * Create a stateful bean and return its oid.
    *
    * @return
    */
   public Object createSession(Class[] initTypes, Object[] initValues)
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         return getCache().create(initTypes, initValues).getId();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   protected void destroySession(Object id)
   {
      getCache().remove(id);
   }

   /**
    * This should be a remote invocation call
    *
    * @param invocation
    * @return
    * @throws Throwable
    */
   public InvocationResponse dynamicInvoke(Object target, Invocation invocation) throws Throwable
   {
      long start = System.currentTimeMillis();
      
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      EJBContainerInvocation newSi = null;
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         StatefulRemoteInvocation si = (StatefulRemoteInvocation) invocation;
         MethodInfo info = (MethodInfo) methodInterceptors.get(si.getMethodHash());
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call");
         }

         InvocationResponse response = null;
         Method unadvisedMethod = info.getUnadvisedMethod();
         Object newId = null;
         
         try
         {
            invokeStats.callIn();
            
            if (info != null && unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               response = invokeHomeMethod(info, si);
            }
            else if (info != null && unadvisedMethod != null && isEJBObjectMethod(unadvisedMethod))
            {
               response = invokeEJBObjectMethod(info, si);
            }
            else
            {
               if (si.getId() == null)
               {
                  StatefulBeanContext ctx = getCache().create();
                  newId = ctx.getId();
               }
               else
               {
                  newId = si.getId();
               }
               newSi = new StatefulContainerInvocation(info, newId);
               newSi.setArguments(si.getArguments());
               newSi.setMetaData(si.getMetaData());
               newSi.setAdvisor(this);
   
               Object rtn = null;
               
               invokedMethod.push(new InvokedMethod(false, unadvisedMethod));
               rtn = newSi.invokeNext();

               response = marshallResponse(invocation, rtn, newSi.getResponseContextInfo());
               if (newId != null) response.addAttachment(StatefulConstants.NEW_ID, newId);
            }
         }
         catch (Throwable throwable)
         {
            Throwable exception = throwable;
            if (newId != null)
            {
               exception = new ForwardId(throwable, newId);
            }
            Map responseContext = null;
            if (newSi != null) newSi.getResponseContextInfo();
            response = marshallException(invocation, exception, responseContext);
            return response;
         }
         finally
         {
            if (unadvisedMethod != null)
            {
               long end = System.currentTimeMillis();
               long elapsed = end - start;
               invokeStats.updateStats(unadvisedMethod, elapsed);
            }
            
            invokeStats.callOut();
            
            invokedMethod.pop();
         }

         return response;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }


   public TimerService getTimerService()
   {
      throw new UnsupportedOperationException("stateful bean doesn't support TimerService (EJB3 18.2#2)");
   }

   public TimerService getTimerService(Object pKey)
   {
      return getTimerService();
   }
   
   @Override
   public void invokePostActivate(BeanContext beanContext)
   {
      for (Injector injector : injectors)
      {
         if (injector instanceof JndiPropertyInjector)
         {
            AccessibleObject field = ((JndiPropertyInjector) injector).getAccessibleObject();
            
            if (field.isAnnotationPresent(javax.ejb.EJB.class))
            {
               continue; // skip nested EJB injection since the local proxy will be (de)serialized correctly
            }
            
            if (field instanceof Field)
            {
               // reinject transient fields
               if ((((Field)field).getModifiers() & Modifier.TRANSIENT) > 0)
                  injector.inject(beanContext);
            }
         }
      }
      callbackHandler.postActivate(beanContext);
   }

   @Override
   public void invokePrePassivate(BeanContext beanContext)
   {
      callbackHandler.prePassivate(beanContext);
   }

   @Override
   protected Class[] getHandledCallbacks()
   {
      return new Class[]
              {PostConstruct.class, PreDestroy.class, PostActivate.class,
                      PrePassivate.class};
   }

   public void invokeInit(Object bean)
   {
      try
      {
         Method[] methods = bean.getClass().getDeclaredMethods();

         for (int i = 0; i < methods.length; i++)
         {
            if (methods[i].getParameterTypes().length == 0)
            {
               if ((methods[i].getAnnotation(Init.class) != null)
                       || (resolveAnnotation(methods[i], Init.class) != null))
               {
                  methods[i].invoke(bean, new Object[0]);
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void invokeInit(Object bean, Class[] initParameterTypes,
                          Object[] initParameterValues)
   {
      try
      {
         Method[] methods = bean.getClass().getDeclaredMethods();

         for (int i = 0; i < methods.length; i++)
         {
            if ((methods[i].getAnnotation(Init.class) != null)
                    || (resolveAnnotation(methods[i], Init.class) != null))
            {
               Object[] parameters = getInitParameters(methods[i],
                       initParameterTypes, initParameterValues);

               if (parameters != null)
                  methods[i].invoke(bean, parameters);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected Object[] getInitParameters(Method method,
                                        Class[] initParameterTypes, Object[] initParameterValues)
   {
      if (method.getParameterTypes().length == initParameterTypes.length)
      {
         for (int i = 0; i < initParameterTypes.length; ++i)
         {
            Class formal = method.getParameterTypes()[i];
            Class actual = initParameterTypes[i];
            if (!isMethodInvocationConvertible(formal, actual == null ? null
                    : actual))
               return null;
         }
         return initParameterValues;
      }
      return null;
   }

   /**
    * Determines whether a type represented by a class object is convertible to
    * another type represented by a class object using a method invocation
    * conversion, treating object types of primitive types as if they were
    * primitive types (that is, a Boolean actual parameter type matches boolean
    * primitive formal type). This behavior is because this method is used to
    * determine applicable methods for an actual parameter list, and primitive
    * types are represented by their object duals in reflective method calls.
    *
    * @param formal the formal parameter type to which the actual parameter type
    *               should be convertible
    * @param actual the actual parameter type.
    * @return true if either formal type is assignable from actual type, or
    *         formal is a primitive type and actual is its corresponding object
    *         type or an object type of a primitive type that can be converted
    *         to the formal type.
    */
   private static boolean isMethodInvocationConvertible(Class formal,
                                                        Class actual)
   {
      /*
       * if it's a null, it means the arg was null
       */
      if (actual == null && !formal.isPrimitive())
      {
         return true;
      }
      /*
       * Check for identity or widening reference conversion
       */
      if (actual != null && formal.isAssignableFrom(actual))
      {
         return true;
      }
      /*
       * Check for boxing with widening primitive conversion. Note that actual
       * parameters are never primitives.
       */
      if (formal.isPrimitive())
      {
         if (formal == Boolean.TYPE && actual == Boolean.class)
            return true;
         if (formal == Character.TYPE && actual == Character.class)
            return true;
         if (formal == Byte.TYPE && actual == Byte.class)
            return true;
         if (formal == Short.TYPE
                 && (actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Integer.TYPE
                 && (actual == Integer.class || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Long.TYPE
                 && (actual == Long.class || actual == Integer.class
                 || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Float.TYPE
                 && (actual == Float.class || actual == Long.class
                 || actual == Integer.class || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Double.TYPE
                 && (actual == Double.class || actual == Float.class
                 || actual == Long.class || actual == Integer.class
                 || actual == Short.class || actual == Byte.class))
            return true;
      }
      return false;
   }

   private Object invokeEJBLocalObjectMethod(Object id, MethodInfo info,
                                             Object[] args) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("remove"))
      {
         destroySession(id);

         return null;
      }
      else if (unadvisedMethod.getName().equals("getEJBLocalHome"))
      {
         Object bean = getCache().get(id).getInstance();

         return bean;
      }
      else if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         return id;
      }
      else if (unadvisedMethod.getName().equals("isIdentical"))
      {
         EJBObject bean = (EJBObject) args[0];

         Object primaryKey = bean.getPrimaryKey();

         boolean isIdentical = id.equals(primaryKey);

         return isIdentical;
      }
      else
      {
         return null;
      }
   }

   private Object invokeLocalHomeMethod(MethodInfo info, Object[] args)
           throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("create"))
      {
         Class[] initParameterTypes =
                 {};
         Object[] initParameterValues =
                 {};
         if (unadvisedMethod.getParameterTypes().length > 0)
         {
            initParameterTypes = unadvisedMethod.getParameterTypes();
            initParameterValues = args;
         }

         LocalBinding binding = (LocalBinding) resolveAnnotation(LocalBinding.class);

         StatefulLocalProxyFactory factory = new StatefulLocalProxyFactory();
         factory.setContainer(this);
         factory.init();

         Object proxy = factory.createProxy(initParameterTypes,
                 initParameterValues);

         return proxy;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         StatefulHandleImpl handle = (StatefulHandleImpl) args[0];

         destroySession(handle.id);

         return null;
      }
      else
      {
         return null;
      }
   }
   
   public Object createLocalProxy(Object id) throws Exception
   {
      StatefulLocalProxyFactory factory = new StatefulLocalProxyFactory();
      factory.setContainer(this);
      factory.init();

      return factory.createProxy(id);
   }
   
   public Object createRemoteProxy(Object id) throws Exception
   {
      RemoteBinding binding = null;
      RemoteBindings bindings = (RemoteBindings) resolveAnnotation(RemoteBindings.class);
      if (bindings != null)
         binding = bindings.value()[0];
      else
         binding = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
      
      StatefulRemoteProxyFactory factory = new StatefulRemoteProxyFactory();
      factory.setContainer(this);
      factory.setRemoteBinding(binding);
      factory.init();

      if (id != null)
         return factory.createProxy(id);
      else
         return factory.createProxy();
   }
   
   public boolean isClustered()
   {
      return hasAnnotation(getBeanClass(), org.jboss.annotation.ejb.Clustered.class.getName());
   }

   protected InvocationResponse invokeHomeMethod(MethodInfo info,
                                                 StatefulRemoteInvocation statefulInvocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("create"))
      {
         Class[] initParameterTypes =
                 {};
         Object[] initParameterValues =
                 {};
         if (unadvisedMethod.getParameterTypes().length > 0)
         {
            initParameterTypes = unadvisedMethod.getParameterTypes();
            initParameterValues = statefulInvocation.getArguments();
         }

         StatefulContainerInvocation newStatefulInvocation = buildNewInvocation(
                 info, statefulInvocation, initParameterTypes,
                 initParameterValues);

         Object proxy = createRemoteProxy(newStatefulInvocation.getId());

         InvocationResponse response = marshallResponse(statefulInvocation, proxy, newStatefulInvocation.getResponseContextInfo());
         if (newStatefulInvocation.getId() != null)
            response.addAttachment(StatefulConstants.NEW_ID,
                    newStatefulInvocation.getId());
         return response;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         StatefulHandleImpl handle = (StatefulHandleImpl) statefulInvocation
                 .getArguments()[0];

         destroySession(handle.id);

         InvocationResponse response = new InvocationResponse(null);
         response.setContextInfo(statefulInvocation.getResponseContextInfo());
         return response;
      }
      else if (unadvisedMethod.getName().equals("getEJBMetaData"))
      {
         Class remote = null;
         Class home = null;
         Class pkClass = Object.class;
         HomeHandleImpl homeHandle = null;

         Remote remoteAnnotation = (Remote) resolveAnnotation(Remote.class);
         if (remoteAnnotation != null)
            remote = remoteAnnotation.value()[0];
         RemoteHome homeAnnotation = (RemoteHome) resolveAnnotation(RemoteHome.class);
         if (homeAnnotation != null)
            home = homeAnnotation.value();
         RemoteBinding remoteBindingAnnotation = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            homeHandle = new HomeHandleImpl(remoteBindingAnnotation
                    .jndiBinding());

         EJBMetaDataImpl metadata = new EJBMetaDataImpl(remote, home, pkClass,
                 true, false, homeHandle);

         InvocationResponse response = marshallResponse(statefulInvocation, metadata, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getHomeHandle"))
      {
         HomeHandleImpl homeHandle = null;

         RemoteBinding remoteBindingAnnotation = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            homeHandle = new HomeHandleImpl(remoteBindingAnnotation
                    .jndiBinding());


         InvocationResponse response = marshallResponse(statefulInvocation, homeHandle, null);
         return response;
      }
      else
      {
         return null;
      }
   }

   protected InvocationResponse invokeEJBObjectMethod(MethodInfo info,
                                                      StatefulRemoteInvocation statefulInvocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("getHandle"))
      {
         StatefulContainerInvocation newStatefulInvocation = buildInvocation(
                 info, statefulInvocation);

         StatefulHandleImpl handle = new StatefulHandleImpl();
         handle.id = newStatefulInvocation.getId();
         RemoteBinding remoteBinding = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBinding != null)
            handle.jndiName = remoteBinding.jndiBinding();
         InvocationResponse response = marshallResponse(statefulInvocation, handle, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         destroySession(statefulInvocation.getId());

         InvocationResponse response = new InvocationResponse(null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getEJBHome"))
      {
         HomeHandleImpl homeHandle = null;

         RemoteBinding remoteBindingAnnotation = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(this));

         EJBHome ejbHome = homeHandle.getEJBHome();

         InvocationResponse response = marshallResponse(statefulInvocation, ejbHome, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         Object id = statefulInvocation.getId();

         InvocationResponse response = marshallResponse(statefulInvocation, id, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("isIdentical"))
      {
         Object id = statefulInvocation.getId();
         EJBObject bean = (EJBObject) statefulInvocation.getArguments()[0];

         Object primaryKey = bean.getPrimaryKey();

         boolean isIdentical = id.equals(primaryKey);

         InvocationResponse response = marshallResponse(statefulInvocation, isIdentical, null);
         return response;
      }
      else
      {
         return null;
      }
   }

   private StatefulContainerInvocation buildNewInvocation(MethodInfo info,
                                                          StatefulRemoteInvocation statefulInvocation,
                                                          Class[] initParameterTypes, Object[] initParameterValues)
   {
      StatefulContainerInvocation newStatefulInvocation = null;

      StatefulBeanContext ctx = null;
      if (initParameterTypes.length > 0)
         ctx = getCache().create(initParameterTypes, initParameterValues);
      else
         ctx = getCache().create();

      Object newId = ctx.getId();
      newStatefulInvocation = new StatefulContainerInvocation(info, newId);

      newStatefulInvocation.setArguments(statefulInvocation.getArguments());
      newStatefulInvocation.setMetaData(statefulInvocation.getMetaData());
      newStatefulInvocation.setAdvisor(this);

      return newStatefulInvocation;
   }

   private StatefulContainerInvocation buildInvocation(MethodInfo info,
                                                       StatefulRemoteInvocation statefulInvocation)
   {
      StatefulContainerInvocation newStatefulInvocation = null;
      Object newId = null;
      if (statefulInvocation.getId() == null)
      {
         StatefulBeanContext ctx = getCache().create();
         newId = ctx.getId();
         newStatefulInvocation = new StatefulContainerInvocation(info, newId);
      }
      else
      {
         newStatefulInvocation = new StatefulContainerInvocation(info, statefulInvocation.getId());
      }

      newStatefulInvocation.setArguments(statefulInvocation.getArguments());
      newStatefulInvocation.setMetaData(statefulInvocation.getMetaData());
      newStatefulInvocation.setAdvisor(this);

      return newStatefulInvocation;
   }

   @Override
   public Object getBusinessObject(BeanContext beanContext, Class businessInterface) throws IllegalStateException
   {
      StatefulBeanContext ctx = (StatefulBeanContext) beanContext;

      boolean isRemote = false;
      boolean found = false;
      Class[] remoteInterfaces = ProxyFactoryHelper.getRemoteInterfaces(this);
      if (remoteInterfaces != null)
      {
         for (Class intf : remoteInterfaces)
         {
            if (intf.getName().equals(businessInterface.getName()))
            {
               isRemote = true;
               found = true;
               break;
            }
         }
      }
      if (found == false)
      {
         Class[] localInterfaces = ProxyFactoryHelper.getLocalInterfaces(this);
         if (localInterfaces != null)
         {
            for (Class intf : localInterfaces)
            {
               if (intf.getName().equals(businessInterface.getName()))
               {
                  found = true;
                  break;
               }
            }

         }
      }
      if (found == false) throw new IllegalStateException(businessInterface.getName() + " is not a business interface");

      for (ProxyFactory factory : proxyDeployer.getProxyFactories())
      {
         if (isRemote && factory instanceof StatefulRemoteProxyFactory)
         {
            return ((StatefulRemoteProxyFactory) factory).createProxy(ctx.getId());
         }
         else if (!isRemote && factory instanceof StatefulLocalProxyFactory)
         {
            return ((StatefulLocalProxyFactory) factory).createProxy(ctx.getId());
         }
      }
      throw new IllegalStateException("Unable to create proxy for getBusinessObject as a proxy factory was not found");
   }

   protected void removeHandle(Handle arg) throws Exception
   {
      /*
      StatefulHandleImpl handle = (StatefulHandleImpl) arg;

      destroySession(handle.id);
      */
      arg.getEJBObject().remove();
   }
}
