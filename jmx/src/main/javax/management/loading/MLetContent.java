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
package javax.management.loading;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

/** The MLET tag representation
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57200 $
 */
class MLetContent
{
   private URL mletURL;
   private URL codeBase;
   private Map attributes;

   /**
    * 
    * @param url - MLet text file
    * @param attributes 
    */
   public MLetContent(URL url, Map attributes)
   {
      this.mletURL = url;
      this.attributes = attributes;
      String codebase = (String) getParameter("codebase");
      if( codebase != null )
      {
         if( codebase.endsWith("/") == false )
         {
            codebase += "/";
         }
         try
         {
            codeBase = new URL(mletURL, codebase);
         }
         catch (MalformedURLException e)
         {
         }
      }

      if (codeBase == null)
      {
         String file = mletURL.getFile();
         int i = file.lastIndexOf('/');
         if (i > 0 && i < file.length() - 1)
         {
            try
            {
               codeBase = new URL(mletURL, file.substring(0, i + 1));
            }
            catch (MalformedURLException e)
            {
            }
         }
      }
      if (codeBase == null)
         codeBase = mletURL;
   }

   public Map getAttributes()
   {
      return attributes;
   }

   public String getCode()
   {
      String code = (String) getParameter("code");
      return code;
   }

   public URL getCodeBase()
   {
      return codeBase;
   }

   public URL getDocumentBase()
   {
      return mletURL;
   }

   public String getJarFiles()
   {
      String jarFile = (String) getParameter("archives");
      return jarFile;
   }

   public String getName()
   {
      String name = (String) getParameter("name");
      return name;
   }

   public String getSerializedObject()
   {
      String object = (String) getParameter("object");
      return object;
   }

   public String getVersion()
   {
      String version = (String) getParameter("version");
      return version;
   }

   public Object getParameter(String name)
   {
      return attributes.get(name.toLowerCase());
   }
}
