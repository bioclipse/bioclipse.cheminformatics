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

import static org.openscience.cdk.graph.ConnectivityChecker.partitionIntoMolecules;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.Activator;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.smiles.FixBondOrdersTool;

public class NewFromSMILESWizard extends BasicNewResourceWizard {

    private static final Logger logger =
        Logger.getLogger(NewFromSMILESWizard.class);

    public static final String WIZARD_ID =
        "net.bioclipse.cdk.ui.wizards.NewFromSMILESWizard"; //$NON-NLS-1$
    
    private SMILESInputWizardPage mainPage;
    
    private String smiles = null;
    
    public void setSMILES(String smiles) {
        this.smiles = smiles;
    }

    public String getSMILES() {
        return smiles;
    }

    public boolean canFinish() {
        return getSMILES() != null;
    }
    
    public void addPages() {
        super.addPages();
        mainPage = new SMILESInputWizardPage("newFilePage0", this);//$NON-NLS-1$
        mainPage.setTitle("Open SMILES");
        mainPage.setDescription("Create a new resource from a SMILES"); 
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle("New Molecule From SMILES");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        //Open editor with content (String) as content
    	 IRunnableWithProgress job = new IRunnableWithProgress() {
    	      public void run(IProgressMonitor monitor) {
    	    	  SubMonitor progress = SubMonitor.convert(monitor);
    	    	  progress.beginTask("Creating molecule from SMILES", 200);
    	    	  Executor executor = Executors.newSingleThreadExecutor();
        		try {
        			ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
        			progress.subTask("Generating molecule");
        			ICDKMolecule mol = cdk.fromSMILES(getSMILES());
        			IMoleculeSet containers = partitionIntoMolecules(mol.getAtomContainer());
        			progress.worked(50);
        			for (IAtomContainer container : containers.molecules()) {
        				// ok, try to do something smart (tm) with SMILES input:
        				//   try to resolve bond orders
        				final FixBondOrdersTool tool = new FixBondOrdersTool();
        				final IMolecule cdkMol = asCDKMolecule(container);
        				try{
        					progress.setWorkRemaining(150);
        					FutureTask<IMolecule> future =
        						       new FutureTask<IMolecule>(new Callable<IMolecule>() {
        						         public IMolecule call() throws CDKException{
        						        	 IMolecule betterMol = tool.kekuliseAromaticRings(cdkMol);
        						        	 return betterMol;
        						       }});
        					progress.subTask("Finding double bonds");
        				    executor.execute(future);
        				    while(!future.isDone()) {
        				    	progress.worked(1);
        				    	if(progress.isCanceled()) {
        				    		future.cancel(true);
                                    tool.setInterrupted( true );
        				    		throw new OperationCanceledException();
        				    	}
        				    	Thread.sleep(1000);
        				    }
                            mol = new CDKMolecule( future.get() );
        				} catch(Exception e) {
        					logger.warn("Aromaticity detection failed due to "+e.getMessage());
                            mol = new CDKMolecule( cdkMol );
        				}
        				progress.setWorkRemaining(50);
        				progress.subTask("Generating coordinates");
        				progress.worked(25);
        				mol = cdk.generate2dCoordinates(mol);
        				net.bioclipse.ui.business.Activator.getDefault().getUIManager()
        					.open(mol, "net.bioclipse.cdk.ui.editors.jchempaint.cml");
        			}
        		} catch (Exception e) {
        			LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
        		}
        		progress.done();
        	}
        };
        try {
			getContainer().run(true, true,job);
		} catch (InvocationTargetException e) {
			LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
		} catch (InterruptedException e) {
			LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
		}
        return true;
    }

    /**
     * Converts (if needed) a CDK {@link IAtomContainer} into a CDK
     * {@link IMolecule}.
     */
	private org.openscience.cdk.interfaces.IMolecule
	    asCDKMolecule(IAtomContainer container) {
		if (container instanceof org.openscience.cdk.interfaces.IMolecule)
			return (org.openscience.cdk.interfaces.IMolecule)container;

		return container.getBuilder().newInstance(
			org.openscience.cdk.interfaces.IMolecule.class, container
		);
	}
    
}
