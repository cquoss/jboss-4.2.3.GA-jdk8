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

import org.jboss.cache.AbstractTreeCacheListener;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;
import org.jgroups.View;

/**
 * Overrides the standard JBoss Cache version to eliminate the overhead
 * of trace logging checks. Each method is implemented as a complete no-op.
 *
 * @author Brian Stansberry
 */
public abstract class AbstractCacheListener extends AbstractTreeCacheListener
{
    @Override
    public void nodeCreated(Fqn fqn)
    {
    }

    @Override
    public void nodeRemoved(Fqn fqn)
    {
    }

    @Override
    public void nodeLoaded(Fqn fqn)
    {       
    }

    @Override
    public void nodeEvicted(Fqn fqn)
    {
    }

    @Override
    public void nodeModify(Fqn fqn, boolean pre, boolean isLocal)
    {
    }
    
    @Override
    public void nodeModified(Fqn fqn)
    {
    }

    @Override
    public void nodeVisited(Fqn fqn)
    {
    }

    @Override
    public void cacheStarted(TreeCache cache)
    {
    }

    @Override
    public void cacheStopped(TreeCache cache)
    {
    }

    @Override
    public void viewChange(View new_view)
    {
    }

    @Override
    public void nodeEvict(Fqn fqn, boolean pre)
    {
      
    }

    @Override
    public void nodeRemove(Fqn fqn, boolean pre, boolean isLocal)
    {
    }

    @Override
    public void nodeActivate(Fqn fqn, boolean pre)
    {
    }

    @Override
    public void nodePassivate(Fqn fqn, boolean pre)
    {
    }
}
