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
package org.jboss.jms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 */
public class SortedSetPriorityQueue implements PriorityQueue
{
    private SortedSet contents = null;

    public SortedSetPriorityQueue()
    {
        this.contents = new TreeSet();
    }

    public SortedSetPriorityQueue(Comparator comparator)
    {
        this.contents = new TreeSet(comparator);
    }

    public void enqueue(Object object)
    {
        this.contents.add(object);
    }

    public void enqueue(Collection collection)
    {
        this.contents.addAll(collection);
    }

    public Object dequeue()
    {
        if (!this.contents.isEmpty())
        {
            Iterator iterator = this.contents.iterator();
            Object object = iterator.next();
            iterator.remove();
            return object;
        }
        else
        {
            return null;
        }
    }

    public Collection dequeue(int maximumItems)
    {
        Collection items = new ArrayList(maximumItems);
        Iterator iterator = this.contents.iterator();
        int i = 0;
        while (iterator.hasNext() && i++ < maximumItems)
        {
            items.add(iterator.next());
            iterator.remove();
        }
        return items;
    }

    public Object peek()
    {
        return this.contents.first();
    }

    public Collection peek(int maximumItems)
    {
        Collection items = new ArrayList(maximumItems);
        Iterator iterator = this.contents.iterator();
        int i = 0;
        while (iterator.hasNext() && i++ < maximumItems)
        {
            items.add(iterator.next());
        }
        return items;
    }

    public void purge()
    {
        this.contents.clear();
    }

    public int size()
    {
        return this.contents.size();
    }

    public boolean isEmpty()
    {
        return this.contents.isEmpty();
    }
}
