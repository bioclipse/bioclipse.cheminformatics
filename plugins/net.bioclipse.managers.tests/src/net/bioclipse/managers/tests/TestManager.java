package net.bioclipse.managers.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.managers.business.IBioclipseManager;

import static org.junit.Assert.*;

/**
 * @author jonalv
 */
public class TestManager implements IBioclipseManager {

    public String getNamespace() {
        return "test";
    }
    
    public void getBioObjects( IFile file, 
                               BioclipseJob<IBioObject> job, 
                               IProgressMonitor monitor ) {

        assertNotNull( file );
        assertNotNull( monitor );
    }
    
    public String getGreeting(String name) {
        return "OH HAI " + name;
    }
}
