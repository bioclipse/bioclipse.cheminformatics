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

import net.bioclipse.chebi.domain.IChEBIMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass(
    value="Manager that wraps around the libChEBIj library (https://github.com/libChEBI/libChEBIj).",
    doi={"10.1186/s13321-016-0123-9"}
)
public interface IChebiManager extends IBioclipseManager {

	@Recorded
    @PublishedMethod(
    	params="String identifier",
        methodSummary = "Creates a ChEBI molecule from an identifier"
    )
    public IChEBIMolecule newMolecule(String identifier) throws BioclipseException;
	
}
