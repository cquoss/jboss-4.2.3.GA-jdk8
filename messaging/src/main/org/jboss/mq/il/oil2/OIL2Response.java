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

import java.io.IOException;

import javax.jms.JMSException;

/**
 * 
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: 1.2 $
 */
public class OIL2Response
{   
   Integer   correlationRequestId;
   byte      operation;
   Object    result;
   Throwable exception;
   
   public OIL2Response() {}
   public OIL2Response(OIL2Request request) {
      correlationRequestId = request.requestId;
      operation = request.operation;
   }
   
   
   public Object evalThrowsJMSException() throws JMSException, IOException {
      if( exception != null ) {
         if( exception instanceof JMSException ) {
            throw (JMSException)exception;
         } else {
            throw new IOException("Protocol violation: unexpected exception found in response: "+exception);
         }
      }
      return result;
   }
   
   public Object evalThrowsException() throws Exception {
      if( exception != null ) {
         if( exception instanceof Exception ) {
            throw (Exception)exception;
         } else {
            throw new IOException("Protocol violation: unexpected exception found in response: "+exception);
         }
      }
      return result;
   }
   
   public Object evalThrowsThrowable() throws Throwable {
      if( exception != null ) {
         throw exception;
      }
      return result;
   }
   

   public void writeExternal(java.io.ObjectOutput out) throws IOException
   {
      out.writeByte(operation);
      
      if( correlationRequestId == null ) {
         out.writeByte(0);      
      } else {
         out.writeByte(1);      
         out.writeInt(correlationRequestId.intValue());
      }
      
      if( exception != null ) {
         out.writeByte(OIL2Constants.RESULT_EXCEPTION);
         out.writeObject(exception);
         return;
      }
      if( result == null ) {
         out.writeByte(OIL2Constants.RESULT_VOID);
         return;         
      }
      switch (operation)
      {         
         default :
            out.writeByte(OIL2Constants.RESULT_OBJECT);
            out.writeObject(result);
            return;         
      }
   }

   public void readExternal(java.io.ObjectInput in) throws IOException, ClassNotFoundException
   {
      operation = in.readByte();
      if( in.readByte() == 1)
          correlationRequestId = new Integer(in.readInt());
          
      byte responseType = in.readByte();
      switch( responseType ) {
         case OIL2Constants.RESULT_VOID:
            result=null;
            exception=null;
            break;
         case OIL2Constants.RESULT_EXCEPTION:
            result=null;
            exception=(Throwable)in.readObject();
            break;
         case OIL2Constants.RESULT_OBJECT:
            exception=null;
            switch (operation)
            {
               default :
               result=in.readObject();
            }
            break;
         default :
            throw new IOException("Protocol Error: Bad response type code '"+responseType+"' ");
         
      }
     
   }
   
   public String toString() {
      return "[operation:"+operation+","+"correlationRequestId:"+correlationRequestId+",result:"+result+",exception:"+exception+"]";
   }

}
