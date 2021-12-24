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
package org.jboss.test.jmx.ha;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ha.jmx.HAServiceMBeanSupport;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.Registry;

public class HAService
   extends HAServiceMBeanSupport
   implements HAServiceRemote, HAServiceMBean
{
   private int count = 0;

   private Map marshalledInvocationMapping;

   public void startService()
      throws Exception
   {
      super.startService();
      
      // Calulate method hashes for remote invocation
      Method[] methods = HAServiceRemote.class.getMethods();
      HashMap tmpMap = new HashMap(methods.length);
      for(int m = 0; m < methods.length; m ++)
      {
         Method method = methods[m];
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         tmpMap.put(hash, method);
      }
      marshalledInvocationMapping = Collections.unmodifiableMap(tmpMap);

      // Place our ObjectName hash into the Registry so invokers can resolve it
      Registry.bind(new Integer(serviceName.hashCode()), serviceName);
   }

   public void stopService()
      throws Exception
   {
      super.stopService();
      
      // No longer available to the invokers
      Registry.unbind(new Integer(serviceName.hashCode()));
   }

   /** 
    * Expose the client mapping
    */
   public Map getMethodMap()
   {
      return marshalledInvocationMapping;
   }

   /** 
    * This is the "remote" entry point
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // Invoked remotely, inject method resolution
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();

      // Setup any security context (only useful if something checks it, this impl doesn't)
      Principal principal = invocation.getPrincipal();
      Object credential = invocation.getCredential();
      SecurityAssociation.setPrincipal(principal);
      SecurityAssociation.setCredential(credential);

      // Dispatch the invocation
      try
      {
         return method.invoke(this, args);
      }
      catch(InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) t;
         else
            throw new UndeclaredThrowableException(t, method.toString());
      }
      finally
      {
         // Clear the security context
         SecurityAssociation.clear();
      }
   }

   // Implementation of remote methods

   public String hello()
   {
      return "Hello";
   }
   
   public String getClusterNode()
   {
      return getPartition().getNodeName();
   }

}
