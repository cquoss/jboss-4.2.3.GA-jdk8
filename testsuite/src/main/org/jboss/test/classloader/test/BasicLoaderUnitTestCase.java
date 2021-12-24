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
package org.jboss.test.classloader.test;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

import org.jboss.mx.loading.HeirarchicalLoaderRepository3;
import org.jboss.mx.loading.UnifiedClassLoader3;
import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.test.util.ClassMover;
import org.apache.log4j.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

/** Basic tests of the org.jboss.mx.loading.* classes
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 63653 $
 */
public class BasicLoaderUnitTestCase extends TestCase
{
   static Logger log = Logger.getLogger(BasicLoaderUnitTestCase.class);
   String jbosstestDeployDir;

   public BasicLoaderUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      jbosstestDeployDir = System.getProperty("jbosstest.deploy.dir");
      if( jbosstestDeployDir == null )
      {
         // Try to use ./output/lib
         File libDir = new File("output/lib");
         if( libDir.exists() == false )
            throw new Exception("System property jbosstest.deploy.dir is not defined");
         jbosstestDeployDir = libDir.getAbsolutePath();
      }
   }

   /** Test the UnifiedLoaderRepository for multi-threaded class loading
    */
   public void testNoClassDefFoundError() throws Exception
   {
      UnifiedLoaderRepository3 ulr = new UnifiedLoaderRepository3();
      File cwd = new File(jbosstestDeployDir);
      URL cp = new URL(cwd.toURL(), "../classes");
      log.info("Using cp: " + cp);
      ClassLoader loader = ulr.newClassLoader(cp, true);

      File bakFile = null;
      try
      {
         bakFile = ClassMover.move("org.jboss.test.classloader.test.ex.BaseException");
         loader.loadClass("org.jboss.test.classloader.test.ex.DerivedException");
         fail("Should not have loaded DerivedException");
      }
      catch (NoClassDefFoundError e)
      {
         String msg = e.getMessage();
         log.info("NCDFE msg: " + msg, e);
         int index = msg.indexOf("BaseException");
         assertTrue("Saw BaseException in NCDFE: (" + index + "), msg=" + msg, index > 0);
      }
      finally
      {
         if (bakFile != null)
            ClassMover.restore(bakFile);
      }

   }

   public void testNoClassDefFoundError2() throws Exception
   {
      UnifiedLoaderRepository3 ulr = new UnifiedLoaderRepository3();
      File cwd = new File(jbosstestDeployDir);
      URL cp = new URL(cwd.toURL(), "../classes");
      log.info("Using cp: " + cp);
      File bakFile = ClassMover.move("org.jboss.test.classloader.test.ex.BaseException");
      ClassLoader loader = ulr.newClassLoader(cp, true);

      try
      {
         Class c = loader.loadClass("org.jboss.test.classloader.test.ex.ExThrower");
         Method[] methods = c.getMethods();
         for(int n = 0; n < methods.length; n ++)
         {
            Method m = methods[n];
            m.getExceptionTypes();
         }
         fail("Should not have gotten ExThrower methods");
      }
      catch (NoClassDefFoundError e)
      {
         String msg = e.getMessage();
         log.info("CNFE msg: " + msg, e);
         int index = msg.indexOf("BaseException");
         assertTrue("Saw BaseException in CNFE: (" + index + "), msg=" + msg, index > 0);
      }
      finally
      {
         if (bakFile != null)
            ClassMover.restore(bakFile);
      }

   }

   public void testDeadlockScenario1() throws Exception
   {
      File libDir = new File(jbosstestDeployDir);
      log.info("Using cp: " + libDir);
      DeadlockTests32 test = new DeadlockTests32(libDir);
      test.testDeadLock();
   }

   public void testDeadlockScenario2() throws Exception
   {
      File libDir = new File(jbosstestDeployDir);
      log.info("Using cp: " + libDir);
      DeadlockTests32 test = new DeadlockTests32(libDir);
      test.testDeadLockAndCircularity();
   }

   /**
    Validate that the added order/classpath order is used when loading classes
    from the ULR.

    @throws Exception
    */
   public void testClasspathOrdering() throws Exception
   {
      File libDir = new File(jbosstestDeployDir);
      File classes1 = new File(libDir, "classes1");
      classes1.mkdir();

      // Create a test.Info class with a static String version = "Version 1.0"
      ClassPool defaultPool = ClassPool.getDefault();
      ClassPool classes1Pool = new ClassPool(defaultPool);
      CtClass info = classes1Pool.makeClass("test.Info");
      CtClass s = classes1Pool.get("java.lang.String");
      CtField version = new CtField(s, "version", info);
      version.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
      info.addField(version, CtField.Initializer.constant("Version 1.0"));
      info.writeFile(classes1.getAbsolutePath());

      // Create a test.Info class with a static String version = "Version 2.0"
      ClassPool classes2Pool = new ClassPool(defaultPool);
      info = classes2Pool.makeClass("test.Info");
      File classes2 = new File(libDir, "classes2");
      classes2.mkdir();
      version = new CtField(s, "version", info);
      version.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
      info.addField(version, CtField.Initializer.constant("Version 2.0"));      
      info.writeFile(classes2.getAbsolutePath());

      // Create a test.Info class with a static String version = "Version 3.0"
      ClassPool classes3Pool = new ClassPool(defaultPool);
      info = classes3Pool.makeClass("test.Info");
      File classes3 = new File(libDir, "classes3");
      classes3.mkdir();
      version = new CtField(s, "version", info);
      version.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
      info.addField(version, CtField.Initializer.constant("Version 3.0"));      
      info.writeFile(classes3.getAbsolutePath());

      // Create a URL with classpath {classes1, classes2, classes3}
      UnifiedLoaderRepository3 ulr = new UnifiedLoaderRepository3();
      RepositoryClassLoader loader1 = ulr.newClassLoader(classes1.toURL(), true);
      loader1.addURL(classes2.toURL());
      RepositoryClassLoader loader2 = ulr.newClassLoader(classes3.toURL(), true);

      // This should see version 1
      Class infoClass = loader1.loadClass("test.Info");
      log.info("#1.1"+infoClass.getProtectionDomain().getCodeSource());
      Field theVersion = infoClass.getField("version");
      String v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 1.0"));

      // This should also see version 1
      infoClass = loader2.loadClass("test.Info");
      log.info("#1.2"+infoClass.getProtectionDomain().getCodeSource());
      theVersion = infoClass.getField("version");
      v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 1.0"));
      ulr.removeClassLoader(loader1);
      ulr.removeClassLoader(loader2);
      ulr.flush();

      // Create a URL with classpath {classes2, classes1, classes3}
      ulr = new UnifiedLoaderRepository3();
      loader1 = ulr.newClassLoader(classes2.toURL(), true);
      loader1.addURL(classes1.toURL());
      loader2 = ulr.newClassLoader(classes3.toURL(), true);

      // This should see version 2
      infoClass = loader1.loadClass("test.Info");
      log.info("#2.1"+infoClass.getProtectionDomain().getCodeSource());
      theVersion = infoClass.getField("version");
      v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 2.0"));

      // This should also see version 2
      infoClass = loader2.loadClass("test.Info");
      log.info("#2.2"+infoClass.getProtectionDomain().getCodeSource());
      theVersion = infoClass.getField("version");
      v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 2.0"));
      ulr.removeClassLoader(loader1);
      ulr.removeClassLoader(loader2);
      ulr.flush();

      // Create a URL with classpath {classes3, classes1, classes2}
      ulr = new UnifiedLoaderRepository3();
      loader1 = ulr.newClassLoader(classes3.toURL(), true);
      loader1.addURL(classes1.toURL());
      loader2 = ulr.newClassLoader(classes2.toURL(), true);

      // This should see version 3
      infoClass = loader1.loadClass("test.Info");
      log.info("#3.1"+infoClass.getProtectionDomain().getCodeSource());
      theVersion = infoClass.getField("version");
      v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 3.0"));

      // This should also see version 3
      infoClass = loader2.loadClass("test.Info");
      log.info("#3.2"+infoClass.getProtectionDomain().getCodeSource());
      theVersion = infoClass.getField("version");
      v = (String) theVersion.get(null);
      assertTrue(v, v.equals("Version 3.0"));
   }

   /**
    * Force a LinkageError during loading of a class to validate that
    * a legitimate LinkageError does not cause an infinite loop.
    * JBAS-4441
    * @throws Exception
    */
   public void testLinkageError() throws Exception
   {
      log.info("Begin testLinkageError");
      UnifiedLoaderRepository3 parentRepo = new UnifiedLoaderRepository3();
      HeirarchicalLoaderRepository3 repo2 = new HeirarchicalLoaderRepository3(parentRepo);
      repo2.setUseParentFirst(false);
      File libDir = new File(jbosstestDeployDir);
      URL origURL = libDir.toURL();
      log.info("Lib origURL="+origURL);
      URL j2 = new URL(origURL, "xsub2.jar");
      log.info("j2 = "+j2);
      URL j1 = new URL(origURL, "xsub1.jar");
      log.info("j1 = "+j1);
      ClassLoader parent = new ClassLoader()
      {
          @Override
          protected synchronized Class<?> loadClass(String name, boolean resolve)
             throws ClassNotFoundException
          {
             if( name.startsWith("java"))
                return super.loadClass(name, resolve);
             throw new ClassNotFoundException("No class loading");
          }
      };
      final UnifiedClassLoader3 ucl0 = new UnifiedClassLoader3(j1, j1, parent, parentRepo);
      parentRepo.addClassLoader(ucl0);
      final UnifiedClassLoader3 ucl2 = new UnifiedClassLoader3(j2, j2, parent, repo2)
      {
         /**
          * Throw a LinkageError to trigger the LoadMgr retry logic
          */
         @Override
         public Class loadClassLocally(String name, boolean resolve) throws ClassNotFoundException
         {
            if( name.equals("org.jboss.test.classloader.circularity.support.linkage.X") )
               throw new LinkageError("Cannot load X");
            return super.loadClassLocally(name, resolve);
         }
      };
      repo2.addClassLoader(ucl2);

      try
      {
         Class c = ucl2.loadClass("org.jboss.test.classloader.circularity.support.linkage.Xsub");
         fail("Was able to load Xsub");
      }
      catch(NoClassDefFoundError e)
      {
         log.info("Saw NoClassDefFoundError as expected", e);
      }

   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new BasicLoaderUnitTestCase("testNoClassDefFoundError"));
      suite.addTest(new BasicLoaderUnitTestCase("testNoClassDefFoundError2"));
      suite.addTest(new BasicLoaderUnitTestCase("testDeadlockScenario1"));
      suite.addTest(new BasicLoaderUnitTestCase("testDeadlockScenario2"));
      suite.addTest(new BasicLoaderUnitTestCase("testClasspathOrdering"));
      suite.addTest(new BasicLoaderUnitTestCase("testLinkageError"));
      return suite;
   }
}
