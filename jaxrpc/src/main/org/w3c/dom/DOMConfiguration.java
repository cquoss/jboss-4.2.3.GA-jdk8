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

// $Id: DOMConfiguration.java 41038 2006-02-08 07:03:26Z starksm $

/**
 * The <code>DOMConfiguration</code> interface represents the configuration
 * of a document and maintains a table of recognized parameters.
 *
 * @since DOM Level 3
 */
public interface DOMConfiguration
{
   /**
    * Set the value of a parameter.
    */
   public void setParameter(String name, Object value) throws DOMException;

   /**
    *  Return the value of a parameter if known.
    */
   public Object getParameter(String name) throws DOMException;

   /**
    * Check if setting a parameter to a specific value is supported.
    */
   public boolean canSetParameter(String name, Object value);

   /**
    *  The list of the parameters supported by this
    * <code>DOMConfiguration</code> object and for which at least one value
    * can be set by the application. Note that this list can also contain
    * parameter names defined outside this specification.
    */
   public DOMStringList getParameterNames();

}
