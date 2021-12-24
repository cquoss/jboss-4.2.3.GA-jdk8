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
package org.jboss.injection;

// $Id: WebServiceRefHandler.java 69023 2008-01-16 10:49:40Z heiko.braun@jboss.com $

import org.jboss.logging.Logger;
import org.jboss.metadata.serviceref.ServiceRefDelegate;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;

import javax.naming.Context;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Handle @WebServiceRef annotations
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class WebServiceRefHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(WebServiceRefHandler.class);
   private Map<String, ServiceRefMetaData> srefMap = new HashMap<String, ServiceRefMetaData>();

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getServiceRefs() == null) return;
      for (ServiceRefMetaData sref : xml.getServiceRefs())
      {
         log.debug("@WebServiceRef override: " + sref);
         if (srefMap.get(sref.getServiceRefName()) != null)
               throw new IllegalStateException ("Duplicate <service-ref-name> in " + sref);
         
         srefMap.put(sref.getServiceRefName(), sref);
      }
   }

   public void handleClassAnnotations(Class type, InjectionContainer container)
   {
      WebServiceRef wsref = container.getAnnotation(WebServiceRef.class, type);
      if (wsref != null)
      {
         bindRefOnType(type, container, wsref);
      }

      WebServiceRefs refs = container.getAnnotation(WebServiceRefs.class, type);
      if (refs != null)
      {
         for (WebServiceRef refItem : refs.value())
         {
            bindRefOnType(type, container, refItem);
         }
      }
   }

   private void bindRefOnType(Class type, InjectionContainer container, WebServiceRef wsref)
   {
      String name = wsref.name();
      if (name.equals(""))
         name = InjectionUtil.getEncName(type).substring(4);
      
      if (!container.getEncInjectors().containsKey(name))
      {
         String encName = "env/" + name;
         ServiceRefMetaData sref = getServiceRefForName(name);
         container.getEncInjectors().put(name, new WebServiceRefInjector(encName, type, sref));
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {      
      String serviceRefName = null;

      // injector first
      ServiceRefMetaData tmp = getServiceRefForInjectionTarget(method);
      if(tmp!=null)
      {
         serviceRefName = tmp.getServiceRefName();
      }
      else
      {
         // annotation second
         WebServiceRef wsref = method.getAnnotation(WebServiceRef.class);
         if(wsref!=null)
         {
            serviceRefName = wsref.name();

            if (serviceRefName.equals(""))
               serviceRefName = InjectionUtil.getEncName(method).substring(4);
         }         
      }

      if(null==serviceRefName)
         return;

      if (!method.getName().startsWith("set"))
      {
         throw new RuntimeException("@WebServiceRef can only be used with a set method: " + method);
      }

      String encName = "env/" + serviceRefName;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(serviceRefName))
      {
         ServiceRefMetaData sref = getServiceRefForName(serviceRefName);
         container.getEncInjectors().put(serviceRefName, new WebServiceRefInjector(encName, method, sref));
      }

      injectors.put(method, new JndiMethodInjector(method, encName, encCtx));
   }

   private ServiceRefMetaData getServiceRefForInjectionTarget(Method method)
   {
      ServiceRefMetaData match = null;
      Iterator<String> iterator = srefMap.keySet().iterator();
      while(iterator.hasNext())
      {
         ServiceRefMetaData sref = srefMap.get(iterator.next());
         for(String[] injectionTuple : sref.getInjectionTargets())
         {
            if(method.getDeclaringClass().getName().equals(injectionTuple[0])
              && method.getName().equals(injectionTuple[1]))
            {
               match = sref;
               break;
            }
         }
      }

      return match;
   }

   private ServiceRefMetaData getServiceRefForInjectionTarget(Field field)
   {
      ServiceRefMetaData match = null;
      Iterator<String> iterator = srefMap.keySet().iterator();
      while(iterator.hasNext())
      {
         ServiceRefMetaData sref = srefMap.get(iterator.next());
         for(String[] injectionTuple : sref.getInjectionTargets())
         {
            if(field.getDeclaringClass().getName().equals(injectionTuple[0])
              && field.getName().equals(injectionTuple[1]))
            {
               match = sref;
               break;
            }
         }
      }

      return match;
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      String serviceRefName = null;

      // injector first
      ServiceRefMetaData tmp = getServiceRefForInjectionTarget(field);
      if(tmp!=null)
      {
         serviceRefName = tmp.getServiceRefName();
      }
      else
      {
         // annotation second
         WebServiceRef wsref = field.getAnnotation(WebServiceRef.class);
         if(wsref!=null)
         {
            serviceRefName = wsref.name();

            if (serviceRefName.equals(""))
               serviceRefName = InjectionUtil.getEncName(field).substring(4);
         }
      }

      if(null==serviceRefName)
         return;

      String encName = "env/" + serviceRefName;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(serviceRefName))
      {
         ServiceRefMetaData sref = getServiceRefForName(serviceRefName);
         container.getEncInjectors().put(serviceRefName, new WebServiceRefInjector(encName, field, sref));
      }

      injectors.put(field, new JndiFieldInjector(field, encName, encCtx));
   }

   private ServiceRefMetaData getServiceRefForName(String name)
   {
      ServiceRefMetaData sref = srefMap.get(name);
      if (sref == null)
      {
         log.debug("No override for @WebServiceRef.name: " + name);
         sref = new ServiceRefDelegate().newServiceRefMetaData();
         sref.setServiceRefName(name);
      }
      return sref;
   }
}
