/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemoinformatics.util;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * Filter for getting only files containing sinlge molcules
 * 
 * @author jonalv
 *
 */
public class SingleMoleculeContentTypeViewerFilter extends ViewerFilter {

    @Override
    public boolean select( Viewer viewer,
                           Object parentElement,
                           Object element ) {

        //Filter out resources starting with '.'
        if ( element instanceof IResource ) {
            IResource res = (IResource) element;
            if (res.getName().startsWith( "." ) )
                return false;
        }

        //Filter out non-single-moleculear files
        if ( element instanceof IFile ) {
            IFile file = (IFile) element;
            try {
                if ( !ChemoinformaticUtils.isMolecule( file ) ) {
                    return false;
                }
            } 
            catch ( CoreException e ) {
                return false;
            } 
            catch ( IOException e ) {
                return false;
            }
        }

        //Filter out empty folders
        else if ( element instanceof IFolder ) {
            IFolder folder = (IFolder) element;
            try {
                if ( folder.members() != null && folder.members().length > 0 ) {
                    return true;
                }
            } 
            catch ( CoreException e ) {
                return false;
            }    
        }
        return true;
    }
}