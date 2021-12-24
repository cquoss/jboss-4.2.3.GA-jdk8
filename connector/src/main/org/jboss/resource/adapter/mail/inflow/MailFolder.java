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
package org.jboss.resource.adapter.mail.inflow;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Message;

/** An encapsulation of a mail store folder used by the MailActivation.run to
 * poll and retrieve new messages.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60787 $
 */
public class MailFolder
{
   private Session session;
   private Store store;
   private Folder folder;
   private String mailServer;
   private String folderName;
   private String userName;
   private String password;
   private Properties sessionProps;

   public MailFolder(MailActivationSpec spec)
      throws Exception
   {
      mailServer = spec.getMailServer();
      String protocol = spec.getStoreProtocol();
      folderName = spec.getMailFolder();
      userName = spec.getUserName();
      password = spec.getPassword();

      sessionProps = new Properties();
      sessionProps.setProperty("mail.transport.protocol", "smtp");
      sessionProps.setProperty("mail.store.protocol", protocol);
      sessionProps.setProperty("mail.smtp.host", mailServer);
   }

   public void open()
      throws Exception
   {
      // Get a session object
      session = Session.getInstance(sessionProps);
      
      // Get a store object
      store = session.getStore();
      store.connect(mailServer, userName, password);
      folder = store.getFolder(folderName);

      if (folder == null || (!this.folder.exists()))
      {
         MessagingException e = new MessagingException("Failed to find folder: " + folderName);
         throw e;
      }

      folder.open(Folder.READ_WRITE);
   }

   public Message[] getNewMessages()
      throws Exception
   {
      Message msgs[] = {};
      /* This does not seem to be the most reliable new msg check. This should
      probably be unread msgs with the msgs marked as read on successful
      delivery.
      */
      if( folder.hasNewMessages() )
      {
         int newCount = folder.getNewMessageCount();
         int msgCount = folder.getMessageCount();
         msgs = folder.getMessages(msgCount - newCount + 1, msgCount);
         return msgs;
      }
      return msgs;
   }

   public void close() throws MessagingException
   {
      this.folder.close(true);
      this.store.close();
      this.folder = null;
      this.store = null;
      this.session = null;
   }

}
