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

import javax.management.ObjectName;
import java.util.List;
import java.util.Map;

/**
 * This interface is implemented by an MBean that represents a relation.<p>
 * <p/>
 * Relations with only roles can be created by the relation service using
 * a {@link RelationSupport} object.<p>
 * <p/>
 * More complicated relations have to implemented manually. The
 * {@link RelationSupport} can be used to help in the implementation.<p>
 * <p/>
 * Any properties or methods must be exposed for management by the
 * implementing MBean.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface Relation
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * Retrieves the role for the passed role name. The role
    * must exist and be readable.<p>
    * <p/>
    * The return value is an ArrayList of object names in the role.
    *
    * @param roleName the role name.
    * @return the role.
    * @throws IllegalArgumentException for a null role name.
    * @throws RoleNotFoundException    when there is no such role or
    *                                  it is not readable.
    * @throws RelationServiceNotRegisteredException
    *                                  when the relation service
    *                                  is not registered with an MBeanServer.
    */
   public List getRole(String roleName)
           throws IllegalArgumentException, RoleNotFoundException, RelationServiceNotRegisteredException;

   /**
    * Retrieves the roles in this relation with the passed names.
    *
    * @param roleNames an array of role names
    * @return the roles both resolved and unresolved.
    * @throws IllegalArgumentException for a null role names.
    * @throws RelationServiceNotRegisteredException
    *                                  when the relation service
    *                                  is not registered with an MBeanServer.
    */
   public RoleResult getRoles(String[] roleNames)
           throws IllegalArgumentException, RelationServiceNotRegisteredException;

   /**
    * Retrieves the number of MBeans in a given role.
    *
    * @param roleName the role name.
    * @return the number of MBeans.
    * @throws IllegalArgumentException for a null role name.
    * @throws RoleNotFoundException    when there is no such role.
    */
   public Integer getRoleCardinality(String roleName)
           throws IllegalArgumentException, RoleNotFoundException;

   /**
    * Retrieves all the roles in this relation.
    *
    * @return the roles both resolved and unresolved.
    * @throws RelationServiceNotRegisteredException
    *          when the relation service
    *          is not registered with an MBeanServer.
    */
   public RoleResult getAllRoles()
           throws RelationServiceNotRegisteredException;

   /**
    * Retrieve all the roles in this relation without checking the role mode.
    *
    * @return the list of roles.
    */
   public RoleList retrieveAllRoles();

   /**
    * Sets the passed role for this relation.<p>
    * <p/>
    * The role is checked according to its role definition in the relation type.
    * The role is not valid if there are the wrong number of MBeans, an MBean
    * is of an incorrect class or an MBean does not exist.<p>
    * <p/>
    * The notification <i>RELATION_BASIC_UPDATE</i> is sent when the relation is
    * not an MBean or <i>RELATION_MBEAN_UPDATE</i> when it is.<p>
    *
    * @param role the new role.
    * @throws IllegalArgumentException      for a null role.
    * @throws InvalidRoleValueException     if the role is not valid.
    * @throws RoleNotFoundException         if the role is not writable.
    *                                       This test is not performed at initialisation.
    * @throws RelationServiceNotRegisteredException
    *                                       when the relation service
    *                                       is not registered with an MBeanServer.
    * @throws RelationTypeNotFoundException when the relation type has
    *                                       not been registered in the relation service.
    * @throws RelationNotFoundException     when this method is called for
    *                                       for an MBean not registered with the relation service.
    */
   public void setRole(Role role)
           throws IllegalArgumentException, RoleNotFoundException,  RelationTypeNotFoundException,
           InvalidRoleValueException, RelationServiceNotRegisteredException, RelationNotFoundException;

   /**
    * Sets the roles.<p>
    * <p/>
    * The roles are checked according to its role definition in the relation type.
    * The role is not valid if there are the wrong number of MBeans, an MBean
    * is of an incorrect class or an MBean does not exist.<p>
    * <p/>
    * A notification <i>RELATION_BASIC_UPDATE</i> is sent when the relation is
    * not an MBean or <i>RELATION_MBEAN_UPDATE</i> when it is for every updated
    * role.<p>
    * <p/>
    * The return role result has a role list for successfully updated roles and
    * an unresolved list for roles not set.
    *
    * @param roleList the new roles.
    * @return the resulting role result.
    * @throws IllegalArgumentException      for a null role name.
    * @throws RelationServiceNotRegisteredException
    *                                       when the relation service
    *                                       is not registered with an MBeanServer.
    * @throws RelationTypeNotFoundException when the relation type has
    *                                       not been registered in the relation service.
    * @throws RelationNotFoundException     when this method is called for
    *                                       for an MBean not registered with the relation service.
    */
   public RoleResult setRoles(RoleList roleList)
           throws IllegalArgumentException, RelationServiceNotRegisteredException,
           RelationTypeNotFoundException, RelationNotFoundException;

   /**
    * The relation service will call this service when an MBean
    * referenced in a role is unregistered.<p>
    * <p/>
    * The object name should be removed from the role.<p>
    * <p/>
    * <b>Calling this method manually may result in incorrect behaviour</b>
    *
    * @param objectName the object name unregistered.
    * @param roleName   the role the containing the object.
    * @throws RoleNotFoundException         if the role does exist or it is not
    *                                       writable.
    * @throws InvalidRoleValueException     when the role does not conform
    *                                       to the associated role info.
    * @throws RelationServiceNotRegisteredException
    *                                       when the relation service
    *                                       is not registered with an MBeanServer.
    * @throws RelationTypeNotFoundException when the relation type has
    *                                       not been registered in the relation service.
    * @throws RelationNotFoundException     when this method is called for
    *                                       for an MBean not registered with the relation service.
    */
   public void handleMBeanUnregistration(ObjectName objectName, String roleName)
           throws IllegalArgumentException, RoleNotFoundException, InvalidRoleValueException,
           RelationServiceNotRegisteredException, RelationTypeNotFoundException,
           RelationNotFoundException;

   /**
    * Retrieves MBeans referenced by roles of this relation.<p>
    * <p/>
    * The return value is a map keyed by MBean object names. The objects
    * are associated with an ArrayList that contains all the role names
    * the MBean has within this relation.
    *
    * @return the map of object names and their roles.
    */
   public Map getReferencedMBeans();

   /**
    * Retrieves the relation type for this relation.
    *
    * @return the relation type.
    */
   public String getRelationTypeName();

   /**
    * Retrieves the object name of the relation service this relation
    * is registered with.
    *
    * @return the relation service object name.
    */
   public ObjectName getRelationServiceName();

   /**
    * Retrieves the relation id used to identify the relation within
    * the relation service.
    *
    * @return the unique id.
    */
   public String getRelationId();

}
