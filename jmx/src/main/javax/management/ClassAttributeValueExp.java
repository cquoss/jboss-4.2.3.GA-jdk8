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
 * Get the class name of the mbean
 *
 * <p><b>Revisions:</b>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class ClassAttributeValueExp extends AttributeValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -1081892073854801359L;
   
   /* WTF? This is in the javadoc and it has this value in the RI */
   String attr = "Class"; 
   
   // Attributes --------------------------------------------------

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct an class attribute value expression
    */
   public ClassAttributeValueExp()
   {
      // REVIEW: Correct?
      super(null);
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
          return Query.value(instance.getClassName());
       }
       catch (Exception e)
       {
          // REVIEW: Correct?
          throw new InvalidApplicationException(name);
       }
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return new String("class");
   }

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
