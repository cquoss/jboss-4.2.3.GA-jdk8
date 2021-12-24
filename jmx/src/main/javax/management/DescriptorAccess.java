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


/**
 * This interface is used to gain access to descriptors of a JMX component.<p>
 *
 * ModelMBeans use this interface in ModelMBeanInfo classes.
 *
 * @see javax.management.Descriptor
 * @see javax.management.modelmbean.ModelMBean
 * @see javax.management.modelmbean.ModelMBeanInfo
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 *
 */
public interface DescriptorAccess
{
   // Constants ---------------------------------------------------

   // Public ------------------------------------------------------

   /**
    * Retrieves the descriptor.
    *
    * @return the descriptor.
    */
   public Descriptor getDescriptor();


   /**
    * Sets the descriptor.
    *
    * @param inDescriptor the new descriptor.
    */
   public void setDescriptor(Descriptor inDescriptor);
}
