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
package javax.management;

/**
 * This interface is implemented by an MBean that wants to perform
 * operations pre and post registration and deregistration.<p>
 *
 * The preRegister method is called by the MBeanServer before registration.<p>
 *
 * The postRegister method is called by the MBeanServer after registration.<p>
 *
 * The preDeregister method is called by the MBeanServer before deregistration.<p>
 *
 * The postDeregister method is called by the MBeanServer after deregistration.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 */
public interface MBeanRegistration
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * This method is called by the MBeanServer before registration takes
    * place. The MBean is passed a reference of the MBeanServer it is
    * about to be registered with. The MBean must return the ObjectName it
    * will be registered with. The MBeanServer can pass a suggested object
    * depending upon how the MBean is registered.<p>
    *
    * The MBean can stop the registration by throwing an exception.The
    * exception is forwarded to the invoker wrapped in an
    * MBeanRegistrationException.
    *
    * @param MBeanServer the MBeanServer the MBean is about to be
    * registered with.
    * @param ObjectName the suggested ObjectName supplied by the
    * MBeanServer.
    * @return the actual ObjectName to register this MBean with.
    * @exception Exception for any error, the MBean is not registered.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception;

   /**
    * This method is called by the MBeanServer after registration takes
    * place or when registration fails.
    *
    * @param registrationDone the MBeanServer passes true when the
    * MBean was registered, false otherwise.
    */
   public void postRegister(Boolean registrationDone);

   /**
    * This method is called by the MBeanServer before deregistration takes
    * place.<p>
    *
    * The MBean can throw an exception, this will stop the deregistration.
    * The exception is forwarded to the invoker wrapped in
    * an MBeanRegistrationException.
    */
   public void preDeregister()
      throws Exception;

   /**
    * This method is called by the MBeanServer after deregistration takes
    * place.
    */
   public void postDeregister();
}
