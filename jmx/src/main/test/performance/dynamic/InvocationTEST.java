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
package test.performance.dynamic;

import junit.framework.TestCase;
import test.performance.PerformanceSUITE;
import test.performance.dynamic.support.Dyn;

import javax.management.*;


public class InvocationTEST extends TestCase
{

   public InvocationTEST(String s)
   {
      super(s);
   }

   public void testVoidInvocationWithDefaultDomain()
   {
      try
      {
         System.out.println("\nDYNAMIC: void invocation with DefaultDomain");
         System.out.println(PerformanceSUITE.ITERATION_COUNT + " Invocations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
         System.out.println("(this may take a while...)\n");

         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName name = new ObjectName(":performanceTest=dynamic");
         String method = "methodInvocation";
         long start = 0, end = 0;
         float avg = 0l;

         server.registerMBean(new Dyn(), name);

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.ITERATION_COUNT; ++invocationIterations)
            {
               server.invoke(name, method, null, null);
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.println("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
         MBeanServerFactory.releaseMBeanServer(server);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }

   }

   public void testVoidInvocation()
   {
      try
      {
         System.out.println("\nDYNAMIC: void invocation");
         System.out.println(PerformanceSUITE.ITERATION_COUNT + " Invocations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
         System.out.println("(this may take a while...)\n");

         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName name = new ObjectName("Domain:performanceTest=dynamic");
         String method = "methodInvocation";
         long start = 0, end = 0;
         float avg = 0l;

         server.registerMBean(new Dyn(), name);

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.ITERATION_COUNT; ++invocationIterations)
            {
               server.invoke(name, method, null, null);
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.println("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
         MBeanServerFactory.releaseMBeanServer(server);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }

   public void testCounterInvocation()
   {
      try
      {
         System.out.println("\nDYNAMIC: counter invocation");
         System.out.println(PerformanceSUITE.ITERATION_COUNT + " Invocations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
         System.out.println("(this may take a while...)\n");

         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName name = new ObjectName("Domain:performanceTest=dynamic");
         Dyn mbean = new Dyn();
         String method = "counter";
         long start = 0, end = 0;
         float avg = 0l;

         server.registerMBean(mbean, name);

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.ITERATION_COUNT; ++invocationIterations)
            {
               server.invoke(name, method, null, null);
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.println("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
         MBeanServerFactory.releaseMBeanServer(server);

         assertTrue(mbean.getCount() == (PerformanceSUITE.REPEAT_COUNT + 1)*PerformanceSUITE.ITERATION_COUNT);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }


   public void testMixedArgsInvocation()
   {
      try
      {
         System.out.println("\nDYNAMIC: mixed arguments invocation");
         System.out.println(PerformanceSUITE.ITERATION_COUNT + " Invocations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
         System.out.println("(this may take a while...)\n");

         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName name    = new ObjectName("Domain:performanceTest=dynamic");
         Dyn mbean      = new Dyn();
         
         String method      = "mixedArguments";
         String[] signature = new String[] { 
                                 Integer.class.getName(),
                                 int.class.getName(),
                                 Object[][][].class.getName(),
                                 Attribute.class.getName()
                              };
                              
         Object[] args      = new Object[] {
                                 new Integer(1234),
                                 new Integer(455617),
                                 new Object[][][] {
                                    { 
                                       { "1x1x1", "1x1x2", "1x1x3" },
                                       { "1x2x1", "1x2x2", "1x2x3" },
                                       { "1x3x1", "1x3x2", "1x3x3" }
                                    },
                                    
                                    {
                                       { "2x1x1", "2x1x2", "2x1x3" },
                                       { "2x2x1", "2x2x2", "2x2x3" },
                                       { "2x3x1", "2x3x2", "2x3x3" }
                                    },
                                    
                                    {
                                       { "3x1x1", "3x1x2", "3x1x3" },
                                       { "3x2x1", "3x2x2", "3x2x3" },
                                       { "3x3x1", "3x3x2", "3x3x3" }
                                    }
                                 },
                                 new Attribute("attribute", "value")
                              };
                                    
         long start = 0, end = 0;
         float avg = 0l;

         server.registerMBean(mbean, name);

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.ITERATION_COUNT; ++invocationIterations)
            {
               server.invoke(name, method, args, signature);
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.println("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
         MBeanServerFactory.releaseMBeanServer(server);

      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }
   
}
