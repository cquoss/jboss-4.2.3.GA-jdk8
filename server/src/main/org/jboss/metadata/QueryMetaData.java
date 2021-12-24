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

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/**
 * Contains information about ejb-ql queries.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 57209 $
 */
public class QueryMetaData extends MetaData {
   public final static String REMOTE = "Remote";
   public final static String LOCAL = "Local";

   private String description;
   private String methodName;
   private ArrayList methodParams;
   private String resultTypeMapping;
   private String ejbQl;

   public QueryMetaData () {
      methodParams = new ArrayList();
   }

   /**
    * Gets the user description of the query.
    * @return the users description of the query
    */
   public String getDescription() {
      return description;
   }

   /**
    * Gets the name of the query for which this metadata applies.
    * @return the name of the query method
    */
   public String getMethodName() {
      return methodName;
   }

   /**
    * Gets an iterator over the parameters of the query method.
    * @return an iterator over the parameters of the query method.
    */
   public Iterator getMethodParams() {
      return methodParams.iterator();
   }

   /**
    * Gets the interface type of returned ejb objects.  This will be
    * Local or Remote, and the default is Local. 
    * @return the type the the interface returned for ejb objects
    */
   public String getResultTypeMapping() {
      return resultTypeMapping;
   }

   /**
    * Gets the ejb-ql for this query.
    * @return the ejb-ql for this query
    */
   public String getEjbQl() {
      return ejbQl;
   }

   /**
    * Loads the data from the query xml element.
    * @param element the query xml element from the ejb-jar.xml file
    * @throws DeploymentException if the query element is malformed
    */
   public void importEjbJarXml(Element element) throws DeploymentException {
      // description
      description = getOptionalChildContent(element, "description");

      // query-method sub-element
      Element queryMethod = getUniqueChild(element, "query-method");

      // method name
      methodName = getUniqueChildContent(queryMethod, "method-name");

      // method params
      Element methodParamsElement = 
            getUniqueChild(queryMethod, "method-params");
      Iterator iterator = 
            getChildrenByTagName(methodParamsElement, "method-param");        
      while (iterator.hasNext()) {
         final String param = getElementContent((Element)iterator.next());
         if(param == null || param.trim().length() == 0)
         {
            throw new DeploymentException("method-param tag has no value for method: " + methodName);
         }
         methodParams.add(param);
      }

      // result type mapping
      resultTypeMapping = 
            getOptionalChildContent(element, "result-type-mapping");
      if(resultTypeMapping == null || LOCAL.equals(resultTypeMapping)) {
         resultTypeMapping = LOCAL;
      } else if(REMOTE.equals(resultTypeMapping)) {
         resultTypeMapping = REMOTE;
      } else {
         throw new DeploymentException("result-type-mapping must be '" +
               REMOTE + "' or '" + LOCAL + "', if specified");
      }

      ejbQl = getElementContent(getUniqueChild(element, "ejb-ql"));
   }
}
/*
vim:ts=3:sw=3:et
*/
