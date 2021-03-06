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
package org.jboss.ejb3;

import java.lang.reflect.Method;
import org.jboss.aop.Advisor;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * Representation of an EJB invocation on the serverside
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60233 $
 */
public class EJBContainerInvocation<A extends EJBContainer, T extends BeanContext> extends MethodInvocation
{
   private static final long serialVersionUID = 4941832732679380382L;
   
   protected T ctx;
   private BeanContextLifecycleCallback<T> callback;

   public EJBContainerInvocation(MethodInfo info)
   {
      super(info, info.getInterceptors());
   }

   public EJBContainerInvocation(Interceptor[] interceptors, long methodHash, Method advisedMethod, Method unadvisedMethod, Advisor advisor)
   {
      super(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
   }

   public EJBContainerInvocation()
   {
      super(null, null);
   }

   @SuppressWarnings("unchecked")
   public A getAdvisor()
   {
      return (A) super.getAdvisor();
   }
   
   public T getBeanContext()
   {
      return ctx;
   }

   public void setBeanContext(T beanCtx)
   {
      if(beanCtx != null)
      {
         ctx = beanCtx;
         
         if(callback != null)
            callback.attached(beanCtx);
      }
      else
      {
         if(callback != null)
            callback.released(ctx);
         
         ctx = null;
      }
   }

   public Invocation getWrapper(Interceptor[] newchain)
   {
      return new EJBContainerInvocationWrapper<A, T>(this, newchain);
   }

   public Invocation copy()
   {
      EJBContainerInvocation<A, T> wrapper = new EJBContainerInvocation<A, T>(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
      wrapper.metadata = this.metadata;
      wrapper.currentInterceptor = this.currentInterceptor;
      wrapper.setTargetObject(this.getTargetObject());
      wrapper.setArguments(this.getArguments());
      wrapper.setBeanContext(this.ctx);
      wrapper.callback = this.callback;
      return wrapper;
   }
   
   public void setContextCallback(BeanContextLifecycleCallback<T> callback)
   {
      this.callback = callback;
   }
}
