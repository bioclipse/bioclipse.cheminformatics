/*
 * Copyright (C) 2005 Bioclipse Project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn - Implementation of the tanimoto ui elements
 */
package net.bioclipse.cdk.ui.wizards;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;

/**
 * A wizard for selecting a file for tanimoto calculation and for executing the 
 * calculation.
 *
 */
public class TanimotoWizard extends Wizard {

    SelectFileWizardPage         selectFilePage;
    private static final Logger  logger =
                                                Logger
                                                        .getLogger( TanimotoWizard.class );

    private IStructuredSelection ssel;

    /**
     * @param ssel The selection in the navigator, containing the files to calculate similarity for.
     */
    public TanimotoWizard(IStructuredSelection ssel) {

        setWindowTitle( "Calculate Tanimoto similarity" );
        setNeedsProgressMonitor( true );
        this.ssel = ssel;
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {

        selectFilePage = new SelectFileWizardPage();
        addPage( selectFilePage );
    }

    @Override
    public boolean performFinish() {

        try {
            ICDKManager cdkmanager =
                    net.bioclipse.cdk.business.Activator.getDefault()
                            .getJavaCDKManager();
            IStructuredSelection referenceselection =
                    selectFilePage.getSelectedRes();
            IMolecule reference =
                    cdkmanager.loadMolecule( (IFile) referenceselection
                            .getFirstElement());
            List<IMolecule> mols = new ArrayList<IMolecule>();
            DecimalFormat formatter = new DecimalFormat( "0.00" );
            for ( int i = 0; i < ssel.size(); i++ ) {
                ICDKMolecule mol =
                        cdkmanager.loadMolecule( (IFile) ssel.toArray()[i]);
                mol
                        .getAtomContainer()
                        .setProperty(
                                      "Similarity",
                                      formatter
                                              .format( cdkmanager
                                                      .calculateTanimoto( mol,
                                                                          reference ) * 100 )
                                              + "%" );
                mols.add( mol );
            }
            IStructuredSelection virtualselection =
                    new StructuredSelection( net.bioclipse.core.Activator
                            .getVirtualProject() );
            IFile sdfile =
                    net.bioclipse.core.Activator
                            .getVirtualProject()
                            .getFile(
                                      WizardHelper
                                              .findUnusedFileName(
                                                                   virtualselection,
                                                                   "similarity",
                                                                   ".sdf" ) );
            cdkmanager.saveSDFile( sdfile, mols);
            net.bioclipse.ui.business.Activator.getDefault().getUIManager()
                    .open( sdfile );
        } catch ( Exception ex ) {
            LogUtils.handleException( ex, logger );
        }
        return true;
    }

}
