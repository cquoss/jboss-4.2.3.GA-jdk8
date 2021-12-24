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
package org.jboss.web;

import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.security.jacc.WebRoleRefPermission;

import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.WebSecurityMetaData;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.logging.Logger;

/**
 * A utility class encapsulating the logic for building the web container JACC
 * permission from a deployment's metadata.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 65246 $
 */
public class WebPermissionMapping
{
   static Logger log = Logger.getLogger(WebPermissionMapping.class);

   /** An prefix pattern "/prefix/*" */
   private static final int PREFIX = 1;
   /** An extension pattern "*.ext" */
   private static final int EXTENSION = 2;
   /** The "/" default pattern */
   private static final int DEFAULT  = 3;
   /** An prefix pattern "/prefix/*" */   
   private static final int EXACT = 4;
 
   /**
    * Apply the JACC rules for creating permissions from the web.xml
    * security-constraints.
    * 
    * @param metaData - the web deployment web.xml/jboss-web.xml metadata
    * @param pc - the active JACC policy configuration
    * @throws PolicyContextException
    */ 
   public static void createPermissions(WebMetaData metaData, PolicyConfiguration pc)
      throws PolicyContextException
   {
      HashMap patternMap = qualifyURLPatterns(metaData);
      log.debug("Qualified url patterns: "+patternMap);

      Iterator constraints = metaData.getSecurityContraints();
      while( constraints.hasNext() )
      {
         WebSecurityMetaData wsmd = (WebSecurityMetaData) constraints.next();
         String transport = wsmd.getTransportGuarantee();
         if( wsmd.isExcluded() || wsmd.isUnchecked() )
         {
            // Process the permissions for the excluded/unchecked resources
            Iterator resources = wsmd.getWebResources().values().iterator();
            while( resources.hasNext() )
            {
               WebSecurityMetaData.WebResourceCollection wrc = (WebSecurityMetaData.WebResourceCollection) resources.next();
               String[] httpMethods = wrc.getHttpMethods();
               String[] urlPatterns = wrc.getUrlPatterns();
               for(int n = 0; n < urlPatterns.length; n ++)
               {
                  String url = urlPatterns[n];
                  PatternInfo info = (PatternInfo) patternMap.get(url);
                  // Add the excluded methods
                  if( wsmd.isExcluded() )
                  {
                     info.addExcludedMethods(httpMethods);
                  }
               }
            }
         }
         else
         {
            // Process the permission for the resources x roles
            Iterator resources = wsmd.getWebResources().values().iterator();
            while( resources.hasNext() )
            {
               WebSecurityMetaData.WebResourceCollection wrc = (WebSecurityMetaData.WebResourceCollection) resources.next();
               String[] httpMethods = wrc.getHttpMethods();
               String[] urlPatterns = wrc.getUrlPatterns();
               for(int n = 0; n < urlPatterns.length; n ++)
               {
                  String url = urlPatterns[n];
                  // Get the qualified url pattern
                  PatternInfo info = (PatternInfo) patternMap.get(url);
                  Iterator roles = wsmd.getRoles().iterator();
                  HashSet mappedRoles = new HashSet();
                  while( roles.hasNext() )
                  {
                     String role = (String) roles.next();
                     if( role.equals("*") )
                     {
                    	//JBAS-1824: Allow "*" to provide configurable authorization bypass
                    	if(metaData.isJaccRoleNameStar())
                    	   mappedRoles.add("*");
                    	else
                    	{
                            //The wildcard ref maps to all declared security-role names
                            Iterator allRoles = metaData.getSecurityRoleNames().iterator();
                            while( allRoles.hasNext() )
                            {
                               role = (String) allRoles.next();
                               mappedRoles.add(role);
                            }	
                    	} 
                     }
                     else
                     {
                        mappedRoles.add(role);
                     }
                  }
                  info.addRoles(mappedRoles, httpMethods);
                  // Add the transport to methods
                  info.addTransport(transport, httpMethods);
               }
            }
         }
      }

      // Create the permissions
      Iterator iter = patternMap.values().iterator();
      while( iter.hasNext() )
      {
         PatternInfo info = (PatternInfo) iter.next();
         String qurl = info.getQualifiedPattern();
         if( info.isOverriden == true )
         {
            log.debug("Dropping overriden pattern: "+info);
            continue;
         }

         // Create the excluded permissions
         String[] httpMethods = info.getExcludedMethods();
         if( httpMethods != null )
         {
            // There were excluded security-constraints
            WebResourcePermission wrp = new WebResourcePermission(qurl, httpMethods);
            WebUserDataPermission wudp = new WebUserDataPermission(qurl,
               httpMethods, null);
            pc.addToExcludedPolicy(wrp);
            pc.addToExcludedPolicy(wudp);
         }

         // Create the role permissions
         Iterator roles = info.getRoleMethods();
         while( roles.hasNext() )
         {
            Map.Entry roleMethods = (Map.Entry) roles.next();
            String role = (String) roleMethods.getKey();
            WebResourcePermission wrp = null;
            if("*".equals(role))
            {
               //JBAS-1824: <role-name>*</role-name>	
               wrp = new WebResourcePermission(qurl, (String)null);
            }
            else
            {
               HashSet methods = (HashSet) roleMethods.getValue();
               httpMethods = new String[methods.size()];
               methods.toArray(httpMethods);
               wrp = new WebResourcePermission(qurl, httpMethods);	
            } 
            pc.addToRole(role, wrp);
         }

         // Create the unchecked permissions
         String[] missingHttpMethods = info.getMissingMethods();
         if( missingHttpMethods.length > 0 )
         {
            // Create the unchecked permissions WebResourcePermissions
            WebResourcePermission wrp = new WebResourcePermission(qurl, missingHttpMethods);
            pc.addToUncheckedPolicy(wrp);

         }

         // Create the unchecked permissions WebUserDataPermissions
         Iterator transportContraints = info.getTransportMethods();
         while( transportContraints.hasNext())
         {
            Map.Entry transportMethods = (Map.Entry) transportContraints.next();
            String transport = (String) transportMethods.getKey();
            Set methods = (Set) transportMethods.getValue();
            httpMethods = new String[methods.size()];
            methods.toArray(httpMethods);
          
            /**
             * If you have any excluding auth-constraint, then add an unchecked
             * permission for the non-excluded methods
             */
            if(info.getExcludedMethods() != null)
            {
               pc.addToUncheckedPolicy(new WebUserDataPermission(qurl, info.getMissingMethods(), null ));
               continue;
            }  
            
            // Dealing with non-excluded methods now
            String[] allMethods = WebSecurityMetaData.ALL_HTTP_METHOD_NAMES;
            boolean ALL_METHODS_FLAG = Arrays.equals(allMethods, httpMethods);
            WebUserDataPermission wudp = new WebUserDataPermission(qurl, httpMethods, transport);
            pc.addToUncheckedPolicy(wudp);

            //Create an unchecked permission for the missing methods
            String[] missingMethods = info.getMissingMethods();
            if(!Arrays.equals(allMethods,missingMethods))
               pc.addToUncheckedPolicy(new WebUserDataPermission(qurl,missingMethods, null )); 

            /**
             * A WebResourcePermission and a WebUserDataPermission must be added to the unchecked 
             * policy statements for each url-pattern in the DD and the default pattern, "/", 
             * that is not combined by the webresource-collection elements of the deployment descriptor 
             * with every HTTP method value. (JACC 1.0: Section 3.1.3.1)
             */
            //Add a null permission if not already added
            boolean unsecureTransport = transport == null || "NONE".equals(transport);
            if(!ALL_METHODS_FLAG  && unsecureTransport )
               pc.addToUncheckedPolicy(new WebUserDataPermission(qurl, null, null ));
         }
      }

      /* Create WebRoleRefPermissions for all servlet/security-role-refs along
      with all the cross product of servlets and security-role elements that
      are not referenced via a security-role-ref as described in JACC section
      3.1.3.2
      */
      Set unreferencedRoles = metaData.getSecurityRoleNames();
      Map servletRoleRefs = metaData.getSecurityRoleRefs();
      Iterator roleRefsIter = servletRoleRefs.keySet().iterator();
      while( roleRefsIter.hasNext() )
      {
         String servletName = (String) roleRefsIter.next();
         ArrayList roleRefs = (ArrayList) servletRoleRefs.get(servletName);
         for(int n = 0; n < roleRefs.size(); n ++)
         {
            SecurityRoleRefMetaData roleRef = (SecurityRoleRefMetaData) roleRefs.get(n);
            String roleName = roleRef.getLink();
            WebRoleRefPermission wrrp = new WebRoleRefPermission(servletName, roleRef.getName());
            pc.addToRole(roleName, wrrp);
            /* A bit of a hack due to how tomcat calls out to its Realm.hasRole()
            with a role name that has been mapped to the role-link value. We
            may need to handle this with a custom request wrapper.
            */
            wrrp = new WebRoleRefPermission(servletName, roleName);
            pc.addToRole(roleRef.getName(), wrrp); 
            // Remove the role from the unreferencedRoles
            unreferencedRoles.remove(roleName);
         }
      } 
      
      // Now build the cross product of the unreferencedRoles and servlets
      Set servletNames = metaData.getServletNames();
      Iterator names = servletNames.iterator();
      while( names.hasNext() )
      {
         String servletName = (String) names.next();
         Iterator roles = unreferencedRoles.iterator();
         while( roles.hasNext() )
         {
            String role = (String) roles.next();
            WebRoleRefPermission wrrp = new WebRoleRefPermission(servletName, role);
            pc.addToRole(role, wrrp);  
         }
      }
      /**
       * The programmatic security checks are made from jsps.
       * JBAS-3054:Use of isCallerInRole from jsp does not work for JACC
       */
      Iterator roles = unreferencedRoles.iterator();
      while( roles.hasNext() )
      {
         String role = (String) roles.next();
         WebRoleRefPermission wrrp = new WebRoleRefPermission("", role);
         pc.addToRole(role, wrrp);  
      } 
   }

   /**
    * Determine the url-pattern type
    * @param urlPattern - the raw url-pattern value
    * @return one of EXACT, EXTENSION, PREFIX, DEFAULT
    */ 
   static int getPatternType(String urlPattern)
   {
      int type = EXACT;
      if( urlPattern.startsWith("*.") )
         type = EXTENSION;
      else if( urlPattern.startsWith("/") && urlPattern.endsWith("/*") ) 
         type = PREFIX;
      else if( urlPattern.equals("/") )
         type = DEFAULT;
      return type;
   }

   /**
    JACC url pattern Qualified URL Pattern Names.

    The rules for qualifying a URL pattern are dependent on the rules for
    determining if one URL pattern matches another as defined in Section 3.1.3.3,
    Servlet URL-Pattern Matching Rules, and are described as follows:
    - If the pattern is a path prefix pattern, it must be qualified by every
    path-prefix pattern in the deployment descriptor matched by and different from
    the pattern being qualified. The pattern must also be qualified by every exact
    pattern appearing in the deployment descriptor that is matched by the pattern
    being qualified.
    - If the pattern is an extension pattern, it must be qualified by every
    path-prefix pattern appearing in the deployment descriptor and every exact
    pattern in the deployment descriptor that is matched by the pattern being
    qualified.
    - If the pattern is the default pattern, "/", it must be qualified by every
    other pattern except the default pattern appearing in the deployment descriptor.
    - If the pattern is an exact pattern, its qualified form must not contain any
    qualifying patterns.

    URL patterns are qualified by appending to their String representation, a
    colon separated representation of the list of patterns that qualify the pattern.
    Duplicates must not be included in the list of qualifying patterns, and any
    qualifying pattern matched by another qualifying pattern may5 be dropped from
    the list.

    Any pattern, qualified by a pattern that matches it, is overridden and made
    irrelevant (in the translation) by the qualifying pattern. Specifically, all
    extension patterns and the default pattern are made irrelevant by the presence
    of the path prefix pattern "/*" in a deployment descriptor. Patterns qualified
    by the "/*" pattern violate the URLPatternSpec constraints of
    WebResourcePermission and WebUserDataPermission names and must be rejected by
    the corresponding permission constructors.

    @param metaData - the web deployment metadata
    @return HashMap<String, PatternInfo> 
    */ 
   static HashMap qualifyURLPatterns(WebMetaData metaData)
   {
      ArrayList prefixList = new ArrayList();
      ArrayList extensionList = new ArrayList();
      ArrayList exactList = new ArrayList();
      HashMap patternMap = new HashMap();
      PatternInfo defaultInfo = null;

      Iterator constraints = metaData.getSecurityContraints();
      while( constraints.hasNext() )
      {
         WebSecurityMetaData wsmd = (WebSecurityMetaData) constraints.next();
         Iterator resources = wsmd.getWebResources().values().iterator();
         while( resources.hasNext() )
         {
            WebSecurityMetaData.WebResourceCollection wrc = (WebSecurityMetaData.WebResourceCollection) resources.next();
            String[] urlPatterns = wrc.getUrlPatterns();
            for(int n = 0; n < urlPatterns.length; n ++)
            {
               String url = urlPatterns[n];
               int type = getPatternType(url);
               PatternInfo info = (PatternInfo) patternMap.get(url);
               if( info == null )
               {
                  info = new PatternInfo(url, type);
                  patternMap.put(url, info);
                  switch( type )
                  {
                     case PREFIX:
                        prefixList.add(info);
                     break;
                     case EXTENSION:
                        extensionList.add(info);
                     break;
                     case EXACT:
                        exactList.add(info);
                     break;
                     case DEFAULT:
                        defaultInfo = info;
                     break;
                  }
               }
            }
         }
      }

      // Qualify all prefix patterns
      for(int i = 0; i < prefixList.size(); i ++)
      {
         PatternInfo info = (PatternInfo) prefixList.get(i);
         // Qualify by every other prefix pattern matching this pattern
         for(int j = 0; j < prefixList.size(); j ++)
         {
            if( i == j )
               continue;
            PatternInfo other = (PatternInfo) prefixList.get(j);
            if( info.matches(other) )
               info.addQualifier(other);
         }
         // Qualify by every exact pattern that is matched by this pattern
         for(int j = 0; j < exactList.size(); j ++)
         {
            PatternInfo other = (PatternInfo) exactList.get(j);
            if( info.matches(other) )
               info.addQualifier(other);            
         }
      }

      // Qualify all extension patterns
      for(int i = 0; i < extensionList.size(); i ++)
      {
         PatternInfo info = (PatternInfo) extensionList.get(i);
         // Qualify by every path prefix pattern
         for(int j = 0; j < prefixList.size(); j ++)
         {
            PatternInfo other = (PatternInfo) prefixList.get(j);
            {
               // Any extension 
               info.addQualifier(other);
            }
         }
         // Qualify by every matching exact pattern
         for(int j = 0; j < exactList.size(); j ++)
         {
            PatternInfo other = (PatternInfo) exactList.get(j);
            if( info.isExtensionFor(other) )
               info.addQualifier(other);            
         }
      }

      // Qualify the default pattern
      if( defaultInfo == null )
      {
         defaultInfo = new PatternInfo("/", DEFAULT);
         patternMap.put("/", defaultInfo);
      }
      Iterator iter = patternMap.values().iterator();
      while( iter.hasNext() )
      {
         PatternInfo info = (PatternInfo) iter.next();
         if( info == defaultInfo )
            continue;
         defaultInfo.addQualifier(info);
      }

      return patternMap;
   }

   /**
    * A representation of all security-constraint mappings for a unique
    * url-pattern
    */ 
   static class PatternInfo
   {
      static final HashMap ALL_TRANSPORTS = new HashMap();
      static
      {
         ALL_TRANSPORTS.put("NONE", WebSecurityMetaData.ALL_HTTP_METHODS);
      }

      /** The raw url-pattern string from the web.xml */
      String pattern;
      /** The qualified url pattern as determined by qualifyURLPatterns */
      String qpattern;
      /** The list of qualifying patterns as determined by qualifyURLPatterns */
      ArrayList qualifiers = new ArrayList();
      /** One of PREFIX, EXTENSION, DEFAULT, EXACT */
      int type;
      /** HashSet<String> Union of all http methods seen in excluded statements */
      HashSet excludedMethods;
      /** HashMap<String, HashSet<String>> role to http methods */
      HashMap roles;
      /** HashMap<String, HashSet<String>> transport to http methods */
      HashMap transports;
      // The url pattern to http methods for patterns for 
      HashSet allMethods = new HashSet();
      /** Does a qualifying pattern match this pattern and make this pattern
       * obsolete?
       */
      boolean isOverriden;

      /**
       * @param pattern - the url-pattern value
       * @param type - one of EXACT, EXTENSION, PREFIX, DEFAULT
       */ 
      PatternInfo(String pattern, int type)
      {
         this.pattern = pattern;
         this.type = type;
      }

      /**
       * Augment the excluded methods associated with this url 
       * @param httpMethods
       */ 
      void addExcludedMethods(String[] httpMethods)
      {
         Collection methods = Arrays.asList(httpMethods);
         if( methods.size() == 0 )
            methods = WebSecurityMetaData.ALL_HTTP_METHODS;
         if( excludedMethods == null )
            excludedMethods = new HashSet();
         excludedMethods.addAll(methods);
         allMethods.addAll(methods);
      }
      /**
       * Get the list of excluded http methods
       * @return excluded http methods if the exist, null if there were no
       *    excluded security constraints
       */ 
      public String[] getExcludedMethods()
      {
         String[] httpMethods = null;
         if( excludedMethods != null )
         {
            httpMethods = new String[excludedMethods.size()];
            excludedMethods.toArray(httpMethods);
         }
         return httpMethods;
      }

      /**
       * Update the role to http methods mapping for this url.
       * @param mappedRoles - the role-name values for the auth-constraint
       * @param httpMethods - the http-method values for the web-resource-collection
       */ 
      public void addRoles(HashSet mappedRoles, String[] httpMethods)
      {
         Collection methods = Arrays.asList(httpMethods);
         if( methods.size() == 0 )
            methods = WebSecurityMetaData.ALL_HTTP_METHODS;
         allMethods.addAll(methods);
         if( roles == null )
            roles = new HashMap();

         Iterator iter = mappedRoles.iterator();
         while( iter.hasNext() )
         {
            String role = (String) iter.next();
            HashSet roleMethods = (HashSet) roles.get(role);
            if( roleMethods == null )
            {
               roleMethods = new HashSet();
               roles.put(role, roleMethods);
            }
            roleMethods.addAll(methods);
         }
      }
      /**
       * Get the role to http method mappings
       * @return Iterator<Map.Entry<String, HasSet<String>>> for the role
       *    to http method mappings.
       */ 
      public Iterator getRoleMethods()
      {
         HashMap tmp = roles;
         if( tmp == null )
            tmp = new HashMap(0);
         Iterator iter = tmp.entrySet().iterator();
         return iter;
      }

      /**
       * Update the role to http methods mapping for this url.
       * @param transport - the transport-guarantee value
       * @param httpMethods - the http-method values for the web-resource-collection
       */ 
      void addTransport(String transport, String[] httpMethods)
      {
         Collection methods = Arrays.asList(httpMethods);
         if( methods.size() == 0 )
            methods = WebSecurityMetaData.ALL_HTTP_METHODS;
         if( transports == null )
            transports = new HashMap();

         HashSet transportMethods = (HashSet) transports.get(transport);
         if( transportMethods == null )
         {
            transportMethods = new HashSet();
            transports.put(transport, transportMethods);
         }
         transportMethods.addAll(methods);
      }
      /**
       * Get the transport to http method mappings
       * @return Iterator<Map.Entry<String, HasSet<String>>> for the transport
       *    to http method mappings.
       */ 
      public Iterator getTransportMethods()
      {
         HashMap tmp = transports;
         if( tmp == null )
            tmp = ALL_TRANSPORTS;
         Iterator iter = tmp.entrySet().iterator();
         return iter;         
      }

      /**
       * Get the list of http methods that were not associated with an excluded
       * or role based mapping of this url.
       * 
       * @return the subset of http methods that should be unchecked
       */ 
      public String[] getMissingMethods()
      {
         String[] httpMethods = {};
         if( allMethods.size() == 0 )
         {
            // There were no excluded or role based security-constraints
            httpMethods = WebSecurityMetaData.ALL_HTTP_METHOD_NAMES;
         }
         else
         {
            httpMethods = WebSecurityMetaData.getMissingHttpMethods(allMethods);
         }
         return httpMethods;
      }

      /**
       * Add the qualifying pattern. If info is a prefix pattern that matches
       * this pattern, it overrides this pattern and will exclude it from
       * inclusion in the policy.
       * 
       * @param info - a url pattern that should qualify this pattern
       */ 
      void addQualifier(PatternInfo info)
      {
         if( qualifiers.contains(info) == false )
         {
            // See if this pattern is matched by the qualifier
            if( info.type == PREFIX && info.matches(this) )
               isOverriden = true;
            qualifiers.add(info);
         }
      }

      /**
       * Get the url pattern with its qualifications
       * @see WebPermissionMapping#qualifyURLPatterns(org.jboss.metadata.WebMetaData)
       * @return the qualified form of the url pattern
       */ 
      public String getQualifiedPattern()
      {
         if( qpattern == null )
         {
            StringBuffer tmp = new StringBuffer(pattern);
            for(int n = 0; n < qualifiers.size(); n ++)
            {
               tmp.append(':');
               PatternInfo info = (PatternInfo) qualifiers.get(n);
               tmp.append(info.pattern);
            }
            qpattern = tmp.toString();
         }
         return qpattern;
      }

      public int hashCode()
      {
         return pattern.hashCode();
      }

      public boolean equals(Object obj)
      {
         PatternInfo pi = (PatternInfo) obj;
         return pattern.equals(pi.pattern);
      }

      /**
       * See if this pattern is matches the other pattern
       * @param other - another pattern
       * @return true if the other pattern starts with this
       *    pattern less the "/*", false otherwise
       */ 
      public boolean matches(PatternInfo other)
      {
         int matchLength = pattern.length()-2;
         boolean matches = pattern.regionMatches(0, other.pattern, 0, matchLength);
         return matches;
      }

      /**
       * See if this is an extension pattern that matches other
       * @param other - another pattern
       * @return true if is an extension pattern and other ends with this
       *    pattern
       */ 
      public boolean isExtensionFor(PatternInfo other)
      {
         int offset = other.pattern.lastIndexOf('.');
         int length = pattern.length() - 1;
         boolean isExtensionFor = false;
         if( offset > 0 )
         {
            isExtensionFor = pattern.regionMatches(1, other.pattern, offset, length);
         }
         return isExtensionFor;
      }

      public String toString()
      {
         StringBuffer tmp = new StringBuffer("PatternInfo[");
         tmp.append("pattern=");
         tmp.append(pattern);
         tmp.append(",type=");
         tmp.append(type);
         tmp.append(",isOverriden=");
         tmp.append(isOverriden);
         tmp.append(",qualifiers=");
         tmp.append(qualifiers);
         tmp.append("]");
         return tmp.toString();
      }

   }
}
