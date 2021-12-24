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
package org.jboss.resource.adapter.jdbc.xa;

import org.jboss.resource.JBossResourceException;
import org.jboss.util.JBossStringBuilder;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.sql.XADataSource;
import javax.security.auth.Subject;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditor;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 59774 $</tt>
 */
public class HAXAManagedConnectionFactory
   extends XAManagedConnectionFactory
{
   private static final long serialVersionUID = 1898242235188801452L;

   private String urlProperty;
   private String urlDelimiter;
   private XADataSelector xadsSelector;

   public String getURLProperty()
   {
      return urlProperty;
   }

   public void setURLProperty(String urlProperty) throws ResourceException
   {
      this.urlProperty = urlProperty;
      initSelector();
   }

   public String getURLDelimiter()
   {
      return urlDelimiter;
   }

   public void setURLDelimiter(String urlDelimetir) throws ResourceException
   {
      this.urlDelimiter = urlDelimetir;
      initSelector();
   }

   public void setXADataSourceProperties(String xaDataSourceProperties) throws ResourceException
   {
      super.setXADataSourceProperties(xaDataSourceProperties);
      initSelector();
   }

   // Protected

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws javax.resource.ResourceException
   {
      if(xadsSelector == null)
      {
         JBossStringBuilder buffer = new JBossStringBuilder();
         buffer.append("Missing configuration for HA XA datasource.");
         if (urlProperty == null)
            buffer.append(" No url property.");
         else if (xaProps.containsKey(urlProperty) == false)
            buffer.append(" ").append(urlProperty).append(" not found in datasource properties.");
         if (urlDelimiter == null)
            buffer.append(" No url-delimiter.");
         throw new JBossResourceException(buffer.toString());
      }

      // try to get a connection as many times as many urls we have in the list
      for(int i = 0; i < xadsSelector.getXADataSourceList().size(); ++i)
      {
         XAData xaData = xadsSelector.getXAData();

         if(log.isTraceEnabled())
         {
            log.trace("Trying to create an XA connection to " + xaData.url);
         }

         try
         {
            return super.createManagedConnection(subject, cri);
         }
         catch(ResourceException e)
         {
            log.warn("Failed to create an XA connection to " + xaData.url + ": " + e.getMessage());
            xadsSelector.failedXAData(xaData);
         }
      }

      // we have supposedly tried all the urls
      throw new JBossResourceException(
         "Could not create connection using any of the URLs: " + xadsSelector.getXADataSourceList()
      );
   }

   protected synchronized XADataSource getXADataSource() throws ResourceException
   {
      return xadsSelector.getXAData().xads;
   }

   // Private

   private XADataSource createXaDataSource(Properties xaProps)
      throws JBossResourceException
   {
      if(getXADataSourceClass() == null)
      {
         throw new JBossResourceException("No XADataSourceClass supplied!");
      }

      XADataSource xads;
      try
      {
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(getXADataSourceClass());
         xads = (XADataSource)clazz.newInstance();
         Class[] NOCLASSES = new Class[]{};
         for(Iterator i = xaProps.keySet().iterator(); i.hasNext();)
         {
            String name = (String)i.next();
            String value = xaProps.getProperty(name);
            //This is a bad solution.  On the other hand the only known example
            // of a setter with no getter is for Oracle with password.
            //Anyway, each xadatasource implementation should get its
            //own subclass of this that explicitly sets the
            //properties individually.
            Class type = null;
            try
            {
               Method getter = clazz.getMethod("get" + name, NOCLASSES);
               type = getter.getReturnType();
            }
            catch(NoSuchMethodException e)
            {
               type = String.class;
            } // end of try-catch

            Method setter = clazz.getMethod("set" + name, new Class[]{type});
            PropertyEditor editor = PropertyEditorManager.findEditor(type);
            if(editor == null)
            {
               throw new JBossResourceException("No property editor found for type: " + type);
            } // end of if ()
            editor.setAsText(value);
            setter.invoke(xads, new Object[]{editor.getValue()});

         } // end of for ()
      }
      catch(ClassNotFoundException cnfe)
      {
         throw new JBossResourceException("Class not found for XADataSource " + getXADataSourceClass(), cnfe);
      } // end of try-catch
      catch(InstantiationException ie)
      {
         throw new JBossResourceException("Could not create an XADataSource: ", ie);
      } // end of catch
      catch(IllegalAccessException iae)
      {
         throw new JBossResourceException("Could not set a property: ", iae);
      } // end of catch

      catch(IllegalArgumentException iae)
      {
         throw new JBossResourceException("Could not set a property: ", iae);
      } // end of catch

      catch(InvocationTargetException ite)
      {
         throw new JBossResourceException("Could not invoke setter on XADataSource: ", ite);
      } // end of catch
      catch(NoSuchMethodException nsme)
      {
         throw new JBossResourceException("Could not find accessor on XADataSource: ", nsme);
      } // end of catch

      return xads;
   }

   private void initSelector() throws JBossResourceException
   {
      if(urlProperty != null && urlProperty.length() > 0)
      {
         String urlsStr = xaProps.getProperty(urlProperty);
         if(urlsStr != null && urlsStr.trim().length() > 0 && urlDelimiter != null && urlDelimiter.trim().length() > 0)
         {
            List xaDataList = new ArrayList();

            // copy xaProps
            // ctor doesn't work because iteration won't include defaults
            // Properties xaPropsCopy = new Properties(xaProps);
            Properties xaPropsCopy = new Properties();
            for(Iterator i = xaProps.keySet().iterator(); i.hasNext();)
            {
               Object key = i.next();
               xaPropsCopy.put(key, xaProps.get(key));
            }

            int urlStart = 0;
            int urlEnd = urlsStr.indexOf(urlDelimiter);
            while(urlEnd > 0)
            {
               String url = urlsStr.substring(urlStart, urlEnd);
               xaPropsCopy.setProperty(urlProperty, url);
               XADataSource xads = createXaDataSource(xaPropsCopy);
               xaDataList.add(new XAData(xads, url));
               urlStart = ++urlEnd;
               urlEnd = urlsStr.indexOf(urlDelimiter, urlEnd);
               log.debug("added XA HA connection url: " + url);
            }

            if(urlStart != urlsStr.length())
            {
               String url = urlsStr.substring(urlStart, urlsStr.length());
               xaPropsCopy.setProperty(urlProperty, url);
               XADataSource xads = createXaDataSource(xaPropsCopy);
               xaDataList.add(new XAData(xads, url));
               log.debug("added XA HA connection url: " + url);
            }

            xadsSelector = new XADataSelector(xaDataList);
         }
      }
   }

   // Inner

   public static class XADataSelector
   {
      private final List xaDataList;
      private int xaDataIndex;
      private XAData xaData;

      public XADataSelector(List xaDataList)
      {
         if(xaDataList == null || xaDataList.size() == 0)
         {
            throw new IllegalStateException("Expected non-empty list of XADataSource/URL pairs but got: " + xaDataList);
         }

         this.xaDataList = xaDataList;
      }

      public synchronized XAData getXAData()
      {
         if(xaData == null)
         {
            if(xaDataIndex == xaDataList.size())
            {
               xaDataIndex = 0;
            }
            xaData = (XAData)xaDataList.get(xaDataIndex++);
         }
         return xaData;
      }

      public synchronized void failedXAData(XAData xads)
      {
         if(xads.equals(this.xaData))
         {
            this.xaData = null;
         }
      }

      public List getXADataSourceList()
      {
         return xaDataList;
      }
   }

   private static class XAData
   {
      public final XADataSource xads;
      public final String url;

      public XAData(XADataSource xads, String url)
      {
         this.xads = xads;
         this.url = url;
      }

      public boolean equals(Object o)
      {
         if(this == o)
         {
            return true;
         }
         if(!(o instanceof XAData))
         {
            return false;
         }

         final XAData xaData = (XAData)o;

         if(!url.equals(xaData.url))
         {
            return false;
         }

         return true;
      }

      public int hashCode()
      {
         return url.hashCode();
      }

      public String toString()
      {
         return "[XA URL=" + url + "]";
      }
   }
}
