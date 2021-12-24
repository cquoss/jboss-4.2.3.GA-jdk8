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

import org.jboss.test.wsrp.core.AccessDeniedFault;
import org.jboss.test.wsrp.core.ClonePortlet;
import org.jboss.test.wsrp.core.DestroyPortlets;
import org.jboss.test.wsrp.core.DestroyPortletsResponse;
import org.jboss.test.wsrp.core.GetPortletDescription;
import org.jboss.test.wsrp.core.GetPortletProperties;
import org.jboss.test.wsrp.core.GetPortletPropertyDescription;
import org.jboss.test.wsrp.core.InconsistentParametersFault;
import org.jboss.test.wsrp.core.InvalidHandleFault;
import org.jboss.test.wsrp.core.InvalidRegistrationFault;
import org.jboss.test.wsrp.core.InvalidUserCategoryFault;
import org.jboss.test.wsrp.core.MissingParametersFault;
import org.jboss.test.wsrp.core.OperationFailedFault;
import org.jboss.test.wsrp.core.PortletContext;
import org.jboss.test.wsrp.core.PortletDescriptionResponse;
import org.jboss.test.wsrp.core.PortletPropertyDescriptionResponse;
import org.jboss.test.wsrp.core.PropertyList;
import org.jboss.test.wsrp.core.SetPortletProperties;

//$Id: PortletManagementEndpoint.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
/**
 *  WSRP Endpoint for PortletManagement
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class PortletManagementEndpoint implements WSRP_v1_PortletManagement_PortType
{

   public PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription) throws AccessDeniedFault, InvalidHandleFault,
         InvalidUserCategoryFault, InconsistentParametersFault, MissingParametersFault, InvalidRegistrationFault, OperationFailedFault, RemoteException
   { 
      return new PortletDescriptionResponse();
   }

   public PortletContext clonePortlet(ClonePortlet clonePortlet) throws InvalidUserCategoryFault, AccessDeniedFault, OperationFailedFault, InvalidHandleFault,
         InvalidRegistrationFault, InconsistentParametersFault, MissingParametersFault, RemoteException
   { 
      return new PortletContext();
   }

   public DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets) throws InconsistentParametersFault, MissingParametersFault,
         InvalidRegistrationFault, OperationFailedFault, RemoteException
   {
      return new DestroyPortletsResponse();
   }

   public PortletContext setPortletProperties(SetPortletProperties setPortletProperties) throws OperationFailedFault, InvalidHandleFault, MissingParametersFault,
         InconsistentParametersFault, InvalidUserCategoryFault, AccessDeniedFault, InvalidRegistrationFault, RemoteException
   { 
      return new PortletContext();
   }

   public PropertyList getPortletProperties(GetPortletProperties getPortletProperties) throws InvalidHandleFault, MissingParametersFault, InvalidRegistrationFault,
         AccessDeniedFault, OperationFailedFault, InconsistentParametersFault, InvalidUserCategoryFault, RemoteException
   { 
      return new PropertyList();
   }

   public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription) throws MissingParametersFault,
         InconsistentParametersFault, InvalidUserCategoryFault, InvalidRegistrationFault, AccessDeniedFault, InvalidHandleFault, OperationFailedFault, RemoteException
   { 
      return new PortletPropertyDescriptionResponse();
   }

}
