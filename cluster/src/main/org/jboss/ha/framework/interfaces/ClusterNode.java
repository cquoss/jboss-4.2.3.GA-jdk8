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
package org.jboss.ha.framework.interfaces;

import java.net.InetAddress;
import java.io.Serializable;

import org.jgroups.stack.IpAddress;

/**
 * Replacement for a JG IpAddress that doesn't base its representation
 * on the JG address but on the computed node name added to the IPAddress instead.
 * This is to avoid any problem in the cluster as some nodes may interpret a node name
 * differently (IP resolution, name case, FQDN or host name, etc.)
 *
 * @see org.jboss.ha.framework.server.ClusterPartition
 *
 * @author  <a href="mailto:sacha.labourey@jboss.org">Sacha Labourey</a>.
 * @version $Revision: 57188 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>August 17 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li> 
 * </ul>
 */

public class ClusterNode
   implements Comparable, Cloneable, Serializable
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   protected String id = null;
   protected String jgId = null;
   protected IpAddress originalJGAddress = null;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
   public ClusterNode()
   {
   }

   public ClusterNode(IpAddress jgAddress)
   {
      if (jgAddress.getAdditionalData() == null)
      {
         this.id = jgAddress.getIpAddress().getHostAddress() + ":" + jgAddress.getPort();
      }
      else
      {
         this.id = new String(jgAddress.getAdditionalData());
      }

      this.originalJGAddress = jgAddress;
      StringBuffer sb = new StringBuffer();
      java.net.InetAddress jgIPAddr = jgAddress.getIpAddress();
      if (jgIPAddr == null)
         sb.append("<null>");
      else
      {
         if (jgIPAddr.isMulticastAddress())
            sb.append(jgIPAddr.getHostAddress());
         else
            sb.append(getShortName(jgIPAddr.getHostName()));
      }
      sb.append(":" + jgAddress.getPort());
      this.jgId = sb.toString();
   }

   // Public --------------------------------------------------------

   public String getName()
   {
      return this.id;
   }

   public String getJGName()
   {
      return this.jgId;
   }

   public IpAddress getOriginalJGAddress()
   {
      return this.originalJGAddress;
   }
   public InetAddress getIpAddress()
   {
      return this.originalJGAddress.getIpAddress();
   }
   public int getPort()
   {
      return this.originalJGAddress.getPort();      
   }

   // Comparable implementation ----------------------------------------------

   // Comparable implementation ----------------------------------------------

   public int compareTo(Object o)
   {
      if ((o == null) || !(o instanceof ClusterNode))
         throw new ClassCastException("ClusterNode.compareTo(): comparison between different classes");

      ClusterNode other = (ClusterNode) o;

      return this.id.compareTo(other.id);
   }
   // java.lang.Object overrides ---------------------------------------------------

   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof ClusterNode)) return false;
      
      ClusterNode other = (ClusterNode) obj;
      return this.id.equals(other.id);
   }

   public int hashCode()
   {
      return id.hashCode();
   }

   public String toString()
   {
      return this.getName();
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   protected String getShortName(String hostname)
   {
      int index = hostname.indexOf('.');

      if (hostname == null) return "";
      if (index > 0 && !Character.isDigit(hostname.charAt(0)))
         return hostname.substring(0, index);
      else
         return hostname;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
