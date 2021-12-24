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
package org.jboss.javagroups.protocols;

import org.jgroups.Event;
import org.jgroups.Message;
import org.jgroups.stack.Protocol;
import org.jboss.logging.Logger;

import java.util.Properties;
import java.util.Map;
import java.util.Iterator;

/**
 * A trival implementation of Protocol that traces all activity through
 * it to its logger. This should be inserted between any two protocols
 * you which to view the events between. Its supports a name property that
 * allows you to insert the element multiple times in a stack to trace
 * multiple protocols. An example config for the ClusterPartition for such
 * a usage is:
 *
 <pre>
   <mbean code="org.jboss.ha.framework.server.ClusterPartition"
         name="jboss:service=JNDITestPartition">
    <!-- -->
    <attribute name="PartitionName">JNDITestPartition</attribute>
    <attribute name="PartitionConfig">
        <Config>
           <TCP start_port="50001" bind_addr="172.17.66.55" />
           <org.jboss.jgroups.protocols.EVENT_TRACE name="TCP-TCPPING-TRACE"
               up_thread="false" down_thread="false" />
           <TCPPING initial_hosts="lamia[50001]"
               port_range="1" timeout="15000"
                up_thread="false" down_thread="false" />
           <MERGE2 min_interval="5000" max_interval="20000" />
           <FD max_tries="4" timeout="15000" />
           <VERIFY_SUSPECT timeout="15000" />
           <pbcast.STABLE desired_avg_gossip="20000" />
           <pbcast.NAKACK gc_lag="50" retransmit_timeout="600,1200,2400,4800" />

           <org.jboss.jgroups.protocols.EVENT_TRACE name="NAKACK-GMS-TRACE"
            up_thread="false" down_thread="false" />
           <pbcast.GMS join_timeout="15000" join_retry_timeout="5000"
              shun="false" print_local_addr="true" />
           <pbcast.STATE_TRANSFER />
        </Config>
     </attribute>
  </mbean>
 </pre>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57188 $
 */
public class EVENT_TRACE extends Protocol
{
   private String name = "EVENT_TRACE";
   private Logger log;

   public String getName()
   {
      return name;
   }

   /**
    * @param props
    * @return
    */
   public boolean setProperties(Properties props)
   {
      super.setProperties(props);
      name = props.getProperty("name", name);
      log = Logger.getLogger("org.jboss.jgroups."+name);
      return true;
   }

   public void up(Event event)
   {
      if( log.isTraceEnabled() )
      {
         log.trace("up, event="+event);
         if( event.getType() == Event.MSG )
         {
            Message msg = (Message) event.getArg();
            msg.getHeaders();
            log.trace("up, MsgHeader: "+printEventMsg(msg));
         }
      }
      // Pass up the protocol stack
      passUp(event);
   }

   public void down(Event event)
   {
      if( log.isTraceEnabled() )
      {
         log.trace("down, event="+event);
         if( event.getType() == Event.MSG )
         {
            Message msg = (Message) event.getArg();
            log.trace("down, MsgHeader: "+printEventMsg(msg));
         }
      }
      // Pass down the protocol stack
      passDown(event);
   }

   public String printEventMsg(Message msg)
   {
      StringBuffer sb = new StringBuffer();
      Map.Entry entry;
      Map headers = msg.getHeaders();
      if (headers != null)
      {
         Iterator iter = headers.entrySet().iterator();
         while( iter.hasNext() )
         {
            entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            sb.append(key);
            sb.append(", value(");
            sb.append(value.getClass());
            sb.append("): ");
            sb.append(value.toString());
            sb.append("\n");
         }
      }
      return sb.toString();
   }

}