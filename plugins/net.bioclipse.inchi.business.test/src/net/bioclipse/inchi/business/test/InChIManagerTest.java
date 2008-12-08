/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
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
package net.bioclipse.inchi.business.test;

import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.inchi.business.IInChIManager;
import net.bioclipse.inchi.business.InChIManager;

public class InChIManagerTest extends AbstractManagerTest {

    IInChIManager inchi;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public InChIManagerTest() {
        inchi = new InChIManager();
    }

    public IBioclipseManager getManager() {
        return inchi;
    }

}
