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
package org.jboss.mq.il.oil;

/**
 * 
 *
 * @author    Brian Weaver (weave@opennms.org)
 * @version   $Revision: 57198 $
 * @created   January 10, 2002
 */
final class OILConstants
{
   // Success and error conditions are 
   // defined here
   //
   final static int OK                             = 0;
   final static int SUCCESS                        = 0;
   final static int OK_OBJECT                      = 1;
   final static int SUCCESS_OBJECT                 = 1;
   final static int EXCEPTION                      = 2;

   // The "message" codes start here.
   //
   final static int ACKNOWLEDGE                    = 8;
   final static int ADD_MESSAGE                    = 9;
   final static int BROWSE                         = 10;
   final static int CHECK_ID                       = 11;
   final static int CONNECTION_CLOSING             = 12;
   final static int CREATE_QUEUE                   = 13;
   final static int CREATE_TOPIC                   = 14;
   final static int DELETE_TEMPORARY_DESTINATION   = 15;
   final static int GET_ID                         = 16;
   final static int GET_TEMPORARY_QUEUE            = 17;
   final static int GET_TEMPORARY_TOPIC            = 18;
   final static int RECEIVE                        = 19;
   final static int SET_ENABLED                    = 20;
   final static int SET_SPY_DISTRIBUTED_CONNECTION = 21;
   final static int SUBSCRIBE                      = 22;
   final static int TRANSACT                       = 23;
   final static int UNSUBSCRIBE                    = 24;
   final static int DESTROY_SUBSCRIPTION           = 25;
   final static int CHECK_USER                     = 26;
   final static int PING                           = 27;
   final static int PONG                           = 28;
   final static int CLOSE                          = 29;
   final static int AUTHENTICATE                   = 30;
}
/*
vim:tabstop=3:expandtab:shiftwidth=3
*/
