/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cdk.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class MoleculeContentProviderTest {

    /**
     * Supporting method, not a Test
     * @return
     * @throws CoreException
     * @throws IOException
     */
    private Map<String, IFile> createWorkspaceWithData() throws CoreException, IOException {
        //Get WS root
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        //Create the project
        IProject project = root.getProject("UnitTestProject");
        
        IProgressMonitor dummyMonitor=new IProgressMonitor(){

            public void beginTask(String name, int totalWork) {
            }

            public void done() {
            }

            public void internalWorked(double work) {
            }

            public boolean isCanceled() {
                return false;
            }

            public void setCanceled(boolean value) {
            }

            public void setTaskName(String name) {
            }

            public void subTask(String name) {
            }

            public void worked(int work) {
            }

        };
        
        project.create(dummyMonitor);
        
        //Open project
        project.open(dummyMonitor);
        IPath projectPath = project.getFullPath();

        //Set up return map
        Map<String, IFile> files=new HashMap<String, IFile>();

        //Create files
        IPath gbkPath= projectPath.append("sar.sdf");
        IFile sarFile = root.getFile(gbkPath);
        InputStream gbkIS = getClass().getResourceAsStream("/testFiles/sar.sdf");
        sarFile.create(gbkIS,true,dummyMonitor);
        gbkIS.close();        
        files.put("sar.sdf", sarFile);

        //Create test file for mal sdf
        IPath malPath= projectPath.append("mal.sdf");
        IFile malFile = root.getFile(malPath);
        InputStream malIS = getClass().getResourceAsStream("/testFiles/mal.sdf");
        malFile.create(malIS,true,dummyMonitor);
        malIS.close();        
        files.put("mal.sdf", malFile);

        IPath gbkFailPath= projectPath.append("iterconftest.sdf");
        IFile iterFile = root.getFile(gbkFailPath);
        InputStream gbkFailIS = getClass().getResourceAsStream("/testFiles/iterconftest.sdf");
        iterFile.create(gbkFailIS,true,dummyMonitor);
        gbkFailIS.close();        
        files.put("iterconftest.sdf", iterFile);

        IPath fastaPath= projectPath.append("0037.cml");
        IFile cmlFile = root.getFile(fastaPath);
        InputStream fastaIS = getClass().getResourceAsStream("/testFiles/0037.cml");
        cmlFile.create(fastaIS,true,dummyMonitor);
        fastaIS.close();        
        files.put("0037.cml", cmlFile);

        //TODO: add more files
        return files;
    }

    /**
     * Helper method
     * @param FileLocation
     * @return
     */
    public static IFile findFileResourceByLocation (String FileLocation)
    {
        IPath ResourcePath = new Path(FileLocation);
        if (!ResourcePath.isAbsolute())
        {
            //this methods does not support relative paths
            return null;
        }
        else
        {
            IFile[] Files =
                getWorkspaceRoot().findFilesForLocation(ResourcePath);
            return (Files.length > 0) ? Files[0] : null;
        }
    }

    private static IWorkspaceRoot getWorkspaceRoot() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        return root;
    }

    public static IFile[] findFileResourcesByLocation (String FileLocation)
    {
        IPath ResourcePath = new Path(FileLocation);
        if (!ResourcePath.isAbsolute())
            //this methods does not support relative paths
            return new IFile[0];
        else
            return getWorkspaceRoot().findFilesForLocation(ResourcePath);
    }



}
