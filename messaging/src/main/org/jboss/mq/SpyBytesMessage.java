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
package org.jboss.mq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.ArrayList;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

/**
 * This class implements javax.jms.BytesMessage
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyBytesMessage extends SpyMessage
   implements Cloneable, BytesMessage, Externalizable
{
   /** The org.jboss.mq.useWriteUTF boolean system property defines whether the
    * writeObject(String) call encodes the string using the pre-4.0.3 format
    * of a series of writeChar calls(=false), or as a writeUTF call(=true).
    * This defaults to true.
    */
   private final static String USE_WRITE_UTF = "org.jboss.mq.useWriteUTF";

   private static boolean useWriteUTF = true;

   /** 
    * The org.jboss.mq.chunkUTF boolean system property defines whether
    * UTF strings greater than 64K are chunked.
    * The default is true.
    */
   private final static String CHUNK_UTF = "org.jboss.mq.chunkUTF";

   private static boolean chunkUTF = true;

   /** The chunkSize */
   private final static int chunkSize = 16384;

   /** The serialVersionUID */
   private final static long serialVersionUID = -6572727147964701014L;

   static
   {
      PrivilegedAction action = new  PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(USE_WRITE_UTF, "true");
         }
      };
      try
      {
         String flag = (String) AccessController.doPrivileged(action);
         useWriteUTF = Boolean.valueOf(flag).booleanValue();
      }
      catch(Throwable ignore)
      {
      }
      action = new  PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(CHUNK_UTF, "true");
         }
      };
      try
      {
         String flag = (String) AccessController.doPrivileged(action);
         chunkUTF = Boolean.valueOf(flag).booleanValue();
      }
      catch(Throwable ignore)
      {
      }
   }

   /** The internal representation */
   byte[] InternalArray = null;
   
   private transient ByteArrayOutputStream ostream = null;
   private transient DataOutputStream p = null;
   private transient ByteArrayInputStream istream = null;
   private transient DataInputStream m = null;

   /**
    * Create a new SpyBytesMessage
    */
   public SpyBytesMessage()
   {
      header.msgReadOnly = false;
      ostream = new ByteArrayOutputStream();
      p = new DataOutputStream(ostream);
   }
   
   public boolean readBoolean() throws JMSException
   {
      checkRead();
      try
      {
         return m.readBoolean();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public byte readByte() throws JMSException
   {
      checkRead();
      try
      {
         return m.readByte();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public int readUnsignedByte() throws JMSException
   {
      checkRead();
      try
      {
         return m.readUnsignedByte();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public short readShort() throws JMSException
   {
      checkRead();
      try
      {
         return m.readShort();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public int readUnsignedShort() throws JMSException
   {
      checkRead();
      try
      {
         return m.readUnsignedShort();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public char readChar() throws JMSException
   {
      checkRead();
      try
      {
         return m.readChar();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public int readInt() throws JMSException
   {
      checkRead();
      try
      {
         return m.readInt();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public long readLong() throws JMSException
   {
      checkRead();
      try
      {
         return m.readLong();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public float readFloat() throws JMSException
   {
      checkRead();
      try
      {
         return m.readFloat();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public double readDouble() throws JMSException
   {
      checkRead();
      try
      {
         return m.readDouble();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public String readUTF() throws JMSException
   {
      checkRead();
      try
      {
         if (chunkUTF == false)
            return m.readUTF();
         
         byte type = m.readByte();
         if (type == NULL)
            return null;

         // apply workaround for string > 64K bug in jdk's 1.3.*

         // Read the no. of chunks this message is split into, allocate
         // a StringBuffer that can hold all chunks, read the chunks
         // into the buffer and set 'content' accordingly
         int chunksToRead = m.readInt();
         int bufferSize = chunkSize * chunksToRead;

         // special handling for single chunk
         if (chunksToRead == 1)
         {
            // The text size is likely to be much smaller than the chunkSize
            // so set bufferSize to the min of the input stream available
            // and the maximum buffer size. Since the input stream
            // available() can be <= 0 we check for that and default to
            // a small msg size of 256 bytes.

            int inSize = m.available();
            if (inSize <= 0)
            {
               inSize = 256;
            }

            bufferSize = Math.min(inSize, bufferSize);
         }

         // read off all of the chunks
         StringBuffer sb = new StringBuffer(bufferSize);

         for (int i = 0; i < chunksToRead; i++)
         {
            sb.append(m.readUTF());
         }

         return sb.toString();
      }
      catch (EOFException e)
      {
         throw new MessageEOFException("");
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public int readBytes(byte[] value) throws JMSException
   {
      checkRead();
      try
      {
         return m.read(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public int readBytes(byte[] value, int length) throws JMSException
   {
      checkRead();
      try
      {
         return m.read(value, 0, length);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeBoolean(boolean value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeBoolean(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeByte(byte value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeByte(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeShort(short value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeShort(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeChar(char value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeChar(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeInt(int value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeInt(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeLong(long value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeLong(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeFloat(float value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeFloat(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeDouble(double value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.writeDouble(value);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeUTF(String value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         if (chunkUTF == false)
            p.writeUTF(value);
         else
         {
            if (value == null)
               p.writeByte(NULL);
            else
            {
               // apply workaround for string > 64K bug in jdk's 1.3.*

               // Split content into chunks of size 'chunkSize' and assemble
               // the pieces into a List ...

               // FIXME: could calculate the number of chunks first, then
               //        write as we chunk for efficiency

               ArrayList v = new ArrayList();
               int contentLength = value.length();

               while (contentLength > 0)
                 {
                  int beginCopy = (v.size()) * chunkSize;
                  int endCopy = contentLength <= chunkSize ? beginCopy + contentLength : beginCopy + chunkSize;

                  String theChunk = value.substring(beginCopy, endCopy);
                  v.add(theChunk);

                  contentLength -= chunkSize;
               }

               // Write out the type (OBJECT), the no. of chunks and finally
               // all chunks that have been assembled previously
               p.writeByte(OBJECT);
               p.writeInt(v.size());

               for (int i = 0; i < v.size(); i++)
               {
                  p.writeUTF((String) v.get(i));
               }
            }
         }
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeBytes(byte[] value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.write(value, 0, value.length);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeBytes(byte[] value, int offset, int length) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         p.write(value, offset, length);
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }

   public void writeObject(Object value) throws JMSException
   {
      if (header.msgReadOnly)
      {
         throw new MessageNotWriteableException("the message body is read-only");
      }
      try
      {
         if (value == null)
         {
            throw new NullPointerException("Attempt to write a new value");
         }
         if (value instanceof String)
         {
            String s = (String) value;
            if( useWriteUTF == true )
               writeUTF(s);
            else
               p.writeChars(s);
         }
         else if (value instanceof Boolean)
         {
            p.writeBoolean(((Boolean) value).booleanValue());
         }
         else if (value instanceof Byte)
         {
            p.writeByte(((Byte) value).byteValue());
         }
         else if (value instanceof Short)
         {
            p.writeShort(((Short) value).shortValue());
         }
         else if (value instanceof Integer)
         {
            p.writeInt(((Integer) value).intValue());
         }
         else if (value instanceof Long)
         {
            p.writeLong(((Long) value).longValue());
         }
         else if (value instanceof Float)
         {
            p.writeFloat(((Float) value).floatValue());
         }
         else if (value instanceof Double)
         {
            p.writeDouble(((Double) value).doubleValue());
         }
         else if (value instanceof byte[])
         {
            p.write((byte[]) value, 0, ((byte[]) value).length);
         }
         else
         {
            throw new MessageFormatException("Invalid object for properties");
         }
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }

   }

   public void reset() throws JMSException
   {
      try
      {
         if (!header.msgReadOnly)
         {
            p.flush();
            InternalArray = ostream.toByteArray();
            ostream.close();
         }
         ostream = null;
         istream = null;
         m = null;
         p = null;
         header.msgReadOnly = true;
      }
      catch (IOException e)
      {
         throw new JMSException("IOException");
      }
   }
   
   public void clearBody() throws JMSException
   {
      try
      {
         if (!header.msgReadOnly)
         {
            ostream.close();
         }
         else
         {
            // REVIEW: istream is only initialised on a read.
            // It looks like it is possible to acknowledge
            // a message without reading it? Guard against
            // an NPE in this case.
            if (istream != null)
               istream.close();
         }
      }
      catch (IOException e)
      {
         //don't throw an exception
      }

      ostream = new ByteArrayOutputStream();
      p = new DataOutputStream(ostream);
      InternalArray = null;
      istream = null;
      m = null;

      super.clearBody();
   }

   public SpyMessage myClone() throws JMSException
   {
      SpyBytesMessage result = MessagePool.getBytesMessage();
      this.reset();
      result.copyProps(this);
      if (this.InternalArray != null)
        {
         result.InternalArray = new byte[this.InternalArray.length];
         System.arraycopy(this.InternalArray, 0, result.InternalArray, 0, this.InternalArray.length);
      }
      return result;
   }
   
   public long getBodyLength() throws JMSException
   {
      checkRead();
      return InternalArray.length;
   }
   
   public void writeExternal(ObjectOutput out) throws IOException
   {
      byte[] arrayToSend = null;
      if (!header.msgReadOnly)
        {
         p.flush();
         arrayToSend = ostream.toByteArray();
      }
      else
        {
         arrayToSend = InternalArray;
      }
      super.writeExternal(out);
      if (arrayToSend == null)
        {
         out.writeInt(0); //pretend to be empty array
      }
      else
        {
         out.writeInt(arrayToSend.length);
         out.write(arrayToSend);
      }
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      int length = in.readInt();
      if (length < 0)
        {
         InternalArray = null;
      }
      else
        {
         InternalArray = new byte[length];
         in.readFully(InternalArray);
      }
   }
   
   /**
    * Check the message is readable
    *
    * @throws JMSException when not readable
    */
   private void checkRead() throws JMSException
   {
      if (!header.msgReadOnly)
      {
         throw new MessageNotReadableException("readByte while the buffer is writeonly");
      }

      //We have just received/reset() the message, and the client is trying to
      // read it
      if (istream == null || m == null)
        {
         istream = new ByteArrayInputStream(InternalArray);
         m = new DataInputStream(istream);
      }
   }
}
