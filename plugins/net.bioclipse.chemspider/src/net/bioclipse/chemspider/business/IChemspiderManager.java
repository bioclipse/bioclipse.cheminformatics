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
package net.bioclipse.chemspider.business;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass(
    value="Manager that allows interaction with the ChemSpider database."
)
public interface IChemspiderManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        params = "int csid, String path", 
        methodSummary = "Loads the ChemSpider MDL molfile with the given " +
        	"ChemSpider ID (csid) to the given path"
    )
    public String loadCompound(int csid, String path)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "String inchiKey", 
        methodSummary = "Returns a List of matching compound CSIDs."
    )
    public List<Integer> resolve(String inchiKey)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer csid", 
        methodSummary = "Loads the ChemSpider compound with the given " +
                "identifier into a String."
    )
    public String downloadAsString(Integer csid)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer csid", 
        methodSummary = "Loads the ChemSpider compound with the given " +
                "identifier into a IMolecule."
    )
    public IMolecule download(Integer cid)
        throws IOException, BioclipseException, CoreException;
}
