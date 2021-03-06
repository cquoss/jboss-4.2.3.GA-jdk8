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
package org.jboss.security.jacc;

import java.net.URL;
import java.security.Policy;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import org.jboss.util.state.StateMachine;
import org.jboss.util.state.xml.StateMachineParser;

/** org.jboss.security.provider
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57203 $
 */
public class JBossPolicyConfigurationFactory
   extends PolicyConfigurationFactory
{
   private StateMachine configStateMachine;
   private ConcurrentReaderHashMap policyConfigMap = new ConcurrentReaderHashMap();
   private DelegatingPolicy policy;

   /** Build the JACC policy configuration state machine from the
    * jacc-policy-config-states.xml file.
    * 
    */ 
   public JBossPolicyConfigurationFactory()
   {
      try
      {
         // Setup the state machine config
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL states = loader.getResource("org/jboss/security/jacc/jacc-policy-config-states.xml");
         StateMachineParser smp = new StateMachineParser();
         configStateMachine = smp.parse(states);        
      }
      catch(Exception e)
      {
         IllegalStateException ex = new IllegalStateException("Failed to parse jacc-policy-config-states.xml");
         ex.initCause(e);
         throw ex;
      }
      // Get the DelegatingPolicy
      Policy p = Policy.getPolicy();
      if( (p instanceof DelegatingPolicy) == false )
      {
         // Assume that the installed policy delegates to the DelegatingPolicy
         p = DelegatingPolicy.getInstance();
      }
      policy = (DelegatingPolicy) p;
   }

   public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove)
      throws PolicyContextException
   {
      JBossPolicyConfiguration pc = (JBossPolicyConfiguration) policyConfigMap.get(contextID);
      if( pc == null )
      {
         StateMachine sm = (StateMachine) configStateMachine.clone();
         pc = new JBossPolicyConfiguration(contextID, policy, sm);
         policyConfigMap.put(contextID, pc);
      }
      pc.initPolicyConfiguration(remove);
      return pc;
   }

   public boolean inService(String contextID)
      throws PolicyContextException
   {
      boolean inService = false;
      JBossPolicyConfiguration pc = (JBossPolicyConfiguration) policyConfigMap.get(contextID);
      if( pc != null )
         inService = pc.inService();
      return inService;
   }
}
