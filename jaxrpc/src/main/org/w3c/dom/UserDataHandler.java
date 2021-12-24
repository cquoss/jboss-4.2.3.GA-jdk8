/*
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package org.w3c.dom;

// $Id: UserDataHandler.java 41038 2006-02-08 07:03:26Z starksm $

/**
 * When associating an object to a key on a node using 
 * <code>Node.setUserData()</code> the application can provide a handler 
 * that gets called when the node the object is associated to is being 
 * cloned, imported, or renamed. This can be used by the application to 
 * implement various behaviors regarding the data it associates to the DOM 
 * nodes. This interface defines that handler. 
 * <p>See also the <a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407'>Document Object Model (DOM) Level 3 Core Specification</a>.
 *
 * @since DOM Level 3
 */
public interface UserDataHandler
{

   public static final short NODE_CLONED = 1;
   public static final short NODE_IMPORTED = 2;
   public static final short NODE_DELETED = 3;
   public static final short NODE_RENAMED = 4;
   public static final short NODE_ADOPTED = 5;

   /**
    * This method is called whenever the node for which this handler is
    * registered is imported or cloned.
    * <br> DOM applications must not raise exceptions in a
    * <code>UserDataHandler</code>. The effect of throwing exceptions from
    * the handler is DOM implementation dependent.
    */
   public void handle(short operation, String key, Object data, Node src, Node dst);

}
