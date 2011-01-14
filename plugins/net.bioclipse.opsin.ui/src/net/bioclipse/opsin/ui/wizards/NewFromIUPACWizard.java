/* Copyright (c) 2011  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/epl-v10.html/.
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opsin.ui.wizards;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.opsin.business.IOpsinManager;
import net.bioclipse.opsin.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

public class NewFromIUPACWizard extends BasicNewResourceWizard {

    private static final Logger logger =
        Logger.getLogger(NewFromIUPACWizard.class);

    public static final String WIZARD_ID =
        "net.bioclipse.cdk.ui.wizards.NewFromSMILESWizard"; //$NON-NLS-1$
    
    private IUPACInputWizardPage mainPage;
    
    private String iupac = null;
    
    public void setIUPAC(String iupac) {
        this.iupac = iupac;
    }

    public String getIUPAC() {
        return iupac;
    }

    public boolean canFinish() {
        return getIUPAC() != null;
    }
    
    public void addPages() {
        super.addPages();
        mainPage = new IUPACInputWizardPage("newFilePage0", this);//$NON-NLS-1$
        mainPage.setTitle("IUPAC Name");
        mainPage.setDescription("Create a new resource from a IUPAC name"); 
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle("New Molecule From a IUPAC Name");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        //Open editor with content (String) as content
        IOpsinManager opsin = net.bioclipse.opsin.Activator.getDefault().getJavaOpsinManager();
        ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
        try {
            ICDKMolecule mol = opsin.parseIUPACName(getIUPAC());
            mol = cdk.generate2dCoordinates(mol);
            CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(
            	NoNotificationChemObjectBuilder.getInstance()
            );
            IAtomType[] types = matcher.findMatchingAtomType(
            	mol.getAtomContainer()
            );
            for (int i=0; i<types.length; i++) {
            	if (types[i] != null) {
            		mol.getAtomContainer().getAtom(i).setAtomTypeName(
            			types[i].getAtomTypeName()
            		);
            	}
            }
            net.bioclipse.ui.business.Activator.getDefault().getUIManager()
            	.open(mol, "net.bioclipse.cdk.ui.editors.jchempaint.cml");
        } catch (Exception e) {
            LogUtils.handleException(
                e, logger,
                Activator.PLUGIN_ID
            );
        }
        return true;
    }

}
