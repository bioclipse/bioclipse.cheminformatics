/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: Bioclipse Project <http://www.bioclipse.net>
 ******************************************************************************/
package net.bioclipse.chemoinformatics.test.feature;

import net.bioclipse.cdk.AllCDKBusinessTestsSuite;
import net.bioclipse.pubchem.tests.AllPubChemManagerTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit testing for all plugins in this feature.
 * 
 * @author egonw
 */
@RunWith(value=Suite.class)
@SuiteClasses(value={
    FeatureTest.class,
    AllCDKBusinessTestsSuite.class,
    AllPubChemManagerTests.class
})
public class AllFeatureTest {

}
