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
package org.jboss.test.util.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Starts, stops, and (eventually) reboots server instances.
 * 
 * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
 * @version $Revision: 61205 $
 */
public abstract class ServerController
{

   private static final String SHUTDOWN_CLASS = "org.jboss.Shutdown";

   private static final String MAIN = "org.jboss.Main";

   private ServerController()
   {
   }

   /**
    * Start the server and pump its output and error streams.
    * 
    * @param server
    * @param manager
    * @throws IOException
    */
   public static void startServer(Server server, ServerManager manager) throws IOException
   {
      if (server.isRunning())
      {
         throw new IllegalArgumentException("The " + server.getName() + " server is already running.");
      }

      if (isServerStarted(server))
      {
         throw new IOException("Found a process already listening on:" + server.getHttpUrl() + " or "+ server.getRmiUrl());
      }

      String execCmd = getStartCommandLine(server, manager);

      System.out.println("Starting server \"" + server.getName() + "\" with command: \n" + execCmd);

      File binDir = new File(manager.getJBossHome(), "/bin");
      final Process process = Runtime.getRuntime().exec(execCmd, null, binDir);

      final BufferedReader errStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      final BufferedReader inStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

      final File outFile = server.getOutputLog();
      initalizeLog(outFile);
      final PrintWriter outlog = new PrintWriter(new FileWriter(outFile));
      server.setOutWriter(outlog);

      Thread outPump = new OutputPumper(inStream, outlog);
      outPump.start();

      final File errorFile = server.getErrorLog();
      initalizeLog(errorFile);
      final PrintWriter errorlog = new PrintWriter(new FileWriter(errorFile));
      server.setErrorWriter(errorlog);

      Thread errorPump = new OutputPumper(errStream, errorlog);
      errorPump.start();

      /*
       * TODO: -TME This is a real problem.  If maintain reference
       * to the process, even for a short period of time, willl
       * cause the spawned process' threads to block when this process
       * blocks.  So if uncomment following line, then the ServerTestHarness
       * will block abnormally, thus causing the tests not to run correctly.
       * 
       * Is this true for our environment? - rcampbell
       */
      server.setProcess(process);

      try
      {
         waitForServer(server, manager);
      }
      catch (IOException e)
      {
         server.setProcess(null);
         throw e;
      }

   }

   /** 
    * Delete & create log files
    * @param logFile
    * @throws IOException
    */
   private static void initalizeLog(final File logFile) throws IOException
   {
      if (logFile.exists())
      {
         logFile.delete();
      }
      if (!logFile.getParentFile().exists())
      {
         logFile.getParentFile().mkdir();
      }

      logFile.createNewFile();
   }

   /** 
    * Create the command line to execute
    * @param server
    * @param manager
    * @return
    * @throws IOException
    */
   private static String getStartCommandLine(Server server, ServerManager manager) throws IOException
   {
      String execCmd = manager.getJavaExecutable() + " -cp " + manager.getStartClasspath() + " ";
      execCmd = execCmd + server.getJvmArgs() + server.getSysProperties();
      execCmd = execCmd + " " + MAIN + " -c " + server.getConfig() + " -b " + server.getHost();
      
      if (manager.getUdpGroup() != null && ! manager.getUdpGroup().equals(""))
      {
         execCmd = execCmd + " -u " + manager.getUdpGroup();   
      }
      execCmd = execCmd + " " + server.getArgs();
      return execCmd;
   }

   /** 
    * Wait until the jboss instance is full initialized
    * @param server
    * @param manager 
    * @throws IOException
    */
   private static void waitForServer(Server server, ServerManager manager) throws IOException
   {

      int tries = 0;
      while (tries++ < manager.getStartupTimeout())
      {
         if (!server.isRunning())
         {
            throw new IOException("Server failed to start; see logs. exit code: " + server.getProcess().exitValue());
         }

         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
         }
         if (isServerStarted(server))
         {
            return;
         }
      }

      Process process = server.getProcess();
      System.err.println("Failed to start server \"" + server.getName()
            + "\" before timeout. Destroying the process.");
      process.destroy();

      throw new IOException("Server failed to start; see logs.");

   }

   /**
    * Check if the server is fully intialized by trying to 
    * open a connection to tomcat.
    * 
    * @param server
    * @return
    * @throws IOException
    */
   private static boolean isServerStarted(Server server) throws IOException
   {
      URL url = server.getHttpUrl();
      if (server.hasWebServer())
      {
         try
         {
            URLConnection conn = url.openConnection();
            if (conn instanceof HttpURLConnection)
            {
               HttpURLConnection http = (HttpURLConnection) conn;
               int responseCode = http.getResponseCode();

               if (responseCode > 0 && responseCode < 400)
               {
                  return true;
               }
            }
         }
         catch (java.io.IOException e)
         {
            return false;
         }
         return false;
      }
      else
      {
         //see if the rmi port is active
         Socket socket = null;
         try
         {
            socket = new Socket(server.getHost(), server.getRmiPort().intValue());
            return true;
         }
         catch (IOException e)
         {
            return false;
         }
         finally
         {
            if (socket != null)
            {
               socket.close();
            }
         }
      }
   }

   /**
    * Stop the server using shutdown.jar.
    * Process.destroy() the server if it fails to shutdown.
    * 
    * @param server
    * @param manager
    * @throws IOException
    */
   public static void stopServer(Server server, ServerManager manager) throws IOException
   {
      if (!server.isRunning())
      {
         throw new IllegalArgumentException("The " + server.getName() + " is not running; it cannot be stopped.");
      }

      System.out.println("Shutting down server: " + server.getName());

      String shutdownCmd = getStopCommandLine(server, manager);

      Runtime.getRuntime().exec(shutdownCmd);

      Process process = server.getProcess();
      if (!waitOnShutdown(server, manager))
      {
         System.err.println("Failed to shutdown server \"" + server.getName()
               + "\" before timeout. Destroying the process.");
         process.destroy();
      }

      closeAllStreams(process);
      server.getErrorWriter().close();
      server.getOutWriter().close();

      server.setProcess(null);
   }

   /**
    * Wait for the server to shutdown. 
    * @param server
    * @param manager 
    * @return true if server process ends before timeout
    */
   private static boolean waitOnShutdown(Server server, ServerManager manager)
   {
      int shutdownTimeout = manager.getShutdownTimeout();
      for (int tries = 0; tries < shutdownTimeout; tries++)
      {
         try
         {
            if (!server.isRunning())
            {
               return true;
            }
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
         }
      }

      return false;
   }

   /** 
    * Get the server shutdown command line.
    * @param server
    * @param manager
    * @return
    * @throws IOException
    */
   private static String getStopCommandLine(Server server, ServerManager manager) throws IOException
   {
      String execCmd = manager.getJavaExecutable() + " -cp " + manager.getStopClasspath() + " ";
      execCmd = execCmd + SHUTDOWN_CLASS + " --server " + server.getRmiUrl();
      execCmd = execCmd + " --shutdown";
      return execCmd;
   }

   /**
    * Close the streams of a process.
    * 
    * @param process
    */
   private static void closeAllStreams(Process process)
   {
      try
      {
         process.getInputStream().close();
         process.getOutputStream().close();
         process.getErrorStream().close();
      }
      catch (IOException e)
      {
      }
   }

   /**
    * A OutputPumper.  Redirect std err & out to log files.
    * 
    * @author <a href="ryan.campbell@jboss.com">Ryan Campbell</a>
    * @version $Revision: 61205 $
    */
   private static class OutputPumper extends Thread
   {
      private BufferedReader outputReader;

      private PrintWriter logWriter;

      public OutputPumper(BufferedReader outputReader, PrintWriter logWriter)
      {
         this.outputReader = outputReader;
         this.logWriter = logWriter;
      }

      public void run()
      {
         try
         {
            String line = null;
            while ((line = outputReader.readLine()) != null)
            {
               logWriter.println(line);
            }
         }
         catch (IOException e)
         {
         }
      }
   }

}
