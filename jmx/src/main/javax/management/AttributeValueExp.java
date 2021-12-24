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
 * A String that is an arguement to a query.<p>
 * 
 * <p><b>Revisions:</b>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li> Make queries thread safe
 * </ul>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization
 * </ul>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public class AttributeValueExp implements ValueExp
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = -7768025046539163385L;

   // Attributes --------------------------------------------------

   /**
    * The attribute name
    */
   private String attr;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct an attribute value expression for the null attribute name
    */
   public AttributeValueExp()
   {
   }

   /**
    * Construct an attribute value expression for the passed attribute name
    *
    * @param attr the attribute name
    */
   public AttributeValueExp(String attr)
   {
      this.attr = attr;
   }

   // Public ------------------------------------------------------

   /**
    * Get the attribute name.
    *
    * @return the attribute name
    */
   public String getAttributeName()
   {
      return attr;
   }

   // ValueExp Implementation -------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
             BadBinaryOpValueExpException,
             BadAttributeValueExpException,
             InvalidApplicationException
   {
      Object object = getAttribute(name);
      if (object != null && object instanceof String)
         return new StringValueExp((String) object);
      if (object != null && object instanceof Boolean)
         return new BooleanValueExp((Boolean) object);
      if (object != null && object instanceof Number)
         return new NumericValueExp((Number) object);
      throw new BadAttributeValueExpException(object);
   }

   public void setMBeanServer(MBeanServer server)
   {
      QueryExpSupport.server.set(server);
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return attr;
   }

   // Protected ---------------------------------------------------

   /**
    * Get the value of the attribute for a given object name
    *
    * @param name - the object name
    * @return the value of the attribute
    */
   protected Object getAttribute(ObjectName name)
   {
      try
      {
         return QueryEval.getMBeanServer().getAttribute(name, attr);
      }
      catch (Exception e)
      {
         return null;
      }
   }

}
