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
package org.jboss.test.cmp2.idxandusersql.ejb;

/**
 * Local interface for CMR2.
 */
public interface CMR2Local
   extends javax.ejb.EJBLocalObject
{

   public void setPKey2( String pKey2 ) ;

   public String getPKey2(  ) ;

   /**
    * This field gets a <dbindex/> that we want to look up in the database to see if the index was really created on the file.
    * @todo set the dbindex property here with a modern xdoclet*    */
   public String getFoo2(  ) ;

   /**
    * This one is not indexed
    */
   public String getBar2(  ) ;

   public java.util.Collection getIdxs(  ) ;

   public void setIdxs( java.util.Collection Idxs ) ;

}
