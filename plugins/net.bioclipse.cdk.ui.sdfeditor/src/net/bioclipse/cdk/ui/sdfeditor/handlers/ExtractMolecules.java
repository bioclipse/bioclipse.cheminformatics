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
package net.bioclipse.cdk.ui.sdfeditor.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ui.dialogs.SaveAsDialog;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.SDFFormat;


/**
 * @author arvid
 *
 */
public class ExtractMolecules extends AbstractHandler implements IHandler {

    Logger logger = Logger.getLogger( ExtractMolecules.class );

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {

        IMoleculeTableManager molTable = Activator.getDefault()
                                        .getMoleculeTableManager();

        ISelection sel = HandlerUtil.getCurrentSelection( event );
        if(sel instanceof IStructuredSelection) {
            final IStructuredSelection selection = (IStructuredSelection) sel;
            if(selection.size()==1) {
                ICDKMolecule model = (ICDKMolecule)((IAdaptable)selection
                        .getFirstElement()).getAdapter( ICDKMolecule.class );
                if(model == null) return null;

                Collection<IPropertyCalculator<?>> calculators
                         = SDFIndexEditorModel.retriveCalculatorContributions();
                IAtomContainer ac = model.getAtomContainer();
                for(IPropertyCalculator<?> calculator:calculators) {
                    String key = calculator.getPropertyName();
                    ac.setProperty( key , model.getProperty( key,
                                                        Property.USE_CACHED ) );
                }

                List<IResourceFormat> formats =new ArrayList<IResourceFormat>();
                formats.add(CMLFormat.getInstance());
                formats.add(SDFFormat.getInstance());
                formats.add(MDLV2000Format.getInstance());

                IFile file= doSaveAs( HandlerUtil.getActiveShell( event ),
                                      formats );
                if(file == null) return null;
                IPath path = file.getLocation();
                try {
                    // do a nasty trick... the SaveAs dialog does not allow us to
                    // ask for a format (yet), so guess something from the file
                    // extension
                    ICDKManager cdk = net.bioclipse.cdk.business.Activator
                                        .getDefault().getJavaCDKManager();
                    IChemFormat format =
                                  cdk.guessFormatFromExtension(path.toString());
                    if (format == null)
                        format = (IChemFormat)CMLFormat.getInstance();

                    if (format instanceof MDLV2000Format) {
                        Properties properties = new Properties();
                        properties.setProperty( "ForceWriteAs2DCoordinates",
                                                "true");
                        cdk.saveMolecule(model, file, format, true, properties);
                    } else {
                        cdk.saveMolecule( model, file, format, true);
                    }
                } catch ( BioclipseException e ) {
                    logger.warn( "Failed to save molecule. " + e.getMessage() );
                } catch ( CDKException e ) {
                    logger.warn( "Failed to save molecule. " + e.getMessage() );
                } catch ( CoreException e ) {
                    logger.warn( "Failed to save molecule. " + e.getMessage() );
                }

            }else {
                final List<Object> selected = selection.toList();

                IMoleculesEditorModel model = new IMoleculesEditorModel() {


                    public ICDKMolecule getMoleculeAt( int index ) {

                        Object o = selected.get( index );
                        if(o instanceof IAdaptable) {
                            ICDKMolecule mol = (ICDKMolecule) ((IAdaptable)o)
                            .getAdapter( ICDKMolecule.class );
                            if(mol!=null)
                                return mol;
                        }
                        throw new RuntimeException("Selection is not a molecule "
                                                    + o);
                    }

                    public int getNumberOfMolecules() {

                        return selected.size();
                    }

                    public void markDirty( int index,
                                           ICDKMolecule moleculeToSave ) {

                        throw new UnsupportedOperationException();

                    }

                    public void save() {
                        throw new UnsupportedOperationException();
                    }

                };
                List<IResourceFormat> formats = new ArrayList<IResourceFormat>();
                formats.add( SDFFormat.getInstance() );
                IFile file = doSaveAs( HandlerUtil.getActiveShell( event ),
                                       formats);
                if(file != null)
                    try {
                        molTable.saveSDF( model, file );
                    } catch ( BioclipseException e ) {
                        LogUtils.handleException( e, logger ,
                                                  Activator.PLUGIN_ID);
                    }
            }
        }
        return null;
    }

    private IFile doSaveAs(Shell shell,List<IResourceFormat> formats) {


        SaveAsDialog saveAsDialog = new SaveAsDialog(
            shell.getShell(), formats
        );
//        if ( model.getResource() instanceof IFile )
//            saveAsDialog.setOriginalFile( (IFile) model.getResource() );
        int result = saveAsDialog.open();
        if ( result == 1 ) {
            logger.debug( "SaveAs canceled." );
            return null;
        }

        IPath path = saveAsDialog.getResult();
        IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile( path );

        return file;
    }
}
