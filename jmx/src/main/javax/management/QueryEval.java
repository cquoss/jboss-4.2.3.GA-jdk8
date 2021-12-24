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
package javax.management;

import java.io.Serializable;

import org.jboss.mx.util.QueryExpSupport;

/**
 * Support for evaluting a query in the context of an MBeanServer.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public abstract class QueryEval
   implements Serializable
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = 2675899265640874796L;
   
   // Attributes --------------------------------------------------

   // Static ------------------------------------------------------

   // Public ------------------------------------------------------

   /** Return the MBean server that was most recently given to the
    * setMBeanServer method by this thread. If this thread never called that
    * method, the result is the value its parent thread would have obtained
    * from getMBeanServer at the moment of the creation of this thread, or
    * null if there is no parent thread.
    *
    * @return the current MBeanServer thread association.
    */
   public static MBeanServer getMBeanServer()
   {
      return (MBeanServer) QueryExpSupport.server.get();
   }

   /**
    * Set the MBeanServer for this query. Only MBeans registered in
    * this server can be used in queries.
    *
    * @param mbeanServer the MBeanServer
    */
   public void setMBeanServer(MBeanServer mbeanServer)
   {
      QueryExpSupport.server.set(mbeanServer);
   }
}
