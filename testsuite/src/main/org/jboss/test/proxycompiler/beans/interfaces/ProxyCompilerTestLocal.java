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
package org.jboss.test.proxycompiler.beans.interfaces;

/**
 * Local interface for ProxyCompilerTest.
 */
public interface ProxyCompilerTestLocal
   extends javax.ejb.EJBLocalObject
{

   public Integer getPk(  ) ;

   public void setPk( Integer pk ) ;

   public boolean getBool(  ) ;

   public void setBool( boolean arg ) ;

   public byte getByte(  ) ;

   public void setByte( byte arg ) ;

   public char getChar(  ) ;

   public void setChar( char arg ) ;

   public double getDouble(  ) ;

   public void setDouble( double arg ) ;

   public float getFloat(  ) ;

   public void setFloat( float arg ) ;

   public int getInt(  ) ;

   public void setInt( int arg ) ;

   public long getLong(  ) ;

   public void setLong( long arg ) ;

   public short getShort(  ) ;

   public void setShort( short arg ) ;

   public Object[] getObjectArray(  ) ;

   public void setObjectArray( Object[] arg ) ;

   public int[] getIntArray(  ) ;

   public void setIntArray( int[] arg ) ;

   public boolean noArgsMethod(  ) ;

   public String complexSignatureMethod( int i,Object ref,int[] ints,Object[] objectRefs ) ;

}
