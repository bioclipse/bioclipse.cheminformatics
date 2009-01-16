/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * Stefan Kuhn
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.SaveAsDialog;

/**
 * A handler class for a Generate 2D Coordinates menu item
 */
public class Create2dHandler extends AbstractHandler {

    private static final Logger logger =
                                               Logger
                                                       .getLogger( Create2dHandler.class );
    public int                  answer;

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        ISelection sel =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        if ( !sel.isEmpty() ) {
            if ( sel instanceof IStructuredSelection ) {
                IStructuredSelection ssel = (IStructuredSelection) sel;
                ICDKMolecule mol;
                try {
                    mol =
                            Activator.getDefault().getCDKManager()
                                    .loadMolecule(
                                                   (IFile) ssel
                                                           .getFirstElement() );
                    mol =
                            (ICDKMolecule) Activator.getDefault()
                                    .getCDKManager()
                                    .generate2dCoordinates( mol );
                } catch ( Exception e ) {
                    LogUtils.handleException( e, logger );
                    return null;
                }
                MessageBox mb =
                        new MessageBox( new Shell(), SWT.YES | SWT.NO | SWT.CANCEL
                                                     | SWT.ICON_QUESTION );
                mb.setText( "Change file" );
                mb.setMessage( "Do you want to write the 2D coordinates into the existing file? If no, a new one will be created." );
                int val = mb.open();
                if ( val == SWT.YES ) {
                    try {
                        Activator.getDefault().getCDKManager()
                                .saveMolecule(
                                               mol,
                                               (IFile) ssel.getFirstElement(),
                                               ((IFile) ssel.getFirstElement())
                                                       .getFileExtension() );
                    } catch ( Exception e ) {
                        throw new RuntimeException( e.getMessage() );
                    }
                } else if ( val == SWT.NO ){
                    SaveAsDialog dialog = new SaveAsDialog( new Shell() );
                    int saveasreturn = dialog.open();
                    IPath result = dialog.getResult();
                    if ( saveasreturn != SaveAsDialog.CANCEL ) {
                        if ( dialog.getResult().getFileExtension() == null )
                            result =
                                    result.addFileExtension( ((IFile) ssel
                                            .getFirstElement())
                                            .getFileExtension() );
                        try {
                            if ( ((IFile) ssel.getFirstElement())
                                    .getWorkspace().getRoot().getFile( result )
                                    .exists() ) {
                                new Shell().getDisplay()
                                        .syncExec( new Runnable() {

                                            public void run() {

                                                MessageBox mb =
                                                        new MessageBox(
                                                                        new Shell(),
                                                                        SWT.YES
                                                                                | SWT.NO
                                                                                | SWT.ICON_QUESTION );
                                                mb.setText( "File exists" );
                                                mb
                                                        .setMessage( "This file already exists. Do you want to overwrite it?" );
                                                Create2dHandler.this.answer =
                                                        mb.open();
                                            }
                                        } );
                                if ( answer == SWT.YES )
                                    Activator
                                            .getDefault()
                                            .getCDKManager()
                                            .saveMolecule(
                                                           mol,
                                                           ((IFile) ssel
                                                                   .getFirstElement())
                                                                   .getWorkspace()
                                                                   .getRoot()
                                                                   .getFile(
                                                                             result ),
                                                           ((IFile) ssel
                                                                   .getFirstElement())
                                                                   .getFileExtension() );
                            } else {
                                Activator
                                        .getDefault()
                                        .getCDKManager()
                                        .saveMolecule(
                                                       mol,
                                                       ((IFile) ssel
                                                               .getFirstElement())
                                                               .getWorkspace()
                                                               .getRoot()
                                                               .getFile( result ),
                                                       ((IFile) ssel
                                                               .getFirstElement())
                                                               .getFileExtension() );
                            }
                        } catch ( Exception e ) {
                            throw new RuntimeException( e.getMessage() );
                        }
                    }
                }
            }
        }
        return null;
    }

}
