package net.bioclipse.chemoinformatics.util;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class MoleculeContentTypeViewerFilter extends ViewerFilter {

    private static final Logger logger 
    = Logger.getLogger( MoleculeContentTypeViewerFilter.class );

    @Override
    public boolean select( Viewer viewer, 
                           Object parentElement, 
                           Object element ) {

        //Filter out all mols and folders starting with '.'
        if ( element instanceof IResource ) {
            IResource res = (IResource) element;
            if (res.getName().startsWith( "." ))
                return false;
        }

        //Filter out non-moleculear files
        if ( element instanceof IFile ) {
            IFile file = (IFile) element;
            try {
                if((!(ChemoinformaticUtils.isMolecule( file ))) 
                        && (!(ChemoinformaticUtils.isMultipleMolecule( file ))))
                    return false;
            } catch ( CoreException e ) {
                return false;
            } catch ( IOException e ) {
                return false;
            }
        }
        
        //Filter out empty folders
        else if ( element instanceof IFolder ) {
            IFolder folder = (IFolder) element;
            try {
                if (folder.members()!=null && folder.members().length>0)
                    return true;
            } catch ( CoreException e ) {
                return false;
            }    
        }
        return true;
    }

}
