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
package org.jboss.ejb;

/**
 * This is an interface for Containers that uses EJBProxyFactory.
 *
 * <p>EJBProxyFactory's may communicate with the Container through
 *    this interface.
 *
 * @see EJBProxyFactory
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 57209 $
 */
public interface EJBProxyFactoryContainer
{
   /**
    * ???
    *
    * @return ???
    */
   Class getHomeClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getRemoteClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getLocalHomeClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getLocalClass();
	
   /**
    * ???
    *
    * @return ???
    */
   EJBProxyFactory getProxyFactory();
   
   /**
    * ???
    *
    * @return ???
    */
   LocalProxyFactory getLocalProxyFactory();
}

