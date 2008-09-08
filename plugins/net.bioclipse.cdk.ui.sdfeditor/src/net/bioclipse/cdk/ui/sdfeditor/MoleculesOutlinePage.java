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
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


/**
 * @author arvid
 *
 */
public class MoleculesOutlinePage extends Page implements IContentOutlinePage, 
        ISelectionChangedListener,
        ISelectionListener {

    private ListenerList listeners = new ListenerList();
    private TreeViewer viewer;
    
    Image image2d;
    
    
    private IEditorInput input;
    
    
    public MoleculesOutlinePage() {
        super();
    }
    
    private TreeViewer getTreeViewer() {
        
           return viewer;
    }
    
    @Override
    public void createControl( Composite parent ) {    
        
        viewer = new TreeViewer(parent, SWT.MULTI |
                                        SWT.H_SCROLL |
                                        SWT.V_SCROLL |
                                        SWT.FULL_SELECTION |
                                        SWT.VIRTUAL
                                        );
        
        viewer.addSelectionChangedListener( this );
        
        
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
                       return MoleculesOutlinePage.this.getName( molecule );
                   }                  
                }
                return "-X-";
            }

            public void addListener( ILabelProviderListener listener ) {

            }

            public void dispose() {
               if( MoleculesOutlinePage.this.image2d != null)
                   MoleculesOutlinePage.this.image2d.dispose();
            }

            public boolean isLabelProperty( Object element, String property ) {
                return false;
            }

            public void removeListener( ILabelProviderListener listener ) { 
                
            }
            
        });
        
       getTreeViewer().setInput( input
                                 .getAdapter( IMoleculesEditorModel.class ) );
       
       getSite().setSelectionProvider( getTreeViewer() );
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
                                    net.bioclipse.core.Activator.PLUGIN_ID,                         
                         //"icons/mol_2d.png" );
                           "icons/source.gif");                                           
            if(imageDescriptor !=null) 
                image2d = imageDescriptor.createImage();
            
        }
        return image2d;
    }
    
    private String getName(ICDKMolecule molecule) {        
        StringBuilder builder = new StringBuilder();
        for(Object o:molecule.getAtomContainer().getProperties().values()) {
            builder.append( o.toString() );
            builder.append( ", " );
        }
        if(builder.length()>=2)
            builder.delete( builder.length()-2, builder.length()-1 );
        else
            builder.append( molecule.getName() );
        return builder.toString();
    }
    
    @Override
    public Control getControl() {
        return (viewer == null ? null: viewer.getControl());
    }
    
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    public void addSelectionChangedListener( ISelectionChangedListener listener ) {

        listeners.add( listener );
    }
    public ISelection getSelection() {
        if( viewer == null) {
            return StructuredSelection.EMPTY;
        }
        return viewer.getSelection();
    }
    public void removeSelectionChangedListener(
                                          ISelectionChangedListener listener ) {

        listeners.remove( listener );
        
    }
    public void setSelection( ISelection selection ) {
        if(viewer != null) {
            viewer.setSelection( selection );
        }       
    }
    public void selectionChanged( SelectionChangedEvent event ) {

        fireSelectionChanged(event.getSelection());        
    }

    private void fireSelectionChanged( ISelection selection ) {

        final SelectionChangedEvent event = new SelectionChangedEvent(
                                                                    this,
                                                                    selection);
        for(Object scl:listeners.getListeners()){
            final ISelectionChangedListener l = (ISelectionChangedListener) scl;
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
        
    }    
}