/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.annotation.ejb.PoolClass;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
{
   @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
   @ActivationConfigProperty(propertyName="destination", propertyValue="queue/expirationmdbtest"),
   @ActivationConfigProperty(propertyName="maxSession", propertyValue="1")
})
public class ExpirationTestMDB implements MessageListener
{
   public void onMessage(Message message)
   {
      System.out.println("*** ExpirationTestMDB onMessage " + TestStatusBean.expirationQueueRan + " in " + this);
      System.out.println("*** now = " + System.currentTimeMillis());
      
//      if(TestStatusBean.expirationQueueRan > 0)
         System.out.println("*** message = " + message);
      
      try
      {
         Thread.sleep(1000);
      }
      catch(InterruptedException e)
      {
         // ignore
         System.out.println("*** ExpirationTestMDB interrupted!");
      }
      
      TestStatusBean.expirationQueueRan++;
   }

}
