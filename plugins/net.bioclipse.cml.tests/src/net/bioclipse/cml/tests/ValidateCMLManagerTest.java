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
package net.bioclipse.cml.tests;

import net.bioclipse.cml.managers.IValidateCMLManager;
import net.bioclipse.cml.managers.ValidateCMLManager;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cml.base.CMLElement;

public class ValidateCMLManagerTest extends AbstractManagerTest {

    IValidateCMLManager cml;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public ValidateCMLManagerTest() {
        cml = new ValidateCMLManager();
    }

    public IBioclipseManager getManager() {
        return cml;
    }

    @Test public void testFromString() throws Exception {
        CMLElement cmlElem = cml.fromString(
            "<molecule xmlns=\"http://www.xmlcml.org/schema\"/>"
        );
        Assert.assertTrue(cmlElem.getClass().getName().contains("CMLMolecule"));
    }

}
