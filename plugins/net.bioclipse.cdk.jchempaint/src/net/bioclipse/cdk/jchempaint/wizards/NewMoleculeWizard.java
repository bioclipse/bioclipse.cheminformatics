/*******************************************************************************
 * Copyright (c) 2008  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.wizards;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ui.business.Activator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openscience.cdk.DefaultChemObjectBuilder;

/**
 * Creates a new molecule and opens it in JChemPaint.
 * 
 * @author egonw
 */
public class NewMoleculeWizard extends Wizard implements INewWizard {

	public static final String WIZARD_ID =
		"net.bioclipse.cdk.jchempaint.wizards.NewMoleculeWizard"; //$NON-NLS-1$
    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewMoleculeWizard() {
        super();
    }

    public void addPages() {
    }

    public boolean canFinish() {
        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle("New Molecule");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
      //Open editor with content (String) as content
        ICDKMolecule mol = new CDKMolecule(DefaultChemObjectBuilder
                                                  .getInstance().newMolecule());
        try {
            Activator.getDefault().getUIManager().open( mol, 
                                "net.bioclipse.cdk.ui.editors.jchempaint.cml" );
        } catch ( BioclipseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
