/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/    
 ******************************************************************************/
package net.bioclipse.cdk.debug.test;

import org.junit.BeforeClass;

public class JavaScriptCDKDebugManagerPluginTest extends
		AbstractCDKDebugManagerPluginTest {

    @BeforeClass public static void setup() {
        debug = net.bioclipse.cdkdebug.Activator.getDefault()
            .getJavaScriptManager();
        cdk = net.bioclipse.cdk.business.Activator.getDefault()
            .getJavaCDKManager();
    }

}
