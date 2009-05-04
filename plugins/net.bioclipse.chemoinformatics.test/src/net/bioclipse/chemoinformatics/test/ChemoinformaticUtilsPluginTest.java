/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 *
 ******************************************************************************/
package net.bioclipse.chemoinformatics.test;

import java.io.IOException;
import java.io.InputStream;

import net.bioclipse.chemoinformatics.util.ChemoinformaticUtils;
import net.bioclipse.core.MockIFile;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChemoinformaticUtilsPluginTest {

    @BeforeClass
    public static void setupCDKManagerPluginTest() {
        // the next line is needed to ensure the OSGI loader properly start
        // the org.springframework.bundle.osgi.extender, so that the manager
        // can be loaded too. Otherwise, it will fail with a time out.
        net.bioclipse.ui.Activator.getDefault();
    }
    
    @Test public void testIsMolecule() throws Exception {
        String path = getClass().getResource("/testFiles/aromatic.mol").getPath();
        MockIFile mf = new MockIFile(path) {
            public InputStream getContents() {
                try {
                    return getClass().getResource("/testFiles/aromatic.mol")
                        .openStream();                    
                } catch (IOException e) {
                }
                return null;
            }
        };
        Assert.assertTrue(ChemoinformaticUtils.isMolecule(mf));
    }
    
}
