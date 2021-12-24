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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.invocation.http.interfaces.Util;
import org.jboss.logging.Logger;

/**
 * Utility class that provides HTTP functionality.  This class is modeled after
 * Scott's Util class in org.jboss.invocation.http.interfaces.Util, however,
 * that class deals with Invocation object, while this handles HTTPILRequest
 * objects.  Other then that, it is a pretty close reproduction.
 *
 * @author    Nathan Phelps (nathan@jboss.org)
 * @version   $Revision: 57198 $
 * @created   January 15, 2003
 */
public class HTTPClient
{
    private static final String CONTENT_TYPE = "application/x-java-serialized-object; class=org.jboss.mq.il.http.HTTPILRequest";
    
    private static Logger log = Logger.getLogger(HTTPClient.class);
    
    public static Object post(URL url, HTTPILRequest request) throws Exception
    {
        if (log.isTraceEnabled())
        {
            log.trace("post(URL " + url.toString() + ", HTTPILRequest " + request.toString() + ")");
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        Util.configureHttpsHostVerifier(connection);

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("ContentType", CONTENT_TYPE);
        connection.setRequestMethod("POST");
        ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.writeObject(request);
        outputStream.close();
        ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());
        HTTPILResponse response = (HTTPILResponse)inputStream.readObject();
        inputStream.close();
        Object responseValue = response.getValue();
        if (responseValue instanceof Exception)
        {
            throw (Exception)responseValue;
        }
        return responseValue;
    }
    
    public static URL resolveServerUrl(String url) throws MalformedURLException
    {
       return Util.resolveURL(url);
    }
}
