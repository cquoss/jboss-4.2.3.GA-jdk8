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
package org.jboss.test.wsrp; 
 
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody; 
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils; 
import org.w3c.dom.Element;

//$Id: WSRPExtensionHandler.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  JAX-RPC Handler that strips the SOAP Message off the wsrp extensions
 *  
 *  Checks for the existence of a SOAP Header element "jboss_wsrp_remove_extension"
 *  which when present, will strip the wsrp extensions.
 *  If there the handler has a init param set to remove the extension, it will.
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 26, 2006
 *  @version $Revision: 57211 $
 */
public class WSRPExtensionHandler extends GenericHandler
{ 
   private static Logger log = Logger.getLogger(WSRPExtensionHandler.class);
   private boolean debug = false; 
   private boolean removeExtensions = false;
   
   public void init(HandlerInfo info) 
   { 
      // this parameter configured in 'webservices.xml'
      String debugStr = (String)info.getHandlerConfig().get("debug"); 
      debug = "true".equalsIgnoreCase(debugStr); 
      String rem = (String)info.getHandlerConfig().get("removeExtensions");
      removeExtensions = "true".equalsIgnoreCase(rem);
   }
   
   public QName[] getHeaders()
   { 
      return null;
   }

   public boolean handleRequest(MessageContext msgContext)
   { 
      SOAPMessageContext soapMessageContext = (SOAPMessageContext)msgContext;
      SOAPMessage soapMessage = soapMessageContext.getMessage(); 
      try
      {
         if(debug)
           soapMessage.writeTo(System.out);
         SOAPHeader soapHeader = soapMessage.getSOAPHeader(); 
         if(shouldRemoveWSRPExtensions(soapHeader) || removeExtensions)
         {
            SOAPBody soapBody = soapMessage.getSOAPBody();
            Element firstEl = DOMUtils.getFirstChildElement(soapBody);
            Iterator iter = DOMUtils.getChildElements(firstEl, "runtimeContext"); 
            if(iter.hasNext())
            {
               Element runtimeCtx = (Element)iter.next();  
               iter = DOMUtils.getChildElements(runtimeCtx, "extensions");
               log.error("extensions exist:"+iter.hasNext());
               if(iter.hasNext())
               {
                  Element exts = (Element)iter.next();
                  iter = DOMUtils.getChildElements(exts);
                  while(iter.hasNext())
                  {
                     Node node = (Node)iter.next();
                     exts.removeChild(node);
                  } 
                  if(debug)
                    soapMessage.writeTo(System.out);
               } 
            }  
         } 
      } 
      catch(Exception e)
      {
         log.error("Error in handleRequest:",e);
      }
      return super.handleRequest(msgContext);
   } 
   
   private boolean shouldRemoveWSRPExtensions(SOAPHeader soapHeader)
   {
      boolean result = false;
      if(soapHeader != null)
      {
        Iterator iter = DOMUtils.getChildElements(soapHeader,"jboss_wsrp_remove_extension");
        if(iter.hasNext())
           result = true;
      }
      return result;
   }
}
