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
package org.jboss.security.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityDomain;
import org.jboss.security.Util;
import org.jboss.security.auth.callback.SecurityAssociationHandler;

/** The JaasSecurityDomain is an extension of JaasSecurityManager that addes
 the notion of a KeyStore, and JSSE KeyManagerFactory and TrustManagerFactory
 for supporting SSL and other cryptographic use cases.
 
 Attributes:
 <ul>
 <li>KeyStoreType: The implementation type name being used, defaults to 'JKS'.
 </li>

 <li>KeyStoreURL: Set the KeyStore database URL string. This is used to obtain
 an InputStream to initialize the KeyStore. If the string is not a value
 URL, its treated as a file.
 </li>
 
 <li>KeyStorePass: the password used to load the KeyStore. Its format is one of:
 <ul>
 <li>The plaintext password for the KeyStore(or whatever format is used
 by the KeyStore). The toCharArray() value of the string is used without any
 manipulation.
 </li>
 <li>A command to execute to obtain the plaintext password. The format
 is '{EXT}...' where the '...' is the exact command line that will be passed
 to the Runtime.exec(String) method to execute a platform command. The first
 line of the command output is used as the password.
 </li>
 <li>A class to create to obtain the plaintext password. The format
 is '{CLASS}classname[:ctorarg]' where the '[:ctorarg]' is an optional
 string delimited by the ':' from the classname that will be passed to the
 classname ctor. The password is obtained from classname by invoking a 'char[]
 toCharArray()' method if found, otherwise, the 'String toString()' method is
 used.
 </li> 
 </ul>
 The KeyStorePass is also used in combination with the Salt and IterationCount
 attributes to create a PBE secret key used with the encode/decode operations.
 </li>

 <li>ManagerServiceName: The JMX object name string of the security manager service
 that the domain registers with to function as a security manager for the
 security domain name passed to the ctor. The makes the JaasSecurityDomain
 available under the standard JNDI java:/jaas/(domain) binding.
 </li>

 <li>LoadSunJSSEProvider: A flag indicating if the Sun com.sun.net.ssl.internal.ssl.Provider 
 security provider should be loaded on startup. This is needed when using
 the Sun JSSE jars without them installed as an extension with JDK 1.3. This
 should be set to false with JDK 1.4 or when using an alternate JSSE provider
 </li>

 <li>Salt:
 </li>
 
 <li>IterationCount:
 </li>
 </ul>

 @todo add support for encode/decode based on a SecretKey in the keystore.
 
 @author Scott.Stark@jboss.org
 @author <a href="mailto:jasone@greenrivercomputing.com">Jason Essington</a>

 @version $Revision: 65319 $
 */
public class JaasSecurityDomain
   extends JaasSecurityManager
   implements SecurityDomain, JaasSecurityDomainMBean
{
   /** The permission required to access encode, encode64 */
   private static final RuntimePermission encodePermission =
      new RuntimePermission("org.jboss.security.plugins.JaasSecurityDomain.encode");
   /** The permission required to access decode, decode64 */
   private static final RuntimePermission decodePermission =
      new RuntimePermission("org.jboss.security.plugins.JaasSecurityDomain.decode");

   /** The KeyStore associated with the security domain */
   private KeyStore keyStore;
   private KeyManagerFactory keyMgr;
   /** The KeyStore implementation type which defaults to 'JKS' */
   private String keyStoreType = "JKS";
   /** The resource for the keystore location */
   private URL keyStoreURL;
   /** The keystore password for loading */
   private char[] keyStorePassword;
   /** The secret key that corresponds to the keystore password */
   private SecretKey cipherKey;
   /** The encode/decode cipher algorigthm */
   private String cipherAlgorithm = "PBEwithMD5andDES";
   private byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
   private int iterationCount = 103;
   private PBEParameterSpec cipherSpec;
   /** The JMX object name of the security manager service */
   private ObjectName managerServiceName = JaasSecurityManagerServiceMBean.OBJECT_NAME;

   private KeyStore trustStore;
   private String trustStoreType = "JKS";
   private char[] trustStorePassword;
   private URL trustStoreURL;
   private TrustManagerFactory trustMgr;

   /** Creates a default JaasSecurityDomain for with a securityDomain
    name of 'other'.
    */
   public JaasSecurityDomain()
   {
      super();
   }

   /** Creates a JaasSecurityDomain for with a securityDomain
    name of that given by the 'securityDomain' argument.
    @param securityDomain , the name of the security domain
    */
   public JaasSecurityDomain(String securityDomain)
   {
      this(securityDomain, new SecurityAssociationHandler());
   }

   /** Creates a JaasSecurityDomain for with a securityDomain
    name of that given by the 'securityDomain' argument.
    @param securityDomain , the name of the security domain
    @param handler , the CallbackHandler to use to obtain login module info
    */
   public JaasSecurityDomain(String securityDomain, CallbackHandler handler)
   {
      super(securityDomain, handler);
   }

   public KeyStore getKeyStore() throws SecurityException
   {
      return keyStore;
   }

   public KeyManagerFactory getKeyManagerFactory() throws SecurityException
   {
      return keyMgr;
   }

   public KeyStore getTrustStore() throws SecurityException
   {
      return trustStore;
   }

   public TrustManagerFactory getTrustManagerFactory() throws SecurityException
   {
      return trustMgr;
   }

   /** The JMX object name string of the security manager service.
    @return The JMX object name string of the security manager service.
    */
   public ObjectName getManagerServiceName()
   {
      return this.managerServiceName;
   }

   /** Set the JMX object name string of the security manager service.
    */
   public void setManagerServiceName(ObjectName managerServiceName)
   {
      this.managerServiceName = managerServiceName;
   }

   public String getKeyStoreType()
   {
      return this.keyStoreType;
   }

   public void setKeyStoreType(String type)
   {
      this.keyStoreType = type;
   }

   public String getKeyStoreURL()
   {
      String url = null;
      if( keyStoreURL != null )
         url = keyStoreURL.toExternalForm();
      return url;
   }

   public void setKeyStoreURL(String storeURL) throws IOException
   {
      this.keyStoreURL = this.validateStoreURL(storeURL);
      log.debug("Using KeyStore=" + keyStoreURL.toExternalForm());
   }

   public void setKeyStorePass(String password)
      throws Exception
   {
      this.keyStorePassword = Util.loadPassword(password);
   }

   public String getTrustStoreType()
   {
      return this.trustStoreType;
   }

   public void setTrustStoreType(String type)
   {
      this.trustStoreType = type;
   }

   public void setTrustStorePass(String password)
      throws Exception
   {
      this.trustStorePassword = Util.loadPassword(password);
   }

   public String getTrustStoreURL()
   {
      String url = null;
      if( trustStoreURL != null )
         url = trustStoreURL.toExternalForm();
      return url;
   }

   public void setTrustStoreURL(String storeURL) throws IOException
   {
      this.trustStoreURL = validateStoreURL(storeURL);
   }

   public void setSalt(String salt)
   {
      this.salt = salt.getBytes();
   }

   public void setIterationCount(int iterationCount)
   {
      this.iterationCount = iterationCount;
   }

   public String getCipherAlgorithm()
   {
      return cipherAlgorithm;
   }

   public void setCipherAlgorithm(String cipherAlgorithm)
   {
      this.cipherAlgorithm = cipherAlgorithm;
   }

   public String getName()
   {
      return "JaasSecurityDomain(" + getSecurityDomain() + ")";
   }

   /** Encrypt the secret using the cipherKey.
    * @param secret - the plaintext secret to encrypt
    * @return the encrypted secret
    * @throws Exception
    */ 
   public byte[] encode(byte[] secret)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         System.out.println("Checking: "+encodePermission);
         sm.checkPermission(encodePermission);
      }

      Cipher cipher = Cipher.getInstance(cipherAlgorithm);
      cipher.init(Cipher.ENCRYPT_MODE, cipherKey, cipherSpec);
      byte[] encoding = cipher.doFinal(secret);
      return encoding;
   }
   /** Decrypt the secret using the cipherKey.
    * 
    * @param secret - the encrypted secret to decrypt.
    * @return the decrypted secret
    * @throws Exception
    */ 
   public byte[] decode(byte[] secret)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(decodePermission);

      Cipher cipher = Cipher.getInstance(cipherAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherSpec);
      byte[] decode = cipher.doFinal(secret);
      return decode;
   }
   /** Encrypt the secret using the cipherKey and return a base64 encoding.
    * @param secret - the plaintext secret to encrypt
    * @return the encrypted secret as a base64 string
    * @throws Exception
    */ 
   public String encode64(byte[] secret)
      throws Exception
   {
      byte[] encoding = encode(secret);
      String b64 = Util.tob64(encoding);
      return b64;
   }
   /** Decrypt the base64 encoded secret using the cipherKey.
    * 
    * @param secret - the base64 encoded encrypted secret to decrypt.
    * @return the decrypted secret
    * @throws Exception
    */
   public byte[] decode64(String secret)
      throws Exception
   {
      byte[] encoding = Util.fromb64(secret);
      byte[] decode = decode(encoding);
      return decode;
   }

   /**
       Reload the key- and truststore
   */
   public void reloadKeyAndTrustStore()
      throws Exception
   {
      loadKeyAndTrustStore();
   }

   protected void startService()
      throws Exception
   {
      // Load the keystore password if it was 
      loadKeystorePassword();

      // Load the key and/or truststore into memory
      loadKeyAndTrustStore();

      // Only register with the JaasSecurityManagerService if its defined
      if( managerServiceName != null )
      {
         /* Register with the JaasSecurityManagerServiceMBean. This allows this
         JaasSecurityDomain to function as the security manager for security-domain
         elements that declare java:/jaas/xxx for our security domain name.
         */
         MBeanServer server = MBeanServerLocator.locateJBoss();
         Object[] params = {getSecurityDomain(), this};
         String[] signature = new String[]{"java.lang.String", "org.jboss.security.SecurityDomain"};
         server.invoke(managerServiceName, "registerSecurityDomain", params, signature);
      }
   }

   protected void stopService()
   {
      if( keyStorePassword != null )
      {
         Arrays.fill(keyStorePassword, '\0');
         keyStorePassword = null;
      }
      cipherKey = null;
   }

   /** If keyStorePassword is null and keyStorePasswordCmd exists,
    * execute it to obtain the password.
    */ 
   private void loadKeystorePassword()
      throws Exception
   {
      // Create the PBE secret key
      cipherSpec = new PBEParameterSpec(salt, iterationCount);
      PBEKeySpec keySpec = new PBEKeySpec(keyStorePassword);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      cipherKey = factory.generateSecret(keySpec);
   }

   private void loadKeyAndTrustStore()
      throws Exception
   {
      if( keyStoreURL != null )
      {
         keyStore = KeyStore.getInstance(keyStoreType);
         InputStream is = keyStoreURL.openStream();
         keyStore.load(is, keyStorePassword);
         String algorithm = KeyManagerFactory.getDefaultAlgorithm();
         keyMgr = KeyManagerFactory.getInstance(algorithm);
         keyMgr.init(keyStore, keyStorePassword);
      }
      if( trustStoreURL != null )
      {
         trustStore = KeyStore.getInstance(trustStoreType);
         InputStream is = trustStoreURL.openStream();
         trustStore.load(is, trustStorePassword);
         String algorithm = TrustManagerFactory.getDefaultAlgorithm();
         trustMgr = TrustManagerFactory.getInstance(algorithm);
         trustMgr.init(trustStore);
      }
      else if( keyStore != null )
      {
         trustStore = keyStore;
         String algorithm = TrustManagerFactory.getDefaultAlgorithm();
         trustMgr = TrustManagerFactory.getInstance(algorithm);
         trustMgr.init(trustStore);         
      }
   }

   private URL validateStoreURL(String storeURL) throws IOException
   {
      URL url = null;
      // First see if this is a URL
      try
      {
         url = new URL(storeURL);
      }
      catch(MalformedURLException e)
      {
         // Not a URL or a protocol without a handler
      }

      // Next try to locate this as file path
      if( url == null )
      {
         File tst = new File(storeURL);
         if( tst.exists() == true )
            url = tst.toURL();
      }

      // Last try to locate this as a classpath resource
      if( url == null )
      {
         ClassLoader loader = SubjectActions.getContextClassLoader();
         url = loader.getResource(storeURL);
      }

      // Fail if no valid key store was located
      if( url == null )
      {
         String msg = "Failed to find url=" + storeURL + " as a URL, file or resource";
         throw new MalformedURLException(msg);
      }
      return url;
   }
}
