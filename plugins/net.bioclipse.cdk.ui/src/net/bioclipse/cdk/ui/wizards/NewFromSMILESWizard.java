/*******************************************************************************
 * Copyright (c) 2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewFromSMILESWizard extends BasicNewResourceWizard {

    public static final String WIZARD_ID =
        "net.bioclipse.cdk.ui.wizards.NewFromSMILESWizard"; //$NON-NLS-1$
    
    private SMILESInputWizardPage mainPage;
    private WizardNewFileCreationPage selectFilePage;
    
    private String smiles = null;
    
    public void setSMILES(String smiles) {
        this.smiles = smiles;
    }

    public String getSMILES() {
        return smiles;
    }

    public boolean canFinish() {
        return (mainPage.canFlipToNextPage()) && (selectFilePage.isPageComplete());
    }
    
    public void addPages() {
        super.addPages();
        mainPage = new SMILESInputWizardPage("newFilePage0", this);//$NON-NLS-1$
        mainPage.setTitle("Open SMILES");
        mainPage.setDescription("Create a new resource from a SMILES"); 
        addPage(mainPage);
        
        selectFilePage = new WizardNewFileCreationPage("newFilePage1", getSelection());//$NON-NLS-1$
        selectFilePage.setTitle("Select File");
        selectFilePage.setDescription("Select target file");
        ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if(sel instanceof IStructuredSelection)
            selectFilePage.setFileName( WizardHelper.findUnusedFileName((IStructuredSelection)sel, "unnamed", ".sdf") );
        addPage(selectFilePage);
    }

    public boolean performFinish() {
        IFile file = selectFilePage.createNewFile();
        if (file == null) {
            return false;
        }
        CDKManager cdk = new CDKManager();
        try {
            ICDKMolecule cdkMol = cdk.fromSMILES(getSMILES());
            IMolecule newMol = cdk.generate2dCoordinates(cdkMol);
            InputStream source = new ByteArrayInputStream(newMol.getCML().getBytes());
            file.setContents(source, true, false, null);
        } catch (CoreException e1) {
            e1.printStackTrace();
            return false;
        } catch (BioclipseException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            // thrown by cdk.generate2dCoordinates()
            e.printStackTrace();
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
