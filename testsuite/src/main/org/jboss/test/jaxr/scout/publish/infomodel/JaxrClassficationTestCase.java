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
package org.jboss.test.jaxr.scout.publish.infomodel;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Key;

import org.jboss.test.jaxr.scout.JaxrBaseTestCase;

//$Id: JaxrClassficationTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Test the storage of classifications on Concepts and Services
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @author <a href="mailto:Noel.Rocher@jboss.org">Noel Rocher</a>
 *  @since  Apr 11, 2006
 *  @version $Revision: 57211 $
 */
public class JaxrClassficationTestCase extends JaxrBaseTestCase
{
   private static final String UUID_TYPE = "uuid:C1ACF26D-9672-4404-9D70-39B756E62AB4";  
  
   public void testClassificationOnConcepts() throws Exception
   { 
      login();
      getJAXREssentials();
      Concept concept=null;
      Collection concepts = new ArrayList(1);
      String portTypeName = "Test Port Type";
      concept = blm.createConcept( null, portTypeName ,"" );
      ExternalLink wsdlLink = blm.createExternalLink("http://test.org/"+portTypeName,"TEST Port Type definition");
      concept.addExternalLink(wsdlLink); 
       
      ClassificationScheme TYPE = (ClassificationScheme)bqm.getRegistryObject(UUID_TYPE, LifeCycleManager.CLASSIFICATION_SCHEME);
      assertTrue("Classifications are not empty", TYPE.getClassifications().size() > 0);
      System.out.println("TYPE.Classifications = " +TYPE.getClassifications());
      concept.addClassification(blm.createClassification( TYPE, blm.createInternationalString("TEST CLASSIFICATION"), "test portType")  );

      concepts.add(concept);
      BulkResponse response = blm.saveConcepts( concepts );
      if (response != null && response.getCollection().size() > 0)
      {
         concept.setKey((Key)response.getCollection().iterator().next() );
         assertNotNull("Key created != null", concept.getKey());
         System.out.println("Concept Key = " + concept.getKey() + "\".");
      }
      
      //Obtain the saved concepts
      Concept savedConcept = (Concept)bqm.getRegistryObject(concept.getKey().getId(), 
                                      LifeCycleManager.CONCEPT );
      assertNotNull("savedConcept is not null", savedConcept);
      Collection collection = savedConcept.getClassifications();
      assertNotNull("Classifications is not null", collection);
      assertTrue("Classifications is not empty", collection.isEmpty() == false); 
   }
}
