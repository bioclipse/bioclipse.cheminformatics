/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package net.bioclipse.cml.managers;

import java.io.IOException;

import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;

@TestClasses("net.bioclipse.cml.tests.ValidateCMLManagerTest")
public interface IValidateCMLManager extends IBioclipseManager {

    @Recorded
    public void validate( IFile input ) throws IOException;

    @Recorded
    @PublishedMethod(params = "String filename", methodSummary = "Checks if the file indicated by filename in workspace is valid  "
                                                                 + "CML")
    public String validate( String filename ) throws IOException,
                                             BioclipseException;
}
