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
package org.jboss.ejb3.stateless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.LocalHome;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.logging.Logger;

import org.jboss.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67509 $
 */
public class StatelessLocalProxyFactory extends BaseStatelessProxyFactory
{
   private static final Logger log = Logger.getLogger(StatelessLocalProxyFactory.class);

   protected Class<?>[] getInterfaces()
   {
      EJBContainer statelessContainer = (EJBContainer)getContainer();
      LocalHome localHome = (LocalHome)statelessContainer.resolveAnnotation(LocalHome.class);

      boolean bindTogether = false;
      
      if (localHome != null && bindHomeAndBusinessTogether(statelessContainer))
         bindTogether = true;

      // Obtain all local interfaces
      List<Class<?>> localInterfaces = new ArrayList<Class<?>>();
      localInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getLocalInterfaces(statelessContainer)));

      // Ensure remote interfaces defined
      if (localInterfaces.size() > 0)
      {
         // Add JBossProxy
         localInterfaces.add(JBossProxy.class);

         // If binding along w/ home, add home
         if (bindTogether)
         {
            localInterfaces.add(localHome.value());
         }
      }
      else
      {
         // No remote interfaces defined, log warning
         log.warn("[EJBTHREE-933] NPE when deploying web service beans");
      }

      // Return
      return localInterfaces.toArray(new Class<?>[]
      {});
   }
   
   protected boolean bindHomeAndBusinessTogether(EJBContainer container)
   {
      return ProxyFactoryHelper.getLocalHomeJndiName(container).equals(ProxyFactoryHelper.getLocalJndiName(container));
   }

   protected void initializeJndiName()
   {
      jndiName = ProxyFactoryHelper.getLocalJndiName(getContainer());
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      EJBContainer statelessContainer = (EJBContainer) getContainer();
      LocalHome localHome = (LocalHome) statelessContainer.resolveAnnotation(LocalHome.class);
    
      if (localHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         StatelessLocalProxy proxy = new StatelessLocalProxy(getContainer());
         
         Class[] interfaces = {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(),
                                                                     interfaces, proxy);
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()), homeProxy);
      }
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      EJBContainer statelessContainer = (EJBContainer) getContainer();
      LocalHome localHome = (LocalHome) statelessContainer.resolveAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()));
      }
   }


   public Object createProxy()
   {
      return constructProxy(new StatelessLocalProxy(getContainer()));
   }

   protected StatelessHandleImpl getHandle()
   {
      StatelessHandleImpl handle = new StatelessHandleImpl();
      LocalBinding localBinding = (LocalBinding) ((Advisor)getContainer()).resolveAnnotation(LocalBinding.class);
      if (localBinding != null)
         handle.jndiName = localBinding.jndiBinding();

      return handle;
   }

}
