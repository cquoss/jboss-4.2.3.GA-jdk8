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
package org.jboss.test.cmp2.commerce;

import java.util.Iterator;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;

import junit.framework.Test;
import org.jboss.test.util.ejb.EJBTestCase;

import org.jboss.test.JBossTestCase;

public class CascadeDeleteTest extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(CascadeDeleteTest.class, "cmp2-commerce.jar");
   }

   public CascadeDeleteTest(String name)
   {
      super(name);
   }

   private OrderHome getOrderHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (OrderHome) jndiContext.lookup("commerce/Order");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getOrderHome: " + e.getMessage());
      }
      return null;
   }

   private ProductCategoryHome getProductCategoryHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (ProductCategoryHome) jndiContext.lookup("commerce/ProductCategory");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getProductCategoryHome: " + e.getMessage());
      }
      return null;
   }

   private ProductCategoryHome getProductCategoryBatchDeleteHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (ProductCategoryHome) jndiContext.lookup("commerce/ProductCategoryBatchDelete");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getProductCategoryBatchDeleteHome: " + e.getMessage());
      }
      return null;
   }

   private ProductCategoryTypeHome getProductCategoryTypeHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (ProductCategoryTypeHome) jndiContext.lookup("commerce/ProductCategoryType");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getProductCategoryTypeHome: " + e.getMessage());
      }
      return null;
   }

   private ProductCategoryTypeHome getProductCategoryTypeBatchDeleteHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (ProductCategoryTypeHome) jndiContext.lookup("commerce/ProductCategoryTypeBatchDelete");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getProductCategoryTypeBatchDeleteHome: " + e.getMessage());
      }
      return null;
   }

   private LineItemHome getLineItemHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (LineItemHome) jndiContext.lookup("commerce/LineItem");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getLineItemHome: " + e.getMessage());
      }
      return null;
   }

   private AddressHome getAddressHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (AddressHome) jndiContext.lookup("commerce/Address");
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception in getAddressHome: " + e.getMessage());
      }
      return null;
   }

   public void testCascadeDelete() throws Exception
   {
      OrderHome orderHome = getOrderHome();
      AddressHome addressHome = getAddressHome();
      LineItemHome lineItemHome = getLineItemHome();

      Order order = orderHome.create();
      Long orderNumber = order.getOrdernumber();

      Long shipId = new Long(99999);
      Address ship = addressHome.create(shipId);
      ship.setState("CA");
      order.setShippingAddress(ship);

      Long billId = new Long(88888);
      Address bill = addressHome.create(billId);
      bill.setState("CA");
      order.setBillingAddress(bill);

      // lineItemId and shipId are the same to check for
      // weird cascade delete problems
      Long lineItemId = shipId;
      LineItem lineItem = lineItemHome.create(lineItemId);
      lineItem.setOrder(order);

      order.remove();

      try
      {
         orderHome.findByPrimaryKey(orderNumber);
         fail("Order should have been deleted");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         addressHome.findByPrimaryKey(billId);
         fail("Billing address should have been deleted");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         lineItemHome.findByPrimaryKey(lineItemId);
         fail("Line item should have been deleted");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         addressHome.findByPrimaryKey(shipId);
         fail("Shipping address should have been deleted");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }
   }

   public void testCategory_Type() throws Exception
   {
      ProductCategoryHome ch = getProductCategoryHome();

      ProductCategory parent = ch.create();
      CompositeId parentId = parent.getPK();

      ProductCategory child = ch.create();
      child.setParent(parent);
      CompositeId childId = child.getPK();

      ProductCategory grandChild = ch.create();
      grandChild.setParent(parent);
      CompositeId grandChildId = grandChild.getPK();

      ProductCategoryTypeHome th = getProductCategoryTypeHome();
      ProductCategoryType type = th.create();
      parent.setType(type);
      child.setType(type);
      Long typeId = type.getId();

      type.remove();

      try
      {
         ch.findByPrimaryKey(parentId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         ch.findByPrimaryKey(childId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         ch.findByPrimaryKey(grandChildId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         th.findByPrimaryKey(typeId);
         fail("ProductCategoryType should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }
   }

   public void testCategory_Type_BatchCascadeDelete() throws Exception
   {
      ProductCategoryHome ch = getProductCategoryBatchDeleteHome();

      ProductCategory parent = ch.create();
      CompositeId parentId = parent.getPK();

      ProductCategory child = ch.create();
      child.setParent(parent);
      CompositeId childId = child.getPK();

      ProductCategory grandChild = ch.create();
      grandChild.setParent(parent);
      CompositeId grandChildId = grandChild.getPK();

      ProductCategoryTypeHome th = getProductCategoryTypeBatchDeleteHome();
      ProductCategoryType type = th.create();
      parent.setType(type);
      child.setType(type);
      Long typeId = type.getId();

      type.remove();

      try
      {
         ch.findByPrimaryKey(parentId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         ch.findByPrimaryKey(childId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         ch.findByPrimaryKey(grandChildId);
         fail("ProductCategory should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }

      try
      {
         th.findByPrimaryKey(typeId);
         fail("ProductCategoryType should have beed deleted.");
      }
      catch(ObjectNotFoundException e)
      {
         // expected
      }
   }

   public void tearDownEJB() throws Exception
   {
      deleteAllOrders(getOrderHome());
      deleteAllLineItems(getLineItemHome());
      deleteAllAddresses(getAddressHome());
      deleteAllCategories(getProductCategoryHome());
   }

   public void deleteAllCategories(ProductCategoryHome catHome) throws Exception
   {
      Iterator cats = catHome.findAll().iterator();
      while(cats.hasNext())
      {
         ProductCategory cat = (ProductCategory) cats.next();
         cat.remove();
      }
   }

   public void deleteAllOrders(OrderHome orderHome) throws Exception
   {
      Iterator orders = orderHome.findAll().iterator();
      while(orders.hasNext())
      {
         Order order = (Order) orders.next();
         order.remove();
      }
   }

   public void deleteAllLineItems(LineItemHome lineItemHome) throws Exception
   {
      Iterator lineItems = lineItemHome.findAll().iterator();
      while(lineItems.hasNext())
      {
         LineItem lineItem = (LineItem) lineItems.next();
         lineItem.remove();
      }
   }

   public void deleteAllAddresses(AddressHome addressHome) throws Exception
   {
      Iterator addresses = addressHome.findAll().iterator();
      while(addresses.hasNext())
      {
         Address address = (Address) addresses.next();
         address.remove();
      }
   }
}
