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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Creates a new molecule and opens it in JChemPaint.
 * 
 * @author egonw
 */
public class NewMoleculeWizard extends Wizard implements INewWizard {

	public static final String WIZARD_ID =
		"net.bioclipse.cdk.jchempaint.wizards.NewMoleculeWizard"; //$NON-NLS-1$
	
	public static String newline = System.getProperty("line.separator");
	
	private static final String FILE_CONTENT =
		"<molecule xmlns=\"http://www.xml-cml.org/schema\">" + newline +
		"  <atomArray>" + newline +
        "  </atomArray>" + newline +
		"  <bondArray>" + newline +
        "  </bondArray>" + newline +
		"</molecule>" + newline;
	
    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewMoleculeWizard() {
        super();
    }

    public void addPages() {
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle("New Molecule");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        return true;
    }
}
