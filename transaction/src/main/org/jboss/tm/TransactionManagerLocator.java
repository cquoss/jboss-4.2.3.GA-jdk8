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
package org.jboss.tm;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.util.NestedRuntimeException;

/**
 * Locates the transaction manager.
 * 
 * @todo this really belongs in some integration layer with
 *       a more pluggable implementation
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57208 $
 */
public class TransactionManagerLocator
{
   /** Logger */
   private static final Logger log = Logger.getLogger(TransactionManagerLocator.class);
   
   /** The instance */ 
   private static TransactionManagerLocator instance = new TransactionManagerLocator();
   
   /** The transaction manager */
   private TransactionManager tm;
   
   /**
    * No external construction
    */
   private TransactionManagerLocator()
   {
   }
   
   /**
    * Get the the locator
    *  
    * @return the locator
    */
   public static TransactionManagerLocator getInstance()
   {
      return instance;
   }
   
   /**
    * Locate the transaction manager
    * 
    * @return the transaction manager
    */
   public TransactionManager locate()
   {
      if (tm != null)
         return tm;

      TransactionManager result = tryJNDI();
      if (result == null)
         result = usePrivateAPI();
      if (result == null)
         throw new NestedRuntimeException("Unable to locate the transaction manager");
      
      return result;
   }
   
   /**
    * Locate the transaction manager in the well known jndi binding for JBoss
    * 
    * @return the tm from jndi
    */
   protected TransactionManager tryJNDI()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         tm = (TransactionManager) ctx.lookup(TransactionManagerService.JNDI_NAME);
         if (log.isTraceEnabled())
            log.trace("Got a transaction manager from jndi " + tm);
      }
      catch (NamingException e)
      {
         log.trace("Unable to lookup: " + TransactionManagerService.JNDI_NAME, e);
      }
      return tm;
   }
   
   /**
    * Use the private api<p>
    * 
    * This is a fallback method for non JBossAS use.
    * 
    * @return the tm from the private api
    */
   protected TransactionManager usePrivateAPI()
   {
      log.trace("Using the JBoss transaction manager");
      return TxManager.getInstance();
   }
}
