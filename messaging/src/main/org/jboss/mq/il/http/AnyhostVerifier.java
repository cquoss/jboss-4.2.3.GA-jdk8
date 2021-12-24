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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* An implementation of the HostnameVerifier that accepts any SSL certificate
 * hostname as matching the https URL that was used to initiate the SSL connection.
 * This is useful for testing SSL setup in development environments using self
 * signed SSL certificates.
 *
 * This is a duplicate object found here and in the server module.  Like the
 * Base64Encoder, we'll probably want to move it somewhere else.  nathan@jboss.org
 
 @author Scott.Stark@jboss.org
 @version $Revision: 57198 $
 */
public class AnyhostVerifier implements HostnameVerifier
{
   public boolean verify(String s, SSLSession sslSession)
   {
      return true;
   }
}