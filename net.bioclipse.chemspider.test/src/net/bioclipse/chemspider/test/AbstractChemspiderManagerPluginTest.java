/*******************************************************************************
 * Copyright (c) 2012  Ola Spjuth <ola@bioclipse.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemspider.test;

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.chemspider.business.IChemspiderManager;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractChemspiderManagerPluginTest
extends AbstractManagerTest {

    protected static IChemspiderManager managerNamespace;
    
    @Test public void testDoSomething() {
        Assert.fail("This method should test something.");
    }

    public Class<? extends IBioclipseManager> getManagerInterface() {
    	return IChemspiderManager.class;
    }
}
