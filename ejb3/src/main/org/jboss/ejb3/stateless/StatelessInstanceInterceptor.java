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

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.*;
import org.jboss.ejb3.stateful.StatefulInstanceInterceptor;
import org.jboss.ejb3.tx.Ejb3TxPolicy;
import org.jboss.logging.Logger;

import javax.ejb.EJBException;
import java.rmi.RemoteException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57207 $
 */
public class StatelessInstanceInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(StatelessInstanceInterceptor.class);

   public String getName()
   {
      return "StatelessInstanceInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      EJBContainerInvocation ejb = (EJBContainerInvocation) invocation;
      EJBContainer container = (EJBContainer)ejb.getAdvisor();
      Pool pool = container.getPool();
      BeanContext ctx = pool.get();
      ejb.setTargetObject(ctx.getInstance());
      ejb.setBeanContext(ctx);

      boolean discard = false;

      try
      {
         return ejb.invokeNext();
      }
      catch (Exception ex)
      {
         discard = (ex instanceof EJBException) ||
                 ((ex instanceof RuntimeException || ex instanceof RemoteException) && !StatefulInstanceInterceptor.isApplicationException(ex.getClass(), container));
         throw ex;
      }
      finally
      {                                                                                   
         ejb.setTargetObject(null);
         ejb.setBeanContext(null);
         if (discard) pool.discard(ctx);
         else pool.release(ctx);
      }
   }
}
