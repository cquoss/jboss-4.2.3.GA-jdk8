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
import java.util.Set;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.util.Strings;

/**
 * Get information about the server.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57191 $
 */
public class ServerInfoCommand
   extends MBeanServerCommand
{
   public static final int UNKNOWN = 0;
   public static final int DEFAULT_DOMAIN = 1;
   public static final int MBEAN_COUNT = 2;
   public static final int LIST_NAMES = 3;
   
   private int mode = UNKNOWN;
   
   public ServerInfoCommand()
   {
      super("serverinfo", "Get information about the MBean server");
   }
   
   public void displayHelp()
   {
      PrintWriter out = context.getWriter();
      
      out.println(desc);
      out.println();
      out.println("usage: " + name + " [options]");
      out.println();
      out.println("options:");
      out.println("    -d, --domain    Get the default domain");
      out.println("    -c, --count     Get the MBean count");
      out.println("    -l, --list      List the MBeans");
      out.println("    --              Stop processing options");
   }

   private void processArguments(final String[] args)
      throws CommandException
   {
      log.debug("processing arguments: " + Strings.join(args, ","));

      if (args.length == 0)
      {
         throw new CommandException("Command requires arguments");
      }
      
      String sopts = "-:dcl";
      LongOpt[] lopts =
      {
         new LongOpt("domain", LongOpt.NO_ARGUMENT, null, 'd'),
         new LongOpt("count", LongOpt.NO_ARGUMENT, null, 'c'),
         new LongOpt("list", LongOpt.NO_ARGUMENT, null, 'l'),
      };

      Getopt getopt = new Getopt(null, args, sopts, lopts);
      getopt.setOpterr(false);
      
      int code;
      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
               throw new CommandException
                  ("Option requires an argument: "+ args[getopt.getOptind() - 1]);

            case '?':
               throw new CommandException
                  ("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

            // non-option arguments
            case 1:
               throw new CommandException("Unused argument: " + getopt.getOptarg());

            case 'd':
               mode = DEFAULT_DOMAIN;
               break;

            case 'c':
               mode = MBEAN_COUNT;
               break;
            case 'l':
               mode = LIST_NAMES;
               break;
         }
      }
   }

   public void execute(String[] args) throws Exception
   {
      processArguments(args);
      
      PrintWriter out = context.getWriter();
      MBeanServerConnection server = getMBeanServer();

      // mode should be valid, either invalid arg or no arg
      
      switch (mode)
      {
         case DEFAULT_DOMAIN:
            out.println(server.getDefaultDomain());
            break;

         case MBEAN_COUNT:
            out.println(server.getMBeanCount());
            break;

         case LIST_NAMES:
            ObjectName all = new ObjectName("*:*");
            Set names = server.queryNames(all, null);
            Iterator iter = names.iterator();
            while( iter.hasNext() )
               out.println(iter.next());
            break;

         default:
            throw new IllegalStateException("invalid mode: " + mode);
      }
      
      out.flush();
   }
}
