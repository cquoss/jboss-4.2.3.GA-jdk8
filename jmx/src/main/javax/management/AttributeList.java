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
package javax.management;

import java.util.ArrayList;


/**
 * A list of a MBean attributes. <p>
 *
 * An AttributeList can be used to get and set multiple MBean attributes
 * in one invocation. <p>
 *
 * It is an array list that can only contain
 * {@link javax.management.Attribute}s <p>
 *
 * <b>Note:</b> AttributeLists must be externally synchronized.
 *
 * @see javax.management.Attribute
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 */
public class AttributeList
   extends ArrayList
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -4077085769279709076L;

   // Attributes --------------------------------------------------------


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Contruct a new empty attribute list.
    */
   public AttributeList()
   {
      super();
   }

   /**
    * Contruct a new empty attriute list with an initial capacity.
    *
    * @param initialCapacity the initial capacity reserved.
    */
   public AttributeList(int initialCapacity)
   {
      super(initialCapacity);
   }

   /**
    * Contruct a new attribute from another attribute list.
    * The order is determined by the ArrayList's iterator.
    *
    * @param list the attribute list to copy.
    */
   public AttributeList(AttributeList list)
   {
      super(list);
   }

   // Public --------------------------------------------------------

   // ArrayList Overrides -------------------------------------------

   /**
    * Append an Attribute to the list.
    *
    * @param object the attribute to append.
    */
   public void add(Attribute object)
   {
      super.add(object);
   }

   /**
    * Insert a new Attribute into the list at the specified location.
    *
    * @param index the location to insert the attribute.
    * @param object the attribute to insert.
    */
   public void add(int index, Attribute object)
   {
      super.add(index, object);
   }

   /**
    * Change the attribute at the specified location.
    *
    * @param index the location of he attribute to change.
    * @param object the new attribute.
    */
   public void set(int index, Attribute object)
   {
      super.set(index, object);
   }

   /**
    * Append the attributes the passed list to the end of this list.
    *
    * @param list the attributes appended.
    * @return true when the list changes as a result of this operation,
    * false otherwise.
    */
   public boolean addAll(AttributeList list)
   {
      return super.addAll(list);
   }

   /**
    * Insert all the attributes in the passed list at the specified
    * location in this list.
    *
    * @param index the location where the attributes are inserted.
    * @param list the attributes inserted.
    * @return true when the list changes as a result of this operation,
    * false otherwise.
    */
   public boolean addAll(int index, AttributeList list)
   {
      return super.addAll(index, list);
   }

   // Implementation ------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

