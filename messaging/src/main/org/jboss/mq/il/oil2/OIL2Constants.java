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
package org.jboss.mq.il.oil2;

/**
 * 
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author    Brian Weaver (weave@opennms.org)
 * @version   $Revision: 1.2 $
 */
interface OIL2Constants
{
   // Success and error conditions are 
   // defined here
   //
   final static byte RESULT_VOID                           = 1;
   final static byte RESULT_OBJECT                         = 2;
   final static byte RESULT_EXCEPTION                      = 3;

   // The "message" codes start here.
   //
   final static byte CLIENT_RECEIVE                        = -1;
   final static byte CLIENT_DELETE_TEMPORARY_DESTINATION   = -2;
   final static byte CLIENT_CLOSE                          = -3;
   final static byte CLIENT_PONG                           = -4;
   
   final static byte SERVER_ACKNOWLEDGE                    = 1;
   final static byte SERVER_ADD_MESSAGE                    = 2;
   final static byte SERVER_BROWSE                         = 3;
   final static byte SERVER_CHECK_ID                       = 4;
   final static byte SERVER_CONNECTION_CLOSING             = 5;
   final static byte SERVER_CREATE_QUEUE                   = 6;
   final static byte SERVER_CREATE_TOPIC                   = 7;
   final static byte SERVER_DELETE_TEMPORARY_DESTINATION   = 8;
   final static byte SERVER_GET_ID                         = 9;
   final static byte SERVER_GET_TEMPORARY_QUEUE            = 10;
   final static byte SERVER_GET_TEMPORARY_TOPIC            = 11;
   final static byte SERVER_RECEIVE                        = 12;
   final static byte SERVER_SET_ENABLED                    = 13;
   final static byte SERVER_SET_SPY_DISTRIBUTED_CONNECTION = 14;
   final static byte SERVER_SUBSCRIBE                      = 15;
   final static byte SERVER_TRANSACT                       = 16;
   final static byte SERVER_UNSUBSCRIBE                    = 17;
   final static byte SERVER_DESTROY_SUBSCRIPTION           = 18;
   final static byte SERVER_CHECK_USER                     = 19;
   final static byte SERVER_PING                           = 20;
   final static byte SERVER_CLOSE                          = 21;
   final static byte SERVER_AUTHENTICATE                   = 22;
}
/*
vim:tabstop=3:expandtab:shiftwidth=3
*/
