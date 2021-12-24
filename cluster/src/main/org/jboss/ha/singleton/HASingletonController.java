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
package org.jboss.ha.singleton;

import java.security.InvalidParameterException;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * A clustered singleton service that calls a configurable
 * method on a target mbean, whenever the current node becomes
 * the master. Correspondigly, it calls a configurable method
 * on the target mbean, whenever the current node resigns from
 * being the master.
 * 
 * Optional string arguments may be passed to those methods.
 * 
 * The prevailing usage of this MBean is to deploy on the
 * master node the content of the deploy-hasingleton directory.
 * 
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:mr@gedoplan.de">Marcus Redeker</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57188 $
 */
public class HASingletonController extends HASingletonSupport
   implements HASingletonControllerMBean
{
   // Private Data --------------------------------------------------
   
   private ObjectName mSingletonMBean;
   private String mSingletonMBeanStartMethod = "startSingleton";
   private String mSingletonMBeanStopMethod  = "stopSingleton";
   private String mSingletonMBeanStartMethodArgument;
   private String mSingletonMBeanStopMethodArgument;

   private static final Object[] NO_ARGS = new Object[0];
   private static final String[] NO_TYPES = new String[0];

   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public HASingletonController()
   {
      // empty
   }
   
   // Attributes ----------------------------------------------------
   
   public ObjectName getTargetName()
   {
      return mSingletonMBean;
   }

   public void setTargetName(ObjectName targetObjectName)
   {
      this.mSingletonMBean = targetObjectName;
   }

   public String getTargetStartMethod()
   {
      return mSingletonMBeanStartMethod;
   }

   public void setTargetStartMethod(String targetStartMethod)
      throws InvalidParameterException
   {
      if (targetStartMethod != null)
         mSingletonMBeanStartMethod = targetStartMethod;
   }


   public String getTargetStopMethod()
   {
      return mSingletonMBeanStopMethod;
   }

   public void setTargetStopMethod(String targetStopMethod)
      throws InvalidParameterException
   {
      if (targetStopMethod != null)
         mSingletonMBeanStopMethod = targetStopMethod;
   }

   public String getTargetStartMethodArgument()
   {
      return mSingletonMBeanStartMethodArgument ;
   }

   public void setTargetStartMethodArgument(String targetStartMethodArgument)
   {
      mSingletonMBeanStartMethodArgument = targetStartMethodArgument;
   }

   public String getTargetStopMethodArgument()
   {
      return mSingletonMBeanStopMethodArgument ;
   }

   public void setTargetStopMethodArgument(String targetStopMethodArgument)
   {
      mSingletonMBeanStopMethodArgument =  targetStopMethodArgument;
   }
  
   // HASingleton implementation ------------------------------------
   
   /**
    * Call the target start method
    * 
    * @see org.jboss.ha.singleton.HASingletonSupport#startSingleton()
    */
   public void startSingleton()
   {
      super.startSingleton();

      try
      {
         invokeSingletonMBeanMethod(
            mSingletonMBean,
            mSingletonMBeanStartMethod,
            mSingletonMBeanStartMethodArgument 
            );
      }
      catch (JMException jme)
      {
         log.error("Controlled Singleton MBean failed to become master", jme);
      }
   }

   /**
    * Call the target stop method
    * 
    * @see org.jboss.ha.singleton.HASingletonSupport#stopSingleton()
    */
   public void stopSingleton()
   {
      super.stopSingleton();

      try
      {
         invokeSingletonMBeanMethod(
            mSingletonMBean,
            mSingletonMBeanStopMethod,
            mSingletonMBeanStopMethodArgument
            );
      }
      catch (JMException jme)
      {
         log.error("Controlled Singleton MBean failed to resign from master position", jme);
      }
   }

   // Protected -----------------------------------------------------
   
   protected Object invokeSingletonMBeanMethod(ObjectName target,
      String operationName, Object param)
      throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      if (target != null && operationName != null)
      {
         Object[] params;
         String[] signature;
         
         if (param != null) 
         {
            params = new Object[] { param };
            signature = new String[] { param.getClass().getName() };
            
            log.debug("Calling operation: " + operationName +
                  "(" + param + "), on target: '" + target + "'");            
         }
         else
         {
            params = NO_ARGS;
            signature = NO_TYPES;
            
            log.debug("Calling operation: " + operationName + 
                  "(), on target: '" + target + "'");               
         }

         return server.invoke(target, operationName, params, signature);
      }
      else
      {
         log.debug("No configured target mbean or operation to call");
         
         return null;
      }
   }
}
