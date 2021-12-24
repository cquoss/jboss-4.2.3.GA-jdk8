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
package org.jboss.test.cmp2.cmr.interfaces;

import javax.ejb.EJBLocalObject;

/**
 * Local interface for CMRBugEJB.
 */
public interface CMRBugEJBLocal extends EJBLocalObject
{
   /**
    * Describe <code>getId</code> method here.
    * @return a <code>String</code> value
    */
   public String getId(  ) ;

   /**
    * Describe <code>setId</code> method here.
    * @param id a <code>String</code> value
    */
   public void setId( String id ) ;

   /**
    * Describe <code>getDescription</code> method here.
    * @return a <code>String</code> value
    */
   public String getDescription(  ) ;

   /**
    * Describe <code>setDescription</code> method here.
    * @param description a <code>String</code> value
    */
   public void setDescription( String description ) ;

   /**
    * Describe <code>getParent</code> method here.
    * @return a <code>CMRBugEJBLocal</code> value
    */
   public CMRBugEJBLocal getParent(  ) ;

   /**
    * Describe <code>setParent</code> method here.
    * @param parent a <code>CMRBugEJBLocal</code> value
    */
   public void setParent( CMRBugEJBLocal parent ) ;

   /**
    * Describe <code>getChildren</code> method here.
    * @return a <code>Collection</code> value
    */
   public java.util.Collection getChildren(  ) ;

   /**
    * Describe <code>setChildren</code> method here.
    * @param children a <code>Collection</code> value
    */
   public void setChildren( java.util.Collection children ) ;

   /**
    * Describe <code>addChild</code> method here.
    * @param child a <code>CMRBugEJBLocal</code> value
    * @return a <code>boolean</code> value
    */
   public boolean addChild( CMRBugEJBLocal child ) ;

   /**
    * Describe <code>removeChild</code> method here.
    * @param child a <code>CMRBugEJBLocal</code> value
    * @return a <code>boolean</code> value
    */
   public boolean removeChild( CMRBugEJBLocal child ) ;

   public  CMRBugEJBLocal getPrevNode(  ) ;

   public void setPrevNode( CMRBugEJBLocal a_ViewComponent ) ;

   public CMRBugEJBLocal getNextNode(  ) ;

   public void setNextNode( CMRBugEJBLocal a_ViewComponent ) ;

}
