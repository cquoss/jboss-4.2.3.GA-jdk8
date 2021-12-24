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
package org.jboss.tm.iiop;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import org.jboss.iiop.CorbaNamingService;
import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Service MBean that provides the CORBA transaction service for JBoss.
 *      
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 57194 $
 */
public class CorbaTransactionService
      extends ServiceMBeanSupport
      implements CorbaTransactionServiceMBean
{
   // Constants -----------------------------------------------------
   public static String COSNAMING_NAME = "TransactionService";
   public static String COSNAMING_USERTX_NAME = "UserTransaction";
    
   // Attributes ----------------------------------------------------

   /** The POA used by the CORBA transaction service. */
   private POA poa;

   // ServiceMBeanSupport overrides ---------------------------------

   protected void startService()
      throws Exception
   {
      Context jndiContext;
      ORB orb;
      POA rootPOA;

      try 
      {
         jndiContext = new InitialContext();
      }
      catch (NamingException e) 
      {
         throw new RuntimeException("Cannot get intial JNDI context: " + e);
      }
      try 
      {
         orb = (ORB)jndiContext.lookup("java:/" + CorbaORBService.ORB_NAME);
      } 
      catch (NamingException e) 
      {
         throw new RuntimeException("Cannot lookup java:/" 
                                    + CorbaORBService.ORB_NAME + ": " + e);
      }
      try 
      {
         rootPOA = (POA)jndiContext.lookup("java:/" + CorbaORBService.POA_NAME);
      } 
      catch (NamingException e) 
      {
         throw new RuntimeException("Cannot lookup java:/" 
                                    + CorbaORBService.POA_NAME + ": " + e);
      }

      // Create the transaction service POA as a child of the root POA
      Policy[] policies = {
         rootPOA.create_lifespan_policy(
                           LifespanPolicyValue.PERSISTENT),
         rootPOA.create_id_assignment_policy(
                           IdAssignmentPolicyValue.USER_ID),
         rootPOA.create_servant_retention_policy(
                           ServantRetentionPolicyValue.NON_RETAIN),
         rootPOA.create_request_processing_policy(
                           RequestProcessingPolicyValue.USE_DEFAULT_SERVANT),
         rootPOA.create_id_uniqueness_policy(
                           IdUniquenessPolicyValue.MULTIPLE_ID),
      };
      poa = rootPOA.create_POA("Transactions", null, policies);
      poa.the_POAManager().activate();

      // Create the transaction service
      TransactionServiceImpl theDefaultServant = 
         new TransactionServiceImpl(orb, poa);
      poa.set_servant(theDefaultServant);
      org.omg.CORBA.Object theTransactionFactory = 
         poa.create_reference_with_id(TransactionServiceImpl.theFactoryId(),
                                      TransactionFactoryExtHelper.id());

      // Register the transaction factory in the CORBA naming service
      NamingContextExt rootContext = null;
      try 
      {
         // Obtain local (in-VM) CORBA naming context
         rootContext = NamingContextExtHelper.narrow((org.omg.CORBA.Object)
               jndiContext.lookup("java:/" + CorbaNamingService.NAMING_NAME));
      }
      catch (NamingException e) 
      {
         throw new Exception("Cannot lookup java:/" +
                             CorbaNamingService.NAMING_NAME + ":\n" + e);
      }
      try 
      {
         // Register transaction factory in local CORBA naming context
         rootContext.rebind(rootContext.to_name(COSNAMING_NAME),
                                                theTransactionFactory);
         getLog().info("TransactionFactory: [" 
                       + orb.object_to_string(theTransactionFactory)
                       + "]");

         // Register it also as "UserTransaction" to please user tx clients
         rootContext.rebind(rootContext.to_name(COSNAMING_USERTX_NAME),
                                                theTransactionFactory);
      }
      catch (Exception e) 
      {
         getLog().error("Cannot bind transaction factory in CORBA naming service:", e);
         throw new Exception("Cannot bind transaction factory in CORBA naming service:\n"
                             + e);
      }
   }
    
   protected void stopService()
   {
      try 
      {
         // Get local (in-VM) CORBA naming context
         Context jndiContext = new InitialContext();
         NamingContextExt rootContext =
               NamingContextExtHelper.narrow((org.omg.CORBA.Object)
                     jndiContext.lookup("java:/" 
                                        + CorbaNamingService.NAMING_NAME));

         // Unregister the transaction factory from the CORBA naming service
         try 
         {
            NameComponent[] name = rootContext.to_name(COSNAMING_NAME);
            rootContext.unbind(name);
         }
         catch (InvalidName invalidName) 
         {
            getLog().error("Cannot unregister transaction factory from CORBA naming service",
                           invalidName);
         }
         catch (NotFound notFound) 
         {
            getLog().error("Cannot unregister transaction factory from CORBA naming service",
                           notFound);
         }
         catch (CannotProceed cannotProceed) 
         {
            getLog().error("Cannot unregister transaction factory from CORBA naming service",
                           cannotProceed);
         }
      }
      catch (NamingException namingException) 
      {
         getLog().error("Unexpected error in JNDI lookup", namingException);
      }

      // Destroy transaction service POA
      try 
      {
         poa.destroy(false, false);
      } 
      catch (Exception e) 
      {
         getLog().error("Exception while stopping CORBA transaction service", e);
      }
   }

}
