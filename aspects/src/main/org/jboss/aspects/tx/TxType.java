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
package org.jboss.aspects.tx;

/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57186 $
 */
public class TxType extends org.jboss.lang.Enum
{
   public static final TxType MANDATORY = new TxType("MANDATORY", 0);
   public static final TxType REQUIRED = new TxType("REQUIRED", 1);
   public static final TxType REQUIRESNEW = new TxType("REQUIRESNEW", 2);
   public static final TxType SUPPORTS = new TxType("SUPPORTS", 3);
   public static final TxType NOTSUPPORTED = new TxType("NOTSUPPORTED", 4);
   public static final TxType NEVER = new TxType("NEVER", 5);

   private static final TxType[] values = {MANDATORY, REQUIRED, REQUIRESNEW, SUPPORTS, NOTSUPPORTED, NEVER};

   private TxType(String name, int ordinal)
   {
      super(name, ordinal);
   }

   Object readResolve() throws java.io.ObjectStreamException
   {
      return values[ordinal];
   }
}
