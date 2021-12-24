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

// $Id: TypeInfo.java 41038 2006-02-08 07:03:26Z starksm $

/**
 * The <code>TypeInfo</code> interface represents a type referenced from
 * <code>Element</code> or <code>Attr</code> nodes, specified in the schemas 
 * associated with the document. The type is a pair of a namespace URI and 
 * name properties, and depends on the document's schema.
 *
 * @since DOM Level 3
 */
public interface TypeInfo
{
   /**
    * The name of a type declared for the associated element or attribute,
    * or <code>null</code> if unknown.
    */
   public String getTypeName();

   /**
    * The namespace of the type declared for the associated element or
    * attribute or <code>null</code> if the element does not have
    * declaration or if no namespace information is available.
    */
   public String getTypeNamespace();

   public static final int DERIVATION_RESTRICTION = 0x00000001;
   public static final int DERIVATION_EXTENSION = 0x00000002;
   public static final int DERIVATION_UNION = 0x00000004;
   public static final int DERIVATION_LIST = 0x00000008;

   /**
    *  This method returns if there is a derivation between the reference
    * type definition, i.e. the <code>TypeInfo</code> on which the method
    * is being called, and the other type definition, i.e. the one passed
    * as parameters.
    */
   public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod);

}
