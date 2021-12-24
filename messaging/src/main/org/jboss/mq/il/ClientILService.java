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
package org.jboss.mq.il;

import org.jboss.mq.Connection;

/**
 *  This class manages the lifecycle of an instance of the ClientIL
 *  Implementations of this class should have a default constructor.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public interface ClientILService {

   /**
    *  After construction, the ClientILService is initialized with a reference
    *  to the Connection object and the connection properties.
    *
    * @param  connection     Description of Parameter
    * @param  props          Description of Parameter
    * @exception  Exception  Description of Exception
    */
   public void init( Connection connection, java.util.Properties props )
      throws Exception;

   /**
    *  After initialization, this method may be called to obtain a reference to
    *  a ClientIL object. This object will eventually be serialized and sent to
    *  the server so that he can invoke methods on connection it was initialized
    *  with.
    *
    * @return                The ClientIL value
    * @exception  Exception  Description of Exception
    */
   public ClientIL getClientIL()
      throws Exception;


   /**
    *  Once started, the ClientIL instance should process all server requests.
    *
    * @exception  Exception  Description of Exception
    */
   public void start()
      throws Exception;

   /**
    *  Once stopped, the ClientIL instance stop processing all server requests.
    *  if( cr_server != null ) cr_server.close(); if (cr!=null && cr instanceof
    *  java.rmi.Remote) { java.rmi.server.UnicastRemoteObject.unexportObject((java.rmi.Remote)cr,
    *  true); }
    *
    * @exception  Exception  Description of Exception
    */

   public void stop()
      throws Exception;
}
