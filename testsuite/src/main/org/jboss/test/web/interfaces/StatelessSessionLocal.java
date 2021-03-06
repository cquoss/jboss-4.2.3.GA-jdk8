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
package org.jboss.test.web.interfaces;

import javax.ejb.EJBLocalObject;
import java.rmi.RemoteException;

/** A trivial SessionBean local interface.

 @author Scott.Stark@jboss.org
 @version $Revision: 57211 $
 */
public interface StatelessSessionLocal extends EJBLocalObject
{
   /** A method that returns its arg */
   public String echo(String arg);

   /** A method that does nothing. It is used to test call optimization.
    */
   public void noop(ReferenceTest test, boolean optimized);

   /** Forward a request to another StatelessSession's echo method */
   public String forward(String echoArg);

   /** A method deployed with no method permissions */
   public void unchecked();

   /** A method deployed with method permissions such that only a run-as
    * assignment will allow access. 
    */
   public void checkRunAs();
}
