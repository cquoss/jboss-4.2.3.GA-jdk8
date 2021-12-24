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
package org.jboss.services.binding;

/** A ServiceConfig is a mapping from an mbean service name to its
 * ServiceBindings.
 *
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57210 $
 */
public class ServiceConfig implements Cloneable
{
   /** The javax.management.ObjectName string of the service the config
    applies to.
    */
   private String serviceName;
   /** The ServicesConfigDelegate implementation class
    */
   private String serviceConfigDelegateClassName;
   /** An aribtrary object used to configure the behavior of
    the ServicesConfigDelegate. An example would be an XML Element.
    */
   private Object serviceConfigDelegateConfig;
   /** The bindings associated with the service
    */
   private ServiceBinding[] bindings;

   /** Creates a new instance of ServiceConfig */
   public ServiceConfig()
   {
   }

   /** Make a deep copy of the ServiceConfig bindings
    */
   public Object clone()
   {
      ServiceConfig copy = new ServiceConfig();
      // Immutable so no reason to copy
      copy.serviceName = serviceName;
      copy.serviceConfigDelegateClassName = serviceConfigDelegateClassName;
      int length = bindings != null ? bindings.length : 0;
      copy.bindings = new ServiceBinding[length];
      for(int b = 0; b < length; b ++)
      {
         copy.bindings[b] = (ServiceBinding) bindings[b].clone();
      }
      return copy;
   }

   /** Getter for property serviceName.
    * @return Value of property serviceName.
    */
   public String getServiceName()
   {
      return this.serviceName;
   }

   /** Setter for property serviceName.
    * @param serviceName New value of property serviceName.
    */
   public void setServiceName(String serviceName)
   {
      this.serviceName = serviceName;
   }

   /** Getter for property port.
    * @return Value of property port.
    */
   public ServiceBinding[] getBindings()
   {
      return this.bindings;
   }

   /** Setter for property port.
    * @param port New value of property port.
    */
   public void setBindings(ServiceBinding[] bindings)
   {
      this.bindings = bindings;
   }

   /** Getter for property serviceConfigDelegateClassName.
    * @return Value of property serviceConfigDelegateClassName.
    */
   public String getServiceConfigDelegateClassName()
   {
      return serviceConfigDelegateClassName;
   }

   /** Setter for property serviceConfigDelegateClassName.
    * @param serviceConfigDelegateClassName New value of property serviceConfigDelegateClassName.
    */
   public void setServiceConfigDelegateClassName(String serviceConfigDelegateClassName)
   {
      this.serviceConfigDelegateClassName = serviceConfigDelegateClassName;
   }

   /** Getter for property serviceConfigDelegateConfig.
    * @return Value of property serviceConfigDelegateConfig.
    */
   public Object getServiceConfigDelegateConfig()
   {
      return serviceConfigDelegateConfig;
   }

   /** Setter for property serviceConfigDelegateConfig.
    * @param serviceConfigDelegateConfig New value of property serviceConfigDelegateConfig.
    */
   public void setServiceConfigDelegateConfig(Object serviceConfigDelegateConfig)
   {
      this.serviceConfigDelegateConfig = serviceConfigDelegateConfig;
   }

   /** Equality is based on the serviceName string
    */
   public boolean equals(Object obj)
   {
      boolean equals = false;
      if( obj instanceof ServiceConfig )
      {
         ServiceConfig sc = (ServiceConfig) obj;
         equals = this.serviceName.equals(sc.serviceName);
      }
      else
      {
         equals = super.equals(obj);
      }
      return equals;
   }

   /** The hash code is based on the serviceName string hashCode.
    */
   public int hashCode()
   {
      int hashCode = serviceName == null ? 0 : serviceName.hashCode();
      return hashCode;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("ServiceConfig(name=");
      buffer.append(serviceName);
      buffer.append("), bindings=");
      int length = bindings != null ? bindings.length : 0;
      for(int b = 0; b < length; b ++)
      {
         buffer.append("\n" + bindings[b].toString());
      }
      return buffer.toString();
   }
   
}
