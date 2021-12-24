package org.jboss.test.jca.statistics;

import org.jboss.resource.statistic.JBossStatistics;

public class NullStatistic implements JBossStatistics
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 4121269296237048914L;

   
   public String toString()
   {
      return "Null Statistics";
   }
}
