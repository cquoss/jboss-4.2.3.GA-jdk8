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
package org.jboss.jms.util;

import javax.jms.MessageFormatException;

/**
 * Enforces JMS conversion rules.
 *
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 */
public class JMSTypeConversions
{
    private static MessageFormatException createMessageFormatException(
            Class fromClass,
            Class toClass)
    {
        return new MessageFormatException(
                "Unsupported conversion: Cannot convert '"
                + fromClass.getName()
                + "' to '"
                + toClass.getName()
                + ".'  Please see section three of the JMS specification for more information.");

    }

    public static boolean getBoolean(Object value)
            throws MessageFormatException
    {
        if (value == null)
        {
            return Boolean.valueOf(null).booleanValue();
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue();
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value).booleanValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Boolean.TYPE);
        }
    }

    public static byte getByte(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Byte.valueOf(null).byteValue();
        }
        else if (value instanceof Byte)
        {
            return ((Byte) value).byteValue();
        }
        else if (value instanceof String)
        {
            return Byte.valueOf((String) value).byteValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Byte.TYPE);
        }
    }

    public static byte[] getBytes(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof Byte[])
        {
            return (byte[]) value;
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Byte[].class);
        }
    }

    public static char getChar(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            throw new NullPointerException("Item does not exist or is null.");
        }
        else if (value instanceof Character)
        {
            return ((Character) value).charValue();
        }
        else
        {
            throw createMessageFormatException(
                    value.getClass(),
                    Character.TYPE);
        }
    }

    public static double getDouble(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Double.valueOf(null).doubleValue();
        }
        else if (value instanceof Double)
        {
            return ((Double) value).doubleValue();
        }
        else if (value instanceof Float)
        {
            return ((Float) value).doubleValue();
        }
        else if (value instanceof String)
        {
            return Double.valueOf((String) value).doubleValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Double.TYPE);
        }
    }

    public static float getFloat(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Float.valueOf(null).floatValue();
        }
        else if (value instanceof Float)
        {
            return ((Float) value).floatValue();
        }
        else if (value instanceof String)
        {
            return Float.valueOf((String) value).floatValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Float.TYPE);
        }
    }

    public static int getInt(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Integer.valueOf(null).intValue();
        }
        else if (value instanceof Integer)
        {
            return ((Integer) value).intValue();
        }
        else if (value instanceof Byte)
        {
            return ((Byte) value).intValue();
        }
        else if (value instanceof Short)
        {
            return ((Short) value).intValue();
        }
        else if (value instanceof String)
        {
            return Short.valueOf((String) value).intValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Integer.TYPE);
        }
    }

    public static long getLong(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Long.valueOf(null).longValue();
        }
        else if (value instanceof Long)
        {
            return ((Long) value).longValue();
        }
        else if (value instanceof Byte)
        {
            return ((Byte) value).longValue();
        }
        else if (value instanceof Short)
        {
            return ((Short) value).longValue();
        }
        else if (value instanceof Integer)
        {
            return ((Integer) value).longValue();
        }
        else if (value instanceof String)
        {
            return Long.valueOf((String) value).longValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Long.TYPE);
        }
    }

    public static Object getObject(Object value)
    {
        return value;
    }

    public static short getShort(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return Short.valueOf(null).shortValue();
        }
        else if (value instanceof Short)
        {
            return ((Short) value).shortValue();
        }
        else if (value instanceof Byte)
        {
            return ((Byte) value).shortValue();
        }
        else if (value instanceof String)
        {
            return Short.valueOf((String) value).shortValue();
        }
        else
        {
            throw createMessageFormatException(value.getClass(), Short.TYPE);
        }
    }

    public static String getString(Object value) throws MessageFormatException
    {
        if (value == null)
        {
            return String.valueOf(null);
        }
        else if (value instanceof Byte[])
        {
            throw createMessageFormatException(Byte[].class, String.class);
        }
        else
        {
            return value.toString();
        }
    }
}