/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egon.willighagen@gmail.com>
 * Copyright (c) 2012  Ola Spjuth <ola.spjuth@gmail.com>
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
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
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
    
    
    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule, Float tanimoto", 
        methodSummary = "Query ChemSpider for similar molecules within a certain tanimoto distance."
    )
	public List<ICDKMolecule> similaritySearch(IMolecule molecule, Float tanimoto)
	throws BioclipseException;
	public List<ICDKMolecule> similaritySearch(IMolecule molecule, Float tanimoto, IProgressMonitor monitor)
	throws BioclipseException;


    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule", 
        methodSummary = "Query ChemSpider for a molecular structure"
    )
	public List<ICDKMolecule> simpleSearch(IMolecule molecule)
	throws BioclipseException;
	
    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule", 
        methodSummary = "Query ChemSpider for compounds where input is a substructure"
    )
	public List<ICDKMolecule> substructureSearch(IMolecule mol)
	throws BioclipseException;
	public List<ICDKMolecule> substructureSearch(IMolecule mol, IProgressMonitor monitor)
	throws BioclipseException;

    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule", 
        methodSummary = "Query ChemSpider for exact matches"
    )
	public List<ICDKMolecule> exactSearch(IMolecule molmonitor)
	throws BioclipseException;
	public List<ICDKMolecule> exactSearch(IMolecule molmonitor, IProgressMonitor monitor)
	throws BioclipseException;

    
}
