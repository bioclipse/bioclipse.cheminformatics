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
package net.bioclipse.pubchem.tests;

import net.bioclipse.core.api.managers.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.pubchem.business.IPubChemManager;
import net.bioclipse.pubchem.business.PubChemManager;

public class APITest extends AbstractManagerTest {

    PubChemManager pubchem = new PubChemManager();

    @Override
    public IBioclipseManager getManager() {
        return pubchem;
    }

    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
        return IPubChemManager.class;
    }

}
