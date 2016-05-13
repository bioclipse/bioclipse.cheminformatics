/*******************************************************************************
 * Copyright (c) 2016  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chebi.business;

import java.io.IOException;
import java.text.ParseException;

import net.bioclipse.chebi.domain.ChEBIMolecule;
import net.bioclipse.chebi.domain.IChEBIMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import uk.ac.manchester.libchebi.ChebiException;

public class ChebiManager implements IBioclipseManager {

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "chebi";
    }

    public IChEBIMolecule newMolecule(String identifier) throws BioclipseException {
    	try {
			return new ChEBIMolecule(identifier);
		} catch (IOException | ParseException | ChebiException e) {
			throw new BioclipseException(
				"Cannot create a molecule for the ChEBI ID " + identifier + ": " + e.getMessage(), e
			);
		}
    }
}
