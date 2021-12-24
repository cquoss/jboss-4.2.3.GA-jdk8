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

import org.jboss.xb.binding.Util;

/**
 * Tests XML names to various Java identifiers conversion.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class XMLNameToJavaIdentifierUnitTestCase
   extends TestCase
{
   private static final String[] xmlNames = new String[]
   {
      "mixedCaseName", "Answer42", "name-with-dashes", "other_punct-chars", "one2three", "__-invalids-at-the-end---",
      "ID", "iD"
   };

   private static final String[] clsNames = new String[]
   {
      "MixedCaseName", "Answer42", "NameWithDashes", "OtherPunctChars", "One2Three", "InvalidsAtTheEnd", "ID", "ID"
   };

   private static final String[] clsNamesWithLowLines = new String[]
   {
      "MixedCaseName", "Answer42", "NameWithDashes", "Other_PunctChars", "One2Three", "__InvalidsAtTheEnd", "ID", "ID"
   };

   private static final String[] fieldNames = new String[]
   {
      "mixedCaseName", "answer42", "nameWithDashes", "otherPunctChars", "one2Three", "invalidsAtTheEnd", "ID", "iD"
   };

   private static final String[] getNames = new String[]
   {
      "getMixedCaseName", "getAnswer42", "getNameWithDashes", "getOtherPunctChars", "getOne2Three",
      "getInvalidsAtTheEnd", "getID", "getID"
   };

   private static final String[] setNames = new String[]
   {
      "setMixedCaseName", "setAnswer42", "setNameWithDashes", "setOtherPunctChars", "setOne2Three",
      "setInvalidsAtTheEnd", "setID", "setID"
   };

   private static final String[] constNames = new String[]
   {
      "MIXED_CASE_NAME", "ANSWER_42", "NAME_WITH_DASHES", "OTHER_PUNCT_CHARS", "ONE_2_THREE", "INVALIDS_AT_THE_END",
      "ID", "I_D"
   };

   public XMLNameToJavaIdentifierUnitTestCase(String localName)
   {
      super(localName);
   }

   public void testXmlNameToClassName() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String clsName = Util.xmlNameToClassName(xmlNames[i], true);
         assertEquals(clsNames[i], clsName);
      }
   }

   public void testXmlNameToClassNameWithLowLines() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String clsName = Util.xmlNameToClassName(xmlNames[i], false);
         assertEquals(clsNamesWithLowLines[i], clsName);
      }
   }

   public void testXmlNameToGetMethodName() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String clsName = Util.xmlNameToGetMethodName(xmlNames[i], true);
         assertEquals(getNames[i], clsName);
      }
   }

   public void testXmlNameToSetMethodName() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String clsName = Util.xmlNameToSetMethodName(xmlNames[i], true);
         assertEquals(setNames[i], clsName);
      }
   }

   public void testXmlNameToConstantName() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String constName = Util.xmlNameToConstantName(xmlNames[i]);
         assertEquals(constNames[i], constName);
      }
   }

   public void testXmlNameToFieldName() throws Exception
   {
      for(int i = 0; i < xmlNames.length; ++i)
      {
         String fieldName = Util.xmlNameToFieldName(xmlNames[i], true);
         assertEquals(fieldNames[i], fieldName);
      }
   }

   public void testXmlNamespaceToJavaPackage() throws Exception
   {
      String[] ns = new String[]{
         "http://www.acme.com/go/espeak.xsd",
         "http://example.org/ns/books/",
         "http://www.w3.org/2001/XMLSchema",
         "http://example.org/",
         "http://example.org",
         "http://www.kloop.com.ua/xsd-schemas/schema1.xsd"
      };

      String[] pkg = new String[]{
         "com.acme.go.espeak",
         "org.example.ns.books",
         "org.w3._2001.xmlschema",
         "org.example",
         "org.example",
         "ua.com.kloop.xsd_schemas.schema1"
      };

      for(int i = 0; i < ns.length; ++i)
      {
         assertEquals(pkg[i], Util.xmlNamespaceToJavaPackage(ns[i]));
      }
   }
}
