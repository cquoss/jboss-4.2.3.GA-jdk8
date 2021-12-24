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
package org.jboss.jms.p2p.naming;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.spi.InitialContextFactory;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Simple {@link InitialContextFactory} implementation to enable using the JMS/JBoss Pure P2P.
 *
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 */
public class InitialContextFactoryImpl implements InitialContextFactory
{
    private static final Map INITIAL_CONTEXT_MAP = new HashMap();

    public synchronized Context getInitialContext(Hashtable environment) throws NamingException
    {
        try
        {
            Integer key = new Integer(environment.hashCode());
            if (!INITIAL_CONTEXT_MAP.containsKey(key))
            {
                Context context = new ContextImpl(environment);
                INITIAL_CONTEXT_MAP.put(key, context);
                return context;
            }
            else
            {
                return (Context) INITIAL_CONTEXT_MAP.get(key);
            }
        }
        catch (Exception exception)
        {
            throw new NamingException(exception.getMessage());
        }
    }

    static synchronized void unregisterInitialContext(Context context)
    {
        if (INITIAL_CONTEXT_MAP.containsValue(context))
        {
            Iterator iterator = INITIAL_CONTEXT_MAP.keySet().iterator();
            while (iterator.hasNext())
            {
                Object key = iterator.next();
                if (INITIAL_CONTEXT_MAP.get(key) == context)
                {
                    INITIAL_CONTEXT_MAP.remove(key);
                }
            }
        }
    }
}
