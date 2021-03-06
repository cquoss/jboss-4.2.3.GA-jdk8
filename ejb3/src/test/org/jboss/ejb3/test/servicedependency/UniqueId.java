/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.servicedependency;

import java.util.UUID;

import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.logging.Logger;

/**
 * A UniqueIdMBean.
 * 
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 60233 $
 */
@Service(objectName="acme:service=uniqueid")
@Management(UniqueIdMBean.class)
public class UniqueId implements UniqueIdMBean
{
   private static final Logger log = Logger.getLogger(UniqueId.class);

   public UUID generate()
   {
      return UUID.randomUUID();
   }

   public void create() throws Exception
   {
      log.info("create()");

   }

   public void start() throws Exception
   {
      log.info("start()");

   }

   public void stop() throws Exception
   {
      log.info("stop()");

   }

   public void destroy() throws Exception
   {
      log.info("destroy()");

   }

}
