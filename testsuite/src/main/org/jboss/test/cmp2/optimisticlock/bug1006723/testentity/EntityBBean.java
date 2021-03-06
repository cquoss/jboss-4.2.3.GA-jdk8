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
package org.jboss.test.cmp2.optimisticlock.bug1006723.testentity;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.EntityContext;
import java.util.Date;

public abstract class EntityBBean implements EntityBean{

	public Long getOID(){
    	return getOIDCMP();
	}

	public void setLastModified(Date date) throws Exception{
    	if (date == null)
			throw new Exception("A date must be specified!");
		setLastModifiedCMP(date.getTime());
	}

	public abstract void setOIDCMP(Long oID);

	public abstract Long getOIDCMP();

	public abstract void setLastModifiedCMP(long date);

	public abstract long getLastModifiedCMP();

	public Long ejbCreate(Long id) throws CreateException{
		setOIDCMP(id);
		return id;
	}

	public void ejbPostCreate(Long id) throws CreateException{}

	public void ejbActivate(){}

	public void ejbPassivate(){}

	public void ejbLoad(){}

	public void ejbStore(){}

	public void ejbRemove(){}

	public void setEntityContext(EntityContext context){}

	public void unsetEntityContext(){}

}