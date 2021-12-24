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

import java.io.Externalizable;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

/**
 * Verifies if all classes are implementing serialVersionUID in a given JAR.
 * Use the method createCompatilibitySuite defined in this class embedded in suite methods. 
 * @author clebert.suconic@jboss.com
 */
public class TestDefinitionOfSerialVersionUID extends TestCase
{
    static Logger log = Logger.getLogger("TestDefinitionOfSerialVersionUID");


    static class TestLookupIndividualSerialUID extends TestCase
    {
        
        Class loadedClass;
        public TestLookupIndividualSerialUID(Class loadedClass)
        {
            super(loadedClass.getName());
            this.loadedClass=loadedClass;
        }

        public void run(TestResult result)
        {
            log.info("Testing " + this.getName());
            try
            {
                result.startTest(this);

                Field primaryField = lookupSerialField(loadedClass);
                assertNotNull("serialVersionUID not defined in current version of " + this.getName(), primaryField);
                primaryField.setAccessible(true);
                log.info("SerialUID for " + this.getName() + " is " + primaryField.getLong(null));
            } 
            catch (AssertionFailedError error)
            {
                result.addFailure(this, error);
            } catch (Throwable e)
            {
                result.addError(this,e);
            }
            log.info("Done " + this.getName());
            result.endTest(this);
        }

        /** Using reflection as ObjectSerialClass will calculate a new one */
        private Field lookupSerialField(Class parameterClass) throws NoSuchFieldException
        {
            try 
            {
                return parameterClass.getDeclaredField("serialVersionUID");
            }
            catch (Throwable e)
            {
                return null;
            }
        }

    }

    /** Create a compatibility suite for a give JAR file */
    public static Test createCompatilibitySuite(File jarFile) throws Exception
    {
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {jarFile.toURL()},TestDefinitionOfSerialVersionUID.class.getClassLoader());
        TestSuite suite = new TestSuite();
        ZipFile zipFile = new ZipFile(jarFile);
        Enumeration i = zipFile.entries();
        while (i.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) i.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class"))
            {
                String classname = entry.getName().replace('/', '.').substring(
                        0, entry.getName().length() - 6);
                
                Class lookupClass = null;
                try {
                    lookupClass = urlClassLoader.loadClass(classname);
                }
                catch (Throwable e) 
                {
                    System.out.println("Warning... Class " + classname + " couldn't be loaded at " + jarFile.getName() + " -> " + e.toString());
                }
                
                
                if (lookupClass!=null && (Externalizable.class.isAssignableFrom(lookupClass) || Serializable.class.isAssignableFrom(lookupClass)) && !lookupClass.isInterface()) {
                    suite.addTest(new TestLookupIndividualSerialUID(lookupClass));
                }
            }
        }
        return suite;
    }
    
    public static Test createCompatilibitySuiteForLib(String libFileName) throws Exception
    {
        String jbossdist = System.getProperty("jboss.dist");
        if (jbossdist==null) {
                System.out.println("jboss.dist not defined");
                throw new Exception("jboss.dist not defined");
        }
        String strfile = jbossdist +  "/server/all/lib/" + libFileName;

        File file = new File(strfile);
        if (file.exists())
        {
           return createCompatilibitySuite(file);
        } else 
        {
           strfile = jbossdist + "/lib/" + libFileName;
           file = new File(strfile);
           if (file.exists()) 
           {
               return createCompatilibitySuite(file);
           } else 
           {
                   System.out.println("library " + strfile + " not found");
                   throw new RuntimeException ("library " + strfile + " not found");
           }
        }
        
    }

}
