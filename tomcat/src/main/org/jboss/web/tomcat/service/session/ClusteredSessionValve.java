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
package org.jboss.web.tomcat.service.session;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;

/**
 * This Valve detects all sessions that were used in a request. All sessions are given to a snapshot
 * manager that handles the distribution of modified sessions.
 * <p/>
 * TOMCAT 4.1.12 UPDATE: Added findLifecycleListeners() to comply with the latest
 * Lifecycle interface.
 *
 * @author Thomas Peuss <jboss@peuss.de>
 * @author Brian Stansberry
 * @version $Revision: 57206 $
 */
public class ClusteredSessionValve extends ValveBase implements Lifecycle
{
   // The info string for this Valve
   private static final String info = "ClusteredSessionValve/1.0";

   // Valve-lifecycle_ helper object
   protected LifecycleSupport support = new LifecycleSupport(this);
   
   protected Manager manager;

   /**
    * Create a new Valve.
    */
   public ClusteredSessionValve(Manager manager)
   {
      this.manager = manager;
   }

   /**
    * Get information about this Valve.
    */
   public String getInfo()
   {
      return info;
   }

   /**
    * Valve-chain handler method.
    * This method gets called when the request goes through the Valve-chain. Our session replication mechanism replicates the
    * session after request got through the servlet code.
    *
    * @param request  The request object associated with this request.
    * @param response The response object associated with this request.
    */
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      // Initialize the context and store the request and response objects 
      // for any clustering code that has no direct access to these objects
      SessionReplicationContext.enterWebapp(request, response, true);
      try
      {  
         // Workaround to JBAS-5735. Ensure we get the session from the manager
         // rather than a cached ref from the Request.
         String requestedId = request.getRequestedSessionId();
         if (requestedId != null)
         {
            manager.findSession(requestedId);
         }
         
         // let the servlet invocation go through
         getNext().invoke(request, response);
      }
      finally // We replicate no matter what
      {
         // --> We are now after the servlet invocation
         try
         {
            SessionReplicationContext ctx = SessionReplicationContext.exitWebapp();
            
            if (ctx.getSoleSnapshotManager() != null)
            {
               ctx.getSoleSnapshotManager().snapshot(ctx.getSoleSession());
            }
            else
            {
               // Cross-context request touched multiple sesssions;
               // need to replicate them all
               Map sessions = ctx.getCrossContextSessions();
               if (sessions != null && sessions.size() > 0)
               {
                  for (Iterator iter = sessions.entrySet().iterator(); iter.hasNext();)
                  {
                     Map.Entry entry = (Map.Entry) iter.next();               
                     ((SnapshotManager) entry.getValue()).snapshot((ClusteredSession) entry.getKey());
                  }
               }
            }
         }
         finally
         {
            SessionReplicationContext.finishCacheActivity();
         }
         
      }
   }

   // Lifecylce-interface
   public void addLifecycleListener(LifecycleListener listener)
   {
      support.addLifecycleListener(listener);
   }

   public void removeLifecycleListener(LifecycleListener listener)
   {
      support.removeLifecycleListener(listener);
   }

   public LifecycleListener[] findLifecycleListeners()
   {
      return support.findLifecycleListeners();
   }

   public void start() throws LifecycleException
   {
      support.fireLifecycleEvent(START_EVENT, this);
   }

   public void stop() throws LifecycleException
   {
      support.fireLifecycleEvent(STOP_EVENT, this);
   }

}
