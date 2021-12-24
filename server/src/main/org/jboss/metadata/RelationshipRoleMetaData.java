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
package org.jboss.metadata;

import org.w3c.dom.Element;
import org.jboss.deployment.DeploymentException;

/** 
 * Represents one ejb-relationship-role element found in the ejb-jar.xml
 * file's ejb-relation elements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 57209 $
 */
public class RelationshipRoleMetaData extends MetaData {
   // one is one
   private static int ONE = 1;
   // and two is many :)
   private static int MANY = 2;
   
   /**
    * Role name
    */
   private String relationshipRoleName;
   
   /**
    * The relation to which the role belongs.
    */
    private RelationMetaData relationMetaData;
   
   /**
    * Multiplicity of role, ONE or MANY.
    */
   private int multiplicity;
   
   /**
    * Should this entity be deleted when related entity is deleted.
    */
   private boolean cascadeDelete;
   
   /**
    * Name of the entity that has this role.
    */
   private String entityName;
   
   /**
    * Name of the entity's cmr field for this role.
    */
   private String cmrFieldName;
   
   /**
    * Type of the cmr field (i.e., collection or set)
    */
   private String cmrFieldType;

   public RelationshipRoleMetaData(RelationMetaData relationMetaData) {
      this.relationMetaData = relationMetaData;
   }
   
   /**
    * Gets the relationship role name
    */
   public String getRelationshipRoleName() {
      return relationshipRoleName;
   }

   /**
    * Gets the relation meta data to which the role belongs.
    * @returns the relation to which the relationship role belongs
    */
   public RelationMetaData getRelationMetaData() {
      return relationMetaData;
   }
   
   /**
    * Gets the related role's metadata
    */
   public RelationshipRoleMetaData getRelatedRoleMetaData() {
      return relationMetaData.getOtherRelationshipRole(this);
   }
   
   /**
    * Checks if the multiplicity is one.
    */
   public boolean isMultiplicityOne() {
      return multiplicity == ONE;
   }
   
   /**
    * Checks if the multiplicity is many.
    */
   public boolean isMultiplicityMany() {
      return multiplicity == MANY;
   }
   
   /**
    * Should this entity be deleted when related entity is deleted.
    */
   public boolean isCascadeDelete() {
      return cascadeDelete;
   }
   
   /**
    * Gets the name of the entity that has this role.
    */
   public String getEntityName() {
      return entityName;
   }
   
   /**
    * Gets the name of the entity's cmr field for this role.
    */
   public String getCMRFieldName() {
      return cmrFieldName;
   }
   
   /**
    * Gets the type of the cmr field (i.e., collection or set)
    */
   public String getCMRFieldType() {
      return cmrFieldType;
   }
   
   public void importEjbJarXml (Element element) throws DeploymentException {
      // ejb-relationship-role-name?
      relationshipRoleName =
            getOptionalChildContent(element, "ejb-relationship-role-name");

      // multiplicity
      String multiplicityString =
            getUniqueChildContent(element, "multiplicity");
      if("One".equals(multiplicityString)) {
         multiplicity = ONE;
      } else if("Many".equals(multiplicityString)) {
         multiplicity = MANY;
      } else {
         throw new DeploymentException("multiplicity must be exactaly 'One' " +
               "or 'Many' but is " + multiplicityString + "; this is case " +
               "sensitive");
      }

      // cascade-delete?
      if(getOptionalChild(element, "cascade-delete") != null) {
         cascadeDelete = true;
      }

      // relationship-role-source
      Element relationshipRoleSourceElement =
            getUniqueChild(element, "relationship-role-source");
      entityName =
            getUniqueChildContent(relationshipRoleSourceElement, "ejb-name");

      // cmr-field?
      Element cmrFieldElement = getOptionalChild(element, "cmr-field");
      if(cmrFieldElement != null) {
         // cmr-field-name
         cmrFieldName =
               getUniqueChildContent(cmrFieldElement, "cmr-field-name");

         // cmr-field-type?
         cmrFieldType =
               getOptionalChildContent(cmrFieldElement, "cmr-field-type");
         if(cmrFieldType != null &&
               !cmrFieldType.equals("java.util.Collection") &&
               !cmrFieldType.equals("java.util.Set")) {

            throw new DeploymentException("cmr-field-type should be " +
                  "java.util.Collection or java.util.Set but is " +
                  cmrFieldType);
         }
      }

      // JBossCMP needs ejb-relationship-role-name if jbosscmp-jdbc.xml is used to map relationships
      if(relationshipRoleName == null)
      {
         relationshipRoleName = entityName + (cmrFieldName == null ? "" : "_" + cmrFieldName);
      }
   }
}
