package org.jboss.test.classloader.circularity.support.linkage;

public class Xsub extends X
{
   Xsub(Y y)
   {
      super(y);
   }
}
