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

import org.jboss.annotation.IgnoreDependency;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.BaseEjbRef;
import org.jboss.metamodel.descriptor.EjbLocalRef;
import org.jboss.metamodel.descriptor.EjbRef;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.naming.NameNotFoundException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Searches bean class for all @Inject and create Injectors
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60233 $
 */
public class EJBHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(EJBHandler.class);

   protected void addDependency(String refName, EJBContainer refcon, InjectionContainer container)
   {
      // Do not depend on myself
      if(!container.equals(refcon))
         container.getDependencyPolicy().addDependency(refcon.getObjectName().getCanonicalName());
   }

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml != null)
      {
         if (xml.getEjbLocalRefs() != null) loadEjbLocalXml(xml.getEjbLocalRefs(), container);
         log.trace("ejbRefs = " + xml.getEjbRefs());
         if (xml.getEjbRefs() != null) loadEjbRefXml(xml.getEjbRefs(), container);
      }
   }

   protected void loadEjbLocalXml(Collection<EjbLocalRef> refs, InjectionContainer container)
   {
      for (EjbLocalRef ref : refs)
      {
         String interfaceName = ref.getLocal();
         String errorType = "<ejb-local-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }

   protected void loadEjbRefXml(Collection<EjbRef> refs, InjectionContainer container)
   {
      for (EjbRef ref : refs)
      {
         String interfaceName = ref.getRemote();
         String errorType = "<ejb-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }

   protected void ejbRefXml(BaseEjbRef ref, String interfaceName, InjectionContainer container, String errorType)
   {
      String encName = "env/" + ref.getEjbRefName();
      InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
      if (container.getEncInjectors().containsKey(encName))
         return;

      String mappedName = ref.getMappedName();
      if (mappedName != null && mappedName.equals("")) mappedName = null;

      String link = ref.getEjbLink();
      if (link != null && link.trim().equals("")) link = null;

      Class refClass = null;

      if (interfaceName != null)
      {
         try
         {
            refClass = container.getClassloader().loadClass(interfaceName);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("could not find " + errorType + "'s local interface " + interfaceName + " in " + container.getDeploymentDescriptorType() + " of " + container.getIdentifier());
         }
      }
      
      //----- injectors

      if (mappedName == null && refClass == null && link == null)
      {
         // must be jboss.xml only with @EJB used to define reference.  jboss.xml used to tag for ignore dependency
         // i think it is ok to assume this because the ejb-jar.xml schema should handle any missing elements
      }
      else
      {
         ejbRefEncInjector(mappedName, encName, refClass, link, errorType, container);
         if (ref.isIgnoreDependency())
         {
            log.debug("IGNORING <ejb-ref> DEPENDENCY: " + encName);
            return;
         }

         ejbRefDependency(link, container, refClass, errorType, encName);
      }
   }

   protected void ejbRefDependency(String link, InjectionContainer container, Class refClass, String errorType, String encName)
   {
      EJBContainer refcon = null;

      if (refClass != null && (refClass.equals(Object.class) || refClass.equals(void.class))) refClass = null;

      if (refClass != null)
      {
         if (link != null && !link.trim().equals(""))
         {
            refcon = (EJBContainer) container.resolveEjbContainer(link, refClass);
            if (refcon == null)
            {
               String msg = "IGNORING DEPENDENCY: unable to find " + errorType + " of interface " + refClass.getName() + " and ejbLink of " + link + " in  " + container.getDeploymentDescriptorType() + " of " + container.getIdentifier() + " it might not be deployed yet";
               log.warn(msg);
            }
         }
         else
         {
            try
            {
               refcon = (EJBContainer) container.resolveEjbContainer(refClass);
               if (refcon == null)
               {
                  String msg = "IGNORING DEPENDENCY: unable to find " + errorType + " from interface only " + refClass.getName() + " in " + container.getDeploymentDescriptorType() + " of " + container.getIdentifier();
                  log.warn(msg);
               }
            }
            catch (NameNotFoundException e)
            {
               String msg = "IGNORING DEPENDENCY: unable to find " + errorType + " from interface only " + refClass.getName() + " in " + container.getDeploymentDescriptorType() + " of " + container.getIdentifier() + e.getMessage();
               log.warn(msg);
            }
         }
      }
      else
      {
         String msg = "IGNORING DEPENDENCY: unable to resolve dependency of EJB, there is too little information";
         log.warn(msg);
      }

      if (refcon != null)
      {
         addDependency(encName, refcon, container);
      }
   }

   protected void ejbRefEncInjector(String mappedName, String encName, Class refClass, String link, String errorType, InjectionContainer container)
   {
      if (refClass != null && (refClass.equals(Object.class) || refClass.equals(void.class))) refClass = null;
      if (mappedName != null && mappedName.trim().equals("")) mappedName = null;

      EncInjector injector = null;

      if (mappedName == null)
      {
         injector = new EjbEncInjector(encName, refClass, link, errorType);
      }
      else
      {
         injector = new EjbEncInjector(encName, mappedName, errorType);
      }

      container.getEncInjectors().put(encName, injector);
   }

   public static EJBContainer getEjbContainer(EJB ref, InjectionContainer container, Class memberType)
   {
      EJBContainer rtn = null;

      if (ref.mappedName() != null && !"".equals(ref.mappedName()))
      {
         return null;
      }

      if (ref.beanName().equals("") && memberType == null)
         throw new RuntimeException("For deployment " + container.getIdentifier() + "not enough information for @EJB.  Please fill out the beanName and/or businessInterface attributes");

      Class businessInterface = memberType;
      if (!ref.beanInterface().getName().equals(Object.class.getName()))
      {
         businessInterface = ref.beanInterface();
      }

      if (ref.beanName().equals(""))
      {
         try
         {
            rtn = (EJBContainer) container.resolveEjbContainer(businessInterface);
         }
         catch (NameNotFoundException e)
         {
            log.warn("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ") " + e.getMessage());
         }
      }
      else
      {
         rtn = (EJBContainer) container.resolveEjbContainer(ref.beanName(), businessInterface);
      }

      return rtn;
   }

   public static String getJndiName(EJB ref, InjectionContainer container, Class memberType)
   {
      String jndiName;

      if (ref.mappedName() != null && !"".equals(ref.mappedName()))
      {
         return ref.mappedName();
      }

      if (ref.beanName().equals("") && memberType == null)
         throw new RuntimeException("For deployment " + container.getIdentifier() + "not enough information for @EJB.  Please fill out the beanName and/or businessInterface attributes");

      Class businessInterface = memberType;
      if (!ref.beanInterface().getName().equals(Object.class.getName()))
      {
         businessInterface = ref.beanInterface();
      }

      if (ref.beanName().equals(""))
      {
         try
         {
            jndiName = container.getEjbJndiName(businessInterface);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ") " + e.getMessage());
         }
         if (jndiName == null)
         {
            throw new RuntimeException("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ")");
         }
      }
      else
      {
         jndiName = container.getEjbJndiName(ref.beanName(), businessInterface);
         if (jndiName == null)
         {
            throw new RuntimeException("For EJB " + container.getIdentifier() + "could not find jndi binding based on beanName and business interface for @EJB(" + ref.beanName() + ", " + businessInterface.getName() + ")");
         }
      }

      return jndiName;
   }

   public void handleClassAnnotations(Class clazz, InjectionContainer container)
   {
      EJBs ref = container.getAnnotation(EJBs.class, clazz);
      if (ref != null)
      {
         EJB[] ejbs = ref.value();

         for (EJB ejb : ejbs)
         {
            handleClassAnnotation(ejb, clazz, container);
         }
      }
      EJB ejbref = container.getAnnotation(EJB.class, clazz);
      if (ejbref != null) handleClassAnnotation(ejbref, clazz, container);
   }

   protected void handleClassAnnotation(EJB ejb, Class clazz, InjectionContainer container)
   {
      String encName = ejb.name();
      if (encName == null || encName.equals(""))
      {
         throw new RuntimeException("JBoss requires the name of the @EJB in the @EJBs: " + clazz);
      }
      encName = "env/" + encName;

      if (container.getEncInjectors().containsKey(encName)) return;
      ejbRefEncInjector(ejb.mappedName(), encName, ejb.beanInterface(), ejb.beanName(), "@EJB", container);

      // handle dependencies

      if (isIgnoreDependency(container, ejb))
         log.debug("IGNORING <ejb-ref> DEPENDENCY: " + encName);
      else
         ejbRefDependency(ejb.beanName(), container, ejb.beanInterface(), "@EJB", encName);
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      
      EJB ref = method.getAnnotation(EJB.class);
      if (ref != null)
      {
         if (!method.getName().startsWith("set"))
            throw new RuntimeException("@EJB can only be used with a set method: " + method);
         String encName = ref.name();
         if (encName == null || encName.equals(""))
         {
            encName = InjectionUtil.getEncName(method);
         }
         else
         {
            encName = "env/" + encName;
         }
         if (!container.getEncInjectors().containsKey(encName))
         {
            ejbRefEncInjector(ref.mappedName(), encName, method.getParameterTypes()[0], ref.beanName(), "@EJB", container);
            
            if (container.getAnnotation(IgnoreDependency.class, method) == null)
            {
               if (isIgnoreDependency(container, ref))
                  log.debug("IGNORING <ejb-ref> DEPENDENCY: " + encName);
               else
                  ejbRefDependency(ref.beanName(), container, method.getParameterTypes()[0], "@EJB", encName);
            }
         }

         injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
      }
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      EJB ref = field.getAnnotation(EJB.class);
      if (ref != null)
      {
         String encName = ref.name();
         if (encName == null || encName.equals(""))
         {
            encName = InjectionUtil.getEncName(field);
         }
         else
         {
            encName = "env/" + encName;
         }
         if (!container.getEncInjectors().containsKey(encName))
         {
            if (container.getAnnotation(IgnoreDependency.class, field) == null)
            {
               if (isIgnoreDependency(container, ref))
                  log.debug("IGNORING <ejb-ref> DEPENDENCY: " + encName);
               else
                  ejbRefDependency(ref.beanName(), container, field.getType(), "@EJB", encName);
            }
            ejbRefEncInjector(ref.mappedName(), encName, field.getType(), ref.beanName(), "@EJB", container);
         }
         injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));

      }
   }

   protected boolean isIgnoreDependency(InjectionContainer container, EJB ref)
   {
      EnvironmentRefGroup refGroup =  (EnvironmentRefGroup)container.getEnvironmentRefGroup();
      
      if (refGroup != null)
      {
         Iterator<EjbRef> ejbRefs = refGroup.getEjbRefs().iterator();
         while (ejbRefs.hasNext())
         {
            EjbRef ejbRef = ejbRefs.next();
            if (ejbRef.getEjbRefName().equals(ref.name()))
            {
               if (ejbRef.isIgnoreDependency())
                  return true;
               else
                  return false;
            }
         }
      }
      
      return false;
   }
}
