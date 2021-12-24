package org.jboss.ejb3.metamodel;

/**
 * Represents the webservices element of jboss.xml
 * @author Heiko.Braun@jboss.com
 */
public class Webservices
{
   private String contextRoot;
   private String descriptionName;
   private String configName;
   private String configFile;
   private String wsdlPublishLocation;

   public String getContextRoot()
   {
      return contextRoot;
   }

   public void setContextRoot(String contextRoot)
   {
      this.contextRoot = contextRoot;
   }


   public String getDescriptionName()
   {
      return descriptionName;
   }

   public void setDescriptionName(String descriptionName)
   {
      this.descriptionName = descriptionName;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getWsdlPublishLocation()
   {
      return wsdlPublishLocation;
   }

   public void setWsdlPublishLocation(String wsdlPublishLocation)
   {
      this.wsdlPublishLocation = wsdlPublishLocation;
   }
}
