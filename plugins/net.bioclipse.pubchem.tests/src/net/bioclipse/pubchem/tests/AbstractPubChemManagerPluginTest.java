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

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.pubchem.business.IPubChemManager;

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
    public void testDownload3d() throws Exception {
        IMolecule molecule = pubchem.download3d(3107);
        Assert.assertNotNull(molecule);
    }

}
