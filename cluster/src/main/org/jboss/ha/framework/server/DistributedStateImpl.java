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
package org.jboss.ha.framework.server;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;
import org.jboss.logging.Logger;
import org.jboss.system.Registry;

/**
 *   This class manages distributed state across the cluster.
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author  <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 57188 $
 */
public class DistributedStateImpl
   implements DistributedStateImplMBean, HAPartitionStateTransfer
{
   // Constants -----------------------------------------------------

   protected final static String SERVICE_NAME = "DistributedState";

   protected final static Class[] set_types=new Class[]{String.class, Serializable.class, Serializable.class};
   protected final static Class[] remove_types=new Class[]{String.class, Serializable.class};

   // Attributes ----------------------------------------------------

   /**
    * HashMap<String, HashMap>. Keys= category, value = HashMap<Object, Object>
    */
   protected HashMap categories = new HashMap ();


   protected HashMap keyListeners = new HashMap ();
   protected HAPartition partition;
   protected Logger log;
   protected MBeanServer mbeanServer = null;
   protected String name = null;

   // Static --------------------------------------------------------c

   // Constructors --------------------------------------------------

   public DistributedStateImpl () {} // for JMX checks

   public DistributedStateImpl (HAPartition partition, MBeanServer server)
   {
      this.partition = partition;
      this.mbeanServer = server;
      this.log = Logger.getLogger (this.getClass ());
   }

   // Public --------------------------------------------------------

   public void init () throws Exception
   {
      // When we subscribe to state transfer events, GetState will be called to initialize
      // this service.
      partition.subscribeToStateTransferEvents (SERVICE_NAME, this);
      partition.registerRPCHandler (SERVICE_NAME, this);

      // subscribed this "sub-service" of HAPartition with JMX
      // TODO: In the future (when state transfer issues will be completed),
      // we will need to redesign the way HAPartitions and its sub-protocols are
      // registered with JMX. They will most probably be independant JMX services.
      //
      this.name = "jboss:service=" + SERVICE_NAME +
                    ",partitionName=" + this.partition.getPartitionName();
      ObjectName jmxName = new ObjectName(this.name);
      mbeanServer.registerMBean(this, jmxName);
      Registry.bind (this.name, this);
   }

   public void start () throws Exception
   {
   }

   public void stop () throws Exception
   {
       // NR 200505:[JBCLUSTER-38] will be done at destroy instead
//      Registry.unbind (this.name);
//      ObjectName jmxName = new ObjectName(this.name);
//      mbeanServer.unregisterMBean (jmxName);
   }

    // NR 200505 : [JBCLUSTER-38] unbind DS MBean
   public void destroy () throws Exception
   {
      Registry.unbind (this.name);
      ObjectName jmxName = new ObjectName(this.name);
      mbeanServer.unregisterMBean (jmxName);
      
      partition.unsubscribeFromStateTransferEvents (SERVICE_NAME, this);
      partition.unregisterRPCHandler(SERVICE_NAME, this);
   }

   public String listContent () throws Exception
   {
      StringBuffer result = new StringBuffer ();
      Collection cats = this.getAllCategories ();
      Iterator catsIter = cats.iterator ();
      while (catsIter.hasNext ())
      {
         String category = (String)catsIter.next ();
         Iterator keysIter = this.getAllKeys(category).iterator ();

         result.append ("-----------------------------------------------\n");
         result.append ("Category : ").append (category).append ("\n\n");
         result.append ("KEY\t:\tVALUE\n");

         while (keysIter.hasNext ())
         {
            Serializable key = (Serializable) keysIter.next ();
            String value = this.get (category, key).toString ();
            result.append ("'").append(key);
            result.append ("'\t:\t'");
            result.append (value);
            result.append("'\n");
         }
         result.append ("\n");
      }
      return result.toString ();
   }

   public String listXmlContent () throws Exception
   {
      StringBuffer result = new StringBuffer ();
      Collection cats = this.getAllCategories ();
      Iterator catsIter = cats.iterator ();

      result.append ("<DistributedState>\n");

      while (catsIter.hasNext ())
      {
         String category = (String)catsIter.next ();
         Iterator keysIter = this.getAllKeys(category).iterator ();

         result.append ("\t<Category>\n");
         result.append ("\t\t<CategoryName>").append (category).append ("</CategoryName>\n");

         while (keysIter.hasNext ())
         {
            Serializable key = (Serializable) keysIter.next ();
            String value = this.get (category, key).toString ();
            result.append ("\t\t<Entry>\n");
            result.append ("\t\t\t<Key>").append (key).append ("</Key>\n");
            result.append ("\t\t\t<Value>").append (value).append ("</Value>\n");
            result.append ("\t\t</Entry>\n");
         }
         result.append ("\t</Category>\n");
      }
      result.append ("</DistributedState>\n");

      return result.toString ();
   }

   // DistributedState implementation ----------------------------------------------

   public void set (String category, Serializable key, Serializable value)
      throws Exception
   {
      set (category, key, value, true);
   }
   public void set (String category, Serializable key, Serializable value,
      boolean asynchronousCall) throws Exception
   {
      Object[] args = {category, key, value};
      if (asynchronousCall)
         partition.callAsynchMethodOnCluster (SERVICE_NAME, "_set", args, set_types, true);
      else
         partition.callMethodOnCluster (SERVICE_NAME, "_set", args, set_types, true);
      this._setInternal (category, key, value);
      notifyKeyListeners (category, key, value, true);
   }

   public Serializable remove (String category, Serializable key)
      throws Exception
   {
      return remove (category, key, true);
   }

   public Serializable remove (String category, Serializable key,
      boolean asynchronousCall) throws Exception
   {
      Object[] args = {category, key};
      if (asynchronousCall)
         partition.callAsynchMethodOnCluster (SERVICE_NAME, "_remove", args, remove_types, true);
      else
         partition.callMethodOnCluster (SERVICE_NAME, "_remove", args, remove_types, true);
      Serializable removed = this._removeInternal (category, key);
      notifyKeyListenersOfRemove (category, key, removed , true);
      return removed ;
   }

   public Serializable get (String category, Serializable key)
   {
      synchronized(this.categories)
      {
         HashMap cat = (HashMap)categories.get (category);
         if (cat == null) return null;

         return (Serializable)cat.get (key);
      }
   }

   public Collection getAllCategories ()
   {
      synchronized(this.categories)
      {
         return Collections.unmodifiableCollection(categories.keySet ());
      }
   }

   public Collection getAllKeys (String category)
   {
      synchronized(this.categories)
      {
         HashMap cat = (HashMap)categories.get (category);
         if (cat == null) return null;

         return Collections.unmodifiableCollection(cat.keySet ());
      }
   }

   public Collection getAllValues (String category)
   {
      synchronized(this.categories)
      {
         HashMap cat = (HashMap)categories.get (category);
         if (cat == null) return null;

         return Collections.unmodifiableCollection(cat.values ());
      }
   }


   public void registerDSListenerEx (String category, DSListenerEx subscriber)
   {
      registerListener(category, subscriber);
   }
   public void unregisterDSListenerEx (String category, DSListenerEx subscriber)
   {
      unregisterListener(category, subscriber);
   }
   public void registerDSListener (String category, DSListener subscriber)
   {
      registerListener(category, subscriber);
   }
   public void unregisterDSListener (String category, DSListener subscriber)
   {
      unregisterListener(category, subscriber);
   }

   // HAPartition RPC method invocations implementation ----------------------------------------------

   public void _set (String category, String key, Serializable value)
      throws Exception
   {
      this._setInternal (category, key, value);
      notifyKeyListeners (category, key, value, false);
   }
   public void _set (String category, Serializable key, Serializable value)
      throws Exception
   {
      this._setInternal (category, key, value);
      notifyKeyListeners (category, key, value, false);
   }

   public void _setInternal (String category, Serializable key, Serializable value)
      throws Exception
   {
      synchronized(this.categories)
      {
         HashMap cat = (HashMap)categories.get (category);
         if (cat == null)
         {
            cat = new HashMap ();
            categories.put (category, cat);
         }
         cat.put (key, value);
      }
   }

   public void _remove (String category, String key) throws Exception
   {
      Serializable removed = this._removeInternal (category, key);
      notifyKeyListenersOfRemove (category, key, removed, false);
   }
   public void _remove (String category, Serializable key) throws Exception
   {
      Serializable removed = this._removeInternal (category, key);
      notifyKeyListenersOfRemove (category, key, removed, false);
   }

   public Serializable _removeInternal (String category, Serializable key)
      throws Exception
   {
      synchronized(this.categories)
      {
         HashMap cat = (HashMap)categories.get (category);
         if (cat == null) return null;
         Object removed = cat.remove (key);
         if (removed != null)
         {
            if (cat.size () == 0)
            {
               categories.remove (category);
            }
         }
         return (Serializable)removed;
      }
   }

   // HAPartitionStateTransfer implementation ----------------------------------------------

   public Serializable getCurrentState ()
   {
      HashMap retval=new HashMap();
      Map.Entry entry;
      String catName;
      HashMap value, newVal;

      synchronized(this.categories)
      {

         for(Iterator it=this.categories.entrySet().iterator(); it.hasNext();)
         {
            entry=(Map.Entry)it.next(); // key: category name, value: HashMap
            catName=(String)entry.getKey();
            value=(HashMap)entry.getValue();
            newVal=value != null? (HashMap)value.clone() : null;
            retval.put(catName, newVal);
         }

         return retval;
      }
   }

   public void setCurrentState (Serializable newState)
   {
      synchronized (this.categories)
      {
         categories.clear ();
         categories.putAll ((HashMap)newState);
         if (keyListeners.size () > 0)
         {
            cleanupKeyListeners ();
         }
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void registerListener (String category, Object subscriber)
   {
      synchronized(this.keyListeners)
      {
         ArrayList listeners = (ArrayList)keyListeners.get (category);
         if (listeners == null)
         {
            listeners = new ArrayList ();
            keyListeners.put (category, listeners);
         }
         listeners.add (subscriber);
      }
   }

   protected void unregisterListener (String category, Object subscriber)
   {
      synchronized(this.keyListeners)
      {
         ArrayList listeners = (ArrayList)keyListeners.get (category);
         if (listeners == null) return;

         listeners.remove (subscriber);
         if (listeners.size () == 0)
         {
            keyListeners.remove (category);
         }
      }
   }

   protected void notifyKeyListeners (String category, Serializable key,
      Serializable value, boolean locallyModified)
   {
      synchronized(this.keyListeners)
      {
         ArrayList listeners = (ArrayList)keyListeners.get (category);
         if (listeners == null)
            return;
         String strKey = key.toString();

         for (int i = 0; i < listeners.size (); i++)
         {
            Object listener = listeners.get (i);
            if( listener instanceof DSListener )
            {
               DSListener dslistener = (DSListener) listener;
               dslistener.valueHasChanged (category, strKey, value, locallyModified);
            }
            else
            {
               DSListenerEx dslistener = (DSListenerEx) listener;
               dslistener.valueHasChanged (category, key, value, locallyModified);
            }
         }
      }
   }

   protected void notifyKeyListenersOfRemove (String category, Serializable key,
      Serializable oldContent, boolean locallyModified)
   {
      synchronized(this.keyListeners)
      {
         ArrayList listeners = (ArrayList)keyListeners.get (category);
         if (listeners == null)
            return;
         String strKey = key.toString();

         for (int i = 0; i < listeners.size (); i++)
         {
            Object listener = listeners.get (i);
            if( listener instanceof DSListener )
            {
               DSListener dslistener = (DSListener) listener;
               dslistener.keyHasBeenRemoved (category, strKey, oldContent, locallyModified);
            }
            else
            {
               DSListenerEx dslistener = (DSListenerEx) listener;
               dslistener.keyHasBeenRemoved (category, key, oldContent, locallyModified);
            }
         }
      }
   }

   protected void cleanupKeyListeners ()
   {
      // NOT IMPLEMENTED YET
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
