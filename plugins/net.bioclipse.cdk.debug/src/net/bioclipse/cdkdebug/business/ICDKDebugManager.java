/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 ******************************************************************************/
package net.bioclipse.cdkdebug.business;

import java.lang.reflect.InvocationTargetException;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass("Contains CDK debug related methods")
@TestClasses("net.bioclipse.cdk.debug.test.CDKDebugManagerTest")
public interface ICDKDebugManager extends IBioclipseManager {

    /**
     * @param mol The molecule to save
     */
    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule mol, ICDKMolecule mol2",
         methodSummary = "Returns the differences between the two molecules"
    )
    @TestMethods("testDiff")
    public String diff(ICDKMolecule mol, ICDKMolecule mol2);
    
    /**
     * @param mol The molecule to save
     */
    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule mol",
         methodSummary = "Returns a string representation of the data structures."
    )
    @TestMethods("testDebug")
    public String debug(ICDKMolecule mol);

    @Recorded
    @PublishedMethod(
         params = "IMolecule mol",
         methodSummary = "Returns a list of Sybyl atom types."
    )
    @TestMethods("testDepictSybylAtomTypes")
    public ICDKMolecule perceiveSybylAtomTypes(IMolecule mol) throws InvocationTargetException;

    @Recorded
    @PublishedMethod(
         params = "IMolecule mol",
         methodSummary = "Returns a list of CDK atom types."
    )
    @TestMethods("testDepictCDKAtomTypes")
    public String perceiveCDKAtomTypes(IMolecule mol) throws InvocationTargetException;

}
