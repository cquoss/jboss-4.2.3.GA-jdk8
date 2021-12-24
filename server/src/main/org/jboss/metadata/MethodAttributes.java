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

/**
 *
 * Provides meta-data for method-attributes
 * <method-attributes>
 * <method>
 * <method-name>get*</method-name>
 * <read-only>true</read-only>
 * <idempotent>true</idempotent>
 * <transaction-timeout>100</tranaction-timeout>
 * </method>
 * </method-attributes>
 *
 * @author <a href="pete@subx.com">Peter Murray</a>
 *
 * @version $Revision: 57209 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/04/10: peter</b>
 *  <ol>
 *  <li>Initial revision
 *  </ol>
 */
public class MethodAttributes
{
   String pattern;
   boolean readOnly;
   boolean idempotent;
   int txTimeout;

   public static MethodAttributes kDefaultMethodAttributes;

   static
   {
      kDefaultMethodAttributes = new MethodAttributes();
      kDefaultMethodAttributes.pattern = "*";
      kDefaultMethodAttributes.readOnly = false;
      kDefaultMethodAttributes.idempotent = false;
      kDefaultMethodAttributes.txTimeout = 0;
   }

   public boolean patternMatches(String methodName)
   {
      int ct, end;

      end = pattern.length();

      if(end > methodName.length())
          return false;

      for(ct = 0; ct < end; ct ++)
      {
         char c = pattern.charAt(ct);
         if(c == '*')
        return true;
         if(c != methodName.charAt(ct))
        return false;
      }
      return ct == methodName.length();
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

   public boolean isIdempotent()
   {
      return idempotent;
   }

   public int getTransactionTimeout()
   {
      return txTimeout;
   }
}
