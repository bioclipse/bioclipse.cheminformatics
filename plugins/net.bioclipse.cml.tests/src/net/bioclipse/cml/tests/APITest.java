/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 *               2009 Egon Willighagen <egonw@users.sf.net> 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *
 ******************************************************************************/
package net.bioclipse.cml.tests;

import net.bioclipse.cml.managers.IValidateCMLManager;
import net.bioclipse.cml.managers.ValidateCMLManager;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;

public class APITest extends AbstractManagerTest {

    IValidateCMLManager cml = new ValidateCMLManager();

    @Override
    public IBioclipseManager getManager() {
        return cml;
    }

    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
        return IValidateCMLManager.class;
    }
}
