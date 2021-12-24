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
package org.jboss.test.cmp2.fkstackoverflow.ejb;

/**
 * Local interface for Child.
 */
public interface ChildLocal
   extends javax.ejb.EJBLocalObject
{ 
   public Long getId(  ) ;

   public String getFirstName(  ) ;

   public Long getSimpleParentId(  ) ;

   public void setSimpleParentId( Long parentId ) ;

   public Long getComplexParentId1(  ) ;

   public void setComplexParentId1( Long parentId ) ;

   public Long getComplexParentId2(  ) ;

   public void setComplexParentId2( Long parentId ) ;

   public  SimpleParentLocal getSimpleParent1(  ) ;

   public void setSimpleParent1(  SimpleParentLocal parent ) ;

   public  SimpleParentLocal getSimpleParent2(  ) ;

   public void setSimpleParent2(  SimpleParentLocal parent ) ;

   public  ComplexParentLocal getComplexParent1(  ) ;

   public void setComplexParent1(  ComplexParentLocal parent ) ;

   public  ComplexParentLocal getComplexParent2(  ) ;

   public void setComplexParent2(  ComplexParentLocal parent ) ;

}
