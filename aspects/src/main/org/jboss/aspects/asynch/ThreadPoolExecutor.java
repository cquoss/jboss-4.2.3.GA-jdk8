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
package org.jboss.aspects.asynch;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import org.jboss.aop.Advisor;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * Use a global thread pool to execute asynch tasks
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57186 $
 */
public class ThreadPoolExecutor implements ExecutorAbstraction
{
   private static Executor executor = new PooledExecutor();

   public void setAdvisor(Advisor advisor)
   {

   }

   public RemotableFuture execute(MethodInvocation invocation) throws Exception
   {
      final MethodInvocation copy = (MethodInvocation) invocation.copy();
      final ClassLoader cl = Thread.currentThread().getContextClassLoader();
      
      FutureResult result = new FutureResult();
      Runnable command = result.setter(new Callable()
      {
         public Object call() throws Exception
         {
            try
            {
               Thread.currentThread().setContextClassLoader(cl);
               return copy.invokeNext();
            }
            catch (Throwable throwable)
            {
               if (throwable instanceof Exception)
               {
                  throw ((Exception) throwable);
               }
               else
                  throw new Exception(throwable);
            }
         }
      });
      executor.execute(command);

      return new FutureImpl(result);
   }

}
