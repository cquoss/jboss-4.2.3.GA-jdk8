package org.jboss.test.jca.statistics;

import org.jboss.resource.statistic.JBossStatistics;
import org.jboss.resource.statistic.formatter.StatisticsFormatter;

public class NullStatisticFormatter implements StatisticsFormatter
{

   public Object formatStatistics(JBossStatistics stats)
   {
      // TODO Auto-generated method stub
      return stats.toString();
   }

}
