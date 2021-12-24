/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree785;

import javax.ejb.Remote;

@Remote
public interface Tester
{
   String sayHiTo(String name);
}
