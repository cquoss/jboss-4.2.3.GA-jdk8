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

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Overrides {@link JMSMap} to enforce the message property specific
 * conversion rules specified in section 3.5.4 of the JMS specification.
 * Additionally, enforces naming restrictions imposed on property names in
 * section 3.5.1 of the JMS specification.
 *
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 */
public final class MessageProperties extends JMSMap
{

    private static final String[] illegalIdentifiers =
            new String[]{
                "NULL",
                "TRUE",
                "FALSE",
                "NOT",
                "AND",
                "OR",
                "BETWEEN",
                "LIKE",
                "IN",
                "IS",
                "ESCAPE"};

    private boolean readOnly = false;

    private static void throwExceptionIfNameIsIllegal(String name)
            throws JMSException
    {
        if (name == null)
        {
            throw new JMSException(""); //TOD: Write exception method
        }
        if (name.startsWith("JMSX") || name.startsWith("JMS_"))
        {
            throw new JMSException(""); //TOD: Write exception method.
        }
        char[] identifierCharArray = name.toCharArray();
        if (identifierCharArray.length < 1)
        {
            throw new JMSException(""); //TOD: Write exception method
        }
        if (!Character.isJavaIdentifierStart(identifierCharArray[0]))
        {
            throw new JMSException(""); //TOD: Write exception method
        }
        for (int i = 1; i < identifierCharArray.length; i++)
        {
            if (!Character.isJavaIdentifierPart(identifierCharArray[i]))
            {
                throw new JMSException("");
                //TOD: Write exception method
            }
        }
        for (int i = 0; i < illegalIdentifiers.length; i++)
        {
            if (name.equalsIgnoreCase(illegalIdentifiers[i]))
            {
                throw new JMSException("");
                //TOD: Write exception method
            }
        }
    }

    public Enumeration getMapNames()
    {
        List filteredList = new ArrayList(this.contents.size());
        Iterator keys = this.contents.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            if (!key.startsWith("JMSX") || !key.startsWith("JMS_"))
            {
                filteredList.add(key);
            }
        }
        return Collections.enumeration(filteredList);
    }

    public final boolean isReadOnly()
    {
        return this.readOnly;
    }

    public void setBoolean(String name, boolean value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Boolean(value));
    }

    public void setByte(String name, byte value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Byte(value));
    }

    public void setDouble(String name, double value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Double(value));
    }

    public void setFloat(String name, float value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Float(value));
    }

    public void setInt(String name, int value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Integer(value));
    }

    public void setLong(String name, long value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Long(value));
    }

    public void setObject(String name, Object value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        if (value instanceof Boolean
                || value instanceof Byte
                || value instanceof Double
                || value instanceof Float
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof String)
        {
            super.contents.put(name, value);
        }
        else
        {
            throw new MessageFormatException(""); //TOD: Implement message
        }
    }

    public final void setReadOnly(boolean value)
    {
        this.readOnly = value;
    }

    public void setShort(String name, short value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, new Short(value));
    }

    public void setString(String name, String value) throws JMSException
    {
        this.throwExceptionIfReadOnly();
        throwExceptionIfNameIsIllegal(name);
        super.contents.put(name, value);
    }

    private void throwExceptionIfReadOnly() throws JMSException
    {
        if (this.isReadOnly())
        {
            throw new MessageNotWriteableException("Unable to write property: the message properties are currently read only.");
        }
    }

}