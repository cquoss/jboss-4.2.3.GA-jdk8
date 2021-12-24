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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.regex.Pattern;

/**
 * A Match Query Expression.<p>
 *
 * Returns true when an attribute value matches the string expression.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020314 Adrian Brock:</b>
 * <ul>
 * <li>Fixed most of the escaping
 * </ul>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class MatchQueryExp extends QueryEval implements QueryExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -7156603696948215014L;
   
   // Attributes --------------------------------------------------

   /**
    * The attribute to test
    */
   private AttributeValueExp exp;

   /**
    * The string to test
    */
   private String pattern;

   /**
    * Regular Expression
    */
   private transient Pattern re;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   public MatchQueryExp()
   {
   }
   
   /**
    * Construct a new MATCH query expression
    *
    * @param attr the attribute to test
    * @param string the string to test
    */
   public MatchQueryExp(AttributeValueExp attr, String string)
   {
      this.exp = attr;
      this.pattern = string;

      generateRegExp();
   }

   // Public ------------------------------------------------------

   // QueryExp implementation -------------------------------------

   public boolean apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      ValueExp calcAttr = exp.apply(name);

      if (calcAttr instanceof StringValueExp)
      {
         return re.matcher(calcAttr.toString()).matches();
      }
      // Correct?
      return false;
   }

   // Object overrides --------------------------------------------
   
   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      generateRegExp();
   }

   public String toString()
   {
      return new String("(" + exp.toString() + " matches " +
                        pattern.toString() + ")");
   }

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------
   
   private void generateRegExp()
   {
      // Translate the pattern to a regexp
      StringBuffer buffer = new StringBuffer();
      char[] chars = pattern.toCharArray();
      boolean escaping = false;
      for (int i=0; i < chars.length; i++)
      {
         // Turn on escaping
         if (chars[i] == '\\' && escaping == false)
            escaping = true;
         else
         {
            // Match any character
            if (chars[i] == '?' && escaping == false)
               buffer.append("(?:.)");
            // A literal question mark
            else if (chars[i] == '?')
               buffer.append("\\?");
            // Match any number of characters including none
            else if (chars[i] == '*' && escaping == false)
               buffer.append("(?:.)*");
            // A literal asterisk
            else if (chars[i] == '*')
               buffer.append("\\*");
            // The hat character is literal
            else if (chars[i] == '^')
               buffer.append("\\^");
            // The dollar sign is literal
            else if (chars[i] == '$')
               buffer.append("\\$");
            // The back slash character is literal (avoids escaping)
            else if (chars[i] == '\\')
               buffer.append("\\\\");
            // The dot character is literal
            else if (chars[i] == '.')
               buffer.append("\\.");
            // The vertical line character is literal
            else if (chars[i] == '|')
               buffer.append("\\|");
            // Escaping the open bracket
            else if (chars[i] == '[' && escaping == true)
               buffer.append("\\[");
            // REVIEW: There are other more complicated expressions to escape
            else
               buffer.append(chars[i]);
            escaping = false;
         }
      }
      // REVIEW: Should this be an error?
      if (escaping)
         buffer.append("\\\\");
         
      re = Pattern.compile(buffer.toString());
   }

   // Inner classes -----------------------------------------------
}
