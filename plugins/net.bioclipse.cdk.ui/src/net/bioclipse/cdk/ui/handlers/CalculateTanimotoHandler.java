/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stefan Kuhn - Implementation of the tanimoto ui elements
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.text.DecimalFormat;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.wizards.TanimotoWizard;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A handler for calculating tanimoto similarities
 *
 */
public class CalculateTanimotoHandler extends AbstractHandler {

    private static final Logger logger =
                                               Logger
                                                       .getLogger( CalculateTanimotoHandler.class );

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {

        ISelection sel =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        DecimalFormat formatter = new DecimalFormat( "0.00" );
        if ( !sel.isEmpty() ) {
            if ( sel instanceof IStructuredSelection ) {
                try {
                    IStructuredSelection ssel = (IStructuredSelection) sel;
                    ICDKManager cdkmanager =
                            net.bioclipse.cdk.business.Activator.getDefault()
                                    .getJavaCDKManager();
                    // In case of two files, we compare each other, else we ask
                    // for a comparision file
                    if ( ssel.toArray().length == 2 ) {
                        ICDKMolecule calculateFor =
                                cdkmanager
                                    .loadMolecule((IFile) ssel.toArray()[0]);
                        ICDKMolecule reference =
                                cdkmanager
                                    .loadMolecule((IFile) ssel.toArray()[1]);
                        double similarity =
                                cdkmanager.calculateTanimoto( calculateFor,
                                                              reference );
                        MessageBox mb =
                                new MessageBox( new Shell(),
                                                SWT.ICON_INFORMATION | SWT.OK );
                        mb.setText( "Similarity" );
                        mb.setMessage( ((IFile) ssel.toArray()[0]).getName()
                                       + " and "
                                       + ((IFile) ssel.toArray()[1]).getName()
                                       + " similarity: "
                                       + formatter.format( similarity * 100 )
                                       + "%" );
                        mb.open();
                    } else {
                        TanimotoWizard wiz=new TanimotoWizard(ssel);
                        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
                        dialog.open();
                    }
                } catch ( Exception ex ) {
                    LogUtils.handleException( ex, logger );
                }
            }
        }
        return null;
    }
}
