/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.test;

import junit.framework.Test;

/**
 * Runs the HASingleton election policy tests with extended ha singleton 
 * election policies. 
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class ExtendedHASingletonElectionPolicyTestCase extends HASingletonElectionPolicyTestCase
{
   public ExtendedHASingletonElectionPolicyTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      // Refer to jboss-service.xml under resources/ha/electionpolicy
      return getDeploySetup(ExtendedHASingletonElectionPolicyTestCase.class, "extended-ha-electionpolicy-service.xml");
   }   
}