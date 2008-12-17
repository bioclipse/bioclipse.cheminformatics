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
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeEditorElement;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditorContentProvider;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditorLabelProvider;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFElementComparer;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
/**
 * @author arvid
 *
 */
public class MoleculesOutlinePage extends Page implements IContentOutlinePage, 
        ISelectionChangedListener,//INullSelectionListener,
        ISelectionListener 
        {
    public static final int STRUCTURE_WITH = 50;
    Logger logger = Logger.getLogger( MoleculesOutlinePage.class );
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
//        viewer.addSelectionChangedListener( this );
        TreeColumn itemColumn = new TreeColumn( viewer.getTree(), SWT.NONE );
        itemColumn.setText( "Index" );
        itemColumn.setResizable( true );
        itemColumn.setWidth( 60 );
        TreeColumn nameColumn = new TreeColumn( viewer.getTree(), SWT.NONE );
        nameColumn.setText( "Structure" );
        nameColumn.setResizable( false );
        nameColumn.setWidth( STRUCTURE_WITH );
        getTreeViewer().setComparer( new SDFElementComparer() );
        getTreeViewer().setContentProvider( 
                           new MoleculesEditorContentProvider(getTreeViewer()));
        /*getTreeViewer().setLabelProvider( new ILabelProvider() {
            public Image getImage( Object element ) {
                return MoleculesOutlinePage.this.getImage();
            }
            public String getText( Object element ) {
                String text = "-X-";
                if(element instanceof IAdaptable){
                    ICDKMolecule molecule = (ICDKMolecule)
                        ((IAdaptable)element).getAdapter( ICDKMolecule.class );
                   if(molecule !=null){
                       text = MoleculesOutlinePage.this.buildNameString( molecule );
                   }else {
                       // TODO Override creation in DeferredTreeContentManager
                       //  and do a getName from WorkspaceAdapter
                       Object o=((IAdaptable)element).getAdapter( 
                                                   IWorkbenchAdapter.class );
                       if(o != null)
                           text = ((IWorkbenchAdapter)o).getLabel( o );                       
                   }
                   MoleculeEditorElement mee = (MoleculeEditorElement)
                                               ((IAdaptable)element)
                                     .getAdapter( MoleculeEditorElement.class );
                   if(mee != null)
                       text = Integer.toString( mee.getIndex() )+ ": "+ text;
                }
                return text;
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
        */
        getTreeViewer().setLabelProvider( new MoleculesEditorLabelProvider(
                                                               STRUCTURE_WITH));
       getTreeViewer().setInput( input
                                 .getAdapter( IMoleculesEditorModel.class ) );
       // TreeViewer provides selections and this listens to them
       getTreeViewer().addSelectionChangedListener( this );
       getSite().setSelectionProvider( viewer );
       getSite().getPage().addSelectionListener( this );
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
            //viewer.setSelection( selection );
        if(part instanceof ContentOutline || part.getSite() == this.getSite()) {
//                logger.debug( "site == site quit ");
                return;
        }
        if(part != this && selection instanceof IStructuredSelection) {
//            logger.debug( "Selection has chaged " + this.getClass().getName() );
            viewer.setSelection( selection, true );
            }        
    }
    public void setInput( IEditorInput editorInput ) {
        this.input = editorInput;
    }
    private Image getImage(){
        if(image2d == null) {
            ImageDescriptor imageDescriptor 
                        = Activator.imageDescriptorFromPlugin( 
                                    //net.bioclipse.ui.Activator.PLUGIN_ID,
                                    "net.bioclipse.ui",
                         "icons/chemistry/mol_2d.png" );
            if(imageDescriptor !=null) 
                image2d = imageDescriptor.createImage();
        }
        return image2d;
    }
    private String buildNameString(ICDKMolecule molecule) {        
        StringBuilder builder = new StringBuilder();
        builder.append( "[" );
        for(Object o:molecule.getAtomContainer().getProperties().values()) {
            builder.append( o.toString() );
            builder.append( ", " );
        }
        if(builder.length()>=2)
            builder.delete( builder.length()-2, builder.length()-1 );
        else
            builder.append( molecule.getName() );
        builder.append( "]" );
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
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
        listeners.add( listener );
        //getTreeViewer().addSelectionChangedListener( listener );
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
        if( viewer == null) {
            return StructuredSelection.EMPTY;
        }
        return viewer.getSelection();
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(
                                          ISelectionChangedListener listener ) {
        listeners.remove( listener );
//        getTreeViewer().removeSelectionChangedListener( listener );
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
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