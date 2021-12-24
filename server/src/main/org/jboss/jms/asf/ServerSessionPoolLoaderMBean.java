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
package org.jboss.jms.asf;

import javax.management.ObjectName;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57209 $
 */
public interface ServerSessionPoolLoaderMBean extends org.jboss.system.ServiceMBean
{
   /**
    * Set the pool name.
    * 
    * @param name The pool name.
    */
   void setPoolName(String name);

   /**
    * Get the pool name.
    * 
    * @return The pool name.
    */
   String getPoolName();

   /**
    * Set the classname of pool factory to use.
    * 
    * @param classname The name of the pool factory class.
    */
   void setPoolFactoryClass(String classname);

   /**
    * Get the classname of pool factory to use.
    * 
    * @return The name of the pool factory class.
    */
   String getPoolFactoryClass();

   /**
    * mbean get-set pair for field xidFactory Get the value of xidFactory
    * 
    * @return value of xidFactory
    */
   ObjectName getXidFactory();

   /**
    * Set the value of xidFactory
    * 
    * @param xidFactory Value to assign to xidFactory
    */
   void setXidFactory(ObjectName xidFactory);
}
