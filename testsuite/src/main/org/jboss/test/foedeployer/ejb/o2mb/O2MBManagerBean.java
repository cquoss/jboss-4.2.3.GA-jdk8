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
package org.jboss.test.foedeployer.ejb.o2mb;


import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import java.rmi.RemoteException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Category;


/**
 * Manager session bean.
 *
 * @ejb.bean
 *    type="Stateless"
 *    name="O2MBManager"
 *    jndi-name="O2MBManagerEJB.O2MBManagerHome"
 *    generate="true"
 *    view-type="remote"
 *
 * @ejb.ejb-ref
 *    ejb-name="Company"
 *    view-type="local"
 * @ejb.ejb-ref
 *    ejb-name="Employee"
 *    view-type="local"
 *
 * @ejb.transaction type="Required"
 */
public class O2MBManagerBean
   implements SessionBean
{
   // Attributes --------------------------------------------------
   static Category log = Category.getInstance( O2MBManagerBean.class );

   static String COMPANY_NAME = "java:comp/env/ejb/Company";
   static String EMPLOYEE_NAME = "java:comp/env/ejb/Employee";

   private CompanyLocalHome companyHome;
   private EmployeeLocalHome employeeHome;

   // Business methods ---------------------------------------------
   /**
    * Creates a company
    *
    * @ejb.interface-method
    */
   public void createCompany( String companyName )
   {
      try
      {
         companyHome.create( companyName );
      }
      catch( CreateException ce )
      {
         throw new EJBException( ce );
      }
   }

   /**
    * Creates an employee
    *
    * @ejb.interface-method
    */
   public void createEmployee( String employeeName )
   {
      try
      {
         employeeHome.create( employeeName );
      }
      catch( CreateException ce )
      {
         throw new EJBException( ce );
      }
   }

   /**
    * Returns all the companies
    *
    * @ejb.interface-method
    */
   public Collection getEmployeesForCompany( String companyName )
   {
      try
      {
         CompanyLocal company = companyHome.findByPrimaryKey( companyName );
         Collection emps = new ArrayList();
         for(Iterator iter=company.getEmployees().iterator(); iter.hasNext();)
         {
            EmployeeLocal employee = (EmployeeLocal) iter.next();
            emps.add( employee.getName() );
         }
         return emps;
      }
      catch( FinderException fe )
      {
        throw new EJBException( fe );
      }
   }

   /**
    * Returns emaployee's company
    *
    * @ejb.interface-method
    */
   public String getCompanyForEmployee( String employeeName )
   {
      try
      {
         CompanyLocal company = employeeHome.findByPrimaryKey( employeeName ).
                                   getCompany();
         if( company == null ) return null;
         return company.getName();
      }
      catch( FinderException fe )
      {
        throw new EJBException( fe );
      }
   }

   /**
    * Creates new emaployee and adds it to a company
    *
    * @ejb.interface-method
    */
   public void createEmployeeForCompany(
      String employeeName, String companyName )
   {
      try
      {
         CompanyLocal company = companyHome.findByPrimaryKey( companyName );
         EmployeeLocal employee = employeeHome.create( employeeName );
         company.getEmployees().add( employee );
      }
      catch( Exception e )
      {
        throw new EJBException( e );
      }
   }

   /**
    * Sets a company for employee
    *
    * @ejb.interface-method
    */
   public void employ( String employeeName, String companyName )
   {
      try
      {
         CompanyLocal company = companyHome.findByPrimaryKey( companyName );
         EmployeeLocal employee = employeeHome.findByPrimaryKey( employeeName );
         employee.setCompany( company );
      }
      catch( Exception e )
      {
        throw new EJBException( e );
      }
   }

   /**
    * Removes a company
    *
    * @ejb.interface-method
    */
   public void removeCompany( String companyName )
   {
      try
      {
         CompanyLocal company = companyHome.findByPrimaryKey( companyName );
         company.remove();
      }
      catch( Exception e )
      {
        throw new EJBException( e );
      }
   }

   /**
    * Removes a company if it exists
    *
    * @ejb.interface-method
    */
   public void removeCompanyIfExists( String companyName )
   {
      try
      {
         CompanyLocal company = companyHome.findByPrimaryKey( companyName );
         company.remove();
      }
      catch( Exception e )
      {
         // yam-yam
      }
   }


   // SessionBean implementation -------------------------------------
 
   public void setSessionContext(SessionContext ctx)
   {
      try
      {
         Context ic = new InitialContext();
       
         companyHome = (CompanyLocalHome)ic.lookup(COMPANY_NAME);
         employeeHome = (EmployeeLocalHome)ic.lookup(EMPLOYEE_NAME);
      }
      catch(NamingException ne)
      {
         throw new EJBException(ne);
      }
   }

   /**
    * create method
    *
    * @ejb.create-method
    */ 
   public void ejbCreate() { }
   public void ejbActivate() { }
   public void ejbPassivate() { }
   public void ejbRemove() { }
}
