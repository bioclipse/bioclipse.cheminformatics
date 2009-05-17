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
package net.bioclipse.cml.tests;

import net.bioclipse.cml.managers.Activator;

import org.junit.BeforeClass;

public class JavaScriptValidateCMLManagerPluginTest
    extends AbstractValidateCMLManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
        cml = Activator.getDefault().getJavaScriptManager();
        cdk = net.bioclipse.cdk.business.Activator.getDefault()
            .getJavaScriptCDKManager();
    }
    
}
