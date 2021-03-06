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
package org.jboss.test.foedeployer.ejb.ql;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;

import org.apache.log4j.Category;

/**
 * Models a car.
 *
 * @ejb.bean
 *    name="Car"
 *    generate="true"
 *    view-type="local"
 *    type="CMP"
 *    local-jndi-name="CarEJB.CarHome"
 *    reentrant="False"
 *    cmp-version="2.x"
 *    primkey-field="number"
 *
 * @ejb.pk
 *    class="java.lang.String"
 *    generate="false"
 *
 * @ejb.transaction type="Required"
 *
 * @ejb.finder
 *    view-type="local"
 *    signature="java.util.Collection findAll()"
 *    query="SELECT OBJECT(c) FROM Car AS c"
 *
 * @ejb.finder
 *     view-type="local"
 *     signature="java.util.Collection findByColor( java.lang.String color )"
 *     query="SELECT OBJECT(c) FROM Car AS c WHERE c.color = ?1"
 *
 * @ejb.finder
 *    view-type="local"
 *    signature="java.util.Collection findAfterYear( int year )"
 *    query="SELECT OBJECT(c) FROM Car AS c WHERE c.year > ?1"
 *
 * @@ejb.persistence table-name="cars"
 * @weblogic:table-name cars
 *
 * @jboss.create-table "${jboss.create.table}"
 * @jboss.remove-table "${jboss.remove.table}"
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 */
public abstract class CarBean
   implements EntityBean
{
   // Constants -----------------------------------------------------
   static Category log = Category.getInstance( CarBean.class );

   // Attributes ----------------------------------------------------
   private EntityContext ctx;


   // CMP Accessors -------------------------------------------------
   /**
    * Car's number: primary key field
    *
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    *
    * xdoclet needs to be updated
    * @@ejb.persistence
    *    column-name="number"
    *    jdbc-type="VARCHAR"
    *    sql-type="VARCHAR(50)"
    *
    * @weblogic:dbms-column number
    */
   public abstract String getNumber();
   public abstract void setNumber(String number);

   /**
    * Car's color: persistent field
    *
    * @ejb.persistent-field
    * @ejb.interface-method
    *
    * xdoclet needs to be updated
    * @@ejb.persistence
    *    column-name="color"
    *    jdbc-type="VARCHAR"
    *    sql-type="VARCHAR(30)"
    *
    * @weblogic:dbms-column color
    */
   public abstract String getColor();
   public abstract void setColor(String color);

   /**
    * Year of birth: persistent field
    *
    * @ejb.persistent-field
    * @ejb.interface-method
    *
    * xdoclet needs to be updated
    * @@ejb.persistence
    *    column-name="year"
    *    jdbc-type="INTEGER"
    *    sql-type="INTEGER"
    *
    * @weblogic:dbms-column year
    */
   public abstract int getYear();
   public abstract void setYear(int year);

   // EntityBean Implementation -------------------------------------
   /**
    * @ejb:create-method
    */
   public String ejbCreate( String number, String color, int year )
      throws CreateException
   {
      setNumber( number );
      setColor( color );
      setYear( year );
      return null; // See 9.4.2 of the EJB 1.1 specification
   }

   public void ejbPostCreate( String number, String color, int year ) { }

   /**
    * @ejb:interface-method
    */
   public void ejbRemove()
      throws RemoveException
   {
      log.debug("removed: " + ctx.getPrimaryKey() );
   }

   public void setEntityContext(EntityContext ctx)
   {
      this.ctx = ctx;
   }

   public void unsetEntityContext()
   {
      ctx = null;
   }

   public void ejbActivate() { }
   public void ejbPassivate() { }
   public void ejbLoad() { }
   public void ejbStore() { }
}
