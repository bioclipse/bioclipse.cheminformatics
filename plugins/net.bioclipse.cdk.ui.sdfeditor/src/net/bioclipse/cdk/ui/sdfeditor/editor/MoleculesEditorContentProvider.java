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
import net.bioclipse.cdk.ui.views.MoleculeContentProvider;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class MoleculesEditorContentProvider extends MoleculeContentProvider implements
        ILazyTreeContentProvider {

    TreeViewer viewer;
    Logger logger = Logger.getLogger(MoleculesEditorContentProvider.class);
    public MoleculesEditorContentProvider(TreeViewer viewer) {

        this.viewer = viewer;

    }

    public void updateChildCount( Object element, int currentChildCount ) {

        if( hasChildren(element )){
            if(element instanceof MoleculesFromSDF){                
                int length =  ((MoleculesFromSDF)element)
                              .getChildren(element ).length;
                if(length != currentChildCount)
                    viewer.setChildCount(element,length );
            }
            
        }
    }

    /*
     * Parse and create CDKMolecule and then create the image?
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object, int)
     */
    public void updateElement( Object parent, int index ) {
        Object[] elements;
        if(parent instanceof MoleculesFromSDF){
           elements=((MoleculesFromSDF)parent).getChildren(parent);           
//        }else if(parent instanceof IMoleculesProvider){
//            logger.debug("SDF.updateElement( AnnotationUIModel)");
//            elements=new Object[0];
        }else{
            elements = getElements( parent );
        }
            
        if ( index >= elements.length
             || !(elements[index] instanceof IAdaptable) )
            return;

        ICDKMolecule molecule =
                (ICDKMolecule) ((IAdaptable) elements[index])
                        .getAdapter( ICDKMolecule.class );
        if ( molecule != null )
            viewer.replace( parent, index, molecule );

    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.MoleculeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {       
        super.inputChanged( viewer, oldInput, newInput );
        if(newInput instanceof MoleculesFromSDF)
            getElements(newInput );
    }
}