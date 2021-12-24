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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/** Encapsulation of the web.xml security-constraints
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class WebSecurityMetaData
{
   /** The set of all http methods: DELETE, GET, HEAD, OPTIONS, POST, PUT, TRACE */
   public static final Set ALL_HTTP_METHODS;
   public static final String[] ALL_HTTP_METHOD_NAMES;

   static
   {
      TreeSet tmp = new TreeSet();
      tmp.add("GET");
      tmp.add("POST");
      tmp.add("PUT");
      tmp.add("DELETE");
      tmp.add("HEAD");
      tmp.add("OPTIONS");
      tmp.add("TRACE");
      ALL_HTTP_METHODS = Collections.unmodifiableSortedSet(tmp);
      ALL_HTTP_METHOD_NAMES = new String[ALL_HTTP_METHODS.size()];
      ALL_HTTP_METHODS.toArray(ALL_HTTP_METHOD_NAMES);
   }

   /** The HashMap<String, WebResourceCollection> for the
    * security-constraint/web-resource-collection elements
    */ 
   private HashMap webResources = new HashMap();
   /** Set<String> of the allowed role names defined by the
    * security-constraint/auth-constraint elements
    */
   private Set roles = new HashSet();

   /** The optional security-constraint/user-data-constraint/transport-guarantee */
   private String transportGuarantee;
   /** The unchecked flag is set when there is no security-constraint/auth-constraint
    */
   private boolean unchecked = false;
   /** The excluded flag is set when there is an empty
    security-constraint/auth-constraint element
    */
   private boolean excluded = false;

   public static String[] getMissingHttpMethods(HashSet httpMethods)
   {
      String[] methods = {};
      if( httpMethods.size() > 0 && httpMethods.containsAll(ALL_HTTP_METHODS) == false )
      {
         HashSet missingMethods = new HashSet(ALL_HTTP_METHODS);
         missingMethods.removeAll(httpMethods);
         methods = new String[missingMethods.size()];
         missingMethods.toArray(methods);
      }
      return methods;         
   }

   public WebResourceCollection addWebResource(String name)
   {
      WebResourceCollection webrc = new WebResourceCollection(name);
      if( webResources.containsKey(name) == true )
      {
         // A non-unique name, unique it
         name = name + '@' + System.identityHashCode(webrc);
      }
      webResources.put(name, webrc);
      return webrc;
   }
   public HashMap getWebResources()
   {
      return webResources;
   }

   public void addRole(String name)
   {
      roles.add(name);
   }
   /** Get the security-constraint/auth-constraint values. An empty role
    * set must be qualified by the isUnchecked and isExcluded methods.
    * 
    * @return Set<String> for the role names
    */ 
   public Set getRoles()
   {
      return roles;
   }
   
   /** Get the security-constraint/transport-guarantee setting
    @return null == no guarantees
      INTEGRAL == an integretity guarantee
      CONFIDENTIAL == protected for confidentiality
    */ 
   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }
   public void setTransportGuarantee(String transportGuarantee)
   {
      this.transportGuarantee = transportGuarantee;
   }

   public boolean isUnchecked()
   {
      return unchecked;
   }
   public void setUnchecked(boolean flag)
   {
      this.unchecked = flag;
   }

   public boolean isExcluded()
   {
      return excluded;
   }
   public void setExcluded(boolean flag)
   {
      this.excluded = flag;
   }

   /** The security-constraint/web-resource-collection child element container
    * 
    */
   public static class WebResourceCollection
   {
      /** The required web-resource-name element */
      private String name;
      /** The required url-pattern element(s) */
      private HashSet urlPatterns = new HashSet();
      /** The optional http-method element(s) */
      private ArrayList httpMethods = new ArrayList();

      public WebResourceCollection(String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }
      public void addPattern(String pattern)
      {
         urlPatterns.add(pattern);
      }
      /** Get the url-patterns specified in the resource collection. 
       * @return
       */ 
      public String[] getUrlPatterns()
      {
         String[] patterns = {};
         patterns = new String[urlPatterns.size()];
         urlPatterns.toArray(patterns);
         return patterns;
      }

      public void addHttpMethod(String method)
      {
         httpMethods.add(method);
      }
      /** The optional security-constraint/web-resource-collection/http-method
       @return empty for all methods, a subset of GET, POST, PUT, DELETE,
               HEAD, OPTIONS, TRACE otherwise
       */ 
      public String[] getHttpMethods()
      {
         String[] methods = {};
         if( httpMethods.containsAll(ALL_HTTP_METHODS) == false )
         {
            methods = new String[httpMethods.size()];
            httpMethods.toArray(methods);
         }
         return methods;
      }
      /** Return the http methods that were not specified in the collection.
       If there were a subset of the ALL_HTTP_METHODS given, then this
       method returns the ALL_HTTP_METHODS - the subset. If no or all
       ALL_HTTP_METHODS were specified this return an empty array.
       @return empty for all methods, a subset of GET, POST, PUT, DELETE,
               HEAD, OPTIONS, TRACE otherwise
       */ 
      public String[] getMissingHttpMethods()
      {
         String[] methods = {};
         if( httpMethods.size() > 0 && httpMethods.containsAll(ALL_HTTP_METHODS) == false )
         {
            HashSet missingMethods = new HashSet(ALL_HTTP_METHODS);
            missingMethods.removeAll(httpMethods);
            methods = new String[missingMethods.size()];
            missingMethods.toArray(methods);
         }
         return methods;         
      }
   }
}
