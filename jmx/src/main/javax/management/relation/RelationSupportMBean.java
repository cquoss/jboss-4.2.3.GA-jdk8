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

/**
 * This interface defines the management interface for a relation
 * created internally within the relation service. The relation can
 * have only roles - no attributes or mehods.<p>
 *
 * The relation support managed bean can be created externally, including
 * extending it, and then registered with the relation service.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface RelationSupportMBean
  extends Relation
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * Check to see whether this relation thinks it is in relation service.<p>
    *
    * WARNING: This is not a dynamic check. The flag is set within the
    * relation support object by the relation service, malicious programs
    * may modifiy it to an incorrect value.
    *
    * @return true when it is registered.
    */
   public Boolean isInRelationService();

   /**
    * Set the flag to specify whether this relation is registered with
    * the relation service.<p>
    *
    * WARNING: This method is exposed for management by the relation
    * service. Using this method outside of the relation service does
    * not affect the registration with the relation service.
    *
    * @param value pass true for managed by the relation service, false 
    *        otherwise.
    * @exception IllegalArgumentException for a null value
    */
   public void setRelationServiceManagementFlag(Boolean value) throws IllegalArgumentException;
}
