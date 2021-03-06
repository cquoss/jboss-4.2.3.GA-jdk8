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
package org.jboss.test.cmp2.cmrtransaction.ejb;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.cmp2.cmrtransaction.interfaces.TreeLocalHome;
import org.jboss.test.cmp2.cmrtransaction.interfaces.TreeLocal;


/**
 *
 * @author  B Stansberry brian_stansberry@wanconcepts.com
 */
public class TreeFacadeSession implements SessionBean
{
    // -------------------------------------------------------  Instance Fields

    private transient TreeLocalHome treeHome = null;

    // --------------------------------------------------------  Bean Lifecycle


    public TreeFacadeSession() {}

    public void ejbCreate() throws CreateException {}
    public void setSessionContext(SessionContext sessionContext) {}
    public void ejbRemove() throws EJBException {}
    public void ejbActivate() throws EJBException {}
    public void ejbPassivate() throws EJBException {}

    // ------------------------------------------------------------  TreeFacade


    public void setup()
    {
        try
        {
            TreeLocalHome tlh = getTreeLocalHome();
            TreeLocal tl = null;
            try
            {
                tl = tlh.findByPrimaryKey("Parent");
                tl.remove();
            }
            catch (ObjectNotFoundException f) {}
            try
            {
                tl = tlh.findByPrimaryKey("Child 1");
                tl.remove();
            }
            catch (ObjectNotFoundException f) {}
            try
            {
                tl = tlh.findByPrimaryKey("Child 2");
                tl.remove();
            }
            catch (ObjectNotFoundException f) {}
        }
        catch (NamingException n)
        {
            throw new EJBException(n);
        }
        catch (RemoveException r)
        {
            throw new EJBException(r);
        }
        catch (FinderException f)
        {
            throw new EJBException(f);
        }
    }


    public void createNodes()
    {
        try
        {
            TreeLocalHome tlh = getTreeLocalHome();
            TreeLocal parent = null;
            parent = tlh.create("Parent", null);
            tlh.create("Child 1", parent);
            tlh.create("Child 2", null);
        }
        catch (NamingException n)
        {
            throw new EJBException(n);
        }
        catch (CreateException c)
        {
            throw new EJBException(c);
        }
    }


    public void rearrangeNodes()
    {
        try
        {
            TreeLocalHome tlh = getTreeLocalHome();
            TreeLocal target = tlh.findByPrimaryKey("Child 2");
            TreeLocal sibling = null;
            sibling = tlh.findByPrimaryKey("Child 1");
            /*
            TreeLocal parent = tlh.findByPrimaryKey("Parent");
            Collection coll = parent.getMenuChildren();
            Iterator iter = coll.iterator();
            sibling = (TreeLocal) iter.next();
            */
            target.setMenuParent(sibling.getMenuParent());
            target.setPrecededBy(sibling);
        }
        catch (NamingException n)
        {
            throw new EJBException(n);
        }
        catch (FinderException f)
        {
            throw new EJBException(f);
        }
    }


    // -------------------------------------------------------  Private Methods

    private TreeLocalHome getTreeLocalHome() throws NamingException
    {
        if (treeHome == null)
        {
            InitialContext ctx = new InitialContext();
            Object obj = ctx.lookup("java:/comp/env/ejb/cmrTransactionTest/CMRTreeLocal");
            treeHome = (TreeLocalHome)
                    PortableRemoteObject.narrow(obj, TreeLocalHome.class);
        }
        return treeHome;
    }

}
