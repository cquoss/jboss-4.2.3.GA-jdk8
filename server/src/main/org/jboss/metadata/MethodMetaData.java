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
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.invocation.InvocationType;

/** The combination of the method-permission, container-transaction
 *
 * <p>
 * The method-permission element specifies that one or more security
 * roles are allowed to invoke one or more enterprise bean methods. The
 * method-permission element consists of an optional description, a list
 * of security role names, or an indicator to specify that the methods
 * are not to be checked for authorization, and a list of method elements.
 * The security roles used in the method-permission element must be
 * defined in the security-role element of the deployment descriptor,
 * and the methods must be methods defined in the enterprise bean?s component
 * and/or home interfaces.
 * </p>
 * <p>
 * The container-transaction element specifies how the container must
 * manage transaction scopes for the enterprise bean?s method invocations.
 * The element consists of an optional description, a list of
 * method elements, and a transaction attribute. The transaction
 * attribute is to be applied to all the specified methods.
 * </p>
 *
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 *   @version $Revision: 57209 $
 */
public class MethodMetaData extends MetaData
{
   // Constants -----------------------------------------------------
   // These method interface contstants are compatible with the Invocation.XXX values
   public static final int ANY_METHOD = -1;
   public static String HOME_TYPE = "Home";
   public static String LOCAL_HOME_TYPE = "LocalHome";
   public static String REMOTE_TYPE = "Remote";
   public static String LOCAL_TYPE = "Local";
   public static String SERVICE_ENDPOINT_TYPE = "ServiceEndpoint";

   private static final ArrayList EMPTY_PARAM_LIST = new ArrayList();

   // Attributes ----------------------------------------------------
   /** The method-name element contains a name of an enterprise bean method,
    * or the asterisk (*) character. The asterisk is used when the element
    * denotes all the methods of an enterprise bean?s component and home
    * interfaces.
    */
   private String methodName;
   /** The ejb-name value the method applies to.
    */
   private String ejbName;

   /** The method-intf element allows a method element to differentiate
    * between the methods with the same name and signature that are multiply
    * defined across the home and component interfaces (e.g., in both an
    * enterprise bean?s remote and local interfaces, or in both an enter-prise
    * bean?s home and remote interfaces, etc.)
    * The method-intf element must be one of the following:
    * <method-intf>Home</method-intf>
    * <method-intf>Remote</method-intf>
    * <method-intf>LocalHome</method-intf>
    * <method-intf>Local</method-intf>
    * <method-intf>ServiceEndpoint</method-intf>
    */
   private boolean intf = false;
   /** One of: InvocationType
    */
   private InvocationType methodType = null;
   private boolean param = false;
   /** The unchecked element specifies that a method is not checked for
    * authorization by the container prior to invocation of the method.
    * Used in: method-permission
    */
   private boolean unchecked = false;
   /** The exclude-list element defines a set of methods which the Assembler
    * marks to be uncallable. It contains one or more methods. If the method
    * permission relation contains methods that are in the exclude list, the
    * Deployer should consider those methods to be uncallable.
    */
   private boolean excluded = false;
   /** The method-params element contains a list of the fully-qualified Java
    * type names of the method parameters.
    */
   private ArrayList paramList = EMPTY_PARAM_LIST;
   /** The trans-attribute element specifies how the container must manage
    * the transaction boundaries when delegating a method invocation to an
    * enterprise bean?s business method.
    * The value of trans-attribute must be one of the following:
    * <trans-attribute>NotSupported</trans-attribute>
    * <trans-attribute>Supports</trans-attribute>
    * <trans-attribute>Required</trans-attribute>
    * <trans-attribute>RequiresNew</trans-attribute>
    * <trans-attribute>Mandatory</trans-attribute>
    * <trans-attribute>Never</trans-attribute>
    */
   private byte transactionType;
   /** Set<String> of the allowed role names */
   private Set roles = new HashSet();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public MethodMetaData()
   {
   }
   
   // Public --------------------------------------------------------
   
   public String getMethodName()
   {
      return methodName;
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public boolean isHomeMethod()
   {
      return methodType == InvocationType.HOME;
   }

   public boolean isRemoteMethod()
   {
      return methodType == InvocationType.REMOTE;
   }

   public boolean isLocalHomeMethod()
   {
      return methodType == InvocationType.LOCALHOME;
   }

   public boolean isLocalMethod()
   {
      return methodType == InvocationType.LOCAL;
   }

   public boolean isServiceEndpointMethod()
   {
      return methodType == InvocationType.SERVICE_ENDPOINT;
   }

   /** Return the interface type name.
    * 
    * @return one of "Home", "LocalHome", "Remote", "Local", "ServiceEndpoint",
    *    or null if no interface was specified.
    */ 
   public String getInterfaceType()
   {
      String type = null;
      if( isHomeMethod() )
         type = HOME_TYPE;
      if( isLocalHomeMethod() )
         type = LOCAL_HOME_TYPE;
      if( isRemoteMethod() )
         type = REMOTE_TYPE;
      if( isLocalMethod() )
         type = LOCAL_TYPE;
      if( isServiceEndpointMethod() )
         type = SERVICE_ENDPOINT_TYPE;
      return type;
   }

   public boolean isUnchecked()
   {
      return unchecked;
   }

   public boolean isExcluded()
   {
      return excluded;
   }

   public boolean isIntfGiven()
   {
      return intf;
   }

   public boolean isParamGiven()
   {
      return param;
   }

   /** The method param type names.
    * @return
    */ 
   public Iterator getParams()
   {
      return paramList.iterator();
   }
   /** The 
    * 
    * @return An array of the method parameter type names
    */ 
   public String[] getMethodParams()
   {
      String[] params = new String[paramList.size()];
      paramList.toArray(params);
      return params;
   }

   public byte getTransactionType()
   {
      return transactionType;
   }

   public void setTransactionType(byte type)
   {
      transactionType = type;
   }

   public Set getRoles()
   {
      return roles;
   }

   public void setRoles(Set perm)
   {
      roles = perm;
   }

   public void setUnchecked()
   {
      unchecked = true;
   }

   public void setExcluded()
   {
      excluded = true;
   }

   public boolean patternMatches(String name, Class[] arg, InvocationType iface)
   {
      return patternMatches(name, getClassNames(arg), iface);
   }

   public boolean patternMatches(String name, String[] arg, InvocationType iface)
   {
      // the wildcard matches everything
      if (getMethodName().equals("*"))
      {
         if (methodType != null && methodType != iface)
            return false;
         return true;
      }

      if (getMethodName().equals(name) == false)
      {
         // different names -> no
         return false;
      }
      else
      {
         // we have the same name, next check the interface type
         if (methodType != null && methodType != iface)
            return false;

         if (isParamGiven() == false)
         {
            // no param given in descriptor -> ok
            return true;
         }
         else
         {
            // we *have* to check the parameters
            return sameParams(arg);
         }
      }
   }

   /**
    * @param a method element
    */
   public void importEjbJarXml(Element element) throws DeploymentException
   {
      methodName = getElementContent(getUniqueChild(element, "method-name"));
      ejbName = getElementContent(getUniqueChild(element, "ejb-name"));

      Element intfElement = getOptionalChild(element, "method-intf");
      if (intfElement != null)
      {
         intf = true;
         String methodIntf = getElementContent(intfElement);
         if (methodIntf.equals("Home"))
         {
            methodType = InvocationType.HOME;
         }
         else if (methodIntf.equals("Remote"))
         {
            methodType = InvocationType.REMOTE;
         }
         else if (methodIntf.equals("LocalHome"))
         {
            methodType = InvocationType.LOCALHOME;
         }
         else if (methodIntf.equals("Local"))
         {
            methodType = InvocationType.LOCAL;
         }
         else if (methodIntf.equals("ServiceEndpoint"))
         {
            methodType = InvocationType.SERVICE_ENDPOINT;
         }
         else
         {
            throw new DeploymentException("method-intf tag should be one of: 'Home', 'Remote', 'LocalHome', 'Local', 'ServiceEndpoint'");
         }
      }

      Element paramsElement = getOptionalChild(element, "method-params");
      if (paramsElement != null)
      {
         param = true;
         paramList = new ArrayList();
         Iterator paramsIterator = getChildrenByTagName(paramsElement, "method-param");
         while (paramsIterator.hasNext())
         {
            paramList.add(getElementContent((Element) paramsIterator.next()));
         }
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private static String[] getClassNames(Class[] source)
   {
      String out[] = new String[source.length];
      for (int i = 0; i < out.length; i++)
      {
         String brackets = "";
         Class cls = source[i];
         while (cls.isArray())
         {
            brackets += "[]";
            cls = cls.getComponentType();
         }
         out[i] = cls.getName() + brackets;
      }
      return out;
   }

   private boolean sameParams(String[] arg)
   {
      if (arg.length != paramList.size()) return false;
      for (int i = 0; i < arg.length; i++)
         if (!arg[i].equals(paramList.get(i)))
            return false;
      return true;
   }

   // Inner classes -------------------------------------------------
}
