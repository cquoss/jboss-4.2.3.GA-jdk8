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
package org.jboss.ejb3.naming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.jboss.logging.Logger;

/**
 * A context which combines two contexts.
 * 
 * Read operations are combined, write operations are done on the first context.
 * All other operations are not supported.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MultiplexerContext implements Context, Serializable
{
   private static final long serialVersionUID = -2306711582586456135L;
   
   private static final Logger log = Logger.getLogger(MultiplexerContext.class);
   
   private Context contextOne;
   private Context contextTwo;

   public MultiplexerContext(Context contextOne, Context contextTwo) throws NamingException
   {
      assert contextOne != null;
      assert contextTwo != null;
      
      this.contextOne = contextOne;
      this.contextTwo = contextTwo;
   }
   
   private <T extends NameClassPair> void addAll(Collection<T> collection, NamingEnumeration<T> ne) throws NamingException
   {
      // TODO: how about duplicates?
      while(ne.hasMore())
      {
         T ncp = ne.next();
         collection.add(ncp);
      }
   }
   
   public Object addToEnvironment(String propName, Object propVal) throws NamingException
   {
      throw new OperationNotSupportedException();
   }

   public void bind(Name name, Object obj) throws NamingException
   {
      log.trace("bind: " + name + " -> " +obj);
      getContextOne().bind(name, obj);  
   }

   public void bind(String name, Object obj) throws NamingException
   {
      log.trace("bind: " + name + " -> " +obj);
      getContextOne().bind(name, obj);
   }

   public void close() throws NamingException
   {
      // do nothing
   }

   public Name composeName(Name name, Name prefix) throws NamingException
   {
      return getContextOne().composeName(name, prefix);
   }

   public String composeName(String name, String prefix) throws NamingException
   {
      return getContextOne().composeName(name, prefix);
   }

   public Context createSubcontext(Name name) throws NamingException
   {
      return getContextOne().createSubcontext(name);
   }

   public Context createSubcontext(String name) throws NamingException
   {
      return getContextOne().createSubcontext(name);
   }

   public void destroySubcontext(Name name) throws NamingException
   {
      getContextOne().destroySubcontext(name);
   }

   public void destroySubcontext(String name) throws NamingException
   {
      getContextOne().destroySubcontext(name);
   }

   private Context getContextOne() throws NamingException
   {
      return contextOne;
   }
   
   private Context getContextTwo() throws NamingException
   {
      return contextTwo;
   }
   
   public Hashtable<?, ?> getEnvironment() throws NamingException
   {
      throw new UnsupportedOperationException();
   }

   public String getNameInNamespace() throws NamingException
   {
      throw new UnsupportedOperationException();
   }

   public NameParser getNameParser(Name name) throws NamingException
   {
      return getContextOne().getNameParser(name);
   }

   public NameParser getNameParser(String name) throws NamingException
   {
      return getContextOne().getNameParser(name);
   }

   public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
   {
      Collection<NameClassPair> set = new ArrayList<NameClassPair>();
      try 
      {
         addAll(set, getContextOne().list(name));
      } 
      catch (NameNotFoundException e){}
      try
      {
         addAll(set, getContextTwo().list(name));
      } 
      catch (NameNotFoundException e){}
      return new NamingEnumerationImpl<NameClassPair>(set);
   }

   public NamingEnumeration<NameClassPair> list(String name) throws NamingException
   {
      Collection<NameClassPair> set = new ArrayList<NameClassPair>();
      try 
      {
         addAll(set, getContextOne().list(name));
      } 
      catch (NameNotFoundException e){}
      try
      {
         addAll(set, getContextTwo().list(name));
      } 
      catch (NameNotFoundException e){}
      return new NamingEnumerationImpl<NameClassPair>(set);
   }

   public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
   {
      Collection<Binding> set = new ArrayList<Binding>();
      try
      {
         addAll(set, getContextOne().listBindings(name));
      }
      catch (NameNotFoundException e){}
      try
      {
         addAll(set, getContextTwo().listBindings(name));
      }
      catch (NameNotFoundException e){}
      return new NamingEnumerationImpl<Binding>(set);
   }

   public NamingEnumeration<Binding> listBindings(String name) throws NamingException
   {
      Collection<Binding> set = new ArrayList<Binding>();
      try
      {
         addAll(set, getContextOne().listBindings(name));
      }
      catch (NameNotFoundException e){}
      try
      {
         addAll(set, getContextTwo().listBindings(name));
      }
      catch (NameNotFoundException e){}
      return new NamingEnumerationImpl<Binding>(set);
   }

   public Object lookup(Name name) throws NamingException
   {
      log.trace("lookup: " + name);
      try
      {
         return getContextOne().lookup(name);
      }
      catch(NamingException e)
      {
         return getContextTwo().lookup(name);
      }
   }

   public Object lookup(String name) throws NamingException
   {
      log.trace("lookup: " + name);
      try
      {
         return getContextOne().lookup(name);
      }
      catch(NamingException e)
      {
         return getContextTwo().lookup(name);
      }
   }

   public Object lookupLink(Name name) throws NamingException
   {
      try
      {
         return getContextOne().lookupLink(name);
      }
      catch(NamingException e)
      {
         return getContextTwo().lookupLink(name);
      }
   }

   public Object lookupLink(String name) throws NamingException
   {
      try
      {
         return getContextOne().lookupLink(name);
      }
      catch(NamingException e)
      {
         return getContextTwo().lookupLink(name);
      }
   }

   public void rebind(Name name, Object obj) throws NamingException
   {
      getContextOne().rebind(name, obj);
   }

   public void rebind(String name, Object obj) throws NamingException
   {
      getContextOne().rebind(name, obj);
   }

   public Object removeFromEnvironment(String propName) throws NamingException
   {
      throw new UnsupportedOperationException();
   }

   public void rename(Name oldName, Name newName) throws NamingException
   {
      getContextOne().rename(oldName, newName);
   }

   public void rename(String oldName, String newName) throws NamingException
   {
      getContextOne().rename(oldName, newName);
   }

   public void unbind(Name name) throws NamingException
   {
      getContextOne().unbind(name);
   }

   public void unbind(String name) throws NamingException
   {
      getContextOne().unbind(name);
   }
}
