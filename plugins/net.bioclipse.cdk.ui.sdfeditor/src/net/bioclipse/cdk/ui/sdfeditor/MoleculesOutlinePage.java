/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     Arvid Berg
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditorContentProvider;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


/**
 * @author arvid
 *
 */
public class MoleculesOutlinePage extends ContentOutlinePage implements
        ISelectionListener {

    
    Image image2d;
    
    
    private IEditorInput input;
    
        
    @Override
    public void createControl( Composite parent ) {    
        
        
        super.createControl( parent );
        
        getTreeViewer().setContentProvider( 
                           new MoleculesEditorContentProvider(getTreeViewer()));
        
        getTreeViewer().setLabelProvider( new ILabelProvider() {

            public Image getImage( Object element ) {

                return MoleculesOutlinePage.this.getImage();
            }

            public String getText( Object element ) {

                if(element instanceof IAdaptable){
                    ICDKMolecule molecule = (ICDKMolecule)
                        ((IAdaptable)element).getAdapter( ICDKMolecule.class );
                   if(molecule !=null){
                       return molecule.getName();
                   }                  
                }
                return "-X-";
            }

            public void addListener( ILabelProviderListener listener ) {

            }

            public void dispose() {

            }

            public boolean isLabelProperty( Object element, String property ) {
                return false;
            }

            public void removeListener( ILabelProviderListener listener ) { 
                
            }
            
        });
        
       getTreeViewer().setInput( input
                                 .getAdapter( IMoleculesEditorModel.class ) );
       
       getSite().setSelectionProvider( this );
       getSite().getPage().addSelectionListener( this );
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        if (part.equals( this )) return;
        if (!( selection instanceof IStructuredSelection )) return;
        IStructuredSelection sel = (IStructuredSelection) selection;

        //Only set selection if something new
        if (((IStructuredSelection)getTreeViewer().getSelection()).toList().containsAll( sel.toList() ))
            return;
        else
            getTreeViewer().setSelection( selection );

    }
    public void setInput( IEditorInput editorInput ) {
        this.input = editorInput;
    }

    private Image getImage(){
        if(image2d == null) {
            ImageDescriptor imageDescriptor 
                        = Activator.imageDescriptorFromPlugin(
                         "net.bioclipse.ui", "icons/mol_2d.png" );
            if(imageDescriptor !=null) 
                image2d = imageDescriptor.createImage();
            
        }
        return image2d;
    }
}
