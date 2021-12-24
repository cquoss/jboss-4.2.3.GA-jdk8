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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJBLocalObject;
import javax.ejb.LocalHome;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 65844 $
 */
public class StatefulLocalProxyFactory extends BaseStatefulProxyFactory
{
   private static final Logger log = Logger.getLogger(StatefulLocalProxyFactory.class);
   
   private VMID vmid = Ejb3Registry.getVMID();

   protected Class<?>[] getInterfaces()
   {      
      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      LocalHome localHome = (LocalHome) statefulContainer.resolveAnnotation(LocalHome.class);

      boolean bindTogether = false;

      if (localHome != null && bindHomeAndBusinessTogether(statefulContainer))
         bindTogether = true;

      // Obtain all local interfaces      
      List<Class<?>> localInterfaces = new ArrayList<Class<?>>();
      localInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getLocalInterfaces(statefulContainer)));

      // Add JBossProxy
      localInterfaces.add(JBossProxy.class);

      // If binding along w/ home, add home
      if (bindTogether)
      {
         localInterfaces.add(localHome.value());
      }

      // Return
      return localInterfaces.toArray(new Class<?>[]
      {});
   }
   
   protected boolean bindHomeAndBusinessTogether(StatefulContainer container)
   {
      return ProxyFactoryHelper.getLocalHomeJndiName(container).equals(ProxyFactoryHelper.getLocalJndiName(container));
   }

   protected void initializeJndiName()
   {
      jndiName = ProxyFactoryHelper.getLocalJndiName(getContainer());
   }
   
   public VMID getVMID()
   {
      return vmid;
   }

   public void start() throws Exception
   {
      super.start();
      
      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, this);
      }
      catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful local proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }

      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      LocalHome localHome = (LocalHome) ((EJBContainer) getContainer()).resolveAnnotation(LocalHome.class);
      if (localHome != null  && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Class[] interfaces = {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(),
                                                                     interfaces, new StatefulLocalHomeProxy(getContainer()));
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()), homeProxy);
      }
   }

   public void stop() throws Exception
   {
      super.stop();
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      
      StatefulContainer statefulContainer = (StatefulContainer) getContainer();
      LocalHome localHome = (LocalHome) ((EJBContainer) getContainer()).resolveAnnotation(LocalHome.class);
      if (localHome != null  && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()));
      }
   }

   public Object createProxy()
   {
      try
      {
         StatefulContainer sfsb = (StatefulContainer) getContainer();
         StatefulBeanContext ctx = sfsb.getCache().create();
         ctx.setInUse(false);
         Object id = ctx.getId();
         Object[] args = {new StatefulLocalProxy(getContainer(), id, vmid)};
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

   public Object createProxy(Object id)
   {
      try
      {
         StatefulContainer sfsb = (StatefulContainer) getContainer();
         Object[] args = {new StatefulLocalProxy(getContainer(), id, vmid)};
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
   public Object createProxy(Class[] initTypes, Object[] initValues)
   {
      try
      {
         StatefulContainer sfsb = (StatefulContainer) getContainer();
         Object id = sfsb.createSession(initTypes, initValues);
         Object[] args = {new StatefulLocalProxy(getContainer(), id, vmid)};
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
      LocalBinding remoteBinding = (LocalBinding) ((Advisor)getContainer()).resolveAnnotation(LocalBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();

      return handle;
   }
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      vmid = (VMID)in.readObject();
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(vmid);
   }
}
