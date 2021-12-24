package org.jboss.test.classloader.circularity.support.linkage;

public class X
{
   private Y y;
   X(Y y)
   {
      this.y = y;
   }
}
