/* Copyright (c) 2009,2013  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.pubchem.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.pubchem.business.IPubChemManager;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractPubChemManagerPluginTest {

    protected static IPubChemManager pubchem;

    @Test
    public void testDownload3DAsString() throws Exception {
        String content = pubchem.download3dAsString(3107);
        Assert.assertNotNull(content);
        Assert.assertNotSame(0, content.length());
    }

    @Test
    public void testDownloadAsString() throws Exception {
        String content = pubchem.downloadAsString(3107);
        Assert.assertNotNull(content);
        Assert.assertNotSame(0, content.length());
    }

    @Test
    public void testDownload() throws Exception {
        IMolecule molecule = pubchem.download(3107);
        Assert.assertNotNull(molecule);
    }

    @Test
    public void testDownload_List() throws Exception {
    	List<Integer> cids = new ArrayList<Integer>();
    	cids.add(3107);
    	cids.add(3108);
        List<IMolecule> molecules = pubchem.download(cids);
        Assert.assertNotNull(molecules);
        Assert.assertEquals(2, molecules.size());
    }

    @Test
    public void testDownload3d() throws Exception {
        IMolecule molecule = pubchem.download3d(3107);
        Assert.assertNotNull(molecule);
    }

    @Test
    public void testDownload3d_List() throws Exception {
    	List<Integer> cids = new ArrayList<Integer>();
    	cids.add(3107);
    	cids.add(3108);
        List<IMolecule> molecules = pubchem.download3d(cids);
        Assert.assertNotNull(molecules);
        Assert.assertEquals(2, molecules.size());
    }

    @Test
    public void testDownloadRDF() throws Exception {
    	RDFManager rdf = new RDFManager();
        IRDFStore store = rdf.createInMemoryStore(); 
        pubchem.downloadRDF(3107, store);
        String turtle = rdf.asTurtle(store);
        Assert.assertNotNull(turtle);
        Assert.assertNotSame(0, turtle.length());
        Assert.assertTrue(turtle.contains("3107"));
    }

    @Test
    public void testSearch() throws Exception {
        List<Integer> cids = pubchem.search("aspirin");
        Assert.assertNotNull(cids);
        Assert.assertNotSame(0, cids.size());
    }

    @Test
    public void testLoadCompoundRDF() throws IOException, BioclipseException, CoreException {
    	String path = "/Virtual/3706_loadCompoundRDF.rdf";
    	String returnedPath = pubchem.loadCompoundRDF(3706, path);
    	IFile file = ResourcePathTransformer.getInstance().transform(path);
    	Assert.assertTrue("File " + path + " does not exist.", file.exists());
    }

    @Test
    public void testLoadCompound3d() throws IOException, BioclipseException, CoreException {
    	String path = "/Virtual/3705_loadCompound3d.rdf";
    	String returnedPath = pubchem.loadCompound3d(3705, path);
    	IFile file = ResourcePathTransformer.getInstance().transform(path);
    	Assert.assertTrue("File " + path + " does not exist.", file.exists());
    }

    @Test
    public void testLoadCompound() throws IOException, BioclipseException, CoreException {
    	String path = "/Virtual/3704_loadCompound.rdf";
    	String returnedPath = pubchem.loadCompound(3704, path);
    	IFile file = ResourcePathTransformer.getInstance().transform(path);
    	Assert.assertTrue("File " + path + " does not exist.", file.exists());
    }
}
