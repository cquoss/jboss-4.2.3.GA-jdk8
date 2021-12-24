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
package org.jboss.ejb3.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;

import javax.ejb.EJBException;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.logging.Logger;
import org.jboss.ejb3.proxy.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.handle.HomeHandleImpl;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 64585 $
 */
public abstract class BaseSessionProxyFactory implements ProxyFactory, Externalizable
{
   private static final Logger log = Logger.getLogger(BaseSessionProxyFactory.class);
   
   private transient Container container;
   protected String containerGuid;
   protected String containerClusterUid;
   protected boolean isClustered = false;
   protected String jndiName;
   protected Class proxyClass;
   protected transient Constructor proxyConstructor;
  
   public Object createHomeProxy()
   {
      throw new RuntimeException("NYI");
   }
   
   public void setContainer(Container container)
   {
      this.container = container;
      this.containerGuid = Ejb3Registry.guid(container);
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      this.isClustered = container.isClustered();
   }
   
   protected Container getContainer()
   {
      if (container == null)
      {
         container = Ejb3Registry.findContainer(containerGuid);
         
         if (container == null && isClustered)
            container = Ejb3Registry.getClusterContainer(containerClusterUid);
      }
      
      return container;
   }
   
   protected void setEjb21Objects(BaseSessionRemoteProxy proxy)
   {
      proxy.setHandle(getHandle());
      proxy.setHomeHandle(getHomeHandle());
      proxy.setEjbMetaData(getEjbMetaData());
   }
   
   protected HomeHandle getHomeHandle()
   {
      EJBContainer ejbContainer = (EJBContainer)getContainer();
      
      HomeHandleImpl homeHandle = null;
      
      RemoteBinding remoteBindingAnnotation = (RemoteBinding)ejbContainer.resolveAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(getContainer()));
      
      return homeHandle;
   }
   
   protected EJBMetaData getEjbMetaData()
   {
      Class remote = null;
      Class home = null;
      Class pkClass = Object.class;
      HomeHandleImpl homeHandle = null;
      
      EJBContainer ejbContainer = (EJBContainer)getContainer();
      
      Remote remoteAnnotation = (Remote)ejbContainer.resolveAnnotation(Remote.class);
      if (remoteAnnotation != null)
         remote = remoteAnnotation.value()[0];
      RemoteHome homeAnnotation = (RemoteHome)ejbContainer.resolveAnnotation(RemoteHome.class);
      if (homeAnnotation != null)
         home = homeAnnotation.value();
      RemoteBinding remoteBindingAnnotation = (RemoteBinding)ejbContainer.resolveAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(remoteBindingAnnotation.jndiBinding());
      
      EJBMetaDataImpl metadata = new EJBMetaDataImpl(remote, home, pkClass, true, false, homeHandle);
      
      return metadata;
   }
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      containerGuid = in.readUTF();
      containerClusterUid = in.readUTF();
      isClustered = in.readBoolean();
      jndiName = in.readUTF();
      proxyClass = (Class)in.readObject();
      
      try
      {
         if (getContainer() == null)
            throw new EJBException("Invalid (i.e. remote) invocation of local interface (null container) for " + containerGuid);
         else
         {
            Class[] interfaces = getInterfaces();
            Class proxyClass = java.lang.reflect.Proxy.getProxyClass(getContainer().getBeanClass().getClassLoader(), interfaces);
            final Class[] constructorParams = {InvocationHandler.class};
            proxyConstructor = proxyClass.getConstructor(constructorParams);
         }
         
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("Unable to read Externalized proxy " + e);
      }
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(containerGuid);
      out.writeUTF(containerClusterUid);
      out.writeBoolean(isClustered);
      out.writeUTF(jndiName);
      out.writeObject(proxyClass);
   }
   
   protected abstract Class[] getInterfaces();
   
   abstract protected Handle getHandle();
}
