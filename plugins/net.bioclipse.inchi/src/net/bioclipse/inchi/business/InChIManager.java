/*******************************************************************************
 * Copyright (c) 2007-2009  Jonathan Alvarsson
 *               2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.inchi.business;

import java.security.InvalidParameterException;

import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.sf.jniinchi.INCHI_RET;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

public class InChIManager implements IBioclipseManager {

	private static final String LOADING_SUCCESS =
		"InChI library is loaded.";

    protected InChIGeneratorFactory factory;
    private boolean loadingFailed = false;
    private boolean isLoaded = false;

    public String getManagerName() {
        return "inchi";
    }

    public void generate( IMolecule molecule, 
                          IReturner<InChI> returner,
                          IProgressMonitor monitor) 
                throws Exception {
    	monitor.beginTask("Calculating InChI", IProgressMonitor.UNKNOWN);
    	// return early if InChI library could not be loaded
    	if (loadingFailed) returner.completeReturn(InChI.FAILED_TO_CALCULATE);
    	
    	Object adapted = molecule.getAdapter(IAtomContainer.class);
        if (adapted != null) {
        	// ensure we can actually generate an InChI
            if (!isLoaded && !LOADING_SUCCESS.equals(load())) {
            	returner.completeReturn(InChI.FAILED_TO_CALCULATE);
            }

            IAtomContainer container = (IAtomContainer)adapted;
            IAtomContainer clone = (IAtomContainer)container.clone();
            // remove aromaticity flags
            for (IAtom atom : clone.atoms())
                atom.setFlag(CDKConstants.ISAROMATIC, false);
            for (IBond bond : clone.bonds())
                bond.setFlag(CDKConstants.ISAROMATIC, false);
            InChIGenerator gen = factory.getInChIGenerator(clone);
            INCHI_RET status = gen.getReturnStatus();
            if(monitor.isCanceled())
            	throw new OperationCanceledException();
            if (status == INCHI_RET.OKAY ||
            		status == INCHI_RET.WARNING) {
            	monitor.done();
            	InChI inchi = new InChI();
            	inchi.setValue(gen.getInchi());
            	inchi.setKey(gen.getInchiKey());
            	returner.completeReturn( inchi );
            } else {
            	throw new InvalidParameterException(
            			"Error while generating InChI (" + status + "): " +
            			gen.getMessage()
            	);
            }
        } else {
            throw new InvalidParameterException(
                "Given molecule must be a CDKMolecule"
            );
        }
    }

    public String load() {
        if (factory == null) {
            try {
				factory = InChIGeneratorFactory.getInstance();
			} catch (Exception exception) {
				loadingFailed = true;
				isLoaded = false;
				return "Loading of the InChI library failed: " +
				       exception.getMessage();
			}
        }
        loadingFailed = false;
        isLoaded = true;
        return LOADING_SUCCESS;
    }

    @Recorded
    @PublishedMethod(
        methodSummary = "Returns true if the InChI library could be loaded.")
    public boolean isLoaded() {
    	return isLoaded;
    }

    public boolean isAvailable() {
    	if (!isLoaded && loadingFailed) return false;
    	if (!loadingFailed && isLoaded) return true;
    	load();
    	return isLoaded;
    }
}
