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

/**
 * MBean interface.
 * @created January 15, 2003
 */
public interface HTTPServerILServiceMBean extends org.jboss.mq.il.ServerILJMXServiceMBean
{

   /**
    * Set the HTTPIL default timeout--the number of seconds that the ClientILService HTTP requests will wait for messages. This can be overridden on the client by setting the system property org.jboss.mq.il.http.timeout to an int value of the number of seconds. NOTE: This value should be in seconds, not millis.
    */
   void setTimeOut(int timeout);

   /**
    * Get the HTTPIL default timeout
    */
   int getTimeOut();

   /**
    * Set the HTTPIL default RestInterval--the number of seconds the ClientILService will sleep after each client request. The default is 0, but you can set this value in conjunction with the TimeOut value to implement a pure timed based polling mechanism. For example, you could simply do a short lived request by setting the TimeOut value to 0 and then setting the RestInterval to 60. This would cause the ClientILService to send a single non-blocking request to the server, return any messages if available, then sleep for 60 seconds, before issuing another request. Like the TimeOut value, this can be explicitly overriden on a given client by specifying the org.jboss.mq.il.http.restinterval with the number of seconds you wish to wait between requests.
    */
   void setRestInterval(int restInterval);

   /**
    * Get the HTTPIL default RestInterval
    */
   int getRestInterval();

   /**
    * Set the HTTPIL URL. This value takes precedence over any individual values set (i.e. the URLPrefix, URLSuffix, URLPort, etc.) It my be a actual URL or a propertyname which will be used on the client side to resolve the proper URL by calling System.getProperty(propertyname).
    */
   void setURL(java.lang.String url);

   /**
    * Get the HTTPIL URL. This value takes precedence over any individual values set (i.e. the URLPrefix, URLSuffix, URLPort, etc.) It my be a actual URL or a propertyname which will be used on the client side to resolve the proper URL by calling System.getProperty(propertyname).
    */
   java.lang.String getURL();

   /**
    * Set the HTTPIL URLPrefix. I.E. "http://" or "https://" The default is "http://"
    */
   void setURLPrefix(java.lang.String prefix);

   /**
    * Get the HTTPIL URLPrefix. I.E. "http://" or "https://" The default is "http://"
    */
   java.lang.String getURLPrefix();

   /**
    * Set the HTTPIL URLName.
    */
   void setURLHostName(java.lang.String hostname);

   /**
    * Get the HTTPIL URLHostName.
    */
   java.lang.String getURLHostName();

   /**
    * Set the HTTPIL URLPort. The default is 8080
    */
   void setURLPort(int port);

   /**
    * Get the HTTPIL URLPort. The default is 8080
    */
   int getURLPort();

   /**
    * Set the HTTPIL URLSuffix. I.E. The section of the URL after the port The default is "jbossmq-httpil/HTTPServerILServlet"
    */
   void setURLSuffix(java.lang.String suffix);

   /**
    * Get the HTTPIL URLSuffix. I.E. The section of the URL after the port The default is "jbossmq-httpil/HTTPServerILServlet"
    */
   java.lang.String getURLSuffix();

   /**
    * Set the HTTPIL UseHostName flag. if true the default URL will include a hostname, if false it will include an IP adddress. The default is false
    */
   void setUseHostName(boolean value);

   /**
    * Get the HTTPIL UseHostName flag. if true the default URL will include a hostname, if false it will include an IP adddress. The default is false
    */
   boolean getUseHostName();

}
