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
package javax.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * An invocation handler to proxy requests through the MBeanServer
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public class MBeanServerInvocationHandler
   implements InvocationHandler
{
   // Constants ---------------------------------------------------

   // Attributes --------------------------------------------------

   /**
    * The MBeanServerConnection
    */
   private MBeanServerConnection connection;

   /**
    * The ObjectName
    */
   private ObjectName name;

   /**
    * The method mappings
    */
   private HashMap mappings;

   // Static  -----------------------------------------------------

   // Signatures
   private static final String[] EMPTY_SIGNATURE = new String[0];
   private static final String[] EQUALS_SIGNATURE = new String[] { Object.class.getName() };
   private static final Class[] LISTENER = new Class[] { NotificationListener.class };
   private static final Class[] TRIPLET = new Class[] { NotificationListener.class, NotificationFilter.class, Object.class };

   // Object methods
   private static final Method EQUALS;
   private static final Method HASH_CODE;
   private static final Method TO_STRING;

   // NotificationEmitter methods
   private static final Method ADD_NOTIFICATION_LISTENER;
   private static final Method GET_NOTIFICATION_INFO;
   private static final Method REMOVE_NOTIFICATION_LISTENER;
   private static final Method REMOVE_NOTIFICATION_LISTENER_TRIPLET;

   static
   {
      try
      {
         ADD_NOTIFICATION_LISTENER = NotificationBroadcaster.class.getDeclaredMethod("addNotificationListener",  TRIPLET);
         GET_NOTIFICATION_INFO = NotificationBroadcaster.class.getDeclaredMethod("getNotificationInfo",  new Class[0]);
         REMOVE_NOTIFICATION_LISTENER = NotificationBroadcaster.class.getDeclaredMethod("removeNotificationListener",  LISTENER);
         REMOVE_NOTIFICATION_LISTENER_TRIPLET = NotificationEmitter.class.getDeclaredMethod("removeNotificationListener",  TRIPLET);
         EQUALS = Object.class.getDeclaredMethod("equals",  new Class[] { Object.class });
         HASH_CODE = Object.class.getDeclaredMethod("hashCode",  new Class[0]);
         TO_STRING = Object.class.getDeclaredMethod("toString",  new Class[0]);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   // Constructors ------------------------------------------------

   /**
    * Construct an MBeanServerInvocationHandler for the
    * given connection and object name.<p>
    *
    * @param connection the MBeanServerConnection
    * @param name the ObjectName of the bean
    */
   public MBeanServerInvocationHandler(MBeanServerConnection connection,
                                       ObjectName name)
   {
      // WARNING: These checks are not in the spec
      if (connection == null)
         throw new IllegalArgumentException("Null connection");
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (name.isPattern())
         throw new IllegalArgumentException("Name is a pattern");

      this.connection = connection;
      this.name = name;
   }

   /**
    * Constructs a new Proxy for the MBean using an
    * MBeanServerInvocationHandler.
    *
    * @param connection the MBeanServerConnection
    * @param name the ObjectName of the bean
    * @param interfaceClass the interface to expose
    * @param broadcaster pass true to expose the NotificationEmitter interface
    */
   public static Object newProxyInstance(MBeanServerConnection connection,
                                         ObjectName name,
                                         Class interfaceClass,
                                         boolean broadcaster)
   {
      MBeanServerInvocationHandler handler =
         new MBeanServerInvocationHandler(connection, name);

      // WARNING: These checks are not in the spec
      if (interfaceClass == null)
         throw new IllegalArgumentException("Null interface");

      Class[] interfaces;
      if (broadcaster)
         interfaces = new Class[] { interfaceClass, NotificationEmitter.class };
      else
         interfaces = new Class[] { interfaceClass };

      return Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaces, handler);
   }

   // Public ------------------------------------------------------

   // InvocationHandler Implementation ----------------------------

   public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
   {
      return getAction(proxy, method).perform(args);
   }

   // Y Overrides -------------------------------------------------

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   /**
    * Get the actions for this method
    */
   private Action getAction(Object proxy, Method method)
      throws Throwable
   {
      // Doesn't really matter if the mappings are
      // setup twice by two different threads, they are the same.
      if (mappings == null)
         mappings = getMappings(proxy);

      // Check the action
      Action result = (Action) mappings.get(method);
      if (result == null)
         throw new IllegalArgumentException("Unknown method " + method);

      // Return the result
      return result;
   }

   /**
    * Set up the mappings
    */
   private HashMap getMappings(Object proxy)
      throws Throwable
   {
      HashMap result = new HashMap();

      Class[] interfaces = proxy.getClass().getInterfaces();
      for (int i = 0; i < interfaces.length; ++i)
      {
         if (interfaces[i].equals(NotificationEmitter.class))
         {
            result.put(ADD_NOTIFICATION_LISTENER, new AddNotificationListenerAction());
            result.put(GET_NOTIFICATION_INFO, new GetNotificationInfoAction());
            result.put(REMOVE_NOTIFICATION_LISTENER, new RemoveNotificationListenerAction());
            result.put(REMOVE_NOTIFICATION_LISTENER_TRIPLET, new RemoveNotificationListenerTripletAction());
         }
         else
         {
            Method[] methods = interfaces[i].getMethods();

            for (int m = 0; m < methods.length; ++m)
            {
               String methodName = methods[m].getName();
               Class returnType = methods[m].getReturnType();
               Class[] parameterTypes = methods[m].getParameterTypes();

               // Getter
               if (methodName.startsWith("get")  && 
                   methodName.length() > 3 &&
                   Void.TYPE.equals(returnType) == false &&
                   parameterTypes.length == 0)
               {
                  result.put(methods[m], new GetAction(methodName.substring(3)));
               }
               // Getter (is)
               else if(methodName.startsWith("is")  && 
                     methodName.length() > 2 &&
                     (Boolean.TYPE.equals(returnType) || Boolean.class.equals(returnType)) &&
                     parameterTypes.length == 0)
               {
                  result.put(methods[m], new GetAction(methodName.substring(2)));
               }
               // Setter (is)
               else if(methodName.startsWith("set")  && 
                     methodName.length() > 3 &&
                     Void.TYPE.equals(returnType) &&
                     parameterTypes.length == 1)
               {
                  result.put(methods[m], new SetAction(methodName.substring(3)));
               }
               // Invoker
               else
               {
                 result.put(methods[m], new InvokeAction(methodName, getSignature(parameterTypes)));
               }
            }
         }
      }

      // Add the Object mappings
      result.put(EQUALS, new InvokeAction(EQUALS.getName(), EQUALS_SIGNATURE));
      result.put(HASH_CODE, new InvokeAction(HASH_CODE.getName(), EMPTY_SIGNATURE));
      result.put(TO_STRING, new InvokeAction(TO_STRING.getName(), EMPTY_SIGNATURE));
      
      return result;
   }

   private static String[] getSignature(final Class[] parameterTypes)
   {
      String[] signature = new String[parameterTypes.length];
      for (int p = 0; p < parameterTypes.length; ++p)
          signature[p] = parameterTypes[p].getName();
      return signature;
   }

   // Inner Classes -----------------------------------------------

   private interface Action
   {
      public Object perform(Object[] args)
         throws Throwable;
   }

   private class GetAction
      implements Action
   {
      private String attribute;

      public GetAction(String attribute)
      {
         this.attribute = attribute;
      }

      public Object perform(Object[] args)
         throws Throwable
      {
         try
         {
            return connection.getAttribute(name, attribute);
         }
         catch(MBeanException exception)
         {
            throw exception.getTargetException();
         }
      }
   }

   private class SetAction
      implements Action
   {
      private String attribute;

      public SetAction(String attribute)
      {
         this.attribute = attribute;
      }

      public Object perform(Object[] args)
         throws Throwable
      {
         try
         {
            connection.setAttribute(name, new Attribute(attribute, args[0]));
            return null;
         }
         catch(MBeanException exception)
         {
            throw exception.getTargetException();
         }
      }
   }

   private class InvokeAction
      implements Action
   {
      private String operation;
      private String[] signature;

      public InvokeAction(String operation, String[] signature)
      {
         this.operation = operation;
         this.signature = signature;
      }

      public Object perform(Object[] args)
         throws Throwable
      {
         try
         {
            return connection.invoke(name, operation, args, signature);
         }
         catch(MBeanException exception)
         {
            throw exception.getTargetException();
         }
      }
   }

   private class AddNotificationListenerAction
      implements Action
   {
      public Object perform(Object[] args)
         throws Throwable
      {
         connection.addNotificationListener(name,
            (NotificationListener) args[0], (NotificationFilter) args[1],  args[2]);
         return null;
      }
   }

   private class GetNotificationInfoAction
      implements Action
   {
      public Object perform(Object[] args)
         throws Throwable
      {
         return connection.getMBeanInfo(name).getNotifications();
      }
   }

   private class RemoveNotificationListenerAction
      implements Action
   {
      public Object perform(Object[] args)
         throws Throwable
      {
         connection.removeNotificationListener(name, (NotificationListener) args[0]);
         return null;
      }
   }

   private class RemoveNotificationListenerTripletAction
      implements Action
   {
      public Object perform(Object[] args)
         throws Throwable
      {
         connection.removeNotificationListener(name,
            (NotificationListener) args[0], (NotificationFilter) args[1],  args[2]);
         return null;
      }
   }
}
