package org.jboss.test.naming.restart;

import org.jboss.naming.NamingServiceMBean;
import org.jnp.interfaces.Naming;
import org.jnp.server.NamingBean;

public interface RestartNamingServiceMBean extends NamingServiceMBean
{
   boolean getUseGlobalService();
   void setUseGlobalService(boolean useGlobal);

   boolean getInstallGlobalService();
   void setInstallGlobalService(boolean installGlobal);

   Naming getNamingInstance();
   
   void setNaming(NamingBean bean);

}