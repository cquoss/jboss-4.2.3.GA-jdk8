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
package org.jboss.test.foedeployer.test;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.Handle;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

import org.jboss.test.foedeployer.ejb.ql.CarCatalog;
import org.jboss.test.foedeployer.ejb.ql.CarCatalogHome;

/**
 * Test of a simple WebLogic QL conversion
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 * @version $Revision: 57211 $
 */
public class QLConversionTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   public static final String FOE_DEPLOYER = "foe-deployer-3.2.sar";
   public static final String FOE_DEPLOYER_NAME = "jboss:service=FoeDeployer";
   public static final String CONVERTOR_DEPLOYER_QUERY_NAME = "jboss:service=Convertor,*";
   public static final String QL_APPLICATION = "foe-deployer-ql-test";
   public static final String CAR_CATALOG_JNDI_NAME = "CarCatalogEJB.CarCatalogHome";

   // Static --------------------------------------------------------
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite lSuite = new TestSuite();
      lSuite.addTest( new TestSuite( QLConversionTestCase.class ) );

      // Create an initializer for the test suite
      TestSetup lWrapper = new JBossTestSetup( lSuite )
      {
         protected void setUp() throws Exception
         {
            super.setUp();
         }
         protected void tearDown() throws Exception
         {
            super.tearDown();
         }
      };
      return lWrapper;
   }

   // Constructors --------------------------------------------------
   public QLConversionTestCase( String pName )
   {
      super( pName );
   }

   // Public --------------------------------------------------------
   /**
    * Test a simple conversion
    **/
   public void testQLConversion()
      throws Exception
   {
      try
      {
         log.debug("+++ testQLConversion");

         // First check if foe-deployer is deployed
         boolean lIsInitiallyDeployed = getServer().isRegistered( new ObjectName( FOE_DEPLOYER_NAME ) );
         if(!lIsInitiallyDeployed)
            deploy(FOE_DEPLOYER);

         boolean lIsDeployed = getServer().isRegistered(new ObjectName(FOE_DEPLOYER_NAME));
         assertTrue("Foe-Deployer is not deployed", lIsDeployed);

         // Count number of convertors (must be a list one)
         int lCount = getServer().queryNames(new ObjectName(CONVERTOR_DEPLOYER_QUERY_NAME), null).size();
         assertTrue("No Convertor found on web server", lCount > 0);

         // Deploy the simple application
         deploy(QL_APPLICATION + ".wlar");

         // Because the Foe-Deployer copies the converted JAR back to the original place
         // it has to be deployed from here again
         deploy(QL_APPLICATION + ".jar");

         // Create some data and test queries
         int i;
         String[] carNumbers = { "apache", "noch", "zzuk", "silvia" };
         String[] carColors = { "red", "black", "red", "yellow" };
         int[] carYears = { 1990, 1989, 2000, 1995 };

         CarCatalog carCatalog = getCarCatalogEJB();

         //getLog().debug( "clean the database" );
         //i = -1;
         //while( ++i < carNumbers.length )
         //   carCatalog.removeCarIfExists( carNumbers[ i ] );

         getLog().debug( "manufacture cars" );
         i = -1;
         while( ++i < carNumbers.length )
         {
            carCatalog.createCar( carNumbers[ i ], carColors[ i ], carYears[ i ] );
            getLog().debug("registered car: " + carNumbers[ i ] );
         }

         getLog().debug( "find all cars" );
         Collection numbersCol = carCatalog.getAllCarNumbers();
         i = -1;
         while( ++i < carNumbers.length)
         {
            assertTrue( "Not all cars returned by converted findAll query",
               numbersCol.contains( carNumbers[ i ] ) );
         }

         getLog().debug( "find cars with red color" );
         Collection cars = carCatalog.getCarsWithColor( "red" );
         i = -1;
         while( ++i < carNumbers.length )
         {
            if( "red".equals( carColors[i] ) )
            {
               assertTrue( "Not all red cars found",
                  cars.contains(carNumbers[ i ]) );
            }
         }

         log.debug( "find cars after 1993 year" );
         cars = carCatalog.getCarsAfterYear( 1993 );
         i = -1;
         while( ++i < carNumbers.length )
         {
            if( 1993 < carYears[i] )
            {
               assertTrue( "Not all cars after year 1993 found",
                  cars.contains(carNumbers[ i ]) );
            }
         }

         // Undeploy converted application to clean up
         undeploy(QL_APPLICATION + ".jar");

         // also now should work without undeploying
         undeploy(QL_APPLICATION + ".wlar");

         // Only undeploy if deployed here
         if(!lIsInitiallyDeployed)
         {
            undeploy(FOE_DEPLOYER);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   // Private -------------------------------------------------------
   private CarCatalog getCarCatalogEJB()
      throws
         Exception
   {
      getLog().debug("looking for CarCatalogHome");
      Object ref = getInitialContext().lookup( CAR_CATALOG_JNDI_NAME );
      CarCatalogHome home = (CarCatalogHome)
         PortableRemoteObject.narrow( ref, CarCatalogHome.class );
      getLog().debug( "creating an instance of CarCatalog" );
      return home.create();
   }
}
