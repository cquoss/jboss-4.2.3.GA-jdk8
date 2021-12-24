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
package org.jboss.aspects.remoting;

import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;

import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57186 $
 */
public class FamilyWrapper implements java.io.Externalizable
{
   private static final long serialVersionUID = 3880844152274576311L;

   private FamilyClusterInfo info;

   public FamilyWrapper() {}

   public FamilyWrapper(String proxyFamilyName, ArrayList targets)
   {
      info = ClusteringTargetsRepository.initTarget(proxyFamilyName, targets);
   }

   public FamilyClusterInfo get() { return info; }

   public void writeExternal(final java.io.ObjectOutput out)
      throws IOException
   {       
      out.writeObject(info.getFamilyName());
      out.writeObject(info.getTargets());
   }
   
   /**
   *  Un-externalize this instance.
   *
   *  We check timestamps of the interfaces to see if the instance is in the original VM of creation
   */
   public void readExternal(final java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      String proxyFamilyName = (String)in.readObject();
      ArrayList targets = (ArrayList)in.readObject();
      // keep a reference on our family object
      //
      this.info = ClusteringTargetsRepository.initTarget(proxyFamilyName, targets);
   }
   
}
