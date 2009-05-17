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

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.inchi.business.IInChIManager;
import net.bioclipse.inchi.business.InChIManager;
import net.bioclipse.managers.business.IBioclipseManager;

public class APITest extends AbstractManagerTest {

    InChIManager inchi = new InChIManager();

    @Override
    public IBioclipseManager getManager() {
        return inchi;
    }

    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
        return IInChIManager.class;
    }

}
