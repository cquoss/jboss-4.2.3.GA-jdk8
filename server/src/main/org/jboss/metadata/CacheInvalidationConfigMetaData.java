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
import org.jboss.cache.invalidation.InvalidationManager;

/**
 * Manages attributes related to distributed (possibly local-only)
 * cache invalidations
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57209 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>26 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class CacheInvalidationConfigMetaData extends MetaData
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected String invalidationGroupName = null;
   protected String cacheInvaliderObjectName = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public CacheInvalidationConfigMetaData () { super (); }
   
   // Public --------------------------------------------------------
   
   public String getInvalidationGroupName ()
   {
      return this.invalidationGroupName;
   }
   
   public String getInvalidationManagerName ()
   {
      return this.cacheInvaliderObjectName;
   }
   
   public void init(BeanMetaData data)
   {
      // by default we use the bean name as the group name
      //
      this.invalidationGroupName = data.getEjbName ();
      
      this.cacheInvaliderObjectName = InvalidationManager.DEFAULT_JMX_SERVICE_NAME;
   }
   
   public void importJbossXml(Element element) throws DeploymentException 
   {
      this.invalidationGroupName = getElementContent(getOptionalChild(element, "invalidation-group-name"), this.invalidationGroupName);
      this.cacheInvaliderObjectName = getElementContent(getOptionalChild(element, "invalidation-manager-name"), this.cacheInvaliderObjectName);
   }

   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
