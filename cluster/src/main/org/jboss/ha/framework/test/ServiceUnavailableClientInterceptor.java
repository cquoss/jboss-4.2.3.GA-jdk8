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
package org.jboss.ha.framework.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.PayloadKey;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;

/**
 * Used for testing clustering: mimics an exhausted set of targets.
 * This interceptor should be placed between a RetryInterceptor and
 * the InvokerInterceptor.
 *
 * @author  brian.stansberry@jboss.com.
 * @version $Id$
 */

public class ServiceUnavailableClientInterceptor extends org.jboss.proxy.Interceptor
{

   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 8830272856328720750L;
   
   // Attributes ----------------------------------------------------
   
   private String proxyFamilyName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public ServiceUnavailableClientInterceptor ()
   {
   }
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Interceptor overrides ---------------------------------------------------
   
   public Object invoke (Invocation mi) throws Throwable
   {
      Object data = mi.getValue ("DO_FAIL_DURING_NEXT_CALL");
      
      if (data != null &&
            data instanceof java.lang.Boolean &&
            data.equals (java.lang.Boolean.TRUE))
      {
         
         // Clear the instruction
         mi.setValue ("DO_FAIL_DURING_NEXT_CALL", Boolean.FALSE, PayloadKey.AS_IS);
         
         if (proxyFamilyName == null)
         {
            proxyFamilyName = getProxyFamilyName(mi);
         }
         
         // Clear the targets to simulate exhausting them all
         FamilyClusterInfo info = ClusteringTargetsRepository.getFamilyClusterInfo(proxyFamilyName);
         ArrayList targets = info.getTargets();
         for (Iterator it = targets.iterator(); it.hasNext(); )
            info.removeDeadTarget(it.next());
         
         throw new ServiceUnavailableException("Service unavailable", 
                                               new Exception("Test"));
      } 

      return getNext().invoke(mi);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
   static String getProxyFamilyName(Invocation invocation) throws Exception
   {
      InvocationContext ctx = invocation.invocationContext;
      JRMPInvokerProxyHA invoker = (JRMPInvokerProxyHA) ctx.getInvoker();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      invoker.writeExternal(oos);
      oos.close();
      byte[] bytes = baos.toByteArray();
      
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bais);
      Object targets = ois.readObject();
      Object loadBalancePolicy = ois.readObject();
      String proxyFamilyName = (String) ois.readObject();
      ois.close();
      
      return proxyFamilyName;
   }
}
