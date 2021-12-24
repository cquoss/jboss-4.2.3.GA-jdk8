package org.jboss.test.scoped.interfaces.dto;

import java.io.Serializable;
import java.util.Random;

public class SimpleResponseDTO implements Serializable
{
   private static final long serialVersionUID = 1L;
//   private static final long serialVersionUID = new Random().nextLong();

   private String firstName;
   private String lastName;

   public String getFirstName()
   {
      return firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public SimpleResponseDTO()
   {


   }


}