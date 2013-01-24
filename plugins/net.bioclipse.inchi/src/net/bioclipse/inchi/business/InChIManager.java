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
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.sf.jniinchi.INCHI_KEY_STATUS;
import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;
import net.sf.jniinchi.INCHI_STATUS;
import net.sf.jniinchi.JniInchiException;
import net.sf.jniinchi.JniInchiWrapper;

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
    	if (!isAvailable()) {
    		returner.completeReturn(InChI.FAILED_TO_CALCULATE);
    		return;
    	}
    	
    	Object adapted = molecule.getAdapter(IAtomContainer.class);
        if (adapted != null) {
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

    public void generate( IMolecule molecule, String options,
            IReturner<InChI> returner,
            IProgressMonitor monitor) 
    throws Exception {
    	monitor.beginTask("Calculating InChI", IProgressMonitor.UNKNOWN);
    	// return early if InChI library could not be loaded
    	if (!isAvailable()) {
    		returner.completeReturn(InChI.FAILED_TO_CALCULATE);
    		return;
    	}

    	Object adapted = molecule.getAdapter(IAtomContainer.class);
    	if (adapted != null) {
    		IAtomContainer container = (IAtomContainer)adapted;
    		IAtomContainer clone = (IAtomContainer)container.clone();
    		// remove aromaticity flags
    		for (IAtom atom : clone.atoms())
    			atom.setFlag(CDKConstants.ISAROMATIC, false);
    		for (IBond bond : clone.bonds())
    			bond.setFlag(CDKConstants.ISAROMATIC, false);
    		InChIGenerator gen = factory.getInChIGenerator(clone, options);
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

    public List<String> options() {
    	List<String> options = new ArrayList<String>();
    	for (INCHI_OPTION option : INCHI_OPTION.values()) {
    		options.add("" + option);
    	}
    	return options;
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

    public boolean checkKey(String inchikey) throws BioclipseException {
    	INCHI_KEY_STATUS status;
		try {
			status = JniInchiWrapper.checkInchiKey(inchikey);
		} catch (JniInchiException exception) {
			throw new BioclipseException("Error while validating the inchi: " + exception.getMessage(), exception);
		}
    	if (status == INCHI_KEY_STATUS.VALID_STANDARD || status == INCHI_KEY_STATUS.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    public boolean check(String inchi) throws BioclipseException {
    	INCHI_STATUS status;
		try {
			status = JniInchiWrapper.checkInchi(inchi, false);
		} catch (JniInchiException exception) {
			throw new BioclipseException("Error while validating the inchi: " + exception.getMessage(), exception);
		}
    	if (status == INCHI_STATUS.VALID_STANDARD || status == INCHI_STATUS.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    public boolean checkStrict(String inchi) throws BioclipseException {
    	INCHI_STATUS status;
		try {
			status = JniInchiWrapper.checkInchi(inchi, true);
		} catch (JniInchiException exception) {
			throw new BioclipseException("Error while validating the inchi: " + exception.getMessage(), exception);
		}
    	if (status == INCHI_STATUS.VALID_STANDARD || status == INCHI_STATUS.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    public boolean isAvailable() {
    	if (!isLoaded && loadingFailed) return false;
    	if (!loadingFailed && isLoaded) return true;
    	load();
    	return (factory != null);
    }
}
