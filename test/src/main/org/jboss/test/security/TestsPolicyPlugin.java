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
package org.jboss.test.security;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;

/**
 * A Test PolicyPlugin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public class TestsPolicyPlugin extends PolicyPlugin
{
   public PermissionCollection getPermissions(CodeSource codesource)
   {
      URL url = codesource.getLocation();
      File file = new File(url.toString());
      String name = file.getName();
      if (name.indexOf("tests") != -1)
      {
         // @TODO: Make configurable
         PermissionCollection pc = fileReadPermissions();
         // Needed for the class loading tests
         pc.add(new RuntimePermission("createClassLoader"));
         pc.add(new RuntimePermission("getProtectionDomain"));
         return pc;
      }
      return allPermissions();
   }
}
