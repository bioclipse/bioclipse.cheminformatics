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
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.business.IRDFStore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

@TestClasses("net.bioclipse.pubchem.tests.AbstractPubChemManagerPluginTest")
public interface IPubChemManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        params = "int cid, String target", 
        methodSummary = "Loads the PubChem Compound XML with the given number" +
        		"to the given path."
    )
    @TestMethods("testLoadCompound")
    public String loadCompound(int cid, String target)
        throws IOException, BioclipseException, CoreException;

    public IFile loadCompound(int cid, IFile target, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    public BioclipseJob<IFile> loadCompound(int cid, 
                                         IFile target, 
                                         BioclipseJobUpdateHook<IFile> hook )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "int cid, String target",
        methodSummary = "Loads the PubChem Compound 3D MDL molfile with the" +
            " given number to the given path"
    )
    @TestMethods("testLoadCompound3d")
    public String loadCompound3d(int cid, String target)
        throws IOException, BioclipseException, CoreException;

    public IFile loadCompound3d(int cid, IFile target, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    public BioclipseJob<IFile> loadCompound3d(int cid, 
                                            IFile target, 
                                            BioclipseJobUpdateHook<IFile> hook )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "int cid, String target",
        methodSummary = "Loads the RDF document of the PubChem Compound with the" +
            " given number to the given path."
    )
    @TestMethods("testLoadCompoundRDF")
    public String loadCompoundRDF(int cid, String target)
        throws IOException, BioclipseException, CoreException;

    public IFile loadCompoundRDF(int cid, IFile target, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    public BioclipseJob<IFile> loadCompoundRDF(int cid, 
                                            IFile target, 
                                            BioclipseJobUpdateHook<IFile> hook )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer cid", 
        methodSummary = "Loads the PubChem Compound XML with the given " +
                "compound identifier into a IMolecule."
    )
    @TestMethods("testDownload")
    public IMolecule download(Integer cid)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer cid", 
        methodSummary = "Loads the PubChem Compound 3D MDL molfile with the " +
        		"given compound identifier into a IMolecule."
    )
    @TestMethods("testDownload3d")
    public IMolecule download3d(Integer cid)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer cid, IRDFStore store", 
        methodSummary = "Loads the PubChem Compound RDF with the " +
        		"given compound identifier into the given RDF store."
    )
    public IRDFStore downloadRDF(Integer cid, IRDFStore store)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer cid", 
        methodSummary = "Loads the PubChem Compound XML with the given " +
                "compound identifier into a String."
    )
    @TestMethods("testDownloadAsString")
    public String downloadAsString(Integer cid)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "Integer cid", 
        methodSummary = "Loads the PubChem Compound 3D MDL molfile with the " +
                "given compound identifier into a String."
    )
    @TestMethods("testDownload3DAsString")
    public String download3dAsString(Integer cid)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "List<Integer> cids", 
        methodSummary = "Loads the PubChem Compound XMLs for the given " +
                "list of compound identifiers into a List<IMolecule>."
    )
    @TestMethods("testDownload_List")
    public List<IMolecule> download(List<Integer> cids)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "List<Integer> cids", 
        methodSummary = "Loads the PubChem Compound 3D MDL molfiles for the " +
                "given list of compound identifiers into a List<IMolecule>."
    )
    @TestMethods("testDownload3D_List")
    public List<IMolecule> download3d(List<Integer> cids)
        throws IOException, BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "String query", 
        methodSummary = "Returns a List of matching compound CIDs."
    )
    @TestMethods("testSearch")
    public List<Integer> search(String query)
        throws IOException, BioclipseException, CoreException;

    public List<Integer> search(String query, IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    public BioclipseJob<IFile> search(String query,
                                      BioclipseJobUpdateHook<IFile> hook)
          throws IOException, BioclipseException, CoreException;

}