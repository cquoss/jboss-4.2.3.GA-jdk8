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

import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.Arrays;

import org.jboss.mx.util.MetaDataUtil;

/**
 * Describes an operation exposed by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 *
 * @version $Revision: 57200 $
 */
public class MBeanOperationInfo extends MBeanFeatureInfo
   implements Serializable, Cloneable
{

   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -6178860474881375330L;
   
   /**
    * Management operation impact: INFO. The operation should not alter the
    * state of the MBean component (read operation).
    */
   public static final int INFO        = 0;
   
   /**
    * Management operation impact: ACTION. The operation changes the state
    * of the MBean component (write operation).
    */
   public static final int ACTION      = 1;
   
   /**
    * Management operation impact: ACTION_INFO. Operation behaves like a 
    * read/write operation.
    */
   public static final int ACTION_INFO = 2;
   
   /**
    * Management operation impact: UNKNOWN. Reserved for Standard MBeans.
    */
   public static final int UNKNOWN     = 3;

   
   // Attributes ----------------------------------------------------
   
   /**
    * Impact of this operation.
    */
   private int impact = UNKNOWN;
   
   /**
    * Signature of this operation.
    */
   private MBeanParameterInfo[] signature = null;
   
   /**
    * Return type of this operation as a fully qualified class name.
    */
   private String type = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   
   /**
    * Constructs management operation metadata from a given <tt>Method</tt>
    * object and description.
    *
    * @param   description human readable description of this operation
    * @param   method   used for build the metadata for the management operation
    */
   public MBeanOperationInfo(String description, Method method) throws IllegalArgumentException
   {
      super(method.getName(), description);
      this.type = method.getReturnType().getName();

      Class[] sign = method.getParameterTypes();
      signature = new MBeanParameterInfo[sign.length];

      for (int i = 0; i < sign.length; ++i)
      {
         String name = sign[i].getName();
         signature[i] = new MBeanParameterInfo(name, name, "MBean Operation Parameter.");
      }
   }

   /**
    * Constructs a management operation metadata.
    *
    * @param   name  name of the management operation
    * @param   description human readable description string of the operation
    * @param   inSignature   inSignature of the operation
    * @param   returnType  return type of the operation as a fully qualified class name
    * @param   impact impact of this operation: {@link #ACTION ACTION}, {@link #INFO INFO}, {@link #ACTION_INFO ACTION_INFO} or {@link #UNKNOWN UNKNOWN}
    * @exception IllegalArgumentException if the name or return type is not a valid java identifier or it is a reserved word
    *            or the impact is invalid
    */
   public MBeanOperationInfo(String name, String description,
                             MBeanParameterInfo[] inSignature,
                             String returnType, int impact) throws IllegalArgumentException
   {
      super(name, description);
      if (MetaDataUtil.isValidJavaType(returnType) == false)
         throw new IllegalArgumentException("Return type is not a valid java identifier (or is reserved): " + returnType);
      if (impact != INFO && impact != ACTION && impact != ACTION_INFO && impact != UNKNOWN)
         throw new IllegalArgumentException("Impact is invalid: " + impact);
      
      this.signature = (null == inSignature) ? new MBeanParameterInfo[0] : (MBeanParameterInfo[]) inSignature.clone();
      this.type = returnType;
      this.impact = impact;
   }

   // Public --------------------------------------------------------
   
   /**
    * Returns a fully qualified class name of the return type of this operation.
    *
    * @return  fully qualified class name
    */
   public String getReturnType()
   {
      return type;
   }

   /**
    * Returns the signature of this operation. <b>Note:</b> an operation with a void
    * signature returns a zero-length array, not a <tt>null</tt> reference.
    *
    * @return  operation's signature
    */
   public MBeanParameterInfo[] getSignature()
   {
      return (MBeanParameterInfo[]) signature.clone();
   }

   /**
    * Returns the impact of this operation. The impact is one of the following values:
    * {@link #ACTION ACTION}, {@link #INFO INFO}, {@link #ACTION_INFO ACTION_INFO} or {@link #UNKNOWN UNKNOWN} (reserved for Standard MBeans).
    *
    * @return  operation's impact
    */
   public int getImpact()
   {
      return impact;
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanOperationInfo) == false)
         return false;

      MBeanOperationInfo other = (MBeanOperationInfo) object;

      if (super.equals(other) == false)
         return false;
      if (this.getReturnType().equals(other.getReturnType()) == false)
         return false;
      if (this.getImpact() != other.getImpact())
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
         cacheHashCode += getReturnType().hashCode();
         cacheHashCode += impact;
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
         buffer.append(" returnType=").append(getReturnType());
         buffer.append(" impact=");
         switch (impact)
         {
         case ACTION:      buffer.append("ACTION");      break;
         case ACTION_INFO: buffer.append("ACTION_INFO"); break;
         case INFO:        buffer.append("INFO");        break;
         default:          buffer.append("UNKNOWN");
         }
         cacheString = buffer.toString();
      }
      return cacheString;
   }

   // Cloneable implementation --------------------------------------
   /**
    * Creates a copy of this object. This is a deep copy; the <tt>MBeanParameterInfo</tt> objects
    * forming the operation's signature are also cloned.
    *
    * @return  a clone of this object
    */
   public synchronized Object clone()
   {
      MBeanOperationInfo clone = null;
      try
      {
         clone = (MBeanOperationInfo) super.clone();
         clone.signature = (MBeanParameterInfo[])this.signature.clone();
         clone.type = this.type;
         clone.impact = this.impact;
      }
      catch(CloneNotSupportedException e)
      {
      }

      return clone;
   }
}
