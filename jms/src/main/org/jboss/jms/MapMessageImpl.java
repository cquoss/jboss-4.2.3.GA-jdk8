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
package org.jboss.jms;

import org.jboss.jms.util.JMSMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Enumeration;

/**
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 */
public class MapMessageImpl extends MessageImpl implements MapMessage
{

    public MapMessageImpl()
    {
        super.type = MessageImpl.MAP_MESSAGE_NAME;
        super.body = JMSMap.createInstance(MapMessage.class);
    }

    public void clearBody()
    {
        this.getBody().clear();
        this.setReadOnly(false);
    }

    private JMSMap getBody()
    {
        return (JMSMap) super.body;
    }

    public final boolean getBoolean(String name) throws JMSException
    {
        return this.getBody().getBoolean(name);
    }

    public final byte getByte(String name) throws JMSException
    {
        return this.getBody().getByte(name);
    }

    public final byte[] getBytes(String name) throws JMSException
    {
        return this.getBody().getBytes(name);
    }

    public final char getChar(String name) throws JMSException
    {
        return this.getBody().getChar(name);
    }

    public final double getDouble(String name) throws JMSException
    {
        return this.getBody().getDouble(name);
    }

    public final float getFloat(String name) throws JMSException
    {
        return this.getBody().getFloat(name);
    }

    public final int getInt(String name) throws JMSException
    {
        return this.getBody().getInt(name);
    }

    public final long getLong(String name) throws JMSException
    {
        return this.getBody().getLong(name);
    }

    public final Enumeration getMapNames() throws JMSException
    {
        return this.getBody().getMapNames();
    }

    public final Object getObject(String name) throws JMSException
    {
        return this.getBody().getObject(name);
    }

    public final short getShort(String name) throws JMSException
    {
        return this.getBody().getShort(name);
    }

    public final String getString(String name) throws JMSException
    {
        return this.getBody().getString(name);
    }

    public final boolean itemExists(String name) throws JMSException
    {
        return this.getBody().itemExists(name);
    }

    public final void setBoolean(String name, boolean value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setBoolean(name, value);
    }

    public final void setByte(String name, byte value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setByte(name, value);
    }

    public final void setBytes(String name, byte[] value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setBytes(name, value);
    }

    public final void setBytes(String name, byte[] value, int offset, int length)
            throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setBytes(name, value, offset, length);
    }

    public final void setChar(String name, char value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setChar(name, value);
    }

    public final void setDouble(String name, double value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.setDouble(name, value);
    }

    public final void setFloat(String name, float value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setFloat(name, value);
    }

    public final void setInt(String name, int value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setInt(name, value);
    }

    public final void setLong(String name, long value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setLong(name, value);
    }

    public final void setObject(String name, Object value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setObject(name, name);
    }

    public final void setShort(String name, short value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setShort(name, value);
    }

    public final void setString(String name, String value) throws JMSException
    {
        super.throwExceptionIfReadOnly();
        this.getBody().setString(name, value);
    }

}