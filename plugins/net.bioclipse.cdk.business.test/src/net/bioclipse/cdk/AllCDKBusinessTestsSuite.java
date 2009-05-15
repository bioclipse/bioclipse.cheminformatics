/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cdk;

import net.bioclipse.cdk.business.test.APITest;
import net.bioclipse.cdk.business.test.CoverageTest;
import net.bioclipse.cdk.domain.tests.TestCDKMolecule;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses({
    TestCDKMolecule.class,
    APITest.class,
    CoverageTest.class
})
public class AllCDKBusinessTestsSuite {

}
