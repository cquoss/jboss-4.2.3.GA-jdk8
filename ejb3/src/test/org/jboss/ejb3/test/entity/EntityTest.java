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
package org.jboss.ejb3.test.entity;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61629 $
 */
public interface EntityTest
{
   Customer oneToManyCreate() throws Exception;

   Customer findCustomerById(Long id) throws Exception;

   Flight manyToOneCreate() throws Exception;

   Flight findFlightById(Long id) throws Exception;

   Company findCompanyById(Integer id) throws Exception;

   void manyToManyCreate() throws Exception;

   FieldCustomer fieldOneToManyCreate() throws Exception;

   FieldCustomer fieldFindCustomerById(Long id) throws Exception;

   FieldFlight fieldManyToOneCreate() throws Exception;

   void fieldManyToManyCreate() throws Exception;

   FieldFlight fieldFindFlightById(Long id) throws Exception;

   FieldCompany fieldFindCompanyById(Integer id) throws Exception;

   void testNamedQueries() throws Exception;

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   void testOutsideTransaction();
   
   Customer createCustomer(String name);
   
   void changeCustomer(Long id, String name);
   
   Customer loadCustomer(Long id);

   boolean isDelegateASession();

   public boolean isTrueHibernateSession();
}
