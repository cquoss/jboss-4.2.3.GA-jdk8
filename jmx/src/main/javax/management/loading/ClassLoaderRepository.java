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

/**
 * A classloader repository.<p>
 *
 * A loader repository per MBeanServer.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>.
 *
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020224 Juha Lindfors:</b>
 * <ul>
 * <li>Updated to match JMX 1.2 specification.</li>
 * </ul>
 */
public interface ClassLoaderRepository
{
   /**
    * Loads a class from the repository. This method attempts to load the class
    * using all the classloader registered to the repository.
    *
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   Class loadClass(String className) throws ClassNotFoundException;

   /**
    * Loads a class from the repository, excluding the given
    * classloader. 
    *
    * @param loader the classloader to exclude
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   Class loadClassWithout(ClassLoader loader, String className)
      throws ClassNotFoundException;
   
   /**
    * Loads a class from the repository, using the classloaders that were
    * registered before the given classloader. 
    *
    * @param   stop      consult all the classloaders registered before this one
    *                    in an attempt to load a class
    * @param   className name of the class to load
    *
    * @return  loaded class instance
    *
    * @throws ClassNotFoundException if none of the consulted classloaders were
    *         able to load the requested class
    */
   Class loadClassBefore(ClassLoader stop, String className)
      throws ClassNotFoundException;      
}
