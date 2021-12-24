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
package org.jboss.ejb3.test.timer;

import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Service
@Management(ServiceManagement.class)
@Remote(LifecycleRemote.class)
public class LifecycleTimerTesterService implements ServiceManagement, LifecycleRemote
{
   private static final Logger log = Logger.getLogger(LifecycleTimerTesterService.class);
   
   private @Resource TimerService timerService;
 
   private Timer timer;
   private int timersStarted = 0;
   private int timersFired = 0;
   private boolean restarted = false;
   
   @Timeout
   public void timeoutHandler(Timer timer)
   {
      log.info("*** EJB TIMEOUT " + timer + " " + timer.getInfo());
      ++timersFired;
      
      if (restarted)
         timer = createTimer();
   }
   
   public void restartTimer()
   {
      timer = createTimer();
      restarted = true;
   }
   
   public int timersStarted()
   {
      return timersStarted;
   }
   
   public int timersFired()
   {
      return timersFired;
   }
   
   public void create() throws Exception
   {
      ++timersStarted;
      timer = createTimer();
      log.info("*** created timer " + timer);
   }
   
   public void start() throws Exception
   {
      
   }
   
   public void stop()
   {
      
   }
   
   public void destroy()
   {
      
   }
   
   protected Timer createTimer()
   {
      return timerService.createTimer(new Date(new Date().getTime() + 5000), "LifecycleTimerTesterService Timer #" + timersStarted);
   }
}
