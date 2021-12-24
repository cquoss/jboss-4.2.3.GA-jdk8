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
package org.jboss.test.wsrp;

import java.rmi.RemoteException;

import org.jboss.test.wsrp.core.InvalidRegistrationFault;
import org.jboss.test.wsrp.core.MissingParametersFault;
import org.jboss.test.wsrp.core.ModifyRegistration;
import org.jboss.test.wsrp.core.OperationFailedFault;
import org.jboss.test.wsrp.core.RegistrationContext;
import org.jboss.test.wsrp.core.RegistrationData;
import org.jboss.test.wsrp.core.RegistrationState;
import org.jboss.test.wsrp.core.ReturnAny;

//$Id: RegistrationEndpoint.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Endpoint Implementation for WSRP Registration Service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class RegistrationEndpoint implements WSRP_v1_Registration_PortType
{

   public RegistrationContext register(RegistrationData register) throws MissingParametersFault, OperationFailedFault, RemoteException
   {
      return new RegistrationContext();
   }

   public ReturnAny deregister(RegistrationContext deregister) throws OperationFailedFault, InvalidRegistrationFault, RemoteException
   { 
      return new ReturnAny();
   }

   public RegistrationState modifyRegistration(ModifyRegistration modifyRegistration) throws MissingParametersFault, OperationFailedFault, InvalidRegistrationFault,
         RemoteException
   { 
      return new RegistrationState();
   } 
}
