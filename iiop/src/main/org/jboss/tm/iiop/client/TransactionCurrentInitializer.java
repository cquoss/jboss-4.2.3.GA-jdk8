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
package org.jboss.tm.iiop.client;

import org.omg.CORBA.LocalObject;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This is an <code>org.omg.PortableInterceptor.ORBInitializer</code> that
 * initializes the <code>TransactionCurrent</code>.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 57194 $
 */
public class TransactionCurrentInitializer
      extends LocalObject
      implements ORBInitializer
{

   public TransactionCurrentInitializer()
   {
      // do nothing
   }

   // org.omg.PortableInterceptor.ORBInitializer operations ---------

   public void pre_init(ORBInitInfo info)
   {
      try
      {
         info.register_initial_reference("TransactionCurrent",
                                         TransactionCurrent.getInstance());
      }
      catch (InvalidName e)
      {
         throw new RuntimeException("Could not register initial " +
                                    "reference for TransactionCurrent: " + e);
      }
   }

   public void post_init(ORBInitInfo info)
   {
      try
      {
         org.omg.CORBA.Object obj = 
            info.resolve_initial_references("NameService");
         NamingContextExt rootContext = NamingContextExtHelper.narrow(obj);
         TransactionCurrent.init(rootContext);
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Unexpected " + e);
      }
   }

}
