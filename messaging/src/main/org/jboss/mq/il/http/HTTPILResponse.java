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
/**
 * This is the date type the HTTPClient expects back from a HTTPILRequest.
 *
 * @author    Nathan Phelps (nathan@jboss.org)
 * @version   $Revision: 57198 $
 * @created   January 15, 2003
 */
public class HTTPILResponse implements Serializable
{
    static final long serialVersionUID = -3142826625041742267L;
    private Object value;
    
    public HTTPILResponse()
    {
    }
    
    public HTTPILResponse(Object value)
    {
        this.value = value;
    }
    
    public void setValue(Object value)
    {
        this.value = value;
    }
    
    public Object getValue()
    {
        return this.value;
    }
    
    public String toString()
    {
        if (this.value == null)
        {
            return null;
        }
        else
        {
            return this.value.toString();
        }
    }
}