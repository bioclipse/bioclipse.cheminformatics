/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *      Arvid Berg 
 *     
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.Iterator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class MoleculesEditorContentProvider  implements
        ILazyTreeContentProvider {

    TreeViewer viewer;
    Logger logger = Logger.getLogger(MoleculesEditorContentProvider.class);
    IFile file;
    int numberOfEntries = 30;
    //DeferredTreeContentManager contentManager;
    
    public MoleculesEditorContentProvider(TreeViewer viewer) {

        this.viewer = viewer;

    }

    public void updateChildCount( Object element, int currentChildCount ) {

        //numberOfEntries = calculateChildCount();
        if(numberOfEntries != currentChildCount)
            viewer.setChildCount(element, numberOfEntries );

    }

    /*
     * Parse and create CDKMolecule and then create the image?
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object, int)
     */
    public void updateElement( Object parent, int index ) {
        
        Object element = null;
        try {
        Iterator<ICDKMolecule> iter = Activator.getDefault().getJavaCDKManager().createMoleculeIterator( file );
        ICDKMolecule molecule;
            int count = 0;
            while (iter.hasNext()){
                molecule=iter.next();
                if(count++ == index) {
                    element= molecule;
                    break;
                }
                
            }
            if(iter.hasNext()) count+=10;
            setChildCount(count);
            
        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
        
          if( element != null) 
              viewer.replace( parent, index, element );
    }
   
    private void setChildCount( int count ) {
        numberOfEntries = count;
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {    
        if( (viewer instanceof TreeViewer) && ((this.viewer != viewer ))){
            this.viewer = (TreeViewer)viewer;
        }
        if(oldInput != newInput){// && newInput instanceof IDeferredWorkbenchAdapter) {
            file = (IFile) newInput;
        }
    }

    public Object getParent( Object element ) {

        // TODO Auto-generated method stub
        return null;
    }

    public void dispose() {

        // TODO Auto-generated method stub
        
    }
}