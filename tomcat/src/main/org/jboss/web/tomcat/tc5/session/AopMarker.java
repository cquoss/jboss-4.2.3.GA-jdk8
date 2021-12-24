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
package org.jboss.web.tomcat.tc5.session;

/**
 * Marker interface for use in aop annotation declaration. For example, can use this in pojo
 * annotation instead of declaring jboss-aop.xml. For a pojo annotation declaration, just declare
 * this in the pojo class level, and then there is a corresponding jboss-aop.xml that is needed.
 * @author Ben Wang
 * 
 * @deprecated use org.jboss.cache.aop.annotation.PojoCacheable (JDK5) 
 *             or org.jboss.cache.aop.AopMarker (JDK 1.4 based builds). Will
 *             be removed in the next minor and major JBoss AS releases. 
 */
public interface AopMarker {
}
