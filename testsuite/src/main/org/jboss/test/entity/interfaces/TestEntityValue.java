/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.entity.interfaces;
 

/**
 * Value object for TestEntity.
 *
 */
public class TestEntityValue
   extends Object 
   implements java.io.Serializable 
{
   private String entityID;
   private boolean entityIDHasBeenSet = false;
   private String value1;
   private boolean value1HasBeenSet = false;

   private String pk;

   public TestEntityValue()
   {
   }

   public TestEntityValue( String entityID,String value1 )
   {
	  this.entityID = entityID;
	  entityIDHasBeenSet = true;
	  this.value1 = value1;
	  value1HasBeenSet = true;
	  pk = this.getEntityID();
   }

   //TODO Cloneable is better than this !
   public TestEntityValue( TestEntityValue otherValue )
   {
	  this.entityID = otherValue.entityID;
	  entityIDHasBeenSet = true;
	  this.value1 = otherValue.value1;
	  value1HasBeenSet = true;

	  pk = this.getEntityID();
   }

   public String getPrimaryKey()
   {
	  return pk;
   }

   public void setPrimaryKey( String pk )
   {
      // it's also nice to update PK object - just in case
      // somebody would ask for it later...
      this.pk = pk;
	  setEntityID( pk );
   }

   public String getEntityID()
   {
	  return this.entityID;
   }

   public void setEntityID( String entityID )
   {
	  this.entityID = entityID;
	  entityIDHasBeenSet = true;

		  pk = entityID;
   }

   public boolean entityIDHasBeenSet(){
	  return entityIDHasBeenSet;
   }
   public String getValue1()
   {
	  return this.value1;
   }

   public void setValue1( String value1 )
   {
	  this.value1 = value1;
	  value1HasBeenSet = true;

   }

   public boolean value1HasBeenSet(){
	  return value1HasBeenSet;
   }

   public String toString()
   {
	  StringBuffer str = new StringBuffer("{");

	  str.append("entityID=" + getEntityID() + " " + "value1=" + getValue1());
	  str.append('}');

	  return(str.toString());
   }

   /**
    * A Value Object has an identity if the attributes making its Primary Key have all been set. An object without identity is never equal to any other object.
    *
    * @return true if this instance has an identity.
    */
   protected boolean hasIdentity()
   {
	  return entityIDHasBeenSet;
   }

   public boolean equals(Object other)
   {
      if (this == other)
         return true;
	  if ( ! hasIdentity() ) return false;
	  if (other instanceof TestEntityValue)
	  {
		 TestEntityValue that = (TestEntityValue) other;
		 if ( ! that.hasIdentity() ) return false;
		 boolean lEquals = true;

		 lEquals = lEquals && isIdentical(that);

		 return lEquals;
	  }
	  else
	  {
		 return false;
	  }
   }

   public boolean isIdentical(Object other)
   {
	  if (other instanceof TestEntityValue)
	  {
		 TestEntityValue that = (TestEntityValue) other;
		 boolean lEquals = true;
		 if( this.entityID == null )
		 {
			lEquals = lEquals && ( that.entityID == null );
		 }
		 else
		 {
			lEquals = lEquals && this.entityID.equals( that.entityID );
		 }
		 if( this.value1 == null )
		 {
			lEquals = lEquals && ( that.value1 == null );
		 }
		 else
		 {
			lEquals = lEquals && this.value1.equals( that.value1 );
		 }

		 return lEquals;
	  }
	  else
	  {
		 return false;
	  }
   }

   public int hashCode(){
	  int result = 17;
      result = 37*result + ((this.entityID != null) ? this.entityID.hashCode() : 0);

      result = 37*result + ((this.value1 != null) ? this.value1.hashCode() : 0);

	  return result;
   }

}
