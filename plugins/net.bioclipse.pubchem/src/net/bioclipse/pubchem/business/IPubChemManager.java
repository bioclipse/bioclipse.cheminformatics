/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.pubchem.business;

import java.io.IOException;
import java.util.List;

import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IPubChemManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        params = "int PubChem Compound ID, String path to save the content too", 
        methodSummary = "Loads the PubChem Compound XML with the given number" +
        		"to the given path"
    )
    public void loadCompound(int cid, String target)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    public void loadCompound(int cid, IFile target, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "int PubChem Compound ID, String path to save the content too",
        methodSummary = "Loads the PubChem Compound 3D MDL molfile with the" +
            " given number to the given path"
    )
    public void loadCompound3d(int cid, String target)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    public void loadCompound3d(int cid, IFile target, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "String query against PubChem", 
        methodSummary = "Returns a List of matching compound CIDs."
    )
    public List<Integer> search(String query)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    public List<Integer> search(String query, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

}
