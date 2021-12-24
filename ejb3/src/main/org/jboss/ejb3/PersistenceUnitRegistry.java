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
package org.jboss.ejb3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import org.jboss.logging.Logger;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision: 60547 $</tt>
 */
public class PersistenceUnitRegistry
{
   private static final Logger log = Logger.getLogger(PersistenceUnitRegistry.class);

   private static ConcurrentHashMap<String, PersistenceUnitDeployment> persistenceUnits = new ConcurrentHashMap<String, PersistenceUnitDeployment>();

   public static void register(PersistenceUnitDeployment container)
   {
      String kernelName = container.getKernelName();
      if (persistenceUnits.contains(kernelName)) throw new RuntimeException("Persistence Unit is already registered: " + kernelName);
      log.trace("register " + kernelName);
      persistenceUnits.put(kernelName, container);
   }

   public static void unregister(PersistenceUnitDeployment container)
   {
      String kernelName = container.getKernelName();
      log.trace("unregister " + kernelName);
      persistenceUnits.remove(kernelName);
   }

   public static PersistenceUnitDeployment getPersistenceUnit(String kernelName)
   {
      PersistenceUnitDeployment unit = persistenceUnits.get(kernelName);
      log.trace("get " + kernelName + " = " + unit);
      if(unit == null)
         throw new IllegalStateException("Unable to find persistence unit " + kernelName);
      return unit;
   }

   public static Collection<PersistenceUnitDeployment> getPersistenceUnits()
   {
      return persistenceUnits.values();
   }

}
