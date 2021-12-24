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
package javax.xml.registry;

import org.jboss.util.id.SerialVersion;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57196 $
 */
public class JAXRException extends Exception
   implements JAXRResponse
{
   /** @since 4.0.2 */
   static final long serialVersionUID;
   static
   {
      if (SerialVersion.version == SerialVersion.LEGACY)
         serialVersionUID = -4890474253443089374L;
      else
         serialVersionUID = -396751348710944315L;
   }

   protected Throwable cause;

   public JAXRException()
   {
   }
   public JAXRException(String msg)
   {
      super(msg);
   }
   public JAXRException(String msg, Throwable cause)
   {
      super(msg, cause);
   }
   public JAXRException(Throwable cause)
   {
      super(cause);
   }
   public String getRequestId()
   {
      return null;
   }
   public int getStatus()
   {
      return 0;
   }
   public boolean isAvailable() throws JAXRException
   {
      return true;
   }

   /** Initializes the cause of this throwable to the specified value. (The
    * cause is the throwable that caused this throwable to get thrown.)
    * 
    * This method can be called at most once. It is generally called from
    * within the constructor, or immediately after creating the throwable. If
    * this throwable was created with JAXRException(Throwable) or
    * JAXRException(String,Throwable), this method cannot be called even once. 
    * 
    * @param cause
    * @return this
    */ 
   public Throwable initCause(Throwable cause)
   {
      if( this.cause != null )
      {
         if( this.cause == this )
            throw new IllegalArgumentException("A throwable cannot be its own cause.");
         else
            throw new IllegalStateException("cause has already been set"); 
      }
      this.cause = cause;
      super.initCause(cause);
      return this;
   }
}
