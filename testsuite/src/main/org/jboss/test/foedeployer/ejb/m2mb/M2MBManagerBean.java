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
package org.jboss.test.foedeployer.ejb.m2mb;

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
 *    name="M2MBManager"
 *    jndi-name="M2MBManagerEJB.M2MBManagerHome"
 *    generate="true"
 *    view-type="remote"
 *
 * @ejb.ejb-ref
 *    ejb-name="Project"
 *    view-type="local"
 * @ejb.ejb-ref
 *    ejb-name="Developer"
 *    view-type="local"
 *
 * @ejb.transaction type="Required"
 */
public class M2MBManagerBean
   implements SessionBean
{
   // Attributes --------------------------------------------------
   static Category log = Category.getInstance( M2MBManagerBean.class );

   static String PROJECT_NAME = "java:comp/env/ejb/Project";
   static String DEVELOPER_NAME = "java:comp/env/ejb/Developer";

   private ProjectLocalHome projectHome;
   private DeveloperLocalHome developerHome;

   // Business methods ---------------------------------------------
   /**
    * Creates a project
    *
    * @ejb.interface-method
    */
   public void createProject( String projectName )
   {
      try
      {
         projectHome.create( projectName );
      }
      catch( CreateException ce )
      {
         throw new EJBException( ce );
      }
   }

   /**
    * Creates a developer
    *
    * @ejb.interface-method
    */
   public void createDeveloper( String developerName )
   {
      try
      {
         developerHome.create( developerName );
      }
      catch( CreateException ce )
      {
         throw new EJBException( ce );
      }
   }

   /**
    * Returns developers for project
    *
    * @ejb.interface-method
    */
   public Collection getDevelopersForProject( String projectName )
   {
      try
      {
         ProjectLocal project = projectHome.findByPrimaryKey( projectName );
         Collection devs = new ArrayList();
         for(Iterator iter=project.getDevelopers().iterator(); iter.hasNext();)
         {
            DeveloperLocal developer = (DeveloperLocal) iter.next();
            devs.add( developer.getName() );
         }
         return devs;
      }
      catch( FinderException fe )
      {
        throw new EJBException( fe );
      }
   }

   /**
    * Returns projects for developer
    *
    * @ejb.interface-method
    */
   public Collection getProjectsForDeveloper( String developerName )
   {
      try
      {
         DeveloperLocal developer = developerHome.findByPrimaryKey( developerName );
         Collection prjs = new ArrayList();
         for(Iterator iter=developer.getProjects().iterator(); iter.hasNext();)
         {
            ProjectLocal project = (ProjectLocal) iter.next();
            prjs.add( project.getName() );
         }
         return prjs;
      }
      catch( FinderException fe )
      {
        throw new EJBException( fe );
      }
   }


   /**
    * Adds a project to developer
    *
    * @ejb.interface-method
    */
   public void addProjectToDeveloper( String developerName,
                                      String projectName )
   {
      try
      {
         DeveloperLocal dev = developerHome.findByPrimaryKey( developerName );
         ProjectLocal prj = projectHome.findByPrimaryKey( projectName );
         dev.getProjects().add( prj );
      }
      catch( Exception e )
      {
        throw new EJBException( e );
      }
   }

   /**
    * Adds a develeloper to project
    *
    * @ejb.interface-method
    */
   public void addDeveloperToProject( String projectName,
                                      String developerName )
   {
      try
      {
         DeveloperLocal dev = developerHome.findByPrimaryKey( developerName );
         ProjectLocal prj = projectHome.findByPrimaryKey( projectName );
         prj.getDevelopers().add( dev );
      }
      catch( Exception e )
      {
        throw new EJBException( e );
      }
   }

   /**
    * Removes project if exists
    *
    * @ejb.interface-method
    */
   public void removeProjectIfExists( String projectName )
   {
      try
      {
         ProjectLocal project = projectHome.findByPrimaryKey( projectName );
         project.remove();
      }
      catch( Exception e )
      {
         // yam-yam
      }
   }

   /**
    * Removes developer if exists
    *
    * @ejb.interface-method
    */
   public void removeDeveloperIfExists( String developerName )
   {
      try
      {
         DeveloperLocal developer = developerHome.findByPrimaryKey( developerName );
         developer.remove();
      }
      catch( Exception e )
      {
         // yam-yam
      }
   }


   // SessionBean implementation -------------------------------------
 
   public void setSessionContext(SessionContext c)
   {
      try
      {
         Context ic = new InitialContext();
         developerHome = (DeveloperLocalHome)ic.lookup(DEVELOPER_NAME);
         projectHome = (ProjectLocalHome)ic.lookup(PROJECT_NAME);
      }
      catch(NamingException ne)
      {
         throw new EJBException(ne);
      }
   }

   /**
    * create method
    *
    * @ejb:create-method
    */ 
   public void ejbCreate() { }
   public void ejbActivate() { }
   public void ejbPassivate() { }
   public void ejbRemove() { }
}
