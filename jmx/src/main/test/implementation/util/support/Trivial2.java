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
package test.implementation.util.support;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 */
public class Trivial2 implements Trivial2MBean
{
   private String something = null;
   private boolean isOperationInvoked = false;
   
   public void setSomething(String thing)
   {
      this.something = thing;
   }

   public String getSomething()
   {
      return something;
   }

   public void doOperation()
   {
      isOperationInvoked = true;
   }
   
   public boolean isOperationInvoked()
   {
      return isOperationInvoked;
   }
   
   public void reset()
   {
      something = null;
      isOperationInvoked = false;
   }
   
}
