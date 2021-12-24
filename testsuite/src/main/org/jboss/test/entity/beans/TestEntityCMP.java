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
package org.jboss.test.entity.beans;

import org.jboss.test.entity.interfaces.TestEntityValue;

/**
 * CMP layer for TestEntity.
 */
public abstract class TestEntityCMP
   extends TestEntityBean
   implements javax.ejb.EntityBean
{

   public void ejbLoad() 
   {
   }

   public void ejbStore() 
   {
   }

   public void ejbActivate() 
   {
   }

   public void ejbPassivate() 
   {

      TestEntityValue = null;
   }

   public void setEntityContext(javax.ejb.EntityContext ctx) 
   {
   }

   public void unsetEntityContext() 
   {
   }

   public void ejbRemove() throws javax.ejb.RemoveException
   {

   }

 /* Value Objects BEGIN */

   private TestEntityValue TestEntityValue = null;

   public TestEntityValue getTestEntityValue()
   {
      TestEntityValue = new TestEntityValue();
      try
         {
            TestEntityValue.setEntityID( getEntityID() );
            TestEntityValue.setValue1( getValue1() );

         }
         catch (Exception e)
         {
            throw new javax.ejb.EJBException(e);
         }

	  return TestEntityValue;
   }

/* Value Objects END */

   public abstract String getEntityID() ;

   public abstract void setEntityID( String entityID ) ;

   public abstract String getValue1() ;

   public abstract void setValue1( String value1 ) ;

}
