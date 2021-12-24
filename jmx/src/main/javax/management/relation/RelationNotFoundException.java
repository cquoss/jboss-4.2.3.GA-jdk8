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
package javax.management.relation;

import org.jboss.util.id.SerialVersion;

/**
 * Thrown when there is no relation for a passed relation id.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class RelationNotFoundException
   extends RelationException
{
   // Constants -----------------------------------------------------
   /** @since 4.0.1 */
   private static final long serialVersionUID = -3793951411158559116L;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new RelationNotFoundException with no message.
    */
   public RelationNotFoundException()
   {
      super();
   }

   /**
    * Construct a new RelationNotFoundException with the given message.
    *
    * @param message the error message.
    */
   public RelationNotFoundException(String message)
   {
      super(message);
   }
}

