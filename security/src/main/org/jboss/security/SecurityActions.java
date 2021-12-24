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
package org.jboss.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.logging.Logger;

/**
 * Priviledged actions for this package
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 59905 $
 */
class SecurityActions
{
   private static final Logger log = Logger.getLogger(SecurityActions.class);

   interface SystemPropertyAction
   {
      SystemPropertyAction PRIVILEGED = new SystemPropertyAction()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            String prop = (String) AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     String p = System.getProperty(name, defaultValue);
                     return p;
                  }
               }
            );
            return prop;
         }
      };

      SystemPropertyAction NON_PRIVILEGED = new SystemPropertyAction()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            String prop = System.getProperty(name, defaultValue);
            return prop;
         }
      };

      String getProperty(final String name, final String defaultValue);
   }
   interface RuntimeActions
   {
      RuntimeActions PRIVILEGED = new RuntimeActions()
      {
         public String execCmd(final String cmd)
            throws Exception
         {
            try
            {
               String line = AccessController.doPrivileged(
               new PrivilegedExceptionAction<String>()
                  {
                     public String run() throws Exception
                     {
                        return NON_PRIVILEGED.execCmd(cmd);
                     }
                  }
               );
               return line;
            }
            catch(PrivilegedActionException e)
            {
               throw e.getException();
            }
         }
      };
      RuntimeActions NON_PRIVILEGED = new RuntimeActions()
      {
         public String execCmd(final String cmd)
            throws Exception
         {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd);
            InputStream stdin = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line = reader.readLine();
            stdin.close();
            int exitCode = p.waitFor();
            log.debug("Command exited with: "+exitCode);
            return line;
         }
      };
      String execCmd(String cmd) throws Exception;
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }

   static String getProperty(final String name, final String defaultValue)
   {
      SecurityManager sm = System.getSecurityManager();
      String prop;
      if( sm != null )
      {
         prop = SystemPropertyAction.PRIVILEGED.getProperty(name, defaultValue);
      }
      else
      {
         prop = SystemPropertyAction.NON_PRIVILEGED.getProperty(name, defaultValue);
      }
      return prop;
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }

   public static String execCmd(String cmd)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      String line;
      if( sm != null )
      {
         line = RuntimeActions.PRIVILEGED.execCmd(cmd);
      }
      else
      {
         line = RuntimeActions.NON_PRIVILEGED.execCmd(cmd);
      }
      return line;
   }

}
