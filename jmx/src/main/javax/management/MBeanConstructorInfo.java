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

import java.util.Arrays;

/**
 * Describes a constructor exposed by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 *
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class MBeanConstructorInfo extends MBeanFeatureInfo
   implements java.io.Serializable, Cloneable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 4433990064191844427L;

   // Attributes ----------------------------------------------------
   private MBeanParameterInfo[] signature = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   public MBeanConstructorInfo(java.lang.String description,
                               java.lang.reflect.Constructor constructor)
   {
      super(constructor.getName(), description);

      Class[] sign = constructor.getParameterTypes();
      signature = new MBeanParameterInfo[sign.length];

      for (int i = 0; i < sign.length; ++i)
      {
         String name = sign[i].getName();
         signature[i] = new MBeanParameterInfo(name, name, "MBean Constructor Parameter.");
      }
   }

   public MBeanConstructorInfo(java.lang.String name, java.lang.String description, MBeanParameterInfo[] signature)
      throws IllegalArgumentException
   {
      super(name, description);

      this.signature = (null == signature) ? new MBeanParameterInfo[0] : (MBeanParameterInfo[]) signature.clone();
   }

   public synchronized Object clone()
   {
      MBeanConstructorInfo clone = null;
      try
      {
         clone = (MBeanConstructorInfo) super.clone();
         clone.signature = (MBeanParameterInfo[])this.signature.clone();
      }
      catch(CloneNotSupportedException e)
      {
      }

      return clone;
   }

   public MBeanParameterInfo[] getSignature()
   {
      return (MBeanParameterInfo[]) signature.clone();
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanConstructorInfo) == false)
         return false;

      MBeanConstructorInfo other = (MBeanConstructorInfo) object;

      if (super.equals(other) == false)
         return false;

      MBeanParameterInfo[] thisParams = this.getSignature();
      MBeanParameterInfo[] otherParams = other.getSignature();
      if (thisParams.length != otherParams.length)
         return false;
      for (int i = 0; i < thisParams.length; ++i)
         if (thisParams[i].equals(otherParams[i]) == false)
            return false;

      return true;
   }

   public int hashCode()
   {
      if (cacheHashCode == 0)
      {
         cacheHashCode =  super.hashCode();
      }
      return cacheHashCode;
   }

   /**
    * @return a human readable string
    */
   public String toString()
   {
      if (cacheString == null)
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append(getClass().getName()).append(":");
         buffer.append(" name=").append(getName());
         buffer.append(" description=").append(getDescription());
         buffer.append(" signature=").append(Arrays.asList(signature));
         cacheString = buffer.toString();
      }
      return cacheString;
   }
}
