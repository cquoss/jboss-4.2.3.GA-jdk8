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
package org.jboss.console.manager.interfaces.impl;

import org.jboss.console.navtree.AppletBrowser;
import org.jboss.console.navtree.AppletTreeAction;
import org.jboss.console.navtree.TreeContext;

import javax.management.ObjectName;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57191 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>3 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class MBeanAction 
   implements AppletTreeAction
{
   protected ObjectName targetObjectName = null;
   protected String actionName = null;
   protected Object[] params = null;
   protected String[] signature = null;

   public MBeanAction () {}
   
   public MBeanAction (ObjectName pName,
                        String pActionName,
                        Object[] pParams,
                        String[] pSignature) 
   {
      this.targetObjectName = pName;
      this.actionName = pActionName;
      this.params = pParams;
      this.signature = pSignature;
   }   

   public void doAction(TreeContext tc, AppletBrowser applet)
   {
      try
      {
         tc.getRemoteMBeanInvoker ().invoke(targetObjectName, actionName, params, signature);
      }
      catch (Exception displayed)
      {
         displayed.printStackTrace();
      }
   }

}
