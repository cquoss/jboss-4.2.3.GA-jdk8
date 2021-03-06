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
package org.jboss.console.plugins;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;
import org.jboss.console.manager.interfaces.impl.HttpLinkTreeAction;
import org.jboss.console.manager.interfaces.impl.MBeanResource;
import org.jboss.console.manager.interfaces.impl.SimpleTreeNodeMenuEntryImpl;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * As the number of MBeans is very big, we use a real Java class which is far
 * faster than beanshell
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 62134 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class EJBModuleLister extends AbstractPluginWrapper
{

   private static final long serialVersionUID = 4815998047707352041L;
   
   protected final static String JMX_JSR77_DOMAIN = "jboss.management.local";

   public EJBModuleLister()
   {
      super();
   }

   ResourceTreeNode[] createBeans(ObjectName parent) throws Exception
   {
      // there is a bug in the current jsr77 implementation with regard to naming
      // of EJBModule that are part of EARs => I've used a workaround
      //
      ObjectInstance[] insts = getMBeansForQuery(JMX_JSR77_DOMAIN + ":EJBModule=" + parent.getKeyProperty("name")
            + ",*", null);

      ResourceTreeNode[] ejbs = new ResourceTreeNode[insts.length];
      for (int i = 0; i < insts.length; i++)
      {
         ObjectName objName = insts[i].getObjectName();
         String type = objName.getKeyProperty("j2eeType");
         String ejbName = objName.getKeyProperty("name");

         String containerName = createContainerName(objName);

         if (containerName == null)
            continue;

         containerName = encode(containerName);
         String containerUrl = "/jmx-console/HtmlAdaptor?action=inspectMBean&name=" + containerName;

         TreeNodeMenuEntry[] menus = new TreeNodeMenuEntry[]
         {new SimpleTreeNodeMenuEntryImpl("View container in other window", new HttpLinkTreeAction(containerUrl,
               "_blank"))};

         String j2eeType = objName.getKeyProperty("j2eeType");
         String filename = "EJB.jsp";
         if (j2eeType.equalsIgnoreCase("StatelessSessionBean"))
         {
            filename = "StatelessEjb.jsp";
         }
         else if (j2eeType.equalsIgnoreCase("StatefulSessionBean"))
         {
            filename = "StatefulEjb.jsp";
         }
         else if (j2eeType.equalsIgnoreCase("EntityBean"))
         {
            filename = "EntityEjb.jsp";
         }
         else if (j2eeType.equalsIgnoreCase("MessageDrivenBean"))
         {
            filename = "MdbEjb.jsp";
         }

         ejbs[i] = createResourceNode(ejbName, // name
               type, // description
               "images/bean.gif", // Icon URL
               filename + "?ObjectName=" + encode(objName.toString()) + "&ContainerObjectName=" + containerName, // Default URL
               menus, null, // sub nodes
               null, // Sub-Resources                  
               objName.toString(), insts[i].getClassName());

      }

      return ejbs;
   }

   protected String createContainerName(ObjectName objName) throws Exception
   {
      String jndiName = (String) mbeanServer.getAttribute(objName, "JndiName");
      String localJndiName = (String) mbeanServer.getAttribute(objName, "LocalJndiName");

      String containerUrl = null;

      String defaultContainerUrl = "jboss.j2ee:service=EJB,jndiName=" + jndiName;
      ObjectName defaultObjectName = new ObjectName(defaultContainerUrl);
      String localContainerUrl = "jboss.j2ee:service=EJB,jndiName=" + localJndiName;
      ObjectName localObjectName = new ObjectName(localContainerUrl);

      if (mbeanServer.isRegistered(defaultObjectName))
      {
         log.debug("Found container with default JNDI name URL for - " + objName.toString());
         containerUrl = defaultContainerUrl;
      }
      else if (mbeanServer.isRegistered(localObjectName))
      {
         log.debug("Found container with local JNDI name URL for - " + objName.toString());
         containerUrl = localContainerUrl;
      }
      else
      {
         log.warn("Unable to find container for - " + objName.toString());
      }

      return containerUrl;
   }

   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         ObjectName objName = ((MBeanResource) resource).getObjectName();

         return createTreeNode(objName.getKeyProperty("name"), // name
               "", // description
               "images/beans.gif", // Icon URL
               "EJBModule.jsp?ObjectName=" + encode(objName.toString()), // Default URL
               null, null, // sub nodes
               createBeans(objName) // Sub-Resources                  
         ).setMasterNode(true);

      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println(checker);
         return null;

      }
   }

}
