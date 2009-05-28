/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import org.eclipse.core.resources.IFile;

import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass(value = "Contains methods for interacting with lists of molecules")
public interface IMoleculeTableManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(methodSummary="Log a value")
    public void dummy();

    @Recorded
    public SDFileIndex createSDFIndex(IFile file,
           BioclipseUIJob<SDFileIndex> uiJob);
    @Recorded
    @PublishedMethod(params = "String file",
      methodSummary = "Creates a index of the molecules positons in a SDFile")
    public SDFileIndex createSDFIndex( String file );

    @Recorded
    @PublishedMethod(params = "SDFIndexEditorModel model,"
                             +" IPropertyCalculator calculator",
      methodSummary = "Calculate a property and sets it on the model for each"
                      +" molecule in the model")
    public void calculateProperty( SDFIndexEditorModel model,
                                   IPropertyCalculator<?> calculator);
}
