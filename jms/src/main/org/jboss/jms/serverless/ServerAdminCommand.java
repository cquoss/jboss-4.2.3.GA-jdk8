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
package org.jboss.jms.serverless;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * TO_DO: change the name to GroupStateChange ?
 * 
 * @author Ovidiu Feodorov <ovidiu@jboss.org>
 * @version $Revision: 57195 $ $Date: 2006-09-26 08:08:17 -0400 (Tue, 26 Sep 2006) $
 *
 **/
class ServerAdminCommand implements Serializable {

    static final long serialVersionUID = 33480310721131848L;

    public static final String ADD_QUEUE_RECEIVER = "ADD_QUEUE_RECEIVER";
    public static final String REMOVE_QUEUE_RECEIVER = "REMOVE_QUEUE_RECEIVER";

    private String comm;
    private List args;

    public ServerAdminCommand(String comm, List args) {
        this.comm = comm;
        this.args = args;
    }

    public ServerAdminCommand(String comm, String arg1, String arg2, String arg3) {
        this(comm, new ArrayList());
        args.add(arg1);
        args.add(arg2);
        args.add(arg3);
    }

    public String getCommand() {
        return comm;
    }

    public Object get(int i) {
        return args.get(i);
    }

}
