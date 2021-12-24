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
 * A Boolean that is an arguement to a query.
 * 
 * @author  Adrian.Brock@jboss.com
 * @version $Revision: 57200 $
 */
class BooleanValueExp extends QueryEval implements ValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = 7754922052666594581L;
   
   // Attributes --------------------------------------------------

   private boolean val;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   public BooleanValueExp()
   {
      val = false;
   }
   
   public BooleanValueExp(Boolean value)
   {
      this.val = value.booleanValue();
   }

   // Public ------------------------------------------------------

   public boolean getValue()
   {
      return val;
   }

   // ValueExp implementation -------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
             BadBinaryOpValueExpException,
             BadAttributeValueExpException,
             InvalidApplicationException
   {
      return this;
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return Boolean.toString(val);
   }

}
