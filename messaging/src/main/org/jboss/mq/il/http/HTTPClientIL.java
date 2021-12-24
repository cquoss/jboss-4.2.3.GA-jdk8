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
package org.jboss.mq.il.http;

import java.io.Serializable;

import org.jboss.logging.Logger;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;

/**
 * The HTTP/S implementation of the ClientIL object
 *
 * @author    Nathan Phelps (nathan@jboss.org)
 * @version   $Revision: 57198 $
 * @created   January 15, 2003
 */
public class HTTPClientIL implements ClientIL, Serializable
{
   static final long serialVersionUID = 3215139925343217398L;
    public boolean stopped = true;
    
    private static Logger log = Logger.getLogger(HTTPClientIL.class);
    private String clientIlId = null;
    
    public HTTPClientIL(String clientIlId)
    {
        this.clientIlId = clientIlId;
        if (log.isTraceEnabled())
        {
            log.trace("created(" + clientIlId + ")");
        }
    }
    
    public void close() throws Exception
    {
        if (log.isTraceEnabled())
        {
            log.trace("close()");
        }
        this.throwIllegalStateExceptionIfStopped();
        HTTPILRequest request = new HTTPILRequest();
        request.setMethodName("asynchClose");
        HTTPClientILStorageQueue.getInstance().put(request, this.clientIlId);
    }
    
    public void deleteTemporaryDestination(SpyDestination dest) throws Exception
    {
        if (log.isTraceEnabled())
        {
            log.trace("deleteTemporaryDestination(SpyDestination " + dest.toString() + ")");
        }
        this.throwIllegalStateExceptionIfStopped();
        HTTPILRequest request = new HTTPILRequest();
        request.setMethodName("asynchDeleteTemporaryDestination");
        request.setArguments(new Object[]
        {dest}, new Class[]
        {SpyDestination.class});
        HTTPClientILStorageQueue.getInstance().put(request, this.clientIlId);
    }
    
    public void pong(long serverTime) throws Exception
    {
        this.throwIllegalStateExceptionIfStopped();
        HTTPILRequest request = new HTTPILRequest();
        request.setMethodName("asynchPong");
        request.setArguments(new Object[]
        {new Long(serverTime)}, new Class[]
        {long.class});
        HTTPClientILStorageQueue.getInstance().put(request, this.clientIlId);
    }
    
    public void receive(ReceiveRequest[] messages) throws Exception
    {
        if (log.isTraceEnabled())
        {
            log.trace("receive(ReceiveRequest[] arraylength=" + String.valueOf(messages.length) + ")");
        }
        this.throwIllegalStateExceptionIfStopped();
        HTTPILRequest request = new HTTPILRequest();
        request.setMethodName("asynchDeliver");
        request.setArguments(new Object[]
        {messages}, new Class[]
        {ReceiveRequest[].class});
        HTTPClientILStorageQueue.getInstance().put(request, this.clientIlId);
    }
    
    private void throwIllegalStateExceptionIfStopped() throws IllegalStateException
    {
        if (this.stopped)
        {
            throw new IllegalStateException("The client IL is stopped.");
        }
    }
    
}