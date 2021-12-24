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
package org.jboss.mq.il.jvm;


/**
 *  The RMI implementation of the ConnectionReceiver object
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public class JVMClientILService implements org.jboss.mq.il.ClientILService {

   //the client IL
   JVMClientIL      clientIL;

   /**
    *  getClientIL method comment.
    *
    * @return                          The ClientIL value
    * @exception  java.lang.Exception  Description of Exception
    */
   public org.jboss.mq.il.ClientIL getClientIL()
      throws java.lang.Exception {
      return clientIL;
   }


   /**
    *  start method comment.
    *
    * @exception  java.lang.Exception  Description of Exception
    */
   public void start()
      throws java.lang.Exception {
      clientIL.stopped = false;
   }

   /**
    * @exception  java.lang.Exception  Description of Exception
    */
   public void stop()
      throws java.lang.Exception {
      clientIL.stopped = true;
   }

   /**
    *  init method comment.
    *
    * @param  connection               Description of Parameter
    * @param  props                    Description of Parameter
    * @exception  java.lang.Exception  Description of Exception
    */
   public void init( org.jboss.mq.Connection connection, java.util.Properties props )
      throws java.lang.Exception {
      clientIL = new JVMClientIL( connection );
   }
}
