/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.invokerha;

import java.rmi.ServerException;

import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.logging.Logger;

import junit.framework.TestCase;

/**
 * InvokerHaFailureType.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public enum InvokerHaFailureType 
{
   BEFORE_SERVER,
   AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER,
   AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS,
   AFTER_SERVER_COMPLETED;      

   private static final Logger log = Logger.getLogger(InvokerHaFailureType.class);
   
   public boolean isRecoverable(boolean injectFailureIn1stCall)
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            /* a failure before even getting to the invocation call itself is 
             * never recoverable */
            return false;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            /* - if failure happens after reaching the server but didn't complete, and 
             * it happens in the 1st transactional invocation, it's recoverable as 
             * long as the failover call succeeds, otherwise (Nth invocation) it's 
             * not recoverable. */
            if (injectFailureIn1stCall)
            {
               return true;
            }
            return false;
            
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            /* if failure happens after reaching the server but didn't complete when 
             * trying to call either server, neither in the 1st or Nth call is 
             * recoverable.*/
            return false;
            
         case AFTER_SERVER_COMPLETED:
            /* if failure happens after reaching the server and completed, neither 
             * 1st or Nth call are recoverable. */
            return false;
            
         default:
            return false;
      }
   }
   
   public void injectFailureIfExistsBeforeServer() throws IllegalStateException
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            log.debug("failing because of " + this);
            throw new IllegalStateException("see how you handle this!!");
      }
   }
   
   public void injectFailureIfExistsAfterServer(Integer failoverCounter) throws GenericClusteringException
   {
      switch(this)
      {
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            if (failoverCounter.equals(new Integer(0)))
            {
               log.debug("failing because of " + this);
               throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, this.toString());               
            }
            break;

         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            log.debug("failing because of " + this);
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, this.toString());
            
         case AFTER_SERVER_COMPLETED:
            log.debug("failing because of " + this);
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_YES, this.toString());
      }
   }

   public void assertFailoverCounter(boolean injectFailureIn1stCall, Object failoverCounter)
   {
      switch(this)
      {
         case BEFORE_SERVER:
            TestCase.assertEquals(0, failoverCounter);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            TestCase.assertEquals(1, failoverCounter);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            if (injectFailureIn1stCall)
            {
               /* Two failovers were attempted before finally giving up */
               TestCase.assertEquals(2, failoverCounter);
            }
            else
            {
               /* It's 1 because after several calls, the transaction has 
                * already reached the server, so that's good enough not to 
                * succeed */
               TestCase.assertEquals(1, failoverCounter);
            }
            break;
            
         case AFTER_SERVER_COMPLETED:
            /* failover counters are always 0 because there's no chance of 
             * calculating failover at all */
            TestCase.assertEquals(0, failoverCounter);
            break;
      }
   }
   
   public void assertException(Exception e)
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            TestCase.assertTrue(e instanceof IllegalStateException);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            TestCase.assertTrue(e instanceof ServiceUnavailableException);
            TestCase.assertTrue(e.getCause() instanceof GenericClusteringException);
            break;
            
         case AFTER_SERVER_COMPLETED:
            TestCase.assertTrue(e instanceof ServerException);
            TestCase.assertTrue(e.getCause() instanceof GenericClusteringException);
            break;
      }
   }
}
