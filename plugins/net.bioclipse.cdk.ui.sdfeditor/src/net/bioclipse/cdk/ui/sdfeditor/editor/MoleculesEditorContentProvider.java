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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.MoleculeContentProvider;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class MoleculesEditorContentProvider  implements
        ILazyTreeContentProvider {

    TreeViewer viewer;
    Logger logger = Logger.getLogger(MoleculesEditorContentProvider.class);
    
    DeferredTreeContentManager contentManager;
    
    public MoleculesEditorContentProvider(TreeViewer viewer) {

        this.viewer = viewer;

    }

    public void updateChildCount( Object element, int currentChildCount ) {
        
        if(element instanceof IMoleculesEditorModel){
            int count = ((IMoleculesEditorModel)element).getNumberOfMolecules();
            if(count != currentChildCount)
                viewer.setChildCount(element, count );
        }
    }

    /*
     * Parse and create CDKMolecule and then create the image?
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object, int)
     */
    public void updateElement( Object parent, int index ) {
        
        Object element = null;
        if(parent instanceof IMoleculesEditorModel){
            element = ((IMoleculesEditorModel)parent).getMoleculeAt(index );          
        }
//        if(element == null && (parent instanceof IDeferredWorkbenchAdapter)){
//            Object[] objs = contentManager.getChildren( parent );
//            if(objs.length>0)
//                viewer.replace( parent, index, objs[0] );
//            return;
//        }
//        
            
        
        if(element instanceof IAdaptable){
            ICDKMolecule molecule = (ICDKMolecule) ((IAdaptable) element)
                                    .getAdapter( ICDKMolecule.class );
        if ( molecule != null )
            viewer.replace( parent, index, 
                                new MoleculeEditorElement(index,molecule) );
        }
    }
   

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.MoleculeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {    
        if( (viewer instanceof TreeViewer) && ( 
                (contentManager == null) || 
                (this.viewer != viewer ))){
            this.viewer = (TreeViewer)viewer;
            contentManager = new DeferredTreeContentManager(
                                                    (AbstractTreeViewer)viewer);            
        }
        if(oldInput != newInput && newInput instanceof IDeferredWorkbenchAdapter) {
            Object[] objs = contentManager.getChildren( newInput );
            this.viewer.add( newInput, objs[0] );
        }
//        super.inputChanged( viewer, oldInput, newInput );
//        if(newInput instanceof MoleculesFromSDF)
//            getElements(newInput );
    }

    public Object getParent( Object element ) {

        // TODO Auto-generated method stub
        return null;
    }

    public void dispose() {

        // TODO Auto-generated method stub
        
    }
}