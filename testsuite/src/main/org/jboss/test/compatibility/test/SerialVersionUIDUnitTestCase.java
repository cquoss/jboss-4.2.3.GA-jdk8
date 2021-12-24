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
package org.jboss.test.compatibility.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.tools.ClassVersionInfo;
import org.jboss.tools.SerialVersionUID;

/** 
 * Tests of serial version uid compatibility across jboss versions. The
 * testsuite/etc/serialVersionUID/xxx.ser is created using the
 * org.jboss.tools.SerialVersionUID utility.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 74467 $
 */
public class SerialVersionUIDUnitTestCase extends TestCase
{
   static Map currentClassInfoMap;

   public SerialVersionUIDUnitTestCase(String name)
   {
      super(name);
   }

   /** Validate the 4.0.5 serial version uids against the current build
    * @throws Exception
    */
   public void test405Compatibility()
         throws Exception
   {
      // The packages in jboss-4.2.x with known 4.0.5 serialization issues
      String[] badPackages = {
         // Ignore javassist.* issues
         "javassist",
         // switch from myfaces to sun jsf 1.2_03
         "javax.faces",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.jstl.core.LoopTagSupport",
         "javax.servlet.jsp.tagext.TagSupport", 
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
         // Ignore org.apache.* issues
         "org.apache",
         // Ignore org.hibernate.* issues. Need to revist this.
         "org.hibernate",
         // Ignore hsqldb upgrades
         "org.hsqldb",
         // Ignore jacorb packages org.jacorb.*, org.omg.*
         "org.jacorb",
         "org.omg",         
         // Ignore jaxen upgrades
         "org.jaxen",
         // Ignore jboss console stuff
         "org.jboss.console",
         // Ignore corba servant locators
         "org.jboss.invocation.iiop.IIOPInvoker$PersistentServantLocator",
         "org.jboss.invocation.iiop.IIOPInvoker$TransientServantLocator",
         // jboss remoting upgrade to 2.0.0.GA
         "org.jboss.remoting",
         // jboss resource adaptor jdbc vendor 
         "org.jboss.resource.adapter.jdbc.vendor",
         // Ignore the JacORB IDL compiler generated stubs/ties
         "org.jboss.tm.iiop._CoordinatorExtStub",
         "org.jboss.tm.iiop._TransactionFactoryExtStub",
         "org.jboss.tm.iiop._TransactionServiceStub",
         "org.jboss.iiop.csiv2.SASCurrentLocalTie",
         // Ignore org.jboss.ws
         "org.jboss.ws",
         // JBAS-4148, ignore org.jfree classes
         "org.jfree",
         // Ignore jgroups issues after the upgrade to 2.4.1
         "org.jgroups"
      };

      System.out.println("+++ test405Compatibility");
      // load the 4.0.5 serialVersionUID database
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/405.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("4.0.5 serial classes count: "+classInfoMap.size());
      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "405", badPackages,bufferMessages );
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
      if (mismatchCount!=0)
      {
          fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
      }
   }
   
   /** Validate the 4.0.4 serial version uids against the current build
    * @throws Exception
    */
   public void test404Compatibility()
         throws Exception
   {
      // The packages in jboss-4.2.x with known 4.0.4 serialization issues
      String[] badPackages = {
         // Ignore javassist.* issues
         "javassist",
         // switch from myfaces to sun jsf 1.2_03
         "javax.faces",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.jstl.core.LoopTagSupport",
         "javax.servlet.jsp.tagext.TagSupport",          
         // ?
         "javax.xml.bind.JAXBException",
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
         // Ignore org.apache.* issues
         "org.apache",
         // Ignore org.hibernate.* issues. Need to revist this.
         "org.hibernate",
         // Ignore hsqldb upgrades
         "org.hsqldb",
         // Ignore jacorb packages org.jacorb.*, org.omg.*
         "org.jacorb",
         "org.omg",
         // Ignore jboss cache stuff
         "org.jboss.cache.OptimisticTreeNode",
         "org.jboss.cache.ReplicationException",
         // Ignore jboss console stuff
         "org.jboss.console",
         // Ignore corba servant locators
         "org.jboss.invocation.iiop.IIOPInvoker$PersistentServantLocator",
         "org.jboss.invocation.iiop.IIOPInvoker$TransientServantLocator",
         // jboss remoting upgrade to 2.0.0.GA
         "org.jboss.remoting",
         // jboss resource adaptor jdbc vendor 
         "org.jboss.resource.adapter.jdbc.vendor",
         // jboss-serialization 1.0.3.GA
         "org.jboss.serial.objectmetamodel.DataContainer",
         // Ignore the JacORB IDL compiler generated stubs/ties
         "org.jboss.tm.iiop._CoordinatorExtStub",
         "org.jboss.tm.iiop._TransactionFactoryExtStub",
         "org.jboss.tm.iiop._TransactionServiceStub",
         "org.jboss.iiop.csiv2.SASCurrentLocalTie",
         // Ignore org.jboss.ws
         "org.jboss.ws",
         // JBAS-4148, ignore org.jfree classes
         "org.jfree",
         // Ignore jgroups issues after the upgrade to 2.4.1
         "org.jgroups",
         // Ignore incompatible classes from the sun-javamail upgrade, JBAS-3488
         "com.sun.mail.imap.protocol.IMAPAddress",
         "javax.mail.MessagingException",
      };

      System.out.println("+++ test404Compatibility");
      // load the 4.0.4 serialVersionUID database
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/404.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("4.0.4 serial classes count: "+classInfoMap.size());
      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "404", badPackages,bufferMessages );
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
      if (mismatchCount!=0)
      {
          fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
      }
   }   
   
   /** Validate the 4.0.3.SP1 serial version uids against the current build
    * @throws Exception
    */
   public void test403SP1Compatibility()
         throws Exception
   {
      // The packages in jboss-4.2.x with known 4.0.3.SP1 serialization issues
      String[] badPackages = {
         // Ignore antlr.* issues. TODO look into exception proagation
         "antlr",
         // Ignore javassist.* issues
         "javassist",
         // switch from myfaces to sun jsf 1.2_03
         "javax.faces",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.jstl.core.LoopTagSupport",
         "javax.servlet.jsp.tagext.TagSupport",          
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
         // Ignore org.apache.* issues
         "org.apache",
         // Ignore org.hibernate.* issues. Need to revist this.
         "org.hibernate",
         // Ignore hsqldb upgrades
         "org.hsqldb",
         // Ignore jacorb packages org.jacorb.*, org.omg.*
         "org.jacorb",
         "org.omg",
         // Ignore jboss console stuff
         "org.jboss.console",
         // Ignore corba servant locators
         "org.jboss.invocation.iiop.IIOPInvoker$PersistentServantLocator",
         "org.jboss.invocation.iiop.IIOPInvoker$TransientServantLocator",
         // unified invoker
         "org.jboss.invocation.unified.marshall.InvocationMarshaller",
         "org.jboss.invocation.unified.marshall.InvocationUnMarshaller",
         // jboss remoting upgrade to 2.0.0.GA
         "org.jboss.remoting",
         // jboss resource adaptor jdbc vendor 
         "org.jboss.resource.adapter.jdbc.vendor",
         // Ignore the JacORB IDL compiler generated stubs/ties
         "org.jboss.tm.iiop._CoordinatorExtStub",
         "org.jboss.tm.iiop._TransactionFactoryExtStub",
         "org.jboss.tm.iiop._TransactionServiceStub",
         "org.jboss.iiop.csiv2.SASCurrentLocalTie",
         // JBAS-4148, ignore org.jfree classes
         "org.jfree",
         // Ignore jgroups issues after the upgrade to 2.4.1
         "org.jgroups",         
         // The aop classes were not finalized until 4.0.3
         "org.jboss.aop",
         // Ignore incompatible classes from the sun-javamail upgrade, JBAS-3488
         "com.sun.mail.imap.protocol.IMAPAddress",
         "javax.mail.MessagingException",
         // JBAS-3736, jaxen-1.1-beta9 has different serialVersionUIDs than beta4
         "org.jaxen"
      };

      System.out.println("+++ test403SP1Compatibility");
      // load the 4.0.3.SP1 serialVersionUID database
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/403SP1.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("4.0.3.SP1 serial classes count: "+classInfoMap.size());
      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "403SP1", badPackages,bufferMessages );
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
      if (mismatchCount!=0)
      {
          fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
      }
   }
   
   /** Validate the 4.0.2 serial version uids against the current build
    * @throws Exception
    */
   public void test402Compatibility()
         throws Exception
   {
      // The packages in jboss-4.2.x with known 4.0.1 serialization issues
      String[] badPackages = {
         // Ignore antlr.* issues. TODO look into exception proagation
         "antlr",
         // Ignore javassist.* issues
         "javassist",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.tagext.TagSupport",          
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
         // Ignore dom4j issues
         "org.dom4j",
         // Ignore org.apache.* issues
         "org.apache",
         // Ignore org.hibernate.* issues. Need to revist this.
         "org.hibernate",
         // Ignore jacorb packages org.jacorb.*, org.omg.*
         "org.jacorb",
         "org.omg",
         // Ignore jgroups issues after the upgrade to 2.4.1
         "org.jgroups",         
         // The aop classes were not finalized until 4.0.3
         "org.jboss.aop",
         // Ignore jboss console stuff
         "org.jboss.console",
         // Ignore corba servant locators
         "org.jboss.invocation.iiop.IIOPInvoker$PersistentServantLocator",
         "org.jboss.invocation.iiop.IIOPInvoker$TransientServantLocator",
         // jboss remoting upgrade to 2.0.0.GA
         "org.jboss.remoting",
         // jboss resource adaptor jdbc vendor 
         "org.jboss.resource.adapter.jdbc.vendor",
         // Ignore the JacORB IDL compiler generated stubs/ties
         "org.jboss.tm.iiop._CoordinatorExtStub",
         "org.jboss.tm.iiop._TransactionFactoryExtStub",
         "org.jboss.tm.iiop._TransactionServiceStub",
         "org.jboss.iiop.csiv2.SASCurrentLocalTie",
         // Ignore org.jboss.webservice for 4.0.1 since the org.apache.axis to
         // org.jboss.axis package name change breaks serialization
         "org.jboss.webservice",
         // JBAS-4148, ignore org.jfree classes
         "org.jfree",
         // Ignore org.hsqldb as there are some utility classes that changed
         // in the upgrade to 1_8_0
         "org.hsqldb",
         // Ignore javacc generated classes
         "org.jboss.ejb.plugins.cmp.ejbql.TokenMgrError",
         "org.jboss.mq.selectors.TokenMgrError",
         "org.jboss.security.auth.login.TokenMgrError",
         // Ignore incompatible classes from the sun-javamail upgrade, JBAS-3488
         "com.sun.mail.imap.protocol.IMAPAddress",
         "javax.mail.MessagingException",
         // JBAS-3736, jaxen-1.1-beta9 has different serialVersionUIDs than beta4
         "org.jaxen"
      };

      System.out.println("+++ test402Compatibility");
      // load the 4.0.2 serialVersionUID database
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/402.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("4.0.2 serial classes count: "+classInfoMap.size());
      // The legacy mode needs to be false for 4.0.2+, so this property should not exist
      // System.setProperty("org.jboss.j2ee.LegacySerialization", "false");
      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "402", badPackages,bufferMessages );
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
      if (mismatchCount!=0)
      {
          fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
      }
   }

   /** Validate the 4.0.1 serial version uids against the current build
    * @throws Exception
    */
   public void test401Compatibility()
         throws Exception
   {
      // The packages in jboss-4.2.x with known 4.0.1 serialization issues
      String[] badPackages = {
         // Ignore javassist.* issues
         "javassist",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.tagext.TagSupport",          
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
         // upgrade to jbossws-2.0.1
         "javax.xml.rpc.JAXRPCException",
         "javax.xml.rpc.ServiceException",
         "javax.xml.rpc.soap.SOAPFaultException",         
         "javax.xml.soap.SOAPException",          
         // Ignore dom4j issues
         "org.dom4j",
         // Ignore org.apache.* issues
         "org.apache",
         // Ignore jacorb packages org.jacorb.*, org.omg.*
         "org.jacorb",
         "org.omg",
         // Ignore jgroups issues after the upgrade to 2.4.1
         "org.jgroups",         
         // The aop classes were not finalized until 4.0.3
         "org.jboss.aop",
         // Ignore jboss console stuff
         "org.jboss.console",
         // Ignore corba servant locators
         "org.jboss.invocation.iiop.IIOPInvoker$PersistentServantLocator",
         "org.jboss.invocation.iiop.IIOPInvoker$TransientServantLocator",
         // jboss remoting upgrade to 2.0.0.GA
         "org.jboss.remoting",
         // jboss resource adaptor jdbc vendor 
         "org.jboss.resource.adapter.jdbc.vendor",
         // serialVersionUIDs were added at v4.0.4
         "org.jboss.monitor.alarm.AlarmNotification",
         "org.jboss.monitor.alarm.AlarmTableNotification",
         // Ignore the JacORB IDL compiler generated stubs/ties
         "org.jboss.tm.iiop._CoordinatorExtStub",
         "org.jboss.tm.iiop._TransactionFactoryExtStub",
         "org.jboss.tm.iiop._TransactionServiceStub",
         "org.jboss.iiop.csiv2.SASCurrentLocalTie",
         // Ignore org.jboss.webservice for 4.0.1 since the org.apache.axis to
         // org.jboss.axis package name change breaks serialization
         "org.jboss.webservice",
         // JBAS-4148, ignore org.jfree classes
         "org.jfree",
         // Ignore org.hsqldb as there are some utility classes that changed
         // in the upgrade to 1_8_0
         "org.hsqldb",
         // Ignore javacc generated classes
         "org.jboss.ejb.plugins.cmp.ejbql.TokenMgrError",
         "org.jboss.mq.selectors.TokenMgrError",
         "org.jboss.security.auth.login.TokenMgrError",
         // Ignore incompatible classes from the sun-javamail upgrade, JBAS-3488
         "com.sun.mail.imap.protocol.IMAPAddress",
         "javax.mail.MessagingException",
         // jmx
         "javax.management.loading.MLet",
         // JBAS-3736, jaxen-1.1-beta9 has different serialVersionUIDs than beta4
         "org.jaxen"
      };

      System.out.println("+++ test401Compatibility");
      // load the 4.0.1 serialVersionUID database
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/401.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("4.0.1 serial classes count: "+classInfoMap.size());
      // The legacy 4.0.1- mode needs to be set
      System.setProperty("org.jboss.j2ee.LegacySerialization", "true");
      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "401", badPackages,bufferMessages);
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
       System.out.println("serialVersionUID mismatches = "+mismatchCount);
       if (mismatchCount!=0)
       {
           fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
       }
   }

   /** Validate the J2EE 1.4.1 RI serial version uids against the current build
    * @throws Exception
    */
   public void testJ2EERI141Compatibility()
         throws Exception
   {
      // The packages in j2ee 1.4.1RI with known serialization issues
      String[] badPackages = {
         // The javax.mail binaries in the ri are not consistent with the javamail 1.3FCS
         "javax.mail",
         // upgrade to jboss-web 2.x
         "javax.servlet.ServletException",
         "javax.servlet.jsp.JspException",
         "javax.servlet.jsp.jstl.core.LoopTagSupport",
         "javax.servlet.jsp.tagext.TagSupport",        
         // ?
         "javax.xml.bind.JAXBException",
         // The bundled javax.xml.namespace.QName is not compatible with the
         // jdk5/xerces 2.7.x version bundled with 4.0.3
         "javax.xml.namespace.QName",
      };
      System.out.println("+++ testJ2EERI141Compatibility");
      System.getProperties().remove("org.jboss.j2ee.LegacySerialization");
      String etc = System.getProperty("jbosstest.src.etc", "../src/etc");
      File serFile = new File(etc, "serialVersionUID/j2ee141.ser");
      FileInputStream fis = new FileInputStream(serFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map classInfoMap = (Map) ois.readObject();
      System.out.println("J2EE RI serial classes count: "+classInfoMap.size());

      Map currentClassInfoMap = calcClassInfoMap();
      StringBuffer bufferMessages = new StringBuffer();
      int mismatchCount = compare(classInfoMap, currentClassInfoMap, "J2EE1.4", badPackages,bufferMessages);
      currentClassInfoMap.clear();
      System.out.println("serialVersionUID mismatches = "+mismatchCount);
      if (mismatchCount!=0)
      {
          fail("Failures on SerialVersionComparisson:" + bufferMessages.toString());
      }
   }

   /**
    Compare two sets of classes for serialVersionUID compatibility.

    @param classInfoMap - the legacy version classes
    @param currentClassInfoMap - the current build classes
    @param versionName - the legacy version name
    @param badPackages - a list of package prefixes to ignore for errors
    @return the number of serial version mismatches
    */
   private int compare(Map classInfoMap, Map currentClassInfoMap,
      String versionName, String[] badPackages, StringBuffer bufferMessages)
      throws IOException
   {
      File out = new File(versionName+".errors");
      System.out.println("Writing errors to: "+out.getAbsolutePath());
      FileWriter errors = new FileWriter(out);
      int mismatchCount = 0;
      Iterator iter = currentClassInfoMap.values().iterator();
      while( iter.hasNext() )
      {
         ClassVersionInfo cvi = (ClassVersionInfo) iter.next();
         String name = cvi.getName();
         ClassVersionInfo cviLegacy = (ClassVersionInfo) classInfoMap.get(name);
         if( cviLegacy != null && cvi.getSerialVersion() != cviLegacy.getSerialVersion() )
         {
            String msg = "serialVersionUID error for "+name
               +", " + versionName + " " + cviLegacy.getSerialVersion()
               +", current: "+cvi.getSerialVersion();
            // Don't count classes from badPackages
            boolean isInBadPkg = false;
            for(int n = 0; n < badPackages.length; n ++)
            {
               String pkg = badPackages[n];
               if( name.startsWith(pkg) )
               {
                  isInBadPkg = true;
                  break;
               }
            }
            if( isInBadPkg == false )
            {
               if (mismatchCount>0)
               {
                   bufferMessages.append(",\n");
               }
               bufferMessages.append(name);
               mismatchCount ++;
               System.err.println(msg);
               errors.write(msg);
               errors.write('\n');
            }
            else
            {
               System.out.println(msg);
            }
         }
      }
      errors.close();
      // If the mismatchCount is 0 remove the error file
      if( mismatchCount == 0 )
         out.delete();

      return mismatchCount;
   }

   static Map calcClassInfoMap()
      throws IOException
   {
      String jbossDist = System.getProperty("jbosstest.dist");
      File jbossHome = new File(jbossDist);
      jbossHome = jbossHome.getCanonicalFile();
      System.out.println("Calculating serialVersionUIDs for jbossHome: "+jbossHome);
      Map classInfoMap = SerialVersionUID.generateJBossSerialVersionUIDReport(
         jbossHome);
      return classInfoMap;
   }
   
   public static Test suite() throws Exception
   {
      // JBAS-3600, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new SerialVersionUIDUnitTestCase("test405Compatibility"));
      suite.addTest(new SerialVersionUIDUnitTestCase("test404Compatibility"));
      suite.addTest(new SerialVersionUIDUnitTestCase("test403SP1Compatibility"));
      suite.addTest(new SerialVersionUIDUnitTestCase("test402Compatibility"));
      suite.addTest(new SerialVersionUIDUnitTestCase("test401Compatibility"));
      suite.addTest(new SerialVersionUIDUnitTestCase("testJ2EERI141Compatibility"));

      return suite;
   }

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(SerialVersionUIDUnitTestCase.class);
   }
}

