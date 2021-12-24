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
package org.jboss.ha.singleton;

/** 
 * The management interface for the singleton support service.
 * 
 * @author <a href="mailto:Alex.Fu@novell.com">Alex Fu</a>
 * @version $Revision$
 */
public interface HASingletonSupportMBean extends HASingletonMBean 
{
   /** The HASingleton election policy MBean */
   void setElectionPolicy(HASingletonElectionPolicy mb);
   HASingletonElectionPolicy getElectionPolicy();

   /**
    * Gets whether this singleton will stop and restart itself if it is the
    * master and a cluster merge occurs.
    * <p/>
    * A restart allows the service to reset any state that may
    * have gotten out-of-sync with the rest of the cluster while
    * the just-merged split was in effect.
    * 
    * @return <code>true</code> if a restart will occur, <code>false</code>
    *         otherwise
    */
   boolean getRestartOnMerge();

   /**
    * Sets whether this singleton will stop and restart itself if it is the
    * master and a cluster merge occurs?
    * <p/>
    * A restart allows the service to reset any state that may
    * have gotten out-of-sync with the rest of the cluster while
    * the just-merged split was in effect.
    * 
    * @param restartOnMerge <code>true</code> if a restart should occur, 
    *                       <code>false</code> otherwise
    */
   void setRestartOnMerge(boolean restartOnMerge);
}
