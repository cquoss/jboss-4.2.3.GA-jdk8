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
package org.jboss.web.tomcat.service.session;


/**
 * This exception is thrown when the clustered HTTPSession-service
 * is not found
 *
 * @author Thomas Peuss <jboss@peuss.de>
 * @version $Revision: 57206 $
 */
public class ClusteringNotSupportedException extends Exception
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2365801349845482947L;

   public ClusteringNotSupportedException(String message)
   {
      super(message);
   }
   
   public ClusteringNotSupportedException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
