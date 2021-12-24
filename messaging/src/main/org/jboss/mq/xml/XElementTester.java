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
package org.jboss.mq.xml;

/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
/**
 *  This class is for a basic unit test of the XElement class
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public class XElementTester {

   /**
    *  Starts the application.
    *
    * @param  args           an array of command-line arguments
    * @exception  Exception  Description of Exception
    */
   public static void main( java.lang.String[] args )
      throws Exception {

      XElement o = XElement.createFrom( new java.net.URL( "file:" + new java.io.File( args[0] ).getAbsolutePath() ) );
      System.out.println( "Result=" + o );

   }
}
