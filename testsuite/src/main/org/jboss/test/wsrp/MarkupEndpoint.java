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
import org.jboss.test.wsrp.core.BlockingInteractionResponse;
import org.jboss.test.wsrp.core.GetMarkup;
import org.jboss.test.wsrp.core.InconsistentParametersFault;
import org.jboss.test.wsrp.core.InitCookie;
import org.jboss.test.wsrp.core.InvalidCookieFault;
import org.jboss.test.wsrp.core.InvalidHandleFault;
import org.jboss.test.wsrp.core.InvalidRegistrationFault;
import org.jboss.test.wsrp.core.InvalidSessionFault;
import org.jboss.test.wsrp.core.InvalidUserCategoryFault;
import org.jboss.test.wsrp.core.MarkupResponse;
import org.jboss.test.wsrp.core.MissingParametersFault;
import org.jboss.test.wsrp.core.OperationFailedFault;
import org.jboss.test.wsrp.core.PerformBlockingInteraction;
import org.jboss.test.wsrp.core.PortletStateChangeRequiredFault;
import org.jboss.test.wsrp.core.ReleaseSessions;
import org.jboss.test.wsrp.core.ReturnAny;
import org.jboss.test.wsrp.core.UnsupportedLocaleFault;
import org.jboss.test.wsrp.core.UnsupportedMimeTypeFault;
import org.jboss.test.wsrp.core.UnsupportedModeFault;
import org.jboss.test.wsrp.core.UnsupportedWindowStateFault;

//$Id: MarkupEndpoint.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  WSRP Endpoint for Markup Service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class MarkupEndpoint implements WSRP_v1_Markup_PortType
{

   public MarkupResponse getMarkup(GetMarkup getMarkup) throws UnsupportedWindowStateFault, InvalidCookieFault, InvalidSessionFault, AccessDeniedFault,
         InconsistentParametersFault, InvalidHandleFault, UnsupportedLocaleFault, UnsupportedModeFault, OperationFailedFault, MissingParametersFault,
         InvalidUserCategoryFault, InvalidRegistrationFault, UnsupportedMimeTypeFault, RemoteException
   {  
      return new MarkupResponse();
   }

   public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction performBlockingInteraction) throws InvalidSessionFault,
         UnsupportedModeFault, UnsupportedMimeTypeFault, OperationFailedFault, UnsupportedWindowStateFault, UnsupportedLocaleFault, AccessDeniedFault,
         PortletStateChangeRequiredFault, InvalidRegistrationFault, MissingParametersFault, InvalidUserCategoryFault, InconsistentParametersFault, InvalidHandleFault,
         InvalidCookieFault, RemoteException
   {
      return new BlockingInteractionResponse();
   }

   public ReturnAny releaseSessions(ReleaseSessions releaseSessions) throws InvalidRegistrationFault, OperationFailedFault, MissingParametersFault, AccessDeniedFault,
         RemoteException
   {
      return new ReturnAny();
   }

   public ReturnAny initCookie(InitCookie initCookie) throws AccessDeniedFault, OperationFailedFault, InvalidRegistrationFault, RemoteException
   {
      return new ReturnAny();
   }

}
