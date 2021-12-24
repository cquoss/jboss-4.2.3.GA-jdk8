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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.jws.WebService;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.LocalHomeBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.ejb.RemoteHomeBinding;
import org.jboss.aop.Advisor;
import org.jboss.ejb.LocalImpl;
import org.jboss.ejb.RemoteImpl;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 66586 $
 */
public class ProxyFactoryHelper
{
   private static final Logger log = Logger.getLogger(ProxyFactoryHelper.class);

   public static Context getProxyFactoryContext(Context ctx)
           throws NamingException
   {

      try
      {
         return (Context) ctx.lookup("proxyFactory");
      }
      catch (NameNotFoundException e)
      {
         return ctx.createSubcontext("proxyFactory");
      }
   }

   public static String getEndpointInterface(Container container)
   {
      WebService ws = (javax.jws.WebService) ((EJBContainer) container).resolveAnnotation(javax.jws.WebService.class);
      if (ws != null)
      {
         return ws.endpointInterface();
      }
      return null;

   }

   public static Class<?>[] getLocalInterfaces(Container container)
   {
      // Obtain @Local
      Local localAnnotation = (Local)((EJBContainer) container).resolveAnnotation(javax.ejb.Local.class);

      // Obtain @Remote
      Remote remoteAnnotation = (Remote)((EJBContainer) container).resolveAnnotation(Remote.class);

      // Obtain all business interfaces
      List<Class<?>> businessInterfaces = getBusinessInterfaces(container.getBeanClass());

      // JIRA EJBTHREE-1062
      // EJB 3 4.6.6
      // If no @Local is defined on the bean class, and the bean class implements a single interface, 
      // this interface is a local business interface unless denoted otherwise
      if (localAnnotation == null && container.getBeanClass().getInterfaces().length == 1)
      {
         // Obtain the implemented interface
         Class<?> singleInterface =  container.getBeanClass().getInterfaces()[0];
         
         // If not explicitly marked as @Remote, and is a valid business interface
         if (remoteAnnotation==null
               && businessInterfaces.contains(singleInterface))
         {
            // Return the implemented interface  
            return container.getBeanClass().getInterfaces();            
         }
      }

      // If @Local is present
      if (localAnnotation != null)
      {
         // If @Local.value is defined
         if (localAnnotation.value().length > 0)
         {
            // Return the value array defined
            return localAnnotation.value();
         }
  
         // If @Local is defined with no value and there are no business interfaces
         if (businessInterfaces.size() == 0){
            throw new RuntimeException("Use of empty @Local on bean class and there are no valid business interfaces: " + container.getEjbName());            
         }
         // If @Local is defined with no value and there is more than one business interface 
         else if (businessInterfaces.size() > 0)
         {
            // Define list to hold all interfaces implemented directly by bean class that are valid business interfaces
            List<Class<?>> beanClassBusinessInterfaces = new ArrayList<Class<?>>();
            // All business interfaces
            for(Class<?> businessInterface : businessInterfaces)
            {
               // All interfaces directly implemented by bean class
               for(Class<?> beanClassInterface : container.getBeanClass().getInterfaces())
               {
                  // If interface directly implemented by bean class is business interface
                  if(businessInterface.equals(beanClassInterface))
                  {
                     // Add to list
                     beanClassBusinessInterfaces.add(businessInterface);
                  }
               }
            }
            
            // If more than one business interface is directly implemented by the bean class
            if(beanClassBusinessInterfaces.size()>1)
            {
               throw new RuntimeException("Use of empty @Local on bean class and there are more than one default interface: " + container.getEjbName());
            }
            // JIRA EJBTHREE-1062
            // EJB 3 4.6.6
            // If the bean class implements only one business interface, that 
            //interface is exposed as local business if not denoted as @Remote
            else
            {
               // If not explicitly marked as @Remote
               if (remoteAnnotation == null)
               {
                  // Return the implemented interface  
                  return beanClassBusinessInterfaces.toArray(new Class<?>[]
                  {});
               }

            }
         }
         
         Class[] rtn = {(Class) businessInterfaces.get(0)};
         localAnnotation = new LocalImpl(rtn);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, localAnnotation);
         return rtn;
      }

      Class beanClass = container.getBeanClass();
      String endpoint = getEndpointInterface(container);
      Class[] remoteInterfaces = getRemoteInterfaces(container);

      if (localAnnotation == null && (remoteInterfaces != null && remoteInterfaces.length == 0) && endpoint == null
            && (beanClass.getInterfaces() == null || beanClass.getInterfaces().length == 0))
         throw new RuntimeException("bean class has no local, webservice, or remote interfaces defined and does not implement at least one business interface: " + container.getEjbName());

      // introspect implemented interfaces.
      if (localAnnotation == null)
      {
         List<Class<?>> intfs = getBusinessInterfaces(beanClass);
         ArrayList<Class> locals = new ArrayList<Class>();
         for (Class clazz : intfs)
         {
            if (clazz.isAnnotationPresent(javax.ejb.Local.class))
            {
               locals.add(clazz);
            }
         }
         if (locals.size() > 0)
         {
            localAnnotation = new LocalImpl(locals.toArray(new Class[]{}));
            ((Advisor) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, localAnnotation);
            //return li.value(); ALR Removed (EJBTHREE-751)
         }
      }
      // no @Local interfaces implemented
      if (localAnnotation == null)
      {
         // search for default
         List<Class<?>> interfaces = getBusinessInterfaces(beanClass);
         if (interfaces.size() != 1) return null; // indeterminate

         Class intf = interfaces.get(0);
         if (remoteInterfaces != null)
         {
            for (Class rintf : remoteInterfaces)
            {
               if (intf.getName().equals(rintf.getName()))
               {
                  return null;
               }
            }
         }
         if (intf.getName().equals(endpoint)) return null;

         Class[] rtn = {intf};
         localAnnotation = new LocalImpl(rtn);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, localAnnotation);
         return rtn;
      }
      

      // Check to ensure @Local and @Remote are not defined on the same interface
      // JIRA EJBTHREE-751
      if (remoteInterfaces != null)
      {
         for (Class<?> remoteInterface : remoteInterfaces)
         {
            for (Class<?> localInterface : localAnnotation.value())
            {
               if (localInterface.equals(remoteInterface))
               {
                  throw new RuntimeException("@Remote and @Local may not both be specified on the same interface \""
                        + remoteInterface.toString() + "\" per EJB3 Spec 4.6.7, Bullet 5.4");
               }
            }
         }
      }
      
      return localAnnotation.value();
   }

   /**
    * Resolve the potential business interfaces on an enterprise bean.
    * Returns all interfaces implemented by this class and it's supers which
    * are potentially a business interface.
    *
    * Note: for normal operation call container.getBusinessInterfaces().
    *
    * @param    beanClass   the EJB implementation class
    * @return   a list of potential business interfaces
    * @see      org.jboss.ejb3.EJBContainer#getBusinessInterfaces()
    */
   public static List<Class<?>> getBusinessInterfaces(Class<?> beanClass)
   {
      // Obtain all business interfaces implemented by this bean class and its superclasses
      return getBusinessInterfaces(beanClass, new ArrayList<Class<?>>()); 
   }
   
   private static List<Class<?>> getBusinessInterfaces(Class<?> beanClass, List<Class<?>> interfaces)
   {
      /*
       * 4.6.6:
       * The following interfaces are excluded when determining whether the bean class has
       * more than one interface: java.io.Serializable; java.io.Externaliz-
       * able; any of the interfaces defined by the javax.ejb package.
       */
      for(Class<?> intf : beanClass.getInterfaces())
      {
         if(intf.equals(java.io.Externalizable.class))
            continue;
         if(intf.equals(java.io.Serializable.class))
            continue;
         if(intf.getName().startsWith("javax.ejb"))
            continue;
         
         // FIXME Other aop frameworks might add other interfaces, this should really be configurable
         if(intf.getName().startsWith("org.jboss.aop"))
            continue;
         
         interfaces.add(intf);
      }

      // If there's no superclass, return
      if (beanClass.getSuperclass() == null)
      {
         return interfaces;
      }
      else
      {
         // Include any superclasses' interfaces
         return getBusinessInterfaces(beanClass.getSuperclass(), interfaces);
      }
   }

   public static Class getLocalHomeInterface(Container container)
   {
      Class beanClass = container.getBeanClass();
      LocalHome li = (javax.ejb.LocalHome) ((EJBContainer) container).resolveAnnotation(javax.ejb.LocalHome.class);
      if (li != null) return li.value();
      return null;
   }

   public static Class getRemoteHomeInterface(Container container)
   {
      Class beanClass = container.getBeanClass();
      RemoteHome li = (javax.ejb.RemoteHome) ((EJBContainer) container).resolveAnnotation(javax.ejb.RemoteHome.class);
      if (li != null) return li.value();
      return null;
   }

   public static boolean publishesInterface(Container container, Class businessInterface)
   {
      if (!(container instanceof SessionContainer)) return false;
      Class[] remotes = getRemoteInterfaces(container);
      if (remotes != null)
      {
         for (Class intf : remotes)
         {
            if (intf.getName().equals(businessInterface.getName())) return true;
         }
      }

      Class remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
            return true;
         }
      }
      Class[] locals = getLocalInterfaces(container);
      if (locals != null)
      {
         for (Class clazz : locals)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               return true;
            }
         }
      }
      Class localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
            return true;
         }
      }

      return false;
   }
   
   public static String getHomeJndiName(Container container)
   {
      Advisor advisor = (Advisor) container;
      RemoteHomeBinding binding = (RemoteHomeBinding)advisor.resolveAnnotation(RemoteHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();
      
      return container.getEjbName() + "/home";
   }
   
   public static String getLocalHomeJndiName(Container container)
   {
      Advisor advisor = (Advisor) container;
      LocalHomeBinding binding = (LocalHomeBinding)advisor.resolveAnnotation(LocalHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();
      
      return container.getEjbName() + "/localHome";
   }

   public static String getJndiName(Container container, Class businessInterface)
   {
      if (!(container instanceof SessionContainer)) return null;
      Advisor advisor = (Advisor) container;
      Class[] remotes = getRemoteInterfaces(container);
      if (remotes != null)
      {
         for (Class clazz : remotes)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               RemoteBindings bindings = (RemoteBindings) ((EJBContainer)container).getRemoteBindings();
              
               return getRemoteJndiName(container, bindings.value()[0]);
            }
         }
      }
      Class remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
            return getHomeJndiName(container);
         }
      }
      Class[] locals = getLocalInterfaces(container);
      if (locals != null)
      {
         for (Class clazz : locals)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               return getLocalJndiName(container);
            }
         }
      }
      Class localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
            return getLocalHomeJndiName(container);
         }
      }

      return null;
   }

   public static String getLocalJndiName(Container container)
   {
      return getLocalJndiName(container, true);
   }

   public static String getLocalJndiName(Container container, boolean conflictCheck)
   {
      Advisor advisor = (Advisor) container;
      LocalBinding localBinding = (LocalBinding) advisor
              .resolveAnnotation(LocalBinding.class);
      if (localBinding == null)
      {
         String name = container.getEjbName() + "/local";
         DeploymentScope deploymentScope = ((EJBContainer) container).getDeployment().getEar();
         if (deploymentScope != null) return deploymentScope.getBaseName() + "/" + name;

         if (conflictCheck)
            checkForRemoteJndiConflict(container);

         return name;
      }
      else
      {
         return localBinding.jndiBinding();
      }
   }

   private static void checkForRemoteJndiConflict(Container container)
   {
      if (((Advisor) container).resolveAnnotation(Remote.class) != null)
      {
         String remoteJndiName = getRemoteJndiName(container, false);
         String ejbName = container.getEjbName();
         if ((remoteJndiName.equals(ejbName) || remoteJndiName.startsWith(ejbName + "/")) && (!remoteJndiName.equals(ejbName + "/remote")))
            throw new javax.ejb.EJBException("Conflict between default local jndi name " + ejbName + "/local and remote jndi name " + remoteJndiName + " for ejb-name:" + ejbName + ", bean class=" + container.getBeanClass());
      }
   }

   public static Class<?>[] getRemoteInterfaces(Container container)
   {
      Remote ri = (Remote) ((Advisor) container).resolveAnnotation(Remote.class);
      if (ri == null)
      {
         Class beanClass = container.getBeanClass();
         Class[] intfs = ProxyFactoryHelper.getBusinessInterfaces(beanClass).toArray(new Class[]{});
         ArrayList<Class> remotes = new ArrayList<Class>();
         for (Class clazz : intfs)
         {
            if (clazz.isAnnotationPresent(Remote.class))
            {
               remotes.add(clazz);
            }
         }
         if (remotes.size() > 0)
         {
            intfs = remotes.toArray(new Class[remotes.size()]);
            ri = new RemoteImpl(intfs);
            ((Advisor) container).getAnnotations().addClassAnnotation(Remote.class, ri);
            return ri.value();
         }

         return null;
      }

      if (ri.value().length > 0) return ri.value();

      // We have an emtpy @Remote annotated bean class

      List list = getBusinessInterfaces(container.getBeanClass());
      if (list.size() == 0)
         throw new RuntimeException("Use of empty @Remote on bean class and there are no valid business interfaces: " + container.getEjbName());
      if (list.size() > 1)
         throw new RuntimeException("Use of empty @Remote on bean class and there are more than one default interface: " + container.getEjbName());
      Class[] rtn = {(Class) list.get(0)};
      ri = new RemoteImpl(rtn);
      ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Remote.class, ri);
      return rtn;
   }

   public static String getRemoteJndiName(Container container)
   {
      return getRemoteJndiName(container, true);
   }

   public static String getRemoteJndiName(Container container, boolean check)
   {
      Advisor advisor = (Advisor) container;
      RemoteBinding binding = (RemoteBinding) advisor
              .resolveAnnotation(RemoteBinding.class);
      
      if (binding == null)
      {
         RemoteBindings bindings = (RemoteBindings) advisor.resolveAnnotation(RemoteBindings.class);
         if (bindings != null)
            binding = bindings.value()[0];
      }
      
      return getRemoteJndiName(container, binding);
   }

   private static void checkForLocalJndiConflict(Container container)
   {
      if (((Advisor) container).resolveAnnotation(Local.class) != null)
      {
         String localJndiName = getLocalJndiName(container, false);
         String ejbName = container.getEjbName();
         if ((localJndiName.equals(ejbName) || localJndiName.startsWith(ejbName + "/")) && (!localJndiName.equals(ejbName + "/local")))
            throw new javax.ejb.EJBException("Conflict between default remote jndi name " + ejbName + "/remote and local jndi name " + localJndiName + " for ejb-name:" + ejbName + ", bean class=" + container.getBeanClass());

      }
   }

   public static String getRemoteJndiName(Container container, RemoteBinding binding)
   {
      return getRemoteJndiName(container, binding, true);
   }

   public static String getRemoteJndiName(Container container, RemoteBinding binding, boolean conflictCheck)
   {
      String jndiName = null;
      if (binding == null || binding.jndiBinding() == null || binding.jndiBinding().equals(""))
      {
         jndiName = getDefaultRemoteJndiName(container);

         if (conflictCheck)
            checkForLocalJndiConflict(container);
      }
      else
      {
         jndiName = binding.jndiBinding();
      }

      return jndiName;
   }

   public static String getDefaultRemoteJndiName(Container container)
   {
      String name = container.getEjbName() + "/remote";
      DeploymentScope deploymentScope = ((EJBContainer) container).getDeployment().getEar();
      if (deploymentScope != null) return deploymentScope.getBaseName() + "/" + name;
      return name;
   }
   
   public static String getClientBindUrl(RemoteBinding binding) throws Exception
   {
      String clientBindUrl = binding.clientBindUrl();
      if (clientBindUrl.trim().length() == 0)
      {
         ObjectName connectionON = new ObjectName("jboss.remoting:type=Connector,name=DefaultEjb3Connector,handler=ejb3");
         KernelAbstraction kernelAbstraction = KernelAbstractionFactory.getInstance();
         try
         {
            clientBindUrl = (String)kernelAbstraction.getAttribute(connectionON, "InvokerLocator");
         }
         catch (Exception e)
         {
            clientBindUrl = RemoteProxyFactory.DEFAULT_CLIENT_BINDING;
         }
      }
      
      return clientBindUrl;
   }
}