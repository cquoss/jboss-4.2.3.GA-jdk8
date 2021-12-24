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
package org.jboss.tools;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Encapsulates a class serialVersionUID and codebase.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57210 $
 */
public class ClassVersionInfo implements Serializable
{
   static final long serialVersionUID = 2036506209171911437L;

   /** The named class serialVersionUID as returned by ObjectStreamClass */
   private long serialVersion;
   /** The binary class name */
   private String name;
   private boolean hasExplicitSerialVersionUID;

   public ClassVersionInfo(String name, ClassLoader loader)
      throws ClassNotFoundException
   {
      this.name = name;
      Class c = loader.loadClass(name);
      if( c.isInterface() == false )
      {
         ObjectStreamClass osc = ObjectStreamClass.lookup(c);
         if( osc != null )
         {
            serialVersion = osc.getSerialVersionUID();
            try
            {
               c.getDeclaredField("serialVersionUID");
               hasExplicitSerialVersionUID = true;
            }
            catch(NoSuchFieldException e)
            {
               hasExplicitSerialVersionUID = false;
            }
         }
      }
   }

   public long getSerialVersion()
   {
      return serialVersion;
   }
   public boolean getHasExplicitSerialVersionUID()
   {
      return hasExplicitSerialVersionUID;
   }
   public String getName()
   {
      return name;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("ClassVersionInfo");
      tmp.append('{');
      tmp.append("serialVersion=");
      tmp.append(serialVersion);
      tmp.append(", hasExplicitSerialVersionUID=");
      tmp.append(hasExplicitSerialVersionUID);
      tmp.append(", name=");
      tmp.append(name);
      tmp.append('}');
      return tmp.toString();
   }

   /**
    * Utility main entry point that allows one to load a Map<String,ClassVersionInfo>
    * from a serialized object file to print a specific ClassVersionInfo
    * 
    * @param args [0] = map.ser file for serialized object image
    *    [1] = class name of the ClassVersionInfo to print 
    * @throws Exception
    */
   public static void main(String[] args)
      throws Exception
   {
      if( args.length != 2 )
      {
         throw new IllegalArgumentException("Usage ClassVersionInfo map.ser [-list|cvi_fqn]");
      }
      FileInputStream fis = new FileInputStream(args[0]);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map cvis = (Map) ois.readObject();
      ois.close();

      if( args[1].equals("-list") )
      {
         System.out.println(cvis.keySet());
         System.exit(0);
      }

      String fqn = args[1];
      if( cvis.containsKey(fqn) == false )
      {
         System.err.println("No entry found for: "+fqn);
         System.exit(1);
      }
      ClassVersionInfo cvi = (ClassVersionInfo) cvis.get(fqn);
      System.out.println(cvi);
      System.exit(0);
   }
}
