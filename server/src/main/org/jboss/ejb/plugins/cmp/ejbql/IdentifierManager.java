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
package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.CMRFieldBridge;

/**
 * This class manages a symbol table for the EJB-QL parser.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 57209 $
 */                            
public final class IdentifierManager {
   private final Catalog catalog;
   private final Map pathLists = new HashMap();
   private final Map fieldLists = new HashMap();
   private final Map identifiers = new HashMap();

   public IdentifierManager(Catalog catalog) {
      this.catalog = catalog;
   }

   public void declareRangeVariable(
         String identifier,
         String abstractSchemaName) {

      identifiers.put(
            identifier, 
            catalog.getEntityByAbstractSchemaName(abstractSchemaName));
   }
     
   public void declareCollectionMember(
         String identifier, 
         String path) {

      List fieldList = (List)fieldLists.get(path);
      Object field = fieldList.get(fieldList.size()-1);
      if(!(field instanceof CMRFieldBridge)) {
         throw new IllegalArgumentException("Path is collection valued: "+path);
      }
      CMRFieldBridge cmrField = (CMRFieldBridge)field;
      if(cmrField.isSingleValued()) {
         throw new IllegalArgumentException("Path is collection valued: "+path);
      }
      identifiers.put(identifier, cmrField.getRelatedEntity());
   }

   public EntityBridge getEntity(String identificationVariable) {
      return (EntityBridge)identifiers.get(identificationVariable);
   }
   
   public void registerPath(
         String path,
         List pathList,
         List fieldList) {

      if(pathList.size() != fieldList.size()) {
         throw new IllegalArgumentException("Path list and field list must " +
               "have the same size: pathList.size=" + pathList.size() +
               " fieldList.size=" + fieldList.size());
      }
      pathLists.put(path, pathList);
      fieldLists.put(path, fieldList);
   }

   public List getPathList(String path) {
      return (List)pathLists.get(path);
   }

   public List getFieldList(String path) {
      return (List)fieldLists.get(path);
   }

}

