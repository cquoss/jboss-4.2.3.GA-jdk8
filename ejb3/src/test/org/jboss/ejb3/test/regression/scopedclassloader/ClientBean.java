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
package org.jboss.ejb3.test.regression.scopedclassloader;

import javax.naming.InitialContext;
import javax.ejb.Stateless;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60233 $
 */
@Stateless
public class ClientBean implements ClientRemote
{
   public void doit() throws Exception
   {
      StatelessRemote remote = (StatelessRemote)new InitialContext().lookup("scopedclassloader-ejb/StatelessBean/remote");
      ValueObject vo = remote.doit();
      System.out.println(vo.getValue());
      Customer cust = new Customer();
      cust.setName("Bill");
      int id = remote.createCustomer(cust);
      cust = remote.findCustomer(id);
      if (!cust.getName().equals("Bill")) throw new RuntimeException("Failed to persist");

      try
      {
         remote.throwException();
      }
      catch (ScopedException ignored) {}

   }

}
