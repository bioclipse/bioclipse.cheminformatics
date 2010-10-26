/*******************************************************************************
 * Copyright (c) 2008-2009  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/    
 ******************************************************************************/
package net.bioclipse.inchi.business;

import net.bioclipse.core.api.Recorded;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.core.api.jobs.IBioclipseJob;
import net.bioclipse.core.api.jobs.IReturner;
import net.bioclipse.core.api.managers.IBioclipseManager;
import net.bioclipse.core.api.managers.PublishedClass;
import net.bioclipse.core.api.managers.PublishedMethod;
import net.bioclipse.core.api.managers.TestClasses;
import net.bioclipse.core.api.managers.TestMethods;
import net.bioclipse.inchi.InChI;

@PublishedClass ("Manager for creating InChI and InChIKeys.")
@TestClasses(
    "net.bioclipse.inchi.business.test.APITest," +
    "net.bioclipse.inchi.business.test.JavaInChIManagerPluginTest"
)
public interface IInChIManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule",
        methodSummary = "Generates the InChI and InChIKey for the " +
        		"given molecule.")
    @TestMethods("testGenerate")
    public InChI generate(IMolecule molecule) throws Exception;
    public IBioclipseJob<InChI> generate( IMolecule molecule,
                                          IReturner<InChI> h );

    @Recorded
    @PublishedMethod(
        methodSummary = "Loads the InChI library.")
    public String load();

    @Recorded
    @PublishedMethod(
        methodSummary = "Returns true if the InChI library could be loaded.")
    public boolean isLoaded();

    @Recorded
    @PublishedMethod(
        methodSummary = "Returns true if the InChI library is available for your" +
        		"platform.")
    public boolean isAvailable();
}
