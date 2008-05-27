package net.bioclipse.cdk.ui.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.ui.views.MoleculeContentProvider;
import net.bioclipse.core.domain.IAASequence;
import net.bioclipse.core.domain.IDNASequence;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.ISequence;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MoleculeContentProviderTest {


    @Test
    public void testGetChildren() throws CoreException, IOException {

        //Create WS with data
        Map<String, IFile> files =createWorkspaceWithData();

        //Create ContentProvider to test
        MoleculeContentProvider provider=new MoleculeContentProvider();

        //New file to test, contains one molecule
        //================
        IFile gbkFile=files.get("sar.sdf");
        assertNotNull(gbkFile);
        
        Object[] obj=provider.getChildren(gbkFile);
        assertNotNull(obj);
        assertEquals(1, obj.length);
        assertTrue(obj[0] instanceof IMolecule);

        //New file to test
        //================
        gbkFile=files.get("iterconftest.sdf");
        assertNotNull(gbkFile);
        
        obj=provider.getChildren(gbkFile);
        assertNotNull(obj);
        assertEquals(39, obj.length);
        assertTrue(obj[0] instanceof IMolecule);
        assertTrue(obj[1] instanceof IMolecule);
        assertTrue(obj[2] instanceof IMolecule);
        assertTrue(obj[3] instanceof IMolecule);
        //...

        //New file to test
        //================
        IFile fastaFile=files.get("0037.cml");
        assertNotNull(fastaFile);
        
        obj=provider.getChildren(fastaFile);
        assertNotNull(obj);
        assertEquals(1, obj.length);
        assertTrue(obj[0] instanceof IMolecule);

        //New file to test
        //================
        IFile malFile=files.get("mal.sdf");
        assertNotNull(malFile);
        
        obj=provider.getChildren(malFile);
        assertNotNull(obj);
        assertEquals(3, obj.length);

    }



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
