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
package org.jboss.ha.framework.interfaces;

import org.jboss.util.NestedException;

/**
 * Generic clustering exception that can be used to replace other exceptions
 * that occur on the server. Thus, only this class is needed on the client side
 * and some specific server side exceptions class are not needed on the client side
 * (such as JMX exceptions for example). 
 * Furhtermore, it has support for "COMPLETED" status flag a la IIOP.
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>8 avril 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class GenericClusteringException
   extends NestedException
{
   // When an invocation reaches a node, it may be invoked on the actual
   // target or not (or not completely). If COMPLETED_NO and working in 
   // a clustered environment, the client proxy is allowed to invoke
   // the same invocation on a different node. Otherwise, it will depend
   // if the target method is idempotent: this is no more the problem of
   // this class but rather the meta-data of the business environment
   // that can give this information
   //
   public final static int COMPLETED_YES = 0;
   public final static int COMPLETED_NO = 1;
   public final static int COMPLETED_MAYBE = 2;
   
   public boolean isDefinitive = true;
   
   // if yes, then the invocation may be retried against another node
   // because the state has not been modified by the current invocation
   //
   public int completed;
   
   public GenericClusteringException ()
   {
      this.completed = this.COMPLETED_MAYBE;
   }
   
   public GenericClusteringException (int CompletionStatus)
   {
      this.completed = CompletionStatus;
   }

   public GenericClusteringException (int CompletionStatus, String s)
   {
      super(s);
      this.completed = CompletionStatus;
   }

   public GenericClusteringException (int CompletionStatus, String s, boolean isDefinitive)
   {
      super(s);
      this.completed = CompletionStatus;
      this.isDefinitive = isDefinitive;
   }

   public GenericClusteringException (int CompletionStatus, Throwable nested, boolean isDefinitive)
   {
      super(nested);
      this.completed = CompletionStatus;
      this.isDefinitive = isDefinitive;
   }
   
   public GenericClusteringException (int CompletionStatus, Throwable nested)
   {
      super(nested);
      this.completed = CompletionStatus;
   }
   
   public int getCompletionStatus ()
   {
      return this.completed;
   }
   
   public void setCompletionStatus (int completionStatus)
   {
      this.completed = completionStatus;
   }
   
   // Indicates if the exception will most probably be repetitive (definitive)
   // or if it is just a temporary failure and new attempts should be tried
   //
   public boolean isDefinitive ()
   {
      return this.isDefinitive;      
   }
   
   public void setIsDefinitive (boolean definitive)
   {
      this.isDefinitive = definitive;
   }
}
