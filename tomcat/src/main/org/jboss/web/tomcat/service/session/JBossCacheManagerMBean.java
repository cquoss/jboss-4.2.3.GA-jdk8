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

public interface JBossCacheManagerMBean extends JBossManagerMBean
{
   /**
    * Gets the value of the attribute with the given key from the given
    * session.  If the session is in the distributed store but hasn't been
    * loaded on this node, invoking this method will cause it to be loaded.
    * 
    * @param sessionId the id of the session
    * @param key       the attribute key
    * @return          the value, or <code>null</code> if the session or
    *                  key does not exist.
    */
   Object getSessionAttribute(String sessionId, String key);
   
   /**
    * Same as <code>getSessionAttribute(sessionId, key).toString()</code>.
    * 
    */
   String getSessionAttributeString(String sessionId, String key);
   
   /**
    * Expires the given session. If the session is in the distributed store 
    * but hasn't been loaded on this node, invoking this method will cause it 
    * to be loaded.
    * 
    * @param sessionId the id of the session
    */
   void expireSession(String sessionId);
   
   /**
    * Gets the last time the given session was accessed on this node.
    * Information about sessions stored in the distributed store but never
    * accessed on this node will not be made available.
    * 
    * @param sessionId
    * @return the last accessed time, or <code>null</code> if the session
    *         has expired or has never been accessed on this node.
    */
   String getLastAccessedTime(String sessionId);
   
   /**
    * Gets the JMX ObjectName of the distributed session cache as a string.
    */
   String getCacheObjectNameString();
   
   /**
    * Gets the replication granularity.
    * 
    * @return SESSION, ATTRIBUTE or FIELD, or <code>null</code> if this
    *         has not yet been configured.
    */
   String getReplicationGranularityString();

   /**
    * Gets the replication trigger.
    * 
    * @return SET, SET_AND_GET, SET_AND_NON_PRIMITIVE_GET or <code>null</code> 
    *         if this has not yet been configured.
    */
   String getReplicationTriggerString();
   
   /**
    * Gets whether batching of field granularity changes will be done.  Only
    * relevant with replication granularity FIELD.
    * 
    * @return <code>true</code> if per-request batching will be done, 
    *         <code>false</code> if not, <code>null</code> if not configured
    */
   Boolean isReplicationFieldBatchMode();
   
   /**
    * Gets whether JK is being used and special handling of a jvmRoute
    * portion of session ids is needed.
    */
   boolean getUseJK();
   
   /**
    * Gets the snapshot mode.
    * 
    * @return "instant" or "interval"
    */
   String getSnapshotMode();
   
   /**
    * Gets the number of milliseconds between replications if "interval" mode
    * is used.
    */
   int getSnapshotInterval();

   /**
    * Get the maximum interval between requests, in seconds, after which a
    * request will trigger replication of the session's metadata regardless
    * of whether the request has otherwise made the session dirty. Such 
    * replication ensures that other nodes in the cluster are aware of a 
    * relatively recent value for the session's timestamp and won't incorrectly
    * expire an unreplicated session upon failover.
    * <p/>
    * Default value is {@link #DEFAULT_MAX_UNREPLICATED_INTERVAL}.
    * <p/>
    * The cost of the metadata replication depends on the configured
    * {@link #setReplicationGranularityString(String) replication granularity}.
    * With <code>SESSION</code>, the sesssion's attribute map is replicated 
    * along with the metadata, so it can be fairly costly.  With other 
    * granularities, the metadata object is replicated separately from the
    * attributes and only contains a String, and a few longs, ints and booleans.
    * 
    * @return the maximum interval since last replication after which a request
    *         will trigger session metadata replication. A value of 
    *         <code>0</code> means replicate metadata on every request; a value 
    *         of <code>-1</code> means never replicate metadata unless the 
    *         session is otherwise dirty.
    */
   public int getMaxUnreplicatedInterval();

   /**
    * Sets the maximum interval between requests, in seconds, after which a
    * request will trigger replication of the session's metadata regardless
    * of whether the request has otherwise made the session dirty.
    * 
    * @param  maxUnreplicatedInterval  
    *         the maximum interval since last replication after which a request
    *         will trigger session metadata replication. A value of 
    *         <code>0</code> means replicate metadata on every request; a value 
    *         of <code>-1</code> means never replicate metadata unless the 
    *         session is otherwise dirty.
    */
   public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval);
   
   /**
    * Lists all session ids known to this manager, including those in the 
    * distributed store that have not been accessed on this node.
    * 
    * @return a comma-separated list of session ids
    */
   String listSessionIds();
   
   /**
    * Lists all session ids known to this manager, excluding those in the 
    * distributed store that have not been accessed on this node.
    * 
    * @return a comma-separated list of session ids
    */
   String listLocalSessionIds();
}
