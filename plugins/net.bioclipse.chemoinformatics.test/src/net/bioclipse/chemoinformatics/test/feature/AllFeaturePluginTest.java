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

import net.bioclipse.cdk.AllCDKBusinessPluginTestSuite;
import net.bioclipse.cdk.debug.test.AllCDKDebugManagerPluginTests;
import net.bioclipse.cml.tests.AllValidateCMLManagerPluginTest;
import net.bioclipse.inchi.business.test.AllInChIManagerPluginTests;
import net.bioclipse.opsin.test.AllOpsinManagerPluginTests;
import net.bioclipse.pubchem.tests.AllPubChemManagerPluginTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit testing for all managers in this feature.
 * 
 * @author egonw
 */
@RunWith(value=Suite.class)
@SuiteClasses(value={
    AllCDKBusinessPluginTestSuite.class,
    AllPubChemManagerPluginTests.class,
    AllInChIManagerPluginTests.class,
    AllValidateCMLManagerPluginTest.class,
    AllCDKDebugManagerPluginTests.class,
    AllOpsinManagerPluginTests.class
})
public class AllFeaturePluginTest {}
