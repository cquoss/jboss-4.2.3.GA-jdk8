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
package org.jboss.security.auth.spi;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.directory.Attribute;
import javax.naming.ldap.InitialLdapContext;
import javax.security.auth.login.LoginException;
import javax.management.ObjectName;

import org.jboss.security.SimpleGroup;

/**
 The org.jboss.security.auth.spi.LdapExtLoginModule, added in jboss-4.0.3, is an
 alternate ldap login module implementation that uses searches for locating both
 the user to bind as for authentication as well as the associated roles. The
 roles query will recursively follow distinguished names (DNs) to navigate a
 hierarchical role structure.

 The LoginModule options include whatever options your LDAP JNDI provider
 supports. Examples of standard property names are:

 * Context.INITIAL_CONTEXT_FACTORY = "java.naming.factory.initial"
 * Context.SECURITY_PROTOCOL = "java.naming.security.protocol"
 * Context.PROVIDER_URL = "java.naming.provider.url"
 * Context.SECURITY_AUTHENTICATION = "java.naming.security.authentication"

 The authentication happens in 2 steps:
 # An initial bind to the ldap server is done using the __bindDN__ and
 __bindCredential__ options. The __bindDN__ is some user with the ability to
 search both the __baseDN__ and __rolesCtxDN__ trees for the user and roles. The
 user DN to authenticate against is queried using the filter specified by the
 __baseFilter__ attribute (see the __baseFilter__ option description for its
 syntax). 
 # The resulting user DN is then authenticated by binding to ldap server using
 the user DN as the InitialLdapContext environment Context.SECURITY_PRINCIPAL.

 The Context.SECURITY_CREDENTIALS property is either set to the String password
 obtained by the callback handler.

 If this is successful, the associated user roles are queried using the
 __rolesCtxDN__, __roleAttributeID__, __roleAttributeIsDN__,
 __roleNameAttributeID__, and __roleFilter__ options.

 The full odule properties include:
 * __baseCtxDN__ : The fixed DN of the context to start the user search from.
 * __bindDN__ : The DN used to bind against the ldap server for the user and
 roles queries. This is some DN with read/search permissions on the baseCtxDN and
 rolesCtxDN values.
 * __bindCredential__ : The password for the bindDN. This can be encrypted if the
 jaasSecurityDomain is specified.
 * __jaasSecurityDomain__ : The JMX ObjectName of the JaasSecurityDomain to use
 to decrypt the java.naming.security.principal. The encrypted form of the
 password is that returned by the JaasSecurityDomain#encrypt64(byte[]) method.
 The org.jboss.security.plugins.PBEUtils can also be used to generate the
 encrypted form.
 * __baseFilter__ : A search filter used to locate the context of the user to
 authenticate. The input username/userDN as obtained from the login module
 callback will be substituted into the filter anywhere a "{0}" expression is
 seen. This substituion behavior comes from the standard
 __DirContext.search(Name, String, Object[], SearchControls cons)__ method. An
 common example search filter is "(uid={0})".
 * __rolesCtxDN__ : The fixed DN of the context to search for user roles.
 Consider that this is not the Distinguished Name of where the actual roles are;
 rather, this is the DN of where the objects containing the user roles are (e.g.
 for active directory, this is the DN where the user account is)
 * __roleFilter__ : A search filter used to locate the roles associated with the
 authenticated user. The input username/userDN as obtained from the login module
 callback will be substituted into the filter anywhere a "{0}" expression is
 seen. The authenticated userDN will be substituted into the filter anywhere a
 "{1}" is seen.  An example search filter that matches on the input username is:
 "(member={0})". An alternative that matches on the authenticated userDN is:
 "(member={1})".
 * __roleAttributeIsDN__ : A flag indicating whether the user's role attribute
 contains the fully distinguished name of a role object, or the users's role
 attribute contains the role name. If false, the role name is taken from the
 value of the user's role attribute. If true, the role attribute represents the
 distinguished name of a role object.  The role name is taken from the value of
 the roleNameAttributeId` attribute of the corresponding object.  In certain
 directory schemas (e.g., Microsoft Active Directory), role (group)attributes in
 the user object are stored as DNs to role objects instead of as simple names, in
 which case, this property should be set to true. The default value of this
 property is false.
 * __roleNameAttributeID__ : The name of the attribute of the role object which
 corresponds to the name of the role.  If the __roleAttributeIsDN__ property is
 set to true, this property is used to find the role object's name attribute. If
 the __roleAttributeIsDN__ property is set to false, this property is ignored.
 * __roleRecursion__ : How deep the role search will go below a given matching
 context. Disable with 0, which is the default.
 * __searchTimeLimit__ : The timeout in milliseconds for the user/role searches.
 Defaults to 10000 (10 seconds).
 * __searchScope__ : Sets the search scope to one of the strings. The default is
 SUBTREE_SCOPE.
 ** OBJECT_SCOPE : only search the named roles context.
 ** ONELEVEL_SCOPE : search directly under the named roles context.
 ** SUBTREE_SCOPE :  If the roles context is not a DirContext, search only the
 object. If the roles context is a DirContext, search the subtree rooted at the
 named object, including the named object itself
 * __allowEmptyPasswords__ : A flag indicating if empty(length==0) passwords
 should be passed to the ldap server. An empty password is treated as an
 anonymous login by some ldap servers and this may not be a desirable feature.
 Set this to false to reject empty passwords, true to have the ldap server
 validate the empty password. The default is true.
 
 @author Andy Oliver
 @author Scott.Stark@jboss.org
 @version $Revision: 64928 $ */
public class LdapExtLoginModule extends UsernamePasswordLoginModule
{
   private static final String ROLES_CTX_DN_OPT = "rolesCtxDN";
   private static final String ROLE_ATTRIBUTE_ID_OPT = "roleAttributeID";
   private static final String ROLE_ATTRIBUTE_IS_DN_OPT = "roleAttributeIsDN";
   private static final String ROLE_NAME_ATTRIBUTE_ID_OPT = "roleNameAttributeID";
   private static final String PARSE_ROLE_NAME_FROM_DN_OPT = "parseRoleNameFromDN";

   private static final String BIND_DN = "bindDN";
   private static final String BIND_CREDENTIAL = "bindCredential";
   private static final String BASE_CTX_DN = "baseCtxDN";
   private static final String BASE_FILTER_OPT = "baseFilter";
   private static final String ROLE_FILTER_OPT = "roleFilter";
   private static final String ROLE_RECURSION = "roleRecursion";
   private static final String DEFAULT_ROLE = "defaultRole";
   private static final String SEARCH_TIME_LIMIT_OPT = "searchTimeLimit";
   private static final String SEARCH_SCOPE_OPT = "searchScope";
   private static final String SECURITY_DOMAIN_OPT = "jaasSecurityDomain";

   protected String bindDN;
   protected String bindCredential;
   protected String baseDN;
   protected String baseFilter;
   protected String rolesCtxDN;
   protected String roleFilter;
   protected String roleAttributeID;
   protected String roleNameAttributeID;
   protected boolean roleAttributeIsDN;
   protected boolean parseRoleNameFromDN;
   protected int recursion = 0;
   protected int searchTimeLimit = 10000;
   protected int searchScope = SearchControls.SUBTREE_SCOPE;
   protected boolean trace;

   public LdapExtLoginModule()
   {
   }

   private transient SimpleGroup userRoles = new SimpleGroup("Roles");

   /**
    Overriden to return an empty password string as typically one cannot obtain a
    user's password. We also override the validatePassword so this is ok.
    @return and empty password String
    */
   protected String getUsersPassword() throws LoginException
   {
      return "";
   }

   /**
    Overriden by subclasses to return the Groups that correspond to the to the
    role sets assigned to the user. Subclasses should create at least a Group
    named "Roles" that contains the roles assigned to the user. A second common
    group is "CallerPrincipal" that provides the application identity of the user
    rather than the security domain identity.
    @return Group[] containing the sets of roles
    */
   protected Group[] getRoleSets() throws LoginException
   {
      Group[] roleSets = {userRoles};
      return roleSets;
   }

   /**
    Validate the inputPassword by creating a ldap InitialContext with the
    SECURITY_CREDENTIALS set to the password.
    @param inputPassword the password to validate.
    @param expectedPassword ignored
    */
   protected boolean validatePassword(String inputPassword, String expectedPassword)
   {
      boolean isValid = false;
      if (inputPassword != null)
      {
         // See if this is an empty password that should be disallowed
         if (inputPassword.length() == 0)
         {
            // Check for an allowEmptyPasswords option
            boolean allowEmptyPasswords = true;
            String flag = (String) options.get("allowEmptyPasswords");
            if (flag != null)
               allowEmptyPasswords = Boolean.valueOf(flag).booleanValue();
            if (allowEmptyPasswords == false)
            {
               log.trace("Rejecting empty password due to allowEmptyPasswords");
               return false;
            }
         }

         try
         {
            // Validate the password by trying to create an initial context
            String username = getUsername();
            isValid = createLdapInitContext(username, inputPassword);
            defaultRole();
            isValid = true;
         }
         catch (Throwable e)
         {
            super.setValidateError(e);
         }
      }
      return isValid;
   }

   /**
    @todo move to a generic role mapping function at the base login module
    */
   private void defaultRole()
   {
      try
      {
         String defaultRole = (String) options.get(DEFAULT_ROLE);
         if (defaultRole == null || defaultRole.equals(""))
         {
            return;
         }
         Principal p = super.createIdentity(defaultRole);
         log.trace("Assign user to role " + defaultRole);
         userRoles.addMember(p);
      }
      catch (Exception e)
      {
         super.log.debug("could not add default role to user", e);
      }
   }

   /**
    Bind to the ldap server for authentication. 
    
    @param username
    @param credential
    @return true if the bind for authentication succeeded
    @throws NamingException
    */
   private boolean createLdapInitContext(String username, Object credential)
      throws Exception
   {
      bindDN = (String) options.get(BIND_DN);
      bindCredential = (String) options.get(BIND_CREDENTIAL);
      String securityDomain = (String) options.get(SECURITY_DOMAIN_OPT);
      if (securityDomain != null)
      {
         ObjectName serviceName = new ObjectName(securityDomain);
         char[] tmp = DecodeAction.decode(bindCredential, serviceName);
         bindCredential = new String(tmp);
      }

      baseDN = (String) options.get(BASE_CTX_DN);
      baseFilter = (String) options.get(BASE_FILTER_OPT);
      roleFilter = (String) options.get(ROLE_FILTER_OPT);
      roleAttributeID = (String) options.get(ROLE_ATTRIBUTE_ID_OPT);
      if (roleAttributeID == null)
         roleAttributeID = "role";
      // Is user's role attribute a DN or the role name
      String roleAttributeIsDNOption = (String) options.get(ROLE_ATTRIBUTE_IS_DN_OPT);
      roleAttributeIsDN = Boolean.valueOf(roleAttributeIsDNOption).booleanValue();
      roleNameAttributeID = (String) options.get(ROLE_NAME_ATTRIBUTE_ID_OPT);
      if (roleNameAttributeID == null)
         roleNameAttributeID = "name";
      
      //JBAS-4619:Parse Role Name from DN
      String parseRoleNameFromDNOption = (String) options.get(PARSE_ROLE_NAME_FROM_DN_OPT);
      parseRoleNameFromDN = Boolean.valueOf(parseRoleNameFromDNOption).booleanValue();
      
      rolesCtxDN = (String) options.get(ROLES_CTX_DN_OPT);
      String strRecursion = (String) options.get(ROLE_RECURSION);
      try
      {
         recursion = Integer.parseInt(strRecursion);
      }
      catch (Exception e)
      {
         if (trace)
            log.trace("Failed to parse: " + strRecursion + ", disabling recursion");
         // its okay for this to be 0 as this just disables recursion
         recursion = 0;
      }
      String timeLimit = (String) options.get(SEARCH_TIME_LIMIT_OPT);
      if (timeLimit != null)
      {
         try
         {
            searchTimeLimit = Integer.parseInt(timeLimit);
         }
         catch (NumberFormatException e)
         {
            if (trace)
               log.trace("Failed to parse: " + timeLimit + ", using searchTimeLimit=" + searchTimeLimit);
         }
      }
      String scope = (String) options.get(SEARCH_SCOPE_OPT);
      if ("OBJECT_SCOPE".equalsIgnoreCase(scope))
         searchScope = SearchControls.OBJECT_SCOPE;
      else if ("ONELEVEL_SCOPE".equalsIgnoreCase(scope))
         searchScope = SearchControls.ONELEVEL_SCOPE;
      if ("SUBTREE_SCOPE".equalsIgnoreCase(scope))
         searchScope = SearchControls.SUBTREE_SCOPE;

      // Get the admin context for searching
      InitialLdapContext ctx = null;
      try
      {
         ctx = constructInitialLdapContext(bindDN, bindCredential);
         // Validate the user by binding against the userDN
         String userDN = bindDNAuthentication(ctx, username, credential, baseDN, baseFilter);

         // Query for roles matching the role filter
         SearchControls constraints = new SearchControls();
         constraints.setSearchScope(searchScope);
         constraints.setReturningAttributes(new String[0]);
         constraints.setTimeLimit(searchTimeLimit);
         rolesSearch(ctx, constraints, username, userDN, recursion, 0);
      }
      finally
      {
         if( ctx != null )
            ctx.close();
      }
      return true;
   }

   /**
    @param ctx - the context to search from
    @param user - the input username
    @param credential - the bind credential
    @param baseDN - base DN to search the ctx from
    @param filter - the search filter string
    @return the userDN string for the successful authentication 
    @throws NamingException
    */
   protected String bindDNAuthentication(InitialLdapContext ctx,
      String user, Object credential, String baseDN, String filter)
      throws NamingException
   {
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
      constraints.setReturningAttributes(new String[0]);
      constraints.setTimeLimit(searchTimeLimit);

      NamingEnumeration results = null;


      Object[] filterArgs = {user};
      results = ctx.search(baseDN, filter, filterArgs, constraints);
      if (results.hasMore() == false)
      {
    	 results.close();
         throw new NamingException("Search of baseDN(" + baseDN + ") found no matches");
      }

      SearchResult sr = (SearchResult) results.next();
      String name = sr.getName();
      String userDN = null;
      if (sr.isRelative() == true)
         userDN = name + "," + baseDN;
      else
         throw new NamingException("Can't follow referal for authentication: " + name);

      results.close();
      results = null;
      // Bind as the user dn to authenticate the user
      InitialLdapContext userCtx = constructInitialLdapContext(userDN, credential);
      userCtx.close();

      return userDN;
   }

   /**
    @param ctx
    @param constraints
    @param user
    @param userDN
    @param recursionMax
    @param nesting
    @throws NamingException
    */
   protected void rolesSearch(InitialLdapContext ctx, SearchControls constraints,
      String user, String userDN, int recursionMax, int nesting)
      throws NamingException
   {
      Object[] filterArgs = {user, userDN};
      NamingEnumeration results = ctx.search(rolesCtxDN, roleFilter, filterArgs, constraints);
      try
      {
	      while (results.hasMore())
	      {
	         SearchResult sr = (SearchResult) results.next();
	         String dn = canonicalize(sr.getName());
            if( nesting == 0 && roleAttributeIsDN && roleNameAttributeID != null )
            {
               if(parseRoleNameFromDN)
               {
                  parseRole(dn);
               }
               else
               { 
                  // Check the top context for role names
                  String[] attrNames = {roleNameAttributeID};
                  Attributes result2 = ctx.getAttributes(dn, attrNames);
                  Attribute roles2 = result2.get(roleNameAttributeID);
                  if( roles2 != null )
                  {
                     for(int m = 0; m < roles2.size(); m ++)
                     {
                        String roleName = (String) roles2.get(m);
                        addRole(roleName);
                     }
                  }  
               }
            }

            // Query the context for the roleDN values
	         String[] attrNames = {roleAttributeID};
	         Attributes result = ctx.getAttributes(dn, attrNames);
	         if( result != null && result.size() > 0 )
	         {
	            Attribute roles = result.get(roleAttributeID);
	            for (int n = 0; n < roles.size(); n ++)
	            {
	               String roleName = (String) roles.get(n);
	               if(roleAttributeIsDN && parseRoleNameFromDN)
	               { 
	            	   parseRole(roleName); 
	               }
	               else
                  if (roleAttributeIsDN)
                  {
                     // Query the roleDN location for the value of roleNameAttributeID
                     String roleDN = roleName;
                     String[] returnAttribute = {roleNameAttributeID};
                     log.trace("Using roleDN: " + roleDN);
                     try
                     {
                        Attributes result2 = ctx.getAttributes(roleDN, returnAttribute);
                        Attribute roles2 = result2.get(roleNameAttributeID);
                        if( roles2 != null )
                        {
                           for(int m = 0; m < roles2.size(); m ++)
                           {
                              roleName = (String) roles2.get(m);
                              addRole(roleName);
                           }
                        }
                     }
                     catch (NamingException e)
                     {
                        log.trace("Failed to query roleNameAttrName", e);
                     }
                  }
                  else
                  {
                     // The role attribute value is the role name
                     addRole(roleName);
                  }
	            }
	         }
	
	         if (nesting < recursionMax)
	         {
	            rolesSearch(ctx, constraints, user, dn,
	               recursionMax, nesting + 1);
	         }
	      }
      }
      finally
      {
    	  if( results != null )
    		  results.close();
      }

   }

   private InitialLdapContext constructInitialLdapContext(String dn, Object credential) throws NamingException
   {
      Properties env = new Properties();
      Iterator iter = options.entrySet().iterator();
      while (iter.hasNext())
      {
         Entry entry = (Entry) iter.next();
         env.put(entry.getKey(), entry.getValue());
      }

      // Set defaults for key values if they are missing
      String factoryName = env.getProperty(Context.INITIAL_CONTEXT_FACTORY);
      if (factoryName == null)
      {
         factoryName = "com.sun.jndi.ldap.LdapCtxFactory";
         env.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryName);
      }
      String authType = env.getProperty(Context.SECURITY_AUTHENTICATION);
      if (authType == null)
         env.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
      String protocol = env.getProperty(Context.SECURITY_PROTOCOL);
      String providerURL = (String) options.get(Context.PROVIDER_URL);
      if (providerURL == null)
         providerURL = "ldap://localhost:" + ((protocol != null && protocol.equals("ssl")) ? "636" : "389");

      env.setProperty(Context.PROVIDER_URL, providerURL);
      // JBAS-3555, allow anonymous login with no bindDN and bindCredential
      if (dn != null)
         env.setProperty(Context.SECURITY_PRINCIPAL, dn);
      if (credential != null)
         env.put(Context.SECURITY_CREDENTIALS, credential);
      traceLdapEnv(env); 
      return new InitialLdapContext(env, null);
   }
   
   private void traceLdapEnv(Properties env)
   {
      if(trace)
      {
         Properties tmp = new Properties();
         tmp.putAll(env);
         if( tmp.containsKey(BIND_CREDENTIAL) )
            tmp.setProperty(BIND_CREDENTIAL, "***");
         if( tmp.containsKey(Context.SECURITY_CREDENTIALS) )
            tmp.setProperty(Context.SECURITY_CREDENTIALS, "***");
         log.trace("Logging into LDAP server, env=" + tmp.toString()); 
      }
   }

   //JBAS-3438 : Handle "/" correctly
   private String canonicalize(String searchResult)
   {
      String result = searchResult;
      int len = searchResult.length();
      
      if (searchResult.endsWith("\""))
      {
         result = searchResult.substring(0,len - 1) 
                            + "," + rolesCtxDN + "\"";
      }
      else
      {
         result = searchResult + "," + rolesCtxDN;
       }
      return result;
   }

   private void addRole(String roleName)
   {
      if (roleName != null)
      {
         try
         {
            Principal p = super.createIdentity(roleName);
            log.trace("Assign user to role " + roleName);
            userRoles.addMember(p);
         }
         catch (Exception e)
         {
            log.debug("Failed to create principal: " + roleName, e);
         }
      }
   }
   
   private void parseRole(String dn)
   {
      StringTokenizer st = new StringTokenizer(dn, ",");
      while(st != null && st.hasMoreTokens())
      {
         String keyVal = st.nextToken();
         if(keyVal.indexOf(roleNameAttributeID) > -1)
         { 
            StringTokenizer kst = new StringTokenizer(keyVal,"=");
            kst.nextToken();
            addRole(kst.nextToken());
         } 
      }
   }
}
