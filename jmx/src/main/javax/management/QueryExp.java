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

/**
 * A query expression.<p>
 *
 * An implementation of this interface can be used in
 * a query. Multiple query expressions can be used together to form
 * a more complex query.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface QueryExp 
   extends Serializable
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * Apply this query expression to an MBean.
    *
    * @param name the object name of the mbean
    * @return true or false as the result of the query expression.
    * @exception BadStringOperationException when an invalid string operation
    *            is used during query construction
    * @exception BadBinaryOpValueExpException when an invalid binary operation
    *            is used during query construction
    * @exception BadAttributeValueExpException when an invalid MBean attribute
    *            is used during query construction
    * @exception InvalidApplicationException when trying to apply a subquery
    *            expression to an MBean or an attribute expression to an
    *            MBean of the wrong class.
    */
   public boolean apply(ObjectName name)
      throws BadStringOperationException,
             BadBinaryOpValueExpException,
             BadAttributeValueExpException,
             InvalidApplicationException;

   /**
    * Set the MBeanServer for this query. Only MBeans registered in
    * this server can be used in queries.
    *
    * @param server the MBeanServer
    */
   public void setMBeanServer(MBeanServer server);
}
