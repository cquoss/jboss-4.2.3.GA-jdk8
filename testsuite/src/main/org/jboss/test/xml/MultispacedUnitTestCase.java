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
package org.jboss.test.xml;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.List;
import java.net.URL;

import org.jboss.xb.binding.Marshaller;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.XercesXsMarshaller;
import org.jboss.xb.binding.AbstractMarshaller;
import org.jboss.xb.binding.sunday.MarshallerImpl;
import org.jboss.test.xml.multispaced.pm.jdbc.JDBCPmMetaDataFactory;
import org.jboss.test.xml.multispaced.pm.jdbc.JDBCPm;
import org.jboss.test.xml.multispaced.pm.jdbc.JDBCPmMetaDataProvider;
import org.jboss.test.xml.multispaced.XMBeanMetaData;
import org.jboss.test.xml.multispaced.XMBeanMetaDataFactory;
import org.jboss.test.xml.multispaced.XMBeanMetaDataProvider;
import org.jboss.test.xml.multispaced.XMBeanOperationMetaData;
import org.jboss.test.xml.multispaced.XMBeanAttributeMetaData;
import org.jboss.test.xml.multispaced.XMBeanConstructorMetaData;
import org.jboss.test.xml.multispaced.XMBeanNotificationMetaData;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class MultispacedUnitTestCase
   extends TestCase
{
   private static final Logger log = Logger.getLogger(MultispacedUnitTestCase.class);

   public MultispacedUnitTestCase()
   {
   }

   public MultispacedUnitTestCase(String name)
   {
      super(name);
   }

   public void testMultispacedUnmarshalling() throws Exception
   {
      log.debug("--- " + getName());

      InputStream xmlIs = getResource("xml/multispaced/xmbean.xml");
      InputStreamReader xmlReader = new InputStreamReader(xmlIs);

      XMBeanMetaData xmbean = unmarshalXMBean(xmlReader);

      xmlReader.close();

      checkUnmarshalledXMBean(xmbean);
   }

   public void testMultispacedUnmarshalling2() throws Exception
   {
      log.debug("--- " + getName());

      InputStream xmlIs = getResource("xml/multispaced/xmbean-localns.xml");
      InputStreamReader xmlReader = new InputStreamReader(xmlIs);

      XMBeanMetaData xmbean = unmarshalXMBean(xmlReader);

      xmlReader.close();

      checkUnmarshalledXMBean(xmbean);
   }

   public void testMarshallingXerces() throws Exception
   {
      log.debug("--- " + getName());

      System.setProperty(Marshaller.PROP_MARSHALLER, XercesXsMarshaller.class.getName());
      marshallingTest();
   }

   public void testMarshallingSunday() throws Exception
   {
      log.debug("--- " + getName());

      System.setProperty(Marshaller.PROP_MARSHALLER, MarshallerImpl.class.getName());
      marshallingTest();
   }

   // Private

   private void marshallingTest()
      throws Exception
   {
      StringWriter strWriter = new StringWriter();

      AbstractMarshaller marshaller = (AbstractMarshaller)Marshaller.FACTORY.getInstance();
      marshaller.addRootElement("http://jboss.org/xmbean", "xmbean", "mbean");
      marshaller.declareNamespace("xmbean", "http://jboss.org/xmbean");
      marshaller.declareNamespace("jdbcpm", "http://jboss.org/xmbean/persistence/jdbc");

      XMBeanMetaData xmbean = createXMBeanMetaData();
      ObjectModelProvider provider = XMBeanMetaDataProvider.INSTANCE;

      ObjectModelProvider jdbcPmProvider = new JDBCPmMetaDataProvider((JDBCPm)xmbean.getPersistenceManager());
      marshaller.mapClassToGlobalElement(JDBCPm.class,
         "persistence-manager",
         "http://jboss.org/xmbean/persistence/jdbc",
         getResourceUrl("xml/multispaced/jdbcpm.xsd").toString(),
         jdbcPmProvider
      );

      marshaller.marshal(getResourceUrl("xml/multispaced/xmbean.xsd").toString(), provider, xmbean, strWriter);

      final String xml = strWriter.getBuffer().toString();
      log.debug("marshalled with " + marshaller.getClass().getName() + ": " + xml);

      StringReader xmlReader = new StringReader(xml);
      XMBeanMetaData unmarshalled = unmarshalXMBean(xmlReader);

      assertEquals(xmbean, unmarshalled);
   }

   private XMBeanMetaData unmarshalXMBean(Reader xmlReader)
      throws Exception
   {
      XMBeanMetaData xmbean = new XMBeanMetaData();

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.mapFactoryToNamespace(JDBCPmMetaDataFactory.INSTANCE, "http://jboss.org/xmbean/persistence/jdbc");
      unmarshaller.unmarshal(xmlReader, XMBeanMetaDataFactory.INSTANCE, xmbean);

      return xmbean;
   }

   private void checkUnmarshalledXMBean(XMBeanMetaData xmbean)
   {
      log.debug("xmbean: " + xmbean);

      assertEquals("The JBoss XMBean version of the monitor server", xmbean.getDescription());
      assertEquals("monitor.MonitorPOJO", xmbean.getMbeanClass());

      final List constructors = xmbean.getConstructors();
      assertEquals(1, constructors.size());
      XMBeanConstructorMetaData constructor = (XMBeanConstructorMetaData)constructors.get(0);
      assertEquals("The no-arg constructor", constructor.getDescription());
      assertEquals("monitor.MonitorPOJO", constructor.getName());

      final List attributes = xmbean.getAttributes();
      assertEquals(1, attributes.size());
      XMBeanAttributeMetaData attribute = (XMBeanAttributeMetaData)attributes.get(0);
      assertEquals("read-write", attribute.getAccess());
      assertEquals("getInterval", attribute.getGetMethod());
      assertEquals("setInterval", attribute.getSetMethod());
      assertEquals("The interval in milliseconds between checks of VM memory and threads", attribute.getDescription());
      assertEquals("Interval", attribute.getName());
      assertEquals("int", attribute.getType());

      final List operations = xmbean.getOperations();
      assertEquals(1, operations.size());
      XMBeanOperationMetaData operation = (XMBeanOperationMetaData)operations.get(0);
      assertEquals("Access the last HistoryLength monitor reports", operation.getDescription());
      assertEquals("history", operation.getName());
      assertEquals("java.lang.String", operation.getReturnType());

      final List notifications = xmbean.getNotifications();
      assertEquals(1, notifications.size());
      XMBeanNotificationMetaData notification = (XMBeanNotificationMetaData)notifications.get(0);
      assertEquals("A notification sent when the monitor interval expires", notification.getDescription());
      assertEquals("javax.management.Notification", notification.getName());
      assertEquals("monitor.IntervalElapsed", notification.getNotificationType());

      final JDBCPm pm = (JDBCPm)xmbean.getPersistenceManager();
      if(pm == null)
      {
         fail("persistence-manager is null.");
      }

      assertEquals("java:/DefaultDS", pm.getDatasource());
      assertEquals("xmbeans", pm.getTable());
   }

   private XMBeanMetaData createXMBeanMetaData()
   {
      XMBeanMetaData xmbean = new XMBeanMetaData();
      xmbean.setDescription("The JBoss XMBean version of the monitor server");
      xmbean.setMbeanClass("monitor.MonitorPOJO");

      XMBeanConstructorMetaData constructor = new XMBeanConstructorMetaData();
      constructor.setDescription("The no-arg constructor");
      constructor.setName("monitor.MonitorPOJO");
      xmbean.addConstructor(constructor);

      XMBeanAttributeMetaData attribute = new XMBeanAttributeMetaData();
      attribute.setAccess("read-write");
      attribute.setGetMethod("getInterval");
      attribute.setSetMethod("setInterval");
      attribute.setDescription("The interval in milliseconds between checks of VM memory and threads");
      attribute.setName("Interval");
      attribute.setType("int");
      xmbean.addAttribute(attribute);

      XMBeanOperationMetaData operation = new XMBeanOperationMetaData();
      operation.setDescription("Access the last HistoryLength monitor reports");
      operation.setName("history");
      operation.setReturnType("java.lang.String");
      xmbean.addOperation(operation);

      XMBeanNotificationMetaData notification = new XMBeanNotificationMetaData();
      notification.setDescription("A notification sent when the monitor interval expires");
      notification.setName("javax.management.Notification");
      notification.setNotificationType("monitor.IntervalElapsed");
      xmbean.addNotification(notification);

      JDBCPm pm = new JDBCPm();
      pm.setDatasource("java:/DefaultDS");
      pm.setTable("xmbeans");
      xmbean.setPersistenceManager(pm);

      return xmbean;
   }

   private static InputStream getResource(String name)
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
      if(is == null)
      {
         throw new IllegalStateException("Resource not found: " + name);
      }
      return is;
   }

   private static URL getResourceUrl(String name)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      if(url == null)
      {
         throw new IllegalStateException("Resource not found: " + name);
      }
      return url;
   }
}
