/*******************************************************************************
 * Copyright (c) 2012  Ola Spjuth <ola@bioclipse.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/    
 ******************************************************************************/
package net.bioclipse.chemspider.test;

import net.bioclipse.managers.business.IBioclipseManager;

import org.junit.BeforeClass;

public class JavaScriptChemspiderManagerPluginTest
    extends AbstractChemspiderManagerPluginTest {

    @BeforeClass public static void setup() {
        managerNamespace = net.bioclipse.chemspider.Activator.getDefault()
            .getJavaScriptChemspiderManager();
    }

	@Override
	public IBioclipseManager getManager() {
		return managerNamespace;
	}
}
