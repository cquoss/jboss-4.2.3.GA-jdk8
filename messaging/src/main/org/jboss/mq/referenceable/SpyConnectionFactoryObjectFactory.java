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
package org.jboss.mq.referenceable;

import org.jboss.logging.Logger;
import org.jboss.mq.GenericConnectionFactory;

/**
 *  This class is used to create instances of of: SpyTopicConnectionFactory
 *  SpyQueueConnectionFactory SpyXATopicConnectionFactory SpyXAQueueConnectionFactory
 *  classes from a javax.naming.Reference instance.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public class SpyConnectionFactoryObjectFactory implements javax.naming.spi.ObjectFactory {

   static Logger cat = Logger.getLogger( SpyConnectionFactoryObjectFactory.class );


   /**
    *  getObjectInstance method.
    *
    * @param  reference                Description of Parameter
    * @param  name                     Description of Parameter
    * @param  contex                   Description of Parameter
    * @param  properties               Description of Parameter
    * @return                          The ObjectInstance value
    * @exception  java.lang.Exception  Description of Exception
    */
   public java.lang.Object getObjectInstance( java.lang.Object reference, javax.naming.Name name, javax.naming.Context contex, java.util.Hashtable properties )
      throws java.lang.Exception {

      boolean debug = cat.isDebugEnabled();
      if (debug)
         cat.debug( "Extracting SpyConnectionFactory from reference" );
      try {

         javax.naming.Reference ref = ( javax.naming.Reference )reference;
         GenericConnectionFactory dcf = ( GenericConnectionFactory )
               ObjectRefAddr.extractObjectRefFrom( ref, "DCF" );

        if (debug)
	         cat.debug("The GenericConnectionFactory is: "+dcf);

         if ( ref.getClassName().equals( "org.jboss.mq.SpyConnectionFactory" ) ) {
            return new org.jboss.mq.SpyConnectionFactory( dcf );
         } else if ( ref.getClassName().equals( "org.jboss.mq.SpyXAConnectionFactory" ) ) {
            return new org.jboss.mq.SpyXAConnectionFactory( dcf );
         }
      } catch ( Throwable ignore ) {
         ignore.printStackTrace();
         // This method should not throw an exception since
         // It would prevent another ObjectFactory from attempting
         // to create an instance of the object.
      }
      return null;
   }
}
