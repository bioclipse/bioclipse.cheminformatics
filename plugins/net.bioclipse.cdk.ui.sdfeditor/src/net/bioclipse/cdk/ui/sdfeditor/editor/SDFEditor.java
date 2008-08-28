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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.widgets.SWTRenderer;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.cdk.ui.views.MoleculeContentProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;

public class SDFEditor extends EditorPart implements ISelectionProvider,
        ISelectionListener {

    public final static int STRUCTURE_COLUMN_WIDTH = 100;
    Logger                  logger        = Logger.getLogger( SDFEditor.class );
    List<String>            propertyHeaders;
    SWTRenderer             renderer;
    TreeViewer              viewer;
    Collection<ISelectionChangedListener> selectionListeners = 
                                 new LinkedHashSet<ISelectionChangedListener>();

    @Override
    public void doSave( IProgressMonitor monitor ) {

        // TODO Auto-generated method stub

    }

    @Override
    public void doSaveAs() {

        // TODO Auto-generated method stub

    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {

        super.setSite( site );
        super.setInput( input );
        IFile file;
        if ( (file = (IFile) input.getAdapter( IFile.class )) != null )
            setPartName( file.getName() );
        // TODO listen to selections check and focus on selected element from
        // common navigator, load it and get columns

    }

    @Override
    public boolean isDirty() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {

        // TODO Auto-generated method stub
        return false;
    }

    private void setupRenderer() {
        
        renderer = new SWTRenderer( new Renderer2DModel() );
        renderer.getRenderer2DModel().setDrawNumbers( false );
        renderer.getRenderer2DModel().setIsCompact( true );
        renderer.getRenderer2DModel().setBondWidth( 20 );

    }

    @Override
    public void createPartControl( Composite parent ) {
        
        setupRenderer();
        viewer =
                new TreeViewer( parent, SWT.VIRTUAL | SWT.V_SCROLL
                                        | SWT.H_SCROLL | SWT.MULTI
                                        | SWT.FULL_SELECTION | SWT.BORDER ) {

                    @Override
                    public void add( Object parentElementOrTreePath,
                                     Object[] childElements ) {

                        if ( propertyHeaders == null
                             && childElements.length > 0 ) {
                            // TODO make it a job on the GUI thread
                            if ( childElements[0] instanceof SDFElement )
                                createHeaderFromSelection( (SDFElement) childElements[0] );
                        }
                        super.add( parentElementOrTreePath, childElements );
                    }
                };

        Tree tree = viewer.getTree();
        tree.setHeaderVisible( true );

        TreeColumn itemColumn = new TreeColumn( tree, SWT.NONE );
        itemColumn.setText( "Index" );
        itemColumn.setResizable( true );
        itemColumn.setWidth( 100 );

        TreeColumn nameColumn = new TreeColumn( tree, SWT.NONE );
        nameColumn.setText( "Structure" );
        nameColumn.setResizable( false );
        nameColumn.setWidth( STRUCTURE_COLUMN_WIDTH );

        viewer.setComparer( new IElementComparer() {

            public boolean equals( Object a, Object b ) {

                if ( a == b )
                    return true;
                if ( !a.getClass().equals( b.getClass() ) )
                    return false;
                if ( a instanceof SDFElement ) {
                    SDFElement e1 = (SDFElement) a;
                    SDFElement e2 = (SDFElement) b;
                    if ( e1.getNumber() == e2.getNumber()
                         && e1.getPosition() == e2.getPosition()
                         && (e1.getResource() == e2.getResource() || (e1
                                 .getResource() != null && e1.getResource()
                                 .equals( e2.getResource() ))) ) {
                        return true;
                    }

                }
                return (a != null && a.equals( b ));
            }

            public int hashCode( Object element ) {

                if ( element instanceof SDFEditor ) {
                    SDFElement e1 = (SDFElement) element;
                    int var = 8;
                    var = 31 * var + e1.getNumber();
                    var = 31 * var                         
                         + (int) (e1.getPosition() ^ (e1.getPosition() >>> 32));
                    var = 31 * var + (e1.getResource() == null ? 
                                               0 : e1.getResource().hashCode());
                    return var;
                }
                return element.hashCode();
            }

        } );        

        viewer.setColumnProperties( new String[] { "Index", "Name" } );

        viewer.setContentProvider( new MoleculeContentProvider() {

            @Override
            public Object[] getElements( Object parentElement ) {

                if ( parentElement instanceof SDFElement ) {
                    ICDKMolecule mol =
                            (ICDKMolecule) ((SDFElement) parentElement)
                                    .getAdapter( ICDKMolecule.class );
                    if ( mol != null )
                        return mol.getAtomContainer().getProperties().values()
                                .toArray();
                }
                return super.getElements( parentElement );
            }
        } );
        viewer.setLabelProvider( new ITableLabelProvider() {

            Collection<ILabelProviderListener> listeners =
                                                                 new HashSet<ILabelProviderListener>();

            public Image getColumnImage( Object element, int columnIndex ) {

                if ( columnIndex == 1 && element instanceof SDFElement ) {
                    ICDKMolecule mol =
                            (ICDKMolecule) ((SDFElement) element)
                                    .getAdapter( ICDKMolecule.class );
                    if ( mol == null )
                        return null;
                    IAtomContainer drawMolecule = mol.getAtomContainer();
                    Dimension screenSize =
                            new Dimension( STRUCTURE_COLUMN_WIDTH,
                                           STRUCTURE_COLUMN_WIDTH );

                    // If no 2D coords
                    if ( GeometryTools.has2DCoordinates( drawMolecule ) == false ) {
                        // Test if 3D coords
                        if ( GeometryTools.has3DCoordinates( drawMolecule ) == true ) {
                            // Collapse on XY plane
                            drawMolecule = SWTRenderer.generate2Dfrom3D( 
                                                                 drawMolecule );

                        }
                    }

                    GeometryTools.translateAllPositive( drawMolecule );
                    GeometryTools.scaleMolecule( drawMolecule, screenSize, 0.8 );
                    GeometryTools.center( drawMolecule, screenSize );

                    // renderer.getRenderer2DModel().setRenderingCoordinates(
                    // coordinates);
                    renderer.getRenderer2DModel()
                            .setBackgroundDimension( screenSize );
                    renderer.getRenderer2DModel()
                            .setShowExplicitHydrogens( false );

                    Image image =
                            new Image( Display.getDefault(),
                                       STRUCTURE_COLUMN_WIDTH,
                                       STRUCTURE_COLUMN_WIDTH );
                    GC gc;
                    renderer.paintMolecule(
                                            drawMolecule,
                                            gc = new GC( image ),
                                            new Rectangle2D.Double(
                                                     0,
                                                     0,
                                                     STRUCTURE_COLUMN_WIDTH,
                                                     STRUCTURE_COLUMN_WIDTH ) );
                    gc.dispose();
                    return image;
                }
                return null;
            }

            public String getColumnText( Object element, int columnIndex ) {

                // offset the index to the properties so get(x) works
                int propertyindex = columnIndex - 2;
                String text = null;
                if ( element instanceof SDFElement ) {
                    SDFElement row = (SDFElement) element;
                    ICDKMolecule molecule =
                            (ICDKMolecule) row.getAdapter( ICDKMolecule.class );

                    // if(propertyHeaders==null && molecule!=null)
                    // createPropertyHeaders( molecule.getAtomContainer());

                    switch ( columnIndex ) {
                        case 0:
                            text = Integer.toString( row.getNumber() );
                            break;
                        case 1:
                            // text = Long.toString(row.getPosition());
                            break;
                        default:
                            if ( molecule == null
                                 || propertyindex >= propertyHeaders.size() )
                                return null;
                            IAtomContainer atomContainer =
                                    molecule.getAtomContainer();
                            text = atomContainer.getProperty(
                                          propertyHeaders.get( propertyindex ) )
                                        .toString();
                    }
                }
                return text;
            }

            public void addListener( ILabelProviderListener listener ) {

                listeners.add( listener );
            }

            public void dispose() {

                listeners.clear();
            }

            public boolean isLabelProperty( Object element, String property ) {

                // TODO Auto-generated method stub
                return false;
            }

            public void removeListener( ILabelProviderListener listener ) {

                listeners.remove( listener );
            }

        } );
        viewer.setInput( new MoleculesFromSDF( (IFile) getEditorInput()
                .getAdapter( IFile.class ) ) );

        getEditorSite().getPage().addSelectionListener( this );
        // See what's currently selected and select it
        ISelection selection =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection stSelection = (IStructuredSelection) selection;
            reactOnSelection( stSelection );
        }
    }

    private void createHeaderFromSelection( SDFElement element ) {

        ICDKMolecule molecule = null;

        // try and get molecule
        if ( element != null ) {
            molecule = (ICDKMolecule) element.getAdapter( ICDKMolecule.class );
            // create headers
            createPropertyHeaders( molecule.getAtomContainer() );
        }
    }

    private void reactOnSelection( IStructuredSelection selection ) {

        Object element = selection.getFirstElement();
        if ( element instanceof SDFElement )
            viewer.setSelection( new StructuredSelection( element ), true );
    }

    @Override
    public void setFocus() {

       viewer.getTree().setFocus();

    }

    public void addSelectionChangedListener( ISelectionChangedListener listener ) {

        selectionListeners.add(listener );

    }

    public ISelection getSelection() {

        return viewer.getSelection();
        
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {

        selectionListeners.remove(listener );

    }

    public void setSelection( ISelection selection ) {

        viewer.setSelection( selection );

    }

    @SuppressWarnings("unchecked")
    private void createPropertyHeaders( IAtomContainer ac ) {

        // property keys not Strings but i assume they are
        Set<Object> propterties = ac.getProperties().keySet();
        propertyHeaders =
                new ArrayList<String>( new LinkedHashSet( propterties ) );
        Tree tree = viewer.getTree();
        int oldCount = tree.getColumnCount();
        // creates missing columns so that column count is
        // proptertyHeaders.size()+2
        for ( int i = propertyHeaders.size() - (oldCount - 2); i > 0; i-- ) {
            new TreeColumn( tree, SWT.NONE );
        }
        // set property name as column text
        for ( int i = 0; i < (propertyHeaders.size()); i++ ) {
            TreeColumn tc = tree.getColumn( i + 2 );
            tc.setText( propertyHeaders.get( i ) );
            tc.setWidth( 100 );
            tc.setResizable( true );
        }

    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        reactOnSelection( (IStructuredSelection) selection );

    }
}
