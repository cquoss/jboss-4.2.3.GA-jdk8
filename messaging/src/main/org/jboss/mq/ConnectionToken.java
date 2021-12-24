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
package org.jboss.mq;

import java.io.Serializable;

import org.jboss.mq.il.ClientIL;

/**
 * This class is the broker point of view on a SpyConnection (it contains a
 * ConnectionReceiver).
 * 
 * Remember that for most IL's it will be serialized!
 * 
 * @author <a href="Norbert.Lataille@m4x.org">Norbert Lataille</a>
 * @author <a href="Cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author <a href="pra@tim.se">Peter Antman</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class ConnectionToken implements Serializable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   private static final long serialVersionUID = 1344893519455875890L;
   
   // Attributes ----------------------------------------------------
   
   /**
	 * Used by the server to callback to client. Will (most of the time) be
	 * serialized when sent to the server.
	 */
   public ClientIL clientIL;

   /**
	 * The clientID of the connection.
	 */
   protected String clientID;

   /**
	 * A secured hashed unique sessionId that is valid only as long as the
	 * connection live. Set during authentication and used for authorization.
	 */
   private String sessionId;
   
   /** The hash code */
   private int hash;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new ConnectionToken
    *
    * @param clientID the client id
    * @param clientIL the client il
    */
   public ConnectionToken(String clientID, ClientIL clientIL)
   {
      this.clientIL = clientIL;
      setClientID(clientID);
   }
   
   /**
    * Create a new ConnectionToken
    *
    * @param clientID the client id
    * @param clientIL the client il
    * @param sessionId the session id
    */
   public ConnectionToken(String clientID, ClientIL clientIL, String sessionId)
   {
      this(clientID, clientIL);
      this.sessionId = sessionId;
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Get the client id
    *
    * @return the client id
    */
   public String getClientID()
   {
      return clientID;
   }

   /**
    * Set the client id
    *
    * @param clientID the client id
    */
   public void setClientID(String clientID)
   {
      this.clientID = clientID;
      if (clientID == null)
         hash = 0;
      else
         hash = clientID.hashCode();
   }

   /**
    * Get the session id
    *
    * @return the session id
    */
   public String getSessionId()
   {
      return sessionId;
   }
   
   // Object overrides ----------------------------------------------
   
   public boolean equals(Object obj)
   {
      if (!(obj instanceof ConnectionToken) || obj == null)
         return false;

      if (obj.hashCode() != hash)
         return false;
      String yourID = ((ConnectionToken) obj).clientID;
      String yourSessionId = ((ConnectionToken) obj).sessionId;
      if (clientID == null && yourID != null)
         return false;
      else if (sessionId == null && yourSessionId != null)
         return false;
      else if (clientID != null && clientID.equals(yourID) == false)
         return false;
      else if (sessionId != null && sessionId.equals(yourSessionId) == false)
         return false;
      else
         return true;
   }

   public int hashCode()
   {
      return hash;
   }

   public String toString()
   {
      return "ConnectionToken:" + clientID + "/" + sessionId;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}