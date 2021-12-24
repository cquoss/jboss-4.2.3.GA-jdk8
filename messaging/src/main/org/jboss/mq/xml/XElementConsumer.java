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
 *  XMLRecordConsumer Interface defines the method signatures used to notify the
 *  consumer object of parsing errors, document starts, record reads, and
 *  document ends.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public interface XElementConsumer {

   /**
    *  Signals that the END of the XML document has been reached.
    *
    * @exception  Exception  Description of Exception
    */
   public void documentEndEvent()
      throws Exception;

   /**
    *  Signals that the START of the XML document has been reached.
    *
    * @exception  Exception  Description of Exception
    */
   public void documentStartEvent()
      throws Exception;

   /**
    *  Signals that a record object, an xml element, has been fully read in.
    *
    * @param  o              Description of Parameter
    * @exception  Exception  Description of Exception
    */
   public void recordReadEvent( XElement o )
      throws Exception;

}
