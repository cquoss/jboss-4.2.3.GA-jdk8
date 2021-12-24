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
package org.jboss.test.foedeployer.ejb.simple;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

/**
 * Models a top secret.
 *
 * @ejb.bean
 *    name="Secret"
 *    generate="true"
 *    view-type="both"
 *    type="CMP"
 *    jndi-name="ejb/Secret"
 *    local-jndi-name="ejb/SecretLocal"
 *    reentrant="False"
 *    cmp-version="2.x"
 *    primkey-field="secretKey"
 *
 * @ejb.pk
 *    class="java.lang.String"
 *    generate="false"
 *
 * @ejb.transaction type="Required"
 *
 * @@ejb.persistence table-name="SECRET"
 * @weblogic:table-name secret
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 */
public abstract class SecretBean
   implements EntityBean
{
   // Attributes ----------------------------------------------------
   private EntityContext mContext;
   
   // CMP Accessors -------------------------------------------------
   /**
    * Secret key: primary key field
    *
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    *
    * xdoclet needs to be updated
    * @@ejb.persistence
    *    column-name="username"
    *    jdbc-type="VARCHAR"
    *    sql-type="VARCHAR(32)"
    *
    * @weblogic:dbms-column secret_key
    */
   public abstract String getSecretKey();
   public abstract void setSecretKey( String secretKey );
   
   /**
    * Secret: persistent field
    *
    * @ejb.persistent-field
    * @ejb.interface-method
    *
    * xdoclet needs to be updated
    * @@ejb.persistence
    *    column-name="password"
    *
    * @weblogic:dbms-column secret
    */
   public abstract String getSecret();
   /**
    * @ejb.interface-method
    */
   public abstract void setSecret( String secret );
   
   // EntityBean Implementation -------------------------------------
   /**
    * @ejb.create-method
    */
   public String ejbCreate( String secretKey, String secret )
      throws CreateException
   {
      setSecretKey(secretKey);
      setSecret(secret);
      return null;
   }

   public void ejbPostCreate( String secretKey, String secret ) { }

   public void setEntityContext( EntityContext ctx )
   {
      mContext = ctx;
   }
   
   public void unsetEntityContext()
   {
      mContext = null;
   }
   
   public void ejbRemove() throws RemoveException { }
   public void ejbActivate() { }
   public void ejbPassivate() { }
   public void ejbLoad() { }
   public void ejbStore() { }
}
