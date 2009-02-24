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
package net.bioclipse.chemoinformatics.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Creates a new CML molfile.
 * 
 * @author egonw
 */
public class NewCMLFileWizard extends BasicNewResourceWizard {

	public static final String WIZARD_ID =
		"net.bioclipse.chemoinformatics.wizards.NewCMLFileWizard"; //$NON-NLS-1$
	
	public static String newline = System.getProperty("line.separator");
	
	private static final String FILE_CONTENT =
		"<molecule xmlns=\"http://www.xml-cml.org/schema\">" + System.getProperty("line.separator") +
		"  <atomArray>" + System.getProperty("line.separator") +
        "  </atomArray>" + System.getProperty("line.separator") +
		"  <bondArray>" + System.getProperty("line.separator") +
        "  </bondArray>" + System.getProperty("line.separator") +
		"</molecule>" + System.getProperty("line.separator");
	
    private WizardNewFileCreationPage mainPage;

    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewCMLFileWizard() {
        super();
    }

    public void addPages() {
        super.addPages();
        mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection());//$NON-NLS-1$
        mainPage.setFileName(
            WizardHelper.findUnusedFileName(getSelection(), "unnamed", ".cml")
        );
        mainPage.setTitle("New CML File");
        mainPage.setDescription("Create a new CML file");
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle("New CML File");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null) {
			return false;
		}
        InputStream source = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        try {
			file.setContents(source, true, false, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
			return false;
		}

        selectAndReveal(file);

        IWorkbenchWindow bench = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (bench != null) {
                IWorkbenchPage page = bench.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        return true;
    }

}
