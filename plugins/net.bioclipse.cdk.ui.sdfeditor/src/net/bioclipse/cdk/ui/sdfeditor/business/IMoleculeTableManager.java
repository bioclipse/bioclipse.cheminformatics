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

import java.io.IOException;
import java.util.Collection;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IMoleculeTableManager extends IBioclipseManager {

    @Recorded
    public void dummy(String... strings);

    @Recorded
    public void createSDFIndex( IFile file,
                                BioclipseUIJob<SDFIndexEditorModel> uiJob);

    public BioclipseJob<SDFIndexEditorModel> createSDFIndex(IFile file,
                             BioclipseJobUpdateHook<SDFIndexEditorModel> hook);
    @Recorded
    @PublishedMethod( params ="String file",
                      methodSummary = "Creates a list model of molecules from file")
    public SDFIndexEditorModel createSDFIndex( String file );

    @Recorded
    public void calculateProperty( IMoleculesEditorModel model,
                                   IPropertyCalculator<?> calculator);
    @Recorded
    public void calculateProperty( IMoleculesEditorModel model,
                                   IPropertyCalculator<?> calculator,
                                   BioclipseUIJob<Void> uiJob);

    @Recorded
    public void calculateProperty( IMoleculesEditorModel model,
                                   IPropertyCalculator<?>[] calculators,
                                   BioclipseUIJob<Void> uiJob);

    @Recorded
    public void calculateProperties( ICDKMolecule molecule,
                                      IPropertyCalculator<?>[] calculators,
                                      BioclipseUIJob<Void> uiJob);
    @Recorded
    public void saveSDF(IMoleculesEditorModel model, IFile file)
                                                      throws BioclipseException;
    public void saveSDF( IMoleculesEditorModel model, IFile file,
                         BioclipseUIJob<IFileMoleculesEditorModel> uiJob)
                                                      throws BioclipseException;

    public void saveSDF( IFileMoleculesEditorModel model,
                         IReturner<IFileMoleculesEditorModel> returner)
                                                      throws BioclipseException;

    @Recorded
    public String saveSDF(IMoleculesEditorModel model, String file)
                                                      throws BioclipseException;

    @PublishedMethod(
        params="SDFIndexEditorModel model, Collection<String> propertyKeys",
        methodSummary="Extracts the given SD file properties from the raw content"
    )
    @Recorded
    public void parseProperties( SDFIndexEditorModel model,
                                 Collection<String> propertyKeys);

    public BioclipseJob<Void> parseProperties( SDFIndexEditorModel model,
                                 Collection<String> propertyKeys,
                                 BioclipseJobUpdateHook<Void> hook);


    public void calculatePropertiesFor( IFile file,
                                        IPropertyCalculator<?>[] calculator)
                                           throws BioclipseException;

    @PublishedMethod( params="IMoleculesEditorModel mol, String filename",
                      methodSummary="Saves the molecule list as a file as CSV")
    public void saveAsCSV(IMoleculesEditorModel model, String file) throws Exception;
    
    public void saveAsCSV( IMoleculesEditorModel model,IFile file, IProgressMonitor monitor) 
    throws BioclipseException, IOException, CoreException;

    
}
