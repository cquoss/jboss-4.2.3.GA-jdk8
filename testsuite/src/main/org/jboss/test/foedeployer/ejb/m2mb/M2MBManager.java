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

/**
 * Remote interface for M2MBManager.
 */
public interface M2MBManager
   extends javax.ejb.EJBObject
{
   /**
    * Creates a project
    */
   public void createProject( String projectName )
      throws java.rmi.RemoteException;

   /**
    * Creates a developer
    */
   public void createDeveloper( String developerName )
      throws java.rmi.RemoteException;

   /**
    * Returns developers for project
    */
   public java.util.Collection getDevelopersForProject( String projectName )
      throws java.rmi.RemoteException;

   /**
    * Returns projects for developer
    */
   public java.util.Collection getProjectsForDeveloper( String developerName )
      throws java.rmi.RemoteException;

   /**
    * Adds a project to developer
    */
   public void addProjectToDeveloper( String developerName,String projectName )
      throws java.rmi.RemoteException;

   /**
    * Adds a develeloper to project
    */
   public void addDeveloperToProject( String projectName,String developerName )
      throws java.rmi.RemoteException;

   /**
    * Removes project if exists
    */
   public void removeProjectIfExists( String projectName )
      throws java.rmi.RemoteException;

   /**
    * Removes developer if exists
    */
   public void removeDeveloperIfExists( String developerName )
      throws java.rmi.RemoteException;

}
