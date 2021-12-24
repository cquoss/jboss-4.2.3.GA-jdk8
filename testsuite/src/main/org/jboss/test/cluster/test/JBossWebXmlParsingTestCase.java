/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.test;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.WebMetaData;
import org.jboss.test.JBossTestCase;
import org.w3c.dom.Document;

/**
 * Tests parsing of the replication-config elements in a jboss-web.xml.
 * 
 * @author Brian Stansberry
 */
public class JBossWebXmlParsingTestCase extends JBossTestCase
{
   private static final String RESOURCE_ROOT = "cluster/http/xmlparsing/";
   private static final String ATTRIBUTE = RESOURCE_ROOT + "jboss-web-attr.xml";
   private static final String BATCH_FALSE = RESOURCE_ROOT + "jboss-web-batch-false.xml";
   private static final String BOGUS = RESOURCE_ROOT + "jboss-web-bogus.xml";
   private static final String DEFAULT = RESOURCE_ROOT + "jboss-web-default.xml";
   private static final String EMPTY = RESOURCE_ROOT + "jboss-web-empty.xml";
   private static final String FIELD = RESOURCE_ROOT + "jboss-web-field.xml";
   private static final String NO_MAX = RESOURCE_ROOT + "jboss-web-no-max.xml";
   private static final String SET = RESOURCE_ROOT + "jboss-web-set.xml";
   private static final String SET_AND_GET = RESOURCE_ROOT + "jboss-web-set-and-get.xml";
   
   private static enum Element { GRAN, TRIGGER, BATCH, MAX };
   
   /**
    * Create a new JBossWebXmlParsingTestCase.
    * 
    * @param name
    */
   public JBossWebXmlParsingTestCase(String name)
   {
      super(name);
   }
   
   public void testDefault() throws Exception
   {      
      WebMetaData metadata = getWebMetaData(DEFAULT);
      assertEquals("Max unreplicated", new Integer(WebMetaData.DEFAULT_MAX_UNREPLICATED_INTERVAL), metadata.getMaxUnreplicatedInterval());
      defaultCheck(metadata, Element.MAX);
   }
   
   public void testEmpty() throws Exception
   {      
      WebMetaData metadata = getWebMetaData(EMPTY);
      defaultCheck(metadata);      
   }
   
   public void testBogus() throws Exception
   {      
      try
      {
         getWebMetaData(BOGUS);
         fail("Bogus config did not throw DeploymentException");
      }
      catch (DeploymentException expected) {}
   }
   
   public void testAttributeGranularity() throws Exception
   {
      WebMetaData metadata = getWebMetaData(ATTRIBUTE);
      assertEquals("Granularity", WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE, metadata.getReplicationGranularity());
      defaultCheck(metadata, Element.GRAN);
   }
   
   public void testFieldGranularity() throws Exception
   {
      WebMetaData metadata = getWebMetaData(FIELD);
      assertEquals("Granularity", WebMetaData.REPLICATION_GRANULARITY_FIELD, metadata.getReplicationGranularity());
      defaultCheck(metadata, Element.GRAN);
   }
   
   public void testSetTrigger() throws Exception
   {
      WebMetaData metadata = getWebMetaData(SET);
      assertEquals("Trigger", WebMetaData.SESSION_INVALIDATE_SET, metadata.getInvalidateSessionPolicy());
      defaultCheck(metadata, Element.TRIGGER);
   }
   
   public void testSetAndGetTrigger() throws Exception
   {
      WebMetaData metadata = getWebMetaData(SET_AND_GET);
      assertEquals("Trigger", WebMetaData.SESSION_INVALIDATE_SET_AND_GET, metadata.getInvalidateSessionPolicy());
      defaultCheck(metadata, Element.TRIGGER);
   }
   
   public void testBatchModeFalse() throws Exception
   {
      WebMetaData metadata = getWebMetaData(BATCH_FALSE);
      assertFalse("Batch mode", metadata.getReplicationFieldBatchMode());
      defaultCheck(metadata, Element.BATCH);
   }
   
   public void testMaxUnreplicatedInterval() throws Exception
   {
      WebMetaData metadata = getWebMetaData(NO_MAX);
      assertEquals("Max unreplicated", new Integer(-1), metadata.getMaxUnreplicatedInterval());
      defaultCheck(metadata, Element.MAX);
   }
   
   private WebMetaData getWebMetaData(String resourcePath) 
   throws Exception
   { 
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL webxml = cl.getResource(resourcePath);
      assertNotNull("web.xml exists?", webxml);
      return getWebMetaData(webxml.openStream());
   }
   
   private WebMetaData getWebMetaData(InputStream webxml) 
   throws Exception
   { 
      WebMetaData wmd = new WebMetaData();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder().parse(webxml); 
      wmd.importXml(doc.getDocumentElement()); 
      return wmd; 
   }    
   
   private void defaultCheck(WebMetaData metadata)
   {
      Set<Element> set = new HashSet<Element>();
      defaultCheck(metadata, set);
   }
   
   private void defaultCheck(WebMetaData metadata, Element ignore)
   {
      Set<Element> set = new HashSet<Element>();
      set.add(ignore);
      defaultCheck(metadata, set);
   }
   
   private void defaultCheck(WebMetaData metadata, Set<Element> ignore)
   {
      if (!ignore.contains(Element.GRAN))
      {
         assertEquals("Granularity", WebMetaData.REPLICATION_GRANULARITY_SESSION, metadata.getReplicationGranularity());
      }
      if (!ignore.contains(Element.TRIGGER))
      {
         assertEquals("Trigger", WebMetaData.SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET, metadata.getInvalidateSessionPolicy());
      }
      if (!ignore.contains(Element.BATCH))
      {
         assertTrue("Batch mode", metadata.getReplicationFieldBatchMode());
      }
      if (!ignore.contains(Element.MAX))
      {
         assertEquals("Max unreplicated", null, metadata.getMaxUnreplicatedInterval());
      }
   }
}
