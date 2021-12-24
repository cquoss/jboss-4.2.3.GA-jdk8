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
package org.jboss.test.ha.jmx;

import java.util.Stack;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.ha.jmx.HAServiceMBeanSupport;

/**
 * 
 * @author  Ivelin Ivanov <ivelin@jboss.org>
 *
 */
public class HAServiceMBeanSupportTester extends HAServiceMBeanSupport
{

  public Stack __invokationStack__ = new Stack();

  public boolean __isDRMMasterReplica__ = false;

  public boolean __isSingletonStarted__ = false;

  public boolean __shouldSendNotificationRemoteFail__ = false;

  protected void setupPartition() throws Exception
  {
    __invokationStack__.push("setupPartition");
  }

  protected void registerRPCHandler()
  {
    __invokationStack__.push("registerRPCHandler");
  }

  protected void unregisterRPCHandler()
  {
    __invokationStack__.push("unregisterRPCHandler");
  }

  protected void registerDRMListener() throws Exception
  {
    __invokationStack__.push("registerDRMListener");
  }

  protected void unregisterDRMListener() throws Exception
  {
    __invokationStack__.push("unregisterDRMListener");
  }

  protected boolean isDRMMasterReplica()
  {
    __invokationStack__.push("isDRMMasterReplica");
    return __isDRMMasterReplica__;
  }

  public void callMethodOnPartition(String methodName, Object[] args)
    throws Exception
  {
    __invokationStack__.push("callMethodOnCluster:" + methodName);
  }

  protected void sendNotificationRemote(Notification notification)
    throws Exception
  {
    if (__shouldSendNotificationRemoteFail__)
      throw new Exception("simulated exception");
    __invokationStack__.push("sendNotificationRemote");
    __invokationStack__.push(notification);
  }

  protected void sendNotificationToLocalListeners(Notification notification)
  {
    __invokationStack__.push("sendNotificationToLocalListeners");
    __invokationStack__.push(notification);
  }

  public ObjectName getServiceName()
  {
    ObjectName oname = null;
    try
    {
      oname = new ObjectName("jboss.examples:name=HAServiceMBeanSupportTester");
    }
    catch (MalformedObjectNameException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return oname;
  }

}
