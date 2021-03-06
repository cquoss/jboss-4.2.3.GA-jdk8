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
package org.jboss.console.twiddle.command;

import java.io.PrintWriter;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.util.Strings;

/** Query the MBeanInfo for an MBean
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57191 $
 */
public class InfoCommand
   extends MBeanServerCommand
{
   private ObjectName objectName;

   public InfoCommand()
   {
      super("info", "Get the metadata for an MBean");
   }

   public void displayHelp()
   {
      PrintWriter out = context.getWriter();

      out.println(desc);
      out.println();
      out.println("usage: " + name + " <mbean-name>");
      out.println("  Use '*' to query for all attributes");
      out.flush();
   }

   private boolean processArguments(final String[] args)
      throws CommandException
   {
      log.debug("processing arguments: " + Strings.join(args, ","));

      if (args.length == 0)
      {
         throw new CommandException("Command requires arguments");
      }

      String sopts = "-:";
      LongOpt[] lopts =
         {
         };

      Getopt getopt = new Getopt(null, args, sopts, lopts);
      getopt.setOpterr(false);

      int code;
      int argidx = 0;

      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
               throw new CommandException
                  ("Option requires an argument: " + args[getopt.getOptind() - 1]);

            case '?':
               throw new CommandException
                  ("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

               // non-option arguments
            case 1:
               {
                  String arg = getopt.getOptarg();

                  switch (argidx++)
                  {
                     case 0:
                        objectName = createObjectName(arg);
                        log.debug("mbean name: " + objectName);
                        break;
                  }
                  break;
               }
         }
      }

      return true;
   }

   public void execute(String[] args) throws Exception
   {
      processArguments(args);

      if (objectName == null)
         throw new CommandException("Missing object name");

      MBeanServerConnection server = getMBeanServer();
      MBeanInfo mbeanInfo = server.getMBeanInfo(objectName);
      MBeanAttributeInfo[] attrInfo = mbeanInfo.getAttributes();
      MBeanOperationInfo[] opInfo = mbeanInfo.getOperations();

      PrintWriter out = context.getWriter();
      out.println("Description: "+mbeanInfo.getDescription());
      out.println("+++ Attributes:");
      int length = attrInfo != null ? attrInfo.length : 0;
      for(int n = 0; n < length; n ++)
      {
         MBeanAttributeInfo info = attrInfo[n];
         out.print(" Name: ");
         out.println(info.getName());
         out.print(" Type: ");
         out.println(info.getType());
         String rw = "";
         if( info.isReadable() )
            rw = "r";
         else
            rw = "-";
         if( info.isWritable() )
            rw += "w";
         else
            rw += "-";
         out.print(" Access: ");
         out.println(rw);
      }

      out.println("+++ Operations:");
      length = opInfo != null ? opInfo.length : 0;
      for(int n = 0; n < length; n ++)
      {
         MBeanOperationInfo info = opInfo[n];
         out.print(' ');
         out.print(info.getReturnType());
         out.print(' ');
         out.print(info.getName());
         out.print('(');
         MBeanParameterInfo[] sig = info.getSignature();
         for(int s = 0; s < sig.length; s ++)
         {
            out.print(sig[s].getType());
            out.print(' ');
            out.print(sig[s].getName());
            if( s < sig.length-1 )
               out.print(',');
         }
         out.println(')');
      }
   }
}
