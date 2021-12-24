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
 * A qualified string that is an argument to a query.<p>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class QualifiedAttributeValueExp extends AttributeValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = 8832517277410933254L;
   
   // Attributes --------------------------------------------------

   /**
    * The class name
    */
   private String className;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct an attribute value expression for the passed class and 
    * attribute name
    *
    * @param className the class name
    * @param value the attribute name
    */
   public QualifiedAttributeValueExp(String className, String value)
   {
      super(value);
      this.className = className;
   }

   // Public ------------------------------------------------------

   // ValueExp Implementation -------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      try
      {
         ObjectInstance instance = QueryEval.getMBeanServer().getObjectInstance(name);
         if (instance.getClassName().equals(className))
            return super.apply(name);
      }
      catch (Exception e)
      {
         // REVIEW: What happens here? Should this happen?
         return null;
      }
      throw new InvalidApplicationException(new String(name + "\n" + className));
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return new String(className + "." + getAttributeName());
   }

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
