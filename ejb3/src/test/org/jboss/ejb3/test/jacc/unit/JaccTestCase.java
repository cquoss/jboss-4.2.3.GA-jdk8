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

package org.jboss.ejb3.test.jacc.unit;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.transaction.RollbackException;

import org.jboss.ejb3.test.JBossWithKnownIssuesTestCase;
import org.jboss.ejb3.test.jacc.AllEntity;
import org.jboss.ejb3.test.jacc.SomeEntity;
import org.jboss.ejb3.test.jacc.StarEntity;
import org.jboss.ejb3.test.jacc.Stateful;
import org.jboss.ejb3.test.jacc.Stateless;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import junit.framework.Test;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 65758 $
 */
public class JaccTestCase extends JBossWithKnownIssuesTestCase
{
   org.apache.log4j.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public JaccTestCase(String name)
   {
      super(name);
   }

   public void testUnchecked() throws Exception
   {
      Stateful stateful = (Stateful)getInitialContext().lookup("StatefulBean/remote");
      Stateless stateless = (Stateless)getInitialContext().lookup("StatelessBean/remote");

      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      int result = stateful.unchecked(1);
      assertEquals(1,result);

      result = stateless.unchecked(10);
      assertEquals(10,result);

      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      result = stateful.unchecked(100);
      assertEquals(100,result);

      result = stateless.unchecked(99);
      assertEquals(99,result);

   }


   public void testChecked() throws Exception
   {
      Stateful stateful = (Stateful)getInitialContext().lookup("StatefulBean/remote");
      Stateless stateless = (Stateless)getInitialContext().lookup("StatelessBean/remote");

      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      int result = stateful.checked(5);
      assertEquals(5,result);

      result = stateless.checked(50);
      assertEquals(50,result);

      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      boolean securityException = false;
      try
      {
         stateful.checked(500);
      }
      catch (EJBAccessException e){
         securityException = true;
      }

      assertTrue(securityException);

      try
      {
         stateless.checked(501);
      }
      catch (EJBAccessException e){
         securityException = true;
      }

      assertTrue(securityException);
   }

   public void testAllEntity()throws Exception
   {
      Stateless stateless = (Stateless)getInitialContext().lookup("StatelessBean/remote");
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      System.out.println("Good role");
      System.out.println("Inserting...");
      AllEntity e = stateless.insertAllEntity();
      System.out.println("Reading...");
      e = stateless.readAllEntity(e.id);
      e.val += "y";
      System.out.println("Updating...");
      stateless.updateAllEntity(e);
      System.out.println("Deleting...");
      stateless.deleteAllEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertAllEntity();

      System.out.println("Bad role");
      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      AllEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertAllEntity();
         throw new FailedException("Insert check not done for AllEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         System.out.println("Reading...");
         ae2 = stateless.readAllEntity(e.id);
         throw new FailedException("Read check not done for AllEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         e.val += "y";
         stateless.updateAllEntity(e);
         throw new FailedException("Update check not done for AllEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         stateless.deleteAllEntity(e);
         throw new FailedException("Delete check not done for AllEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         e = stateless.insertAllEntity();
         throw new FailedException("Insert check not done for AllEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }
   }


   public void testStarEntity()throws Exception
   {
      Stateless stateless = (Stateless)getInitialContext().lookup("StatelessBean/remote");
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      System.out.println("Good role");
      System.out.println("Inserting...");
      StarEntity e = stateless.insertStarEntity();
      System.out.println("Reading...");
      e = stateless.readStarEntity(e.id);
      e.val += "y";
      System.out.println("Updating...");
      stateless.updateStarEntity(e);
      System.out.println("Deleting...");
      stateless.deleteStarEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertStarEntity();

      System.out.println("Bad role");
      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      StarEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertStarEntity();
         throw new FailedException("Insert check not done for StarEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         System.out.println("Reading...");
         ae2 = stateless.readStarEntity(e.id);
         throw new FailedException("Read check not done for StarEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         e.val += "y";
         stateless.updateStarEntity(e);
         throw new FailedException("Update check not done for StarEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         stateless.deleteStarEntity(e);
         throw new FailedException("Delete check not done for StarEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         e = stateless.insertStarEntity();
         throw new FailedException("Insert check not done for StarEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }
   }

   public void testSomeEntity()throws Exception
   {
      Stateless stateless = (Stateless)getInitialContext().lookup("StatelessBean/remote");
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      System.out.println("Good role");
      System.out.println("Inserting...");
      SomeEntity e = stateless.insertSomeEntity();

      try
      {
         System.out.println("Reading...");
         e = stateless.readSomeEntity(e.id);
         throw new FailedException("Read check not done for SomeEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         e.val += "y";
         System.out.println("Updating...");
         stateless.updateSomeEntity(e);
         throw new FailedException("Update check not done for SomeEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }


      System.out.println("Deleting...");
      stateless.deleteSomeEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertSomeEntity();

      System.out.println("Bad role");
      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      SomeEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertSomeEntity();
         throw new FailedException("Insert check not done for SomeEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }

      try
      {
         stateless.deleteSomeEntity(e);
         throw new FailedException("Delete check not done for SomeEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         if(ex.getCause() != null && ex.getCause() instanceof RollbackException)
         {
            showKnownIssue("EJBTHREE-894");
         }
         else
            hasSecurityOrEJBException(ex);
      }

      try
      {
         e = stateless.insertSomeEntity();
         throw new FailedException("Insert check not done for SomeEntity");
      }
      catch(FailedException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         hasSecurityOrEJBException(ex);
      }
   }


   private void hasSecurityOrEJBException(Exception e)throws FailedException
   {
      Throwable t = e;

      while (t != null)
      {
         //System.out.println(t);
         String classname = t.getClass().getName();
         if (classname.equals(EJBAccessException.class.getName()) ||
               classname.equals(EJBException.class.getName()) )
         {
            return;
         }
         t = t.getCause();
      }

      throw new FailedException("EJBAccessException not thrown", e);
   }




   public static Test suite() throws Exception
   {
      return getDeploySetup(JaccTestCase.class, "jacc-test.jar");
   }

   private class FailedException extends Exception
   {
      public FailedException(String msg)
      {
         super(msg);
      }
      
      public FailedException(String msg, Throwable t)
      {
         super(msg, t);
      }
   }
}