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
package org.jboss.iiop.rmi.ir;

import org.omg.CORBA.ContainedOperations;
import org.omg.CORBA.ContainedPackage.Description;
import org.omg.CORBA.DefinitionKind;
import org.omg.CORBA.Container;
import org.omg.CORBA.ContainerHelper;
import org.omg.CORBA.Contained;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CORBA.BAD_INV_ORDER;

import java.io.UnsupportedEncodingException;


/**
 *  Abstract base class for all contained IR entities.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 57194 $
 */
abstract class ContainedImpl
   extends IRObjectImpl
   implements LocalContained
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static final org.jboss.logging.Logger logger = 
               org.jboss.logging.Logger.getLogger(ContainedImpl.class);

   // Constructors --------------------------------------------------

   ContainedImpl(String id, String name, String version,
                 LocalContainer defined_in,
                 DefinitionKind def_kind, RepositoryImpl repository)
   {
      super(def_kind, repository);
      this.id = id;
      this.name = name;
      this.version = version;
      this.defined_in = defined_in;

      if (defined_in instanceof LocalContained)
        this.absolute_name = ((LocalContained)defined_in).absolute_name() +
                             "::" + name;
      else // must be Repository
        this.absolute_name = "::" + name;
   }

   // Public --------------------------------------------------------

   // Z implementation ----------------------------------------------

   public java.lang.String id()
   {
      logger.trace("ContainedImpl[" + id + "].id() entered.");
      return id;
   }

   public void id(java.lang.String id)
   {
      throw new BAD_INV_ORDER("Cannot change RMI/IIOP mapping.");
   }

   public java.lang.String name()
   {
      logger.trace("ContainedImpl[" + id + "].name() entered.");
      return name;
   }

   public void name(java.lang.String name)
   {
      throw new BAD_INV_ORDER("Cannot change RMI/IIOP mapping.");
   }

   public java.lang.String version()
   {
      logger.trace("ContainedImpl[" + id + "].version() entered.");
      return version;
   }

   public void version(java.lang.String version)
   {
      throw new BAD_INV_ORDER("Cannot change RMI/IIOP mapping.");
   }

   public Container defined_in()
   {
      logger.debug("ContainedImpl[" + id + "].defined_in() entered.");
      return ContainerHelper.narrow(defined_in.getReference());
   }

   public java.lang.String absolute_name()
   {
      logger.trace("ContainedImpl[" + id + "].absolute_name() returning \"" +
                   absolute_name + "\".");
      return absolute_name;
   }

   public Repository containing_repository()
   {
      logger.debug("ContainedImpl[" + id + 
                   "].containing_repository() entered.");
      return RepositoryHelper.narrow(repository.getReference());
   }

   public abstract Description describe();

   public void move(Container new_container,
                    String new_name, String new_version)
   {
      throw new BAD_INV_ORDER("Cannot change RMI/IIOP mapping.");
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    *  The global repository ID of this object.
    */
   protected String id;

   /**
    *  The name of this object within its container.
    */
   protected String name;

   /**
    *  The version of this object. Defaults to 1.0.
    */
   protected String version = "1.0";

   /**
    *  The container this is defined in.
    *  This may not be the same as the container this is contained in.
    */
   protected LocalContainer defined_in;

   /**
    *  The absolute name of this object.
    */
   protected String absolute_name;


   /**
    *  Return the POA object ID of this IR object.
    *  Contained objects use the UTF-8 encoding of their id, prefixed by
    *  "repository_name:".
    */
   protected byte[] getObjectId()
   {
      try {
         return (getRepository().getObjectIdPrefix() + id).getBytes("UTF-8");
      } catch (UnsupportedEncodingException ex) {
         throw new RuntimeException("UTF-8 encoding not supported.");
      }
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
