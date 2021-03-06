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
package test.compliance.server.support;

import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;

/**
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 *   
 */
public class Test4 implements Test4MBean, MBeanRegistration
{

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      throw new MBeanRegistrationException(new MyScreamingException());
   }
   
   public void postRegister(Boolean b)
   {
   }
   
   public void preDeregister() throws Exception
   {
   }
   
   public void postDeregister()
   {
      
   }
}




