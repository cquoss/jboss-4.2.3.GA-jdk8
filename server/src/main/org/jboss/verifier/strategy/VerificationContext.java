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
package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.VerificationContext;
 * Copyright (C) 2000  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
 * $Id: VerificationContext.java 68139 2007-12-11 15:37:01Z csaldanh $
 *
 * You can reach the author by sending email to jplindfo@helsinki.fi.
 */

// standard imports
import java.net.URL;

// non-standard class dependencies

import org.jboss.verifier.event.VerificationEventGenerator;

import org.jboss.metadata.ApplicationMetaData;

/**
 * << DESCRIBE THE CLASS HERE >>
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author  Juha Lindfors
 * @version $Revision: 68139 $
 * @since   JDK 1.3
 */
public interface VerificationContext
   extends VerificationEventGenerator
{

   /*
    * Version identifier.
    */
   public final static String VERSION_1_1 =
      "Enterprise JavaBeans v1.1, Final Release";

   /*
    * Version identifier.
    */
   public final static String VERSION_2_0 =
      "Enterprise JavaBeans V2.0, Final Release";

   /*
    * Version identifier.
    */
   public final static String VERSION_2_1 =
      "Enterprise JavaBeans V2.1, Final Release";

   /*
    * Returns the loaded and parsed ejb jar file
    */
   abstract ApplicationMetaData getApplicationMetaData();

   /*
    * Returns the location of the ejb jar file
    */
   abstract URL getJarLocation();

   /*
    * Returns the class loader to use for verification (optionally)
    */
   abstract ClassLoader getClassLoader();

   /*
    * Returns EJB spec version string
    */
   abstract String getEJBVersion();

}
