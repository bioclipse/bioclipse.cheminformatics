/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.opsin.business;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;

public class OpsinManager implements IBioclipseManager {

	private CDKManager cdk = new CDKManager();

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "opsin";
    }

    public ICDKMolecule parseIUPACName(String iupacName, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();

    	monitor.beginTask("Processing IUPAC name", 1);
        return cdk.fromSMILES(parseIUPACNameAsSMILES(iupacName, monitor));
    }

    public String parseIUPACNameAsCML(String iupacName, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();

    	monitor.beginTask("Processing IUPAC name", 1);
    	NameToStructure nameToStructure;
		try {
			nameToStructure = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			throw new BioclipseException(
				"Error while loading OPSIN: " + e.getMessage(),
				e
			);
		}
        OpsinResult result = nameToStructure.parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
        	return result.getCml();
        }
        throw new BioclipseException(
        	"Could not parse the IUPAC name (" + iupacName + "), because: " +
        	result.getMessage()
        );
    }

    public String parseIUPACNameAsSMILES(String iupacName, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Processing IUPAC name", 1);
        NameToStructure nameToStructure;
        try {
            nameToStructure = NameToStructure.getInstance();
        } catch (NameToStructureException e) {
            throw new BioclipseException(
                "Error while loading OPSIN: " + e.getMessage(),
                e
            );
        }
        OpsinResult result = nameToStructure.parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
            return result.getExtendedSmiles();
        }
        throw new BioclipseException(
            "Could not parse the IUPAC name (" + iupacName + "), because: " +
            result.getMessage()
        );
    }
}
