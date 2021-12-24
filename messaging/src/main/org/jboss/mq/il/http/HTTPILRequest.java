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
 * This is the data type that HTTPServerILServlet expects.
 *
 * @author    Nathan Phelps (nathan@jboss.org)
 * @version   $Revision: 57198 $
 * @created   January 15, 2003
 */
public class HTTPILRequest implements Serializable
{
    static final long serialVersionUID = -4303390310602251849L;
    private String methodName;
    private Object[] arguments;
    private Class[] argumentTypes;
    
    public HTTPILRequest()
    {
    }
    
    public HTTPILRequest(String methodName, Object[] arguments, Class[] argumentTypes)
    {
        this.methodName = methodName;
        this.arguments = arguments;
        this.argumentTypes = argumentTypes;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public String getMethodName()
    {
        return this.methodName;
    }
    
    public void setArguments(Object[] arguments, Class[] argumentTypes)
    {
        this.arguments = arguments;
        this.argumentTypes = argumentTypes;
    }
    
    public Object[] getArguments()
    {
        return this.arguments;
    }
    
    public Class[] getArgumentTypes()
    {
        return this.argumentTypes;
    }
    
    public String toString()
    {
        String argumentString = "(";
        if (this.arguments != null)
        {
            for (int i = 0; i < this.arguments.length; i++ )
            {
                if (i > 0)
                {
                    argumentString = argumentString + ", ";
                }
                if (this.arguments[i] != null)
                {
                    argumentString = argumentString + this.argumentTypes[i].toString() + " " + this.arguments[i].toString();
                }
                else
                {
                    argumentString = argumentString + this.argumentTypes[i].toString() + " null";
                }
            }
        }
        argumentString = argumentString + ")";
        return this.methodName + argumentString;
    }
}