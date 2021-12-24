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
package org.jboss.test.cluster.test;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.cache.Fqn;

/**
 * Utilities for session testing.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 65056 $
 */
public class SessionTestUtil
{  
   private static final String[] TYPES = 
   { String.class.getName(), Object.class.getName() };
   private static final String VERSION_KEY = "VERSION";
   
   private static final Fqn BUDDY_BACKUP_SUBTREE_FQN = Fqn.fromString("_BUDDY_BACKUP_");
   
   private static final ObjectName CACHE_OBJECT_NAME;
   static
   {
      try
      {
         CACHE_OBJECT_NAME =
            new ObjectName("jboss.cache:service=TomcatClusteringCache");
      }
      catch (MalformedObjectNameException e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }
   
   public static Object getSessionVersion(MBeanServerConnection adaptor, String sessionFqn) throws Exception
   {
      return adaptor.invoke(CACHE_OBJECT_NAME, "get", new Object[]
      {sessionFqn, VERSION_KEY}, TYPES);
   }

   public static Object getBuddySessionVersion(MBeanServerConnection adaptor, String sessionFqn) throws Exception
   {
      Object replVersion = null;
      //    Check in the buddy backup tree
      Set buddies = (Set) adaptor.invoke(CACHE_OBJECT_NAME, "getChildrenNames", new Object[]
      {BUDDY_BACKUP_SUBTREE_FQN}, new String[]
      {Fqn.class.getName()});

      if (buddies != null)
      {
         for (Iterator it = buddies.iterator(); it.hasNext() && replVersion == null;)
         {
            Fqn fqn = new Fqn(BUDDY_BACKUP_SUBTREE_FQN, it.next());
            fqn = new Fqn(fqn, Fqn.fromString(sessionFqn));
            replVersion = adaptor.invoke(CACHE_OBJECT_NAME, "get", new Object[]
            {fqn.toString(), VERSION_KEY}, TYPES);
         }
      }

      return replVersion;
   }

   private SessionTestUtil() {}
}
