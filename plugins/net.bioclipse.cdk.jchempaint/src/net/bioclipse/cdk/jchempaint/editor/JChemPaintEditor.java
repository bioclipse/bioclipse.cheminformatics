/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arvid Berg
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.editor;

import java.util.Iterator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.renderer.selection.LogicalSelection;
import org.openscience.cdk.renderer.selection.LogicalSelection.Type;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JChemPaintEditor extends EditorPart implements ISelectionListener {

    private Logger logger = Logger.getLogger(JChemPaintEditor.class);

    private JCPOutlinePage fOutlinePage;

    private ICDKMolecule           model;
    private JChemPaintEditorWidget widget;
    private IControllerModel       c2dm;
    private Menu                   menu;
    
    public JChemPaintEditorWidget getWidget() {
        return widget;
    }
    
    public void undo() throws ExecutionException {
        widget.undo();
    }
    
    public void redo() throws ExecutionException {
        widget.redo();
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {
        ICDKManager cdk = Activator.getDefault().getCDKManager();

        try {
            IFile resource = (IFile)model.getResource();
            IChemFormat chemFormat = cdk.determineFormat(
                resource.getContentDescription().getContentType()
            );
            if (chemFormat == MDLV2000Format.getInstance() ||
                chemFormat == CMLFormat.getInstance()) {
                cdk.saveMolecule(
                        model,
                        model.getResource().getLocationURI().toString(),
                        true // overwrite
                );
                widget.setDirty( false );
            } else {
                doSaveAs();
            }
        } catch ( BioclipseException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        } catch ( CDKException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        } catch ( CoreException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        }
    }

    @Override
    public void doSaveAs() {

        SaveAsDialog saveAsDialog = new SaveAsDialog( this.getSite().getShell() );
        if ( model.getResource() instanceof IFile )
            saveAsDialog.setOriginalFile( (IFile) model.getResource() );
        int result = saveAsDialog.open();
        if ( result == 1 ) {
            logger.debug( "SaveAs canceled." );
            return;
        }

        IPath path = saveAsDialog.getResult();
        try {
            // do a nasty trick... the SaveAs dialog does not allow us to
            // ask for a format (yet), so guess something from the file
            // extension
            IChemFormat format = Activator.getDefault().getCDKManager()
                .guessFormatFromExtension(path.toString());
            if (format == null) format = (IChemFormat)CMLFormat.getInstance();

            Activator.getDefault().getCDKManager().saveMolecule(
                model, path.toPortableString(), format, true
            );
        } catch ( BioclipseException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        } catch ( CDKException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        } catch ( CoreException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        }
        widget.setDirty( false );
    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
                                       throws PartInitException {

        setSite( site );
        setInput( input );
        ICDKMolecule cModel = 
            (ICDKMolecule) input.getAdapter( ICDKMolecule.class );
        
        if ( cModel == null ) {
            IFile file = (IFile) input.getAdapter( IFile.class );
            if ( file != null )
                cModel = (ICDKMolecule) file.getAdapter( ICDKMolecule.class );
        }
        if ( cModel != null ) {

            setPartName( input.getName() );
            model = cModel;
        }
    }

    @Override
    public boolean isDirty() {

        return widget.getDirty();
    }

    @Override
    public boolean isSaveAsAllowed() {

        return true;
    }

    @Override
    public void createPartControl( Composite parent ) {

        // create widget
        widget = new JChemPaintEditorWidget( parent, SWT.NONE ) {

            @Override
            public void setDirty( boolean dirty ) {

                super.setDirty( dirty );
                firePropertyChange( IEditorPart.PROP_DIRTY );
            }
        };

        MenuManager menuMgr = new MenuManager();
        menuMgr.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        getSite().registerContextMenu( menuMgr, widget );
   
        menu = menuMgr.createContextMenu( widget );
        widget.setMenu( menu );

        // setup hub
        getSite().setSelectionProvider( widget );
        getSite().getPage().addSelectionListener( this );
        widget.setInput( model );

        parent.addDisposeListener( new DisposeListener() {

            public void widgetDisposed( DisposeEvent e ) {

                disposeControll( e );
            }
        } );

    }

    @Override
    public void setFocus() {

        widget.setFocus();
    }

    public ControllerHub getControllerHub() {

        return widget.getControllerHub();
    }

    public IControllerModel getControllerModel() {

        return c2dm;
    }

    public void update() {

        widget.redraw();
    }

    public void setInput( Object element ) {

        widget.setInput( element );
        widget.redraw();
    }

    public ICDKMolecule getCDKMolecule() {

        return model;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {

        if ( IContentOutlinePage.class.equals( adapter ) ) {
            if ( fOutlinePage == null ) {
                fOutlinePage = new JCPOutlinePage( getEditorInput(), this );
            }
            return fOutlinePage;
        }
        if ( IAtomContainer.class.equals( adapter ) ) {
            return model.getAtomContainer();
        }
        return super.getAdapter( adapter );
    }

    public void doAddAtom() {

        logger.debug( "Executing 'Add atom' action" );
    }

    public void doChangeAtom() {

        logger.debug( "Executing 'Chage atom' action" );
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        if ( part != null && part.equals( this ) )
            return;
        if ( selection instanceof IStructuredSelection ) {

            IAtomContainer container = null;

            for ( Iterator<?> iter = ((IStructuredSelection) selection)
                    .iterator(); iter.hasNext(); ) {
                Object c = iter.next();
                if ( !(c instanceof CDKChemObject) ) {
                    continue;
                }

                IChemObject o = ((CDKChemObject) c).getChemobj();
                if ( container == null )
                    container = o.getBuilder().newAtomContainer();
                if ( o instanceof IAtom ) {
                    container.addAtom( (IAtom) o );
                } else if ( o instanceof IBond ) {
                    container.addBond( (IBond) o );
                }
            }

            LogicalSelection sel = new LogicalSelection( Type.NONE );
            if ( container != null ) {
                sel.select( container );
                widget.getRenderer2DModel().setSelection( sel );
            } else {
                widget.getRenderer2DModel().setSelection( sel );
            }
            widget.redraw();
        }
    }

    private void disposeControll( DisposeEvent e ) {

        // TODO remove regiistration?
        // getSite().registerContextMenu(
        // "net.bioclipse.cdk.ui.editors.jchempaint.menu",
        // menuMgr, widget);

        getSite().setSelectionProvider( null );
        getSite().getPage().removeSelectionListener( this );

        widget.dispose();
        menu.dispose();
    }
}
