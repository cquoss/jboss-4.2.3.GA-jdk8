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
package org.jboss.test.cmp2.cmr.test;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.rmi.PortableRemoteObject;

import junit.framework.*;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.cmr.interfaces.CMRBugManagerEJB;
import org.jboss.test.cmp2.cmr.interfaces.CMRBugManagerEJBHome;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class CMRPostCreatesWrittenUnitTestCase extends JBossTestCase
{

   public CMRPostCreatesWrittenUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(CMRPostCreatesWrittenUnitTestCase.class, "cmr-postcreateswritten.jar");
   }

   /**
    * This tests directly for bug 523627 and is based on the test case supplied by
    * Michael Newcomb.  It tests whether changes made in ejbPostCreate are committed.
    * It also tests indirectly for bug 523239, since it uses xdoclet.  It reports the same
    * error as seen in bug 523239, from GlobalTxEntityMap.
    */
   public void testCMRWrittenAfterPostCreate() throws Exception
   {
      getLog().debug("looking up CMRBugManager");
      Object ref = getInitialContext().lookup("CMRBugManager");
      getLog().debug("found CMRBugManager");

      CMRBugManagerEJBHome home = (CMRBugManagerEJBHome)
         PortableRemoteObject.narrow(ref, CMRBugManagerEJBHome.class);

      getLog().debug("creating CMRBugManagerEJB");
      CMRBugManagerEJB cmrBugManager = home.create();
      getLog().debug("created CMRBugManagerEJB");

      SortedMap cmrBugs = new TreeMap();
      cmrBugs.put("1", "one");
      cmrBugs.put("1.1", "one.one");
      cmrBugs.put("1.2", "one.two");
      cmrBugs.put("1.3", "one.three");

      getLog().debug("creating " + cmrBugs.size() + " CMR bugs");
      cmrBugManager.createCMRBugs(cmrBugs);
      getLog().debug("created " + cmrBugs.size() + " CMR bugs");

      Iterator i = cmrBugs.entrySet().iterator();
      while(i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();

         String[] parentIdAndDescription =
            cmrBugManager.getParentFor((String)entry.getKey());
         if(!entry.getKey().equals("1"))
         {
            assertTrue("Child has not Parent! cmr post create updates NOT WRITTEN! " + entry.getKey(),
               parentIdAndDescription != null);
         } // end of if ()

         String parentMsg = parentIdAndDescription == null ? " has no parent" :
            (" parent is " + parentIdAndDescription[0] + "-" +
            parentIdAndDescription[1]);
         getLog().debug(entry.getKey() + "-" + entry.getValue() + parentMsg);
      }
   }

   /**
    * Tests correct foreign key state initialization when the foreign key
    * loaded is not a valid (stale) value, i.e. the relationship was already changed in the tx.
    */
   public void testLoadFKState() throws Exception
   {
      Object ref = getInitialContext().lookup("CMRBugManager");
      CMRBugManagerEJBHome home = (CMRBugManagerEJBHome)PortableRemoteObject.narrow(ref, CMRBugManagerEJBHome.class);
      CMRBugManagerEJB manager = home.create();

      try
      {
         // create bugs
         manager.setupLoadFKState();

         // update
         manager.moveLastNodeBack();

         // check results
         assertTrue("The last element is the last in the chain.", !manager.lastHasNextNode());
      }
      finally
      {
         manager.tearDownLoadFKState();
      }
   }
}
