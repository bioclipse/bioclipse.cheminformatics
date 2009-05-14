/*******************************************************************************
 * Copyright (c) 2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.inchi.business.test;

import org.junit.BeforeClass;

public class JavaScriptInChIManagerPluginTest
    extends AbstractInChIManagerPluginTest {

    @BeforeClass public static void setup() {
        inchi = net.bioclipse.inchi.business.Activator.getDefault().
            getJavaScriptInChIManager();
        cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
    }

}
