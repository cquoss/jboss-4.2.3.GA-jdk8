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
package org.jboss.ant.taskdefs;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.FlatFileNameMapper;
import org.apache.tools.ant.types.DirSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;


/**
 * This class will call a specified ant class for each existent file into fileSet or dirSet
 *
 * @author Clebert.suconic@jboss.com
 */
public class AntCallOnDirectoryList extends Task
{
    ArrayList list = new ArrayList();

    String targetToExecute;

    String directoryProperty;
    String versionNameProperty;

    Ant ant = null;

    public String getVersionNameProperty()
    {
        return versionNameProperty;
    }

    public void setVersionNameProperty(String versionNameProperty)
    {
        this.versionNameProperty = versionNameProperty;
    }

    public String getTargetToExecute()
    {
        return targetToExecute;
    }

    public void setTargetToExecute(String targetToExecute)
    {
        this.targetToExecute = targetToExecute;
    }

    public String getDirectoryProperty()
    {
        return directoryProperty;
    }

    public void setDirectoryProperty(String directoryProperty)
    {
        this.directoryProperty = directoryProperty;
    }


    public void init()
    {
        super.init();
        ant = (Ant) this.getProject().createTask("ant");
        ant.setAntfile(this.getProject().getProperty("ant.file"));
        ant.setOwningTarget(getOwningTarget());
        ant.setTaskName(getTaskName());
        ant.setLocation(getLocation());
        ant.init();
    }


    public void addDirSet(DirSet dirSet)
    {
        list.add(dirSet);
    }

    public void execute() throws BuildException
    {
        init();
        ant.setTarget(getTargetToExecute());
        Property parameterDirectory = ant.createProperty();
        parameterDirectory.setName(this.getDirectoryProperty());

        Property parameterName = ant.createProperty();
        parameterName.setName(this.getVersionNameProperty());


        Iterator iter = list.iterator();
        while (iter.hasNext())
        {
            DirSet dirSet = (DirSet) iter.next();
            File currentDir = dirSet.getDir(this.getProject());
            DirectoryScanner scanner = dirSet.getDirectoryScanner(this.getProject());

            SourceFileScanner sourceScanner = new SourceFileScanner(this);
            String[] strfiles = scanner.getIncludedDirectories();
            ArrayList files = new ArrayList();
            for (int i = 0; i < strfiles.length; i++)
            {
                File currentFile = new File(currentDir, strfiles[i]);
                if (currentFile.getParentFile().getAbsolutePath().equals(currentDir.getAbsolutePath()))
                {
                    files.add(currentFile);
                    parameterDirectory.setValue(currentFile.getAbsolutePath());
                    parameterName.setValue(currentFile.getName());
                    ant.execute();
                }
            }

        }
    }
}
