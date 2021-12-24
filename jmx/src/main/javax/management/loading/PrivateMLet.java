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

import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 *
 * @since JMX 1.2
 *
 * @see javax.management.loading.ClassLoaderRepository
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 */
public class PrivateMLet 
   extends MLet implements PrivateClassLoader
{

   private static final long serialVersionUID = 2503458973393711979L;

   public PrivateMLet(URL[] urls, boolean delegateToCLR) 
   {
      super(urls, delegateToCLR);
   }
   
   public PrivateMLet(URL[] urls, ClassLoader parent, boolean delegateToCLR) 
   {
      super(urls, parent, delegateToCLR);
   }
   
   public PrivateMLet(URL[] urls, ClassLoader parent,
                      URLStreamHandlerFactory factory, boolean delegateToCLR)
   {
      super(urls, parent, factory, delegateToCLR);
   }
   
}

