/*******************************************************************************
 *Copyright (c) 2009 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package net.bioclipse.cdk.domain.tests;

import net.bioclipse.cdk.business.test.JavaCDKManagerPluginTest;
import net.bioclipse.cdk.business.test.JavaScriptCDKManagerPluginTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses( value = { JavaCDKManagerPluginTest.class,
                         JavaScriptCDKManagerPluginTest.class } )
public class AllCDKBusinessPluginTestSuite {

}
