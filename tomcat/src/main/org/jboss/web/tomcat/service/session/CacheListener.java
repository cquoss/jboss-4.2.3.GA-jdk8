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
package org.jboss.web.tomcat.service.session;

import org.jboss.cache.Fqn;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;

/**
 * Listens for distributed caches events, notifying the JBossCacheManager
 * of events of interest. 
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: 57206 $
 */
public class CacheListener extends AbstractCacheListener
{
   // Element within an FQN that is JSESSION
   private static final int JSESSION_FQN_INDEX = 0;
   // Element within an FQN that is the hostname
   private static final int HOSTNAME_FQN_INDEX = 1;
   // ELEMENT within an FQN this is the webapp name
   private static final int WEBAPP_FQN_INDEX = 2;
   // Element within an FQN that is the session id
   private static final int SESSION_ID_FQN_INDEX = 3;
   // Size of an Fqn that points to the root of a session
   private static final int SESSION_FQN_SIZE = SESSION_ID_FQN_INDEX + 1;
   // Element within an FQN that is the root of a Pojo attribute map
   private static final int POJO_ATTRIBUTE_FQN_INDEX = SESSION_ID_FQN_INDEX + 1;
   // Element within an FQN that is the root of an individual Pojo attribute
   private static final int POJO_KEY_FQN_INDEX = POJO_ATTRIBUTE_FQN_INDEX + 1;
   // Size of an Fqn that points to the root of a session
   private static final int POJO_KEY_FQN_SIZE = POJO_KEY_FQN_INDEX + 1;
   // The index of the root of a buddy backup subtree
   private static final int BUDDY_BACKUP_ROOT_OWNER_INDEX = BuddyManager.BUDDY_BACKUP_SUBTREE_FQN.size();
   // The size of the root of a buddy backup subtree (including owner)
   private static final int BUDDY_BACKUP_ROOT_OWNER_SIZE = BUDDY_BACKUP_ROOT_OWNER_INDEX + 1;
   
//   private static final String TREE_CACHE_CLASS = "org.jboss.cache.TreeCache";
//   private static final String DATA_GRAVITATION_CLEANUP = "_dataGravitationCleanup";
   
   private static Logger log_ = Logger.getLogger(CacheListener.class);
   private JBossCacheWrapper cacheWrapper_;
   private JBossCacheManager manager_;
   private String webapp_;
   private String hostname_;
   private boolean fieldBased_;
   // When trying to ignore unwanted notifications, do we check for local activity first?
   private boolean disdainLocalActivity_;

   CacheListener(JBossCacheWrapper wrapper, JBossCacheManager manager, String hostname, String webapp)
   {
      cacheWrapper_ = wrapper;
      manager_ = manager;
      hostname_ = hostname;
      webapp_ =  webapp;
      int granularity = manager_.getReplicationGranularity();
      fieldBased_ = (granularity == WebMetaData.REPLICATION_GRANULARITY_FIELD);
      // TODO decide if disdaining local activity is always good for REPL_ASYNC
      disdainLocalActivity_ = (granularity == WebMetaData.REPLICATION_GRANULARITY_SESSION); // for now
   }

   // --------------- TreeCacheListener methods ------------------------------------

   public void nodeRemoved(Fqn fqn)
   {
      // Ignore our own activity if not field based
      boolean local = SessionReplicationContext.isLocallyActive();
      if (!fieldBased_ && local)
         return;
      
      boolean isBuddy = isBuddyFqn(fqn);
      int size = fqn.size();
      
      if(isFqnSessionRootSized(size, isBuddy))
      {
         if (!local && isFqnForOurWebapp(fqn, isBuddy))
         {
            // A session has been invalidated from another node;
            // need to inform manager
            String sessId = getIdFromFqn(fqn, isBuddy);
            manager_.processRemoteInvalidation(sessId);
         }
      }
      else if (fieldBased_ && isFqnForOurWebapp(fqn, isBuddy))
      {
         // Potential removal of a Pojo where we need to unregister as an Observer.
         if (!local && isFqnPojoKeySized(size, isBuddy))
         {
            String sessId = getIdFromFqn(fqn, isBuddy);
            String attrKey = getPojoKeyFromFqn(fqn, isBuddy);
            manager_.processRemoteAttributeRemoval(sessId, attrKey);
         }
         else if (local && isFqnInPojo(size, isBuddy))
         {
            // One of our pojo's is modified
            String sessId = getIdFromFqn(fqn, isBuddy);
            manager_.processLocalPojoModification(sessId);
         }
      }
   }

   public void nodeModified(Fqn fqn)
   {
      boolean local = SessionReplicationContext.isLocallyActive();
      if (!fieldBased_ && local)
         return;
      
      boolean isBuddy = isBuddyFqn(fqn);      
      int size = fqn.size();
      
      // We only care if this is for a session root or it's for a pojo
      if (isFqnSessionRootSized(size, isBuddy))
      {
         if (!local && isFqnForOurWebapp(fqn, isBuddy))
         {
            handleSessionRootModification(fqn, isBuddy);
         }
      }
      else if (fieldBased_ 
                  && local
                  && isFqnForOurWebapp(fqn, isBuddy)
                  && isFqnInPojo(size, isBuddy)) 
      {
         // One of our pojo's is modified
         String sessId = getIdFromFqn(fqn, isBuddy);
         manager_.processLocalPojoModification(sessId);
      }
   }

   private void handleSessionRootModification(Fqn fqn, boolean isBuddy)
   {      
      // We only care if this is for our webapp
      if (!isFqnForOurWebapp(fqn, isBuddy))
         return;
      
      // Query if we have version value in the distributed cache. 
      // If we have a version value, compare the version and invalidate if necessary.
      Integer version = (Integer)cacheWrapper_.get(fqn, JBossCacheService.VERSION_KEY);
      if(version != null)
      {
         String realId = getIdFromFqn(fqn, isBuddy);
         
         ClusteredSession session = manager_.findLocalSession(realId);
         if (session == null)
         {
            String owner = isBuddy ? getBuddyOwner(fqn) : null;
            // Notify the manager that an unloaded session has been updated
            manager_.unloadedSessionChanged(realId, owner);
         }
         else if (session.isNewData(version.intValue()))
         {
            // Need to invalidate the loaded session
            session.setOutdatedVersion(version.intValue());
            if(log_.isTraceEnabled())
            {
               log_.trace("nodeDirty(): session in-memory data is " +
                          "invalidated with id: " + realId + " and version: " +
                          version.intValue());
            }
         }
         else if (isBuddy)
         {
            // We have a local session but got a modification for the buddy tree.
            // This means another node is in the process of taking over the session;
            // we don't worry about it
            ;
         }
         else 
         {
            // This could be an issue but can happen legitimately in unusual 
            // circumstances, so just log something at INFO, not WARN
            
            // Unusual circumstance: create session; don't touch session again
            // until timeout period expired; fail over to another node after
            // timeout but before session expiration thread has run. Existing
            // session will be expired locally on new node and a new session created.
            // When that session replicates, the version id will match the still
            // existing cached session on the first node.  Unlikely, but due
            // to design of a unit test, it happens every testsuite run :-)
            log_.info("Possible concurrency problem: Replicated version id " + 
                       version + " matches in-memory version for session " + realId);
            
            // Mark the loaded session outdated anyway; in the above mentioned
            // "unusual circumstance" that's the correct thing to do
            session.setOutdatedVersion(version.intValue());
         }
      }
      else
      {
         log_.warn("No VERSION_KEY attribute found in " + fqn);
      }
   }

   private boolean isFqnForOurWebapp(Fqn fqn, boolean isBuddy)
   {
      try
      {
         if (webapp_.equals(fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + WEBAPP_FQN_INDEX : WEBAPP_FQN_INDEX))
               && hostname_.equals(fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + HOSTNAME_FQN_INDEX : HOSTNAME_FQN_INDEX))
               && JBossCacheService.SESSION.equals(fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + JSESSION_FQN_INDEX : JSESSION_FQN_INDEX)))
            return true;
      }
      catch (IndexOutOfBoundsException e)
      {
         // can't be ours; too small; just fall through
      }

      return false;
   }
   
   private static boolean isFqnSessionRootSized(int size, boolean isBuddy)
   {
      return size == (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SESSION_FQN_SIZE : SESSION_FQN_SIZE);
   }
   
   private static boolean isFqnPojoKeySized(int size, boolean isBuddy)
   {
      return size == (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + POJO_KEY_FQN_SIZE : POJO_KEY_FQN_SIZE);
   }
   
   private static boolean isFqnInPojo(int size, boolean isBuddy)
   {
      return size >= (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + POJO_KEY_FQN_SIZE : POJO_KEY_FQN_SIZE);
   }
   
   private static String getIdFromFqn(Fqn fqn, boolean isBuddy)
   {
      return (String)fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SESSION_ID_FQN_INDEX : SESSION_ID_FQN_INDEX);
   }
   
   private static String getPojoKeyFromFqn(Fqn fqn, boolean isBuddy)
   {
      return (String) fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + POJO_KEY_FQN_INDEX: POJO_KEY_FQN_INDEX);
   }
   
   private static boolean isBuddyFqn(Fqn fqn)
   {
      try
      {
         return BuddyManager.BUDDY_BACKUP_SUBTREE.equals(fqn.get(0));
      }
      catch (IndexOutOfBoundsException e)
      {
         // Can only happen if fqn is ROOT, and we shouldn't get
         // notifications for ROOT.
         // If it does, just means it's not a buddy
         return false;
      }      
   }
   
   /**
    * Extracts the owner portion of an buddy subtree Fqn.
    * 
    * @param fqn An Fqn that is a child of the buddy backup root node.
    */
   private static String getBuddyOwner(Fqn fqn)
   {
      return (String) fqn.get(BUDDY_BACKUP_ROOT_OWNER_INDEX);     
   }
   
//   /**
//    * FIXME This is a hack that examines the stack trace looking
//    * for the TreeCache._dataGravitationCleanup method.
//    * 
//    * @return
//    */
//   private static boolean isDataGravitationCleanup()
//   {
//      StackTraceElement[] trace = new Throwable().getStackTrace();
//      for (int i = 0; i < trace.length; i++)
//      {
//         if (TREE_CACHE_CLASS.equals(trace[i].getClassName())
//               && DATA_GRAVITATION_CLEANUP.equals(trace[i].getMethodName()))
//            return true;
//      }
//      
//      return false;
//   }
}
