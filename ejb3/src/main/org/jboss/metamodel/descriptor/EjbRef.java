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
package org.jboss.metamodel.descriptor;

/**
 * Represents an ejb-ref element of the ejb-jar.xml deployment descriptor for
 * the 1.4 schema
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision: 60259 $</tt>
 */
public class EjbRef extends BaseEjbRef
{

   private String home;
   
   private String remote;

   public String getHome()
   {
      return home;
   }

   public void setHome(String home)
   {
      this.home = home;
   }

   public String getRemote()
   {
      return remote;
   }

   public void setRemote(String remote)
   {
      this.remote = remote;
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[");
      sb.append("ejbRefName=").append(ejbRefName);
      sb.append(", ejbRefType=").append(ejbRefType);
      sb.append(", remote=").append(remote);
      sb.append(", jndiName=").append(mappedName);
      sb.append("]");
      return sb.toString();
   }
}
