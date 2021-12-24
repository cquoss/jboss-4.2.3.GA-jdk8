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

import org.jboss.mx.util.QueryExpSupport;

/**
 * A String that is an arguement to a query.
 * 
 * @author  Adrian.Brock@jboss.org
 * @version $Revision: 57200 $
 */
public class StringValueExp
   implements ValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -3256390509806284044L;
   
   // Attributes --------------------------------------------------

   /**
    * The value of the string
    */
   private String val;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct a string value expression for the null string.
    */
   public StringValueExp()
   {
   }

   /**
    * Construct a string value expression for the passed string
    *
    * @param value the string
    */
   public StringValueExp(String value)
   {
      this.val = value;
   }

   // Public ------------------------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
             BadBinaryOpValueExpException,
             BadAttributeValueExpException,
             InvalidApplicationException
   {
      return this;
   }

   /**
    * Get the value of the string.
    *
    * @return the string value
    */
   public String getValue()
   {
      return val;
   }

   public void setMBeanServer(MBeanServer server)
   {
      QueryExpSupport.server.set(server);
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return val;
   }

}
