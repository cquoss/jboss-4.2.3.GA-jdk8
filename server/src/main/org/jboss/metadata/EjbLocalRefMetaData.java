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

import java.util.HashMap;
import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 */
public class EjbLocalRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	
   // the name used in the bean code
   private String name;
	
   // entity or session
   private String type;
	
	// the 2 interfaces
   private String localHome;
   private String local;
	
	// internal link: map name to link
   private String link;

   // external link: map name to jndiName
   private String jndiName;
	
   private HashMap invokerMap = new HashMap();

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
	
    // Public --------------------------------------------------------
	
   public String getName() { return name; }
	
   public String getType() { return type; }
	
   public String getLocalHome() { return localHome; }
	
   public String getLocal() { return local; }
	
   public String getLink() { return link; }

   public String getJndiName() { return jndiName; }

   public String getInvokerBinding(String bindingName) { return (String)invokerMap.get(bindingName); }

    public void importEjbJarXml(Element element) throws DeploymentException {
      name = getElementContent(getUniqueChild(element, "ejb-ref-name"));
      type = getElementContent(getUniqueChild(element, "ejb-ref-type"));
      localHome = getElementContent(getUniqueChild(element, "local-home"));
      local = getElementContent(getUniqueChild(element, "local"));
      link = getElementContent(getOptionalChild(element, "ejb-link"));
   }
	
   public void importJbossXml(Element element) throws DeploymentException {
      jndiName = getElementContent(getOptionalChild(element, "local-jndi-name"));
   }
	
    public void importJbossXml(String invokerBinding, Element element) throws DeploymentException 
    {
      String refJndiName = getElementContent(getOptionalChild(element, "local-jndi-name"));
      invokerMap.put(invokerBinding, refJndiName);
    }   
}
