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

import org.jboss.logging.Logger;
import org.jboss.xb.binding.DtdMarshaller;
import org.jboss.xb.binding.MappingObjectModelFactory;
import org.jboss.xb.binding.MappingObjectModelProvider;
import org.jboss.xb.binding.Marshaller;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.MarshallerImpl;
import org.jboss.test.xml.person.Address;
import org.jboss.test.xml.person.Person;
import org.jboss.test.xml.choice.Root;
import org.jboss.test.xml.choice.Choice2;
import org.jboss.test.xml.choice.Choice1;
import org.jboss.test.xml.immutable.Child1;
import org.jboss.test.xml.immutable.Child2;
import org.jboss.test.xml.immutable.Child3;
import org.jboss.test.xml.immutable.Parent;
import org.jboss.test.xml.immutable.ImmutableChoice;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URL;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class MappingTestCase
   extends TestCase
{
   private static final Logger log = Logger.getLogger(MappingTestCase.class);

   public MappingTestCase(String name)
   {
      super(name);
   }

   public void testUnmarshalling() throws Exception
   {
      Reader xmlReader = new FileReader("resources/xml/person.xml");

      Person person = unmarshalPerson(xmlReader);
      xmlReader.close();

      assertNotNull("Person is null", person);
      assertEquals(person.getFirstName(), "Vasiliy");
      assertEquals(person.getLastName(), "Poupkin");
      assertEquals(person.getDateOfBirth(), SimpleTypeBindings.JAVA_UTIL_DATE.unmarshal("1980-01-01"));

      assertEquals(person.getPhones(), Arrays.asList(new Object[]{"01", "02"}));

      ArrayList list = new ArrayList();
      Address addr1 = new Address();
      addr1.setStreet("prosp. Rad. Ukr. 11A, 70");
      list.add(addr1);
      addr1 = new Address();
      addr1.setStreet("Sky 7");
      list.add(addr1);
      assertEquals(person.getAddresses(), list);
   }

   public void testMarshalling() throws Exception
   {
      log.debug("<test-mapping-marshalling>");

      final Person person = Person.newInstance();
      StringWriter xmlOutput = new StringWriter();

      InputStream is = getResource("xml/person.dtd");
      Reader dtdReader = new InputStreamReader(is);

      // create an instance of DTD marshaller
      Marshaller marshaller = new DtdMarshaller();

      // map publicId to systemId as it should appear in the resulting XML file
      marshaller.mapPublicIdToSystemId("-//DTD Person//EN", "resources/xml/person.dtd");

      // create an instance of ObjectModelProvider with the book instance to be marshalled
      MappingObjectModelProvider provider = new MappingObjectModelProvider();
      provider.mapFieldToElement(Person.class, "dateOfBirth", "", "date-of-birth", SimpleTypeBindings.JAVA_UTIL_DATE);

      // marshal the book
      marshaller.marshal(dtdReader, provider, person, xmlOutput);

      // close DTD reader
      dtdReader.close();

      final String xml = xmlOutput.getBuffer().toString();
      log.debug("marshalled: " + xml);

      // check unmarshalled person
      Person unmarshalled = unmarshalPerson(new StringReader(xml));
      assertEquals(person, unmarshalled);

      log.debug("</test-mapping-marshalling>");
   }

   public void testChoice() throws Exception
   {
      log.debug("testChoice> started");
      long startTime = System.currentTimeMillis();

      String xsdUrl = getXsd("xml/choice.xsd");
      Root root = newChoiceRoot();
      String xml = marshalChoiceRoot(xsdUrl, root);

      StringReader reader = new StringReader(xml);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      Root unmarshalled = (Root)unmarshaller.unmarshal(reader, new MappingObjectModelFactory(), null);
      log.info("unmarhalled:\n" + unmarshalled);

      assertEquals(root, unmarshalled);

      log.debug("testChoice> done in " + (System.currentTimeMillis() - startTime));
   }

   public void testImmutable() throws Exception
   {
      log.debug("testImmutable> started");
      long startTime = System.currentTimeMillis();

      String xsd = getXsd("xml/immutable.xsd");

      Parent parent = newImmutableParent();
      String xml = marshalImmutableParent(xsd, parent);

      StringReader reader = new StringReader(xml);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      Parent unmarshalled = (Parent)unmarshaller.unmarshal(reader, new MappingObjectModelFactory(), null);
      log.info("unmarhalled:\n" + unmarshalled);

      assertEquals(parent, unmarshalled);

      log.debug("testImmutable> done in " + (System.currentTimeMillis() - startTime));
   }

   // Private

   private String marshalImmutableParent(String xsd, Parent parent)
      throws IOException, SAXException
   {
      StringWriter writer = new StringWriter();
      MarshallerImpl marshaller = new MarshallerImpl();
      marshaller.declareNamespace("imm", "http://www.jboss.org/test/xml/immutable/");
      marshaller.marshal(xsd,
         new MappingObjectModelProvider(),
         parent,
         writer
      );

      log.info("parent:\n" + parent);
      log.info("marshalled root:\n" + writer.getBuffer());
      return writer.getBuffer().toString();
   }

   private Parent newImmutableParent()
   {
      Child1 child1 = new Child1("child1");
      List child2 = Arrays.asList(new Object[]{new Child2("child2_1"), new Child2("child2_2")});
      List others = Arrays.asList(new Object[]{new Child3("child3_1"), new Child3("child3_2"), new Child3("child3_3")});
      List choice = Arrays.asList(
         new Object[]{new ImmutableChoice("choice1"), new ImmutableChoice(new Child1("child1"))}
      );
      Parent parent = new Parent(child1, child2, others, choice);
      return parent;
   }

   private static String getXsd(String path)
   {
      URL xsdUrl = Thread.currentThread().getContextClassLoader().getResource(path);
      if(xsdUrl == null)
      {
         throw new IllegalStateException("XSD not found: " + path);
      }
      return xsdUrl.getFile();
   }

   private static String marshalChoiceRoot(String xsdUrl, Root root)
      throws IOException, SAXException
   {
      StringWriter writer = new StringWriter();
      MarshallerImpl marshaller = new MarshallerImpl();
      marshaller.declareNamespace("chs", "http://www.jboss.org/test/xml/choice/");
      //marshaller.setProperty(Marshaller.PROP_OUTPUT_INDENTATION, "false");
      marshaller.marshal(xsdUrl,
         new MappingObjectModelProvider(),
         root,
         writer
      );

      log.info("marshalled root:\n" + writer.getBuffer());
      return writer.getBuffer().toString();
   }

   private static Root newChoiceRoot()
   {
      String a = "a";
      String b = "b";
      String c = "c";

      List choice1 = Arrays.asList(new Choice1[]{new Choice1(a, null), new Choice1(null, b)});
      List choice2 = Arrays.asList(new Choice2[]{new Choice2(a, b, null), new Choice2(a, null, c)});

      Root root = new Root();
      root.setChoice1(choice1);
      root.setChoice2(choice2);
      return root;
   }

   private Person unmarshalPerson(Reader xmlReader)
      throws Exception
   {
      MappingObjectModelFactory factory = new MappingObjectModelFactory();
      factory.mapElementToClass("person", Person.class);
      factory.mapElementToField("date-of-birth", Person.class, "dateOfBirth", SimpleTypeBindings.JAVA_UTIL_DATE);
      factory.mapElementToClass("phones", ArrayList.class);
      factory.mapElementToClass("addresses", ArrayList.class);
      factory.mapElementToClass("address", Address.class);

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      return (Person)unmarshaller.unmarshal(xmlReader, factory, null);
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
}