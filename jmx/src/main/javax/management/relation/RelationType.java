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
package javax.management.relation;

import java.io.Serializable;

import java.util.List;

/**
 * This interface is implemented by a class that represents a relation.<p>
 *
 * The class {@link RelationTypeSupport} is available to help
 * implement this interface.<p>
 *
 * A relation type has a name and a list of role info objects for the
 * relation.<p>
 * 
 * A relation type has to registered in the relation service. This is done
 * either by using createRelationType() to get a RelationTypeSupport
 * object kepy in the relation service, or by using addRelationType()
 * to add an external relation type to the relation service.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 */
public interface RelationType
  extends Serializable
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * Retrieves the name of this relation type.
    *
    * @return the name.
    */
   public String getRelationTypeName();

   /**
    * Retrieves the list of role definitions in this relation type.<p>
    *
    * The return value is a list of RoleInfo objects. The list must be
    * an ArrayList.
    *
    * @return the list of Role Infos.
    */
   public List getRoleInfos();

   /**
    * Retrieves the role info for a role name.<p>
    *
    * @return the role info or null.
    * @exception IllegalArgumentException for a null role info name.
    * @exception RoleInfoNotFoundException for no role info with the
    *            passed name in the relation type.
    */
   public RoleInfo getRoleInfo(String roleInfoName)
     throws IllegalArgumentException, RoleInfoNotFoundException;
}
