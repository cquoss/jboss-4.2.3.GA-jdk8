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
package javax.management.loading;

import javax.management.JMRuntimeException;

/**
 * Keeps the list of Class Loaders registered in the MBean Server. It provides the necessary methods to load classes using the registered Class Loaders.
 * <p/>
 * This deprecated class is maintained for compatibility. In previous versions of JMX, there was one DefaultLoaderRepository
 * shared by all MBean servers. As of JMX 1.2, that functionality is approximated by using MBeanServerFactory.findMBeanServer(java.lang.String)
 * to find all known MBean servers, and consulting the ClassLoaderRepository of each one. It is strongly recommended that
 * code referencing DefaultLoaderRepository be rewritten.
 *
 * @deprecated Use MBeanServer.getClassLoaderRepository() instead.
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 */
public class DefaultLoaderRepository
{
   public static Class loadClass(String className) throws ClassNotFoundException
   {
      throw new JMRuntimeException("Deprecated, use MBeanServer.getClassLoaderRepository() instead");
   }

   public static Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException
   {
      throw new JMRuntimeException("Deprecated, use MBeanServer.getClassLoaderRepository() instead");
   }
}


