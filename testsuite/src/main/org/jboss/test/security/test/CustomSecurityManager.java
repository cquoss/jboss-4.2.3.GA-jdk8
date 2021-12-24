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
package org.jboss.test.security.test;

import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.util.CachePolicy;

//$Id: CustomSecurityManager.java 65645 2007-09-27 05:52:04Z anil.saldhana@jboss.com $

/**
 *  JBAS-2703 : Create a AuthenticationManager/AuthorizationManager 
 *  plugin testcase   
 *  
 *  Security Manager Configuration:
 *  This custom security manager depends on two properties files, 
 *  1. one for the principal to credential mapping and 
 *  2. the other for principal to roles mapping. ( A format of 
 *     principal.CallerPrincipal=newPrincipal, provides the mechanism
 *     to specify the caller principal, for a particular principal)
 *  
 *  The property files are custom-users.properties and custom-roles.properties
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 31, 2006
 *  @version $Revision: 65645 $
 */
public class CustomSecurityManager implements SubjectSecurityManager, RealmMapping
{
   private static Logger log = Logger.getLogger(CustomSecurityManager.class);
   private String securityDomain;
   private CallbackHandler callbackHandler;
   private CachePolicy cachePolicy; 
   private final Principal UNAUTHENTICATED_IDENTITY = new SimplePrincipal("guest");
   
   //Local Configuration
   private static Map principalToCredMap = new HashMap();
   private static Map principalToRoleMap = new HashMap();
   private static Map principalToCallerPrincipalMap = new HashMap();
   
   private static final String rolePropertiesFile = "custom-roles.properties";
   private static final String credPropertiesFile = "custom-users.properties";
   
   protected Subject activeSubject;
   
   /**
    * 
    * Create a new CustomSecurityManager.
    *
    */
   public CustomSecurityManager()
   {
      super();   
   } 

   /**
    * 
    * Create a new CustomSecurityManager.
    * (Ctr used by the JaasSecurityManagerService to instantiate
    *  the security manager plugin)
    * @param domain Security Domain
    * @param handler Callback Handler
    */
   public CustomSecurityManager(String domain, CallbackHandler handler)
   {
      this(); 
      callbackHandler = handler;
      securityDomain = domain;

      //For the JCA cases, there is no need for security checks as they are handled
      //in the isValid method below.
      if("HsqlDbRealm".equals(securityDomain) == false &&
            "jbossmq".equals(securityDomain) == false &&
            "JmsXARealm".equals(securityDomain) == false)
      {
         this.fillRoles(securityDomain);
         this.fillCredsCache(securityDomain);
      }
      log.debug("[CallBackHandler="+handler+":domain="+domain+"]");
   } 

   /**
    * Sets the Cache Policy
    * (JaasSecurityManagerService calls this method on the security manager
    *  to plug in a cache policy)
    * 
    * @param cachePolicy
    */
   public void setCachePolicy(CachePolicy cachePolicy)
   {
      this.cachePolicy = cachePolicy;
   }  
   
   //*******************************************************************
   //        SubjectSecurityManager (AuthenticationManager) 
   //             Interface Methods
   //*******************************************************************

   /**
    * @see AuthenticationManager#isValid(Principal , Object )
    */
   public boolean isValid(Principal principal, Object credential)
   { 
      return isValid( principal, credential, null);
   }

   /**
    * @see AuthenticationManager#isValid(Principal, Object, Subject)
    */
   public boolean isValid(Principal principal, Object credential, Subject activeSubject)
   { 
      log.debug("[isValid:principal="+principal+":credential="+credential+ 
            ":activeSubject="+activeSubject + "]");
      boolean result = false;
      
      if(principal == null || checkNullSimplePrincipal(principal))
         principal = UNAUTHENTICATED_IDENTITY;

      if(activeSubject == null )
         activeSubject = new Subject();
      
      if("HsqlDbRealm".equals(securityDomain))
      {
         try
         {
             handleJCA(activeSubject);
             result = true;
         }
         catch (JMException e)
         {
            log.error("Exception in isValid:",e);
         }
      }
      else
         result = authenticate( principal, credential);
      if(result)
      {
         addRolesInSubject(principal, credential, activeSubject);
      }
      return result;
   }

   /**
    * @see AuthenticationManager#getActiveSubject()
    */
   public Subject getActiveSubject()
   { 
      return activeSubject;
   }
 
   /**
    * @see AuthenticationManager#getSecurityDomain()
    */  
   public String getSecurityDomain()
   { 
      return securityDomain;
   }
   
   
   //*******************************************************************
   //              RealmMapping Interface Methods
   //*******************************************************************

   /**
    * @see RealmMapping#doesUserHaveRole(Principal, Set)
    */
   public boolean doesUserHaveRole(Principal principal, Set roles)
   { 
      if(principal == null)
        principal = new SimplePrincipal("guest");
      log.debug("[doesUserHaveRole:Principal="+principal+":rolesCheck="+roles+"]");
      Set containedSet = (Set)principalToRoleMap.get(principal);
      /**
       * Check for roles only when the principal has roles and also
       * when the roleset passed is not empty
       */
      boolean shouldCheck = containedSet != null && roles != null && !roles.isEmpty();
      return shouldCheck ? containedSet.containsAll(roles) : false;
   }

   /**
    * @see RealmMapping#getPrincipal(Principal)
    */
   public Principal getPrincipal(Principal principal)
   { 
      Principal callerP = (Principal)principalToCallerPrincipalMap.get(principal);
      log.debug("[getPrincipal:principal="+principal+ ":callerP="+callerP+ "]");
      return callerP;      
   }
   
   /**
    * @see RealmMapping#getUserRoles(Principal)
    */
   public Set getUserRoles(Principal principal)
   { 
      log.debug("[getUserRoles:Principal="+principal+"]");
      return (Set)principalToRoleMap.get(principal);
   } 
   
   //*******************************************************************
   //              Private Methods
   //******************************************************************* 

   /**
    * Add the roles in the active Subject
    * 
    * @param principal
    * @param cred
    * @param activeSubject
    */
   private void addRolesInSubject(Principal principal, Object cred, Subject activeSubject)
   {
      //Update the subject with principal and roles
      Group group = new SimpleGroup("Roles");
      Set set = (Set)principalToRoleMap.get(principal);
      if(set != null && !set.isEmpty())
      {
         Iterator iter = set.iterator();
         while(iter.hasNext())
         {
            group.addMember((Principal)iter.next());
         }
      } 
      
      activeSubject.getPrincipals().add(principal);
      activeSubject.getPrincipals().add(group);
      SecurityAssociation.setSubject(activeSubject);
      SecurityAssociation.setPrincipal(principal);
      SecurityAssociation.setCredential(cred);
   }
   
   /**
    * Authenticate the principal/credential against the internal
    * hashmap (loaded from the users property file)
    * 
    * @param principal
    * @param credential
    * @return
    */
   private boolean authenticate(Principal principal, Object credential)
   {
      if(principal == null)
         throw new IllegalArgumentException("Illegal Null Argument:"+ principal);
      boolean result = false;
      String username = "";
      String pwd = "";
      if(principal instanceof SimplePrincipal)
      {
         username = ((SimplePrincipal)principal).getName();
      }
      if(credential instanceof char[])
      {
         pwd = new String((char[])credential);
      }
      else
         if(credential instanceof String)
      {
         pwd =  (String)credential;
      }
      String storedCred = (String)this.principalToCredMap.get(principal);
      
      if(this.UNAUTHENTICATED_IDENTITY.equals(principal))
         result = true;
      else
      {
         result = pwd.equals(storedCred);
      }
      log.debug("[authenticate:username="+username
            +":pwd="+pwd+"::storedCred="+storedCred+":result="+result+"]");
      return result;
   }
   
   /**
    * Special case with JMS layer that sends a 
    * SimplePrincipal with null username.
    * 
    * @param p
    * @return
    */
   private boolean checkNullSimplePrincipal(Principal p)
   {
      boolean result = false;
      if(p instanceof SimplePrincipal)
      {
         SimplePrincipal sp = (SimplePrincipal)p;
         if(sp.getName() == null)
            result = true;
      }
      return result;
   } 
   
   /**
    * Read the credentials defined in the users property file
    * into the internal hashmap
    * 
    * @param domain
    */
   private static void fillCredsCache(String domain)
   { 
      Properties prop = new Properties();
      try
      {
         prop.load(getThreadContextClassLoader().getResourceAsStream(credPropertiesFile));
         Iterator iter = prop.keySet().iterator();
         while(iter.hasNext())
         {
            String str = (String)iter.next();
            String pwd = prop.getProperty(str);  
            principalToCredMap.put(new SimplePrincipal(str),pwd);
         }
         log.debug("principalToCredSet:"+principalToCredMap); 
      } 
      catch (Exception e)
      { 
         log.error("Initialization exception:domain="+domain,e);
      }  
   }
   
   /**
    * Read the roles defined in the roles property file
    * into the internal hashmap
    * 
    * @param domain
    */
   private static void fillRoles(String domain)
   { 
      Properties prop = new Properties();
      try
      {
         prop.load(getThreadContextClassLoader().getResourceAsStream(rolePropertiesFile));
         Iterator iter = prop.keySet().iterator();
         while(iter.hasNext())
         {
            String str = (String)iter.next();
            String roles = prop.getProperty(str);
            log.debug("str:before="+str);
            Set roleSet = getSet(roles);
            
            if(str.indexOf(".Roles") > -1)
               str = str.substring(0,str.indexOf(".Roles"));
            if(str.indexOf(".CallerPrincipal") > -1)
            {
               str = str.substring(0,str.indexOf(".CallerPrincipal"));
               principalToCallerPrincipalMap.put(new SimplePrincipal(str), new SimplePrincipal(roles));
               continue;
            }
            Set set = (Set)principalToRoleMap.get(new SimplePrincipal(str));
            if(set != null)
               set.addAll(roleSet);
            else
               principalToRoleMap.put(new SimplePrincipal(str),roleSet);
         }
         log.debug("principalToRoleSet:"+principalToRoleMap);
         log.debug("principalToCallerPrincipalMap:"+principalToCallerPrincipalMap);
      } 
      catch (Exception e)
      { 
         log.error("Initialization exception:domain="+domain,e);
      }  
   }
   
   /**
    * Get the TCL
    * 
    * @return the Thread Context ClassLoader
    */
   private static ClassLoader getThreadContextClassLoader()
   {
      return Thread.currentThread().getContextClassLoader();
   }
   
   /**
    * Given a comma-seperated string of roles, returns a set
    * that contains roles as objects of SimplePrincipal
    * 
    * @param listOfRoles
    * @return a java.util.Set of roles as objects of SimplePrincipal
    */
   private static Set getSet(String listOfRoles)
   {
      Set set = new HashSet();
      StringTokenizer tokenizer = new StringTokenizer(listOfRoles,",");
      while(tokenizer.hasMoreTokens())
      {
         set.add(new SimplePrincipal(tokenizer.nextToken().trim()));
      }
      return set;
   }
   
   /**
    * For the JCA case (HsqdbRealm), we will hardcode stuff
    */
   private void handleJCA(Subject activeSubject) throws JMException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      PasswordCredential pc = new PasswordCredential("sa",new char[0]);
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) server.getAttribute(
            new ObjectName("jboss.jca:service=LocalTxCM,name=DefaultDS"),"ManagedConnectionFactory");
      pc.setManagedConnectionFactory(mcf);
      String principalStr = "sa"; 
      Principal principal = new SimplePrincipal(principalStr);
      activeSubject.getPrincipals().add(principal);
      activeSubject.getPrivateCredentials().add(pc); 
   } 
}
