/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *        Arvid Berg <goglepox@users.sf.net>
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableViewer.Row;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ui.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.IRowContentProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.random.RandomAccessSDFReader;

/**
 * @author arvid
 */
public class MoleculeTableContentProvider implements IRowContentProvider,
        ILazyContentProvider {

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    public static int READ_AHEAD = 100;

    private MoleculeTableViewer viewer;
    IMoleculesEditorModel   model       = null;

    IRenderer2DConfigurator renderer2DConfigurator;
    MoleculesEditorLabelProvider melp = new MoleculesEditorLabelProvider(
                                    MoleculeTableViewer.STRUCTURE_COLUMN_WIDTH);

    public IRenderer2DConfigurator getRenderer2DConfigurator() {

        return renderer2DConfigurator;
    }

    public void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {

        this.renderer2DConfigurator = renderer2DConfigurator;
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        ICDKMolecule molecule = null;
        IMoleculesEditorModel lModel;
        synchronized ( this ) {
            lModel = model;
        }
        if ( lModel != null ) {;
            Object o = lModel.getMoleculeAt( index );
            if ( o instanceof IAdaptable ) {
                molecule = ((ICDKMolecule) ((IAdaptable) o)
                        .getAdapter( ICDKMolecule.class ));
            }
        }

        return molecule;
    }

    public void inputChanged( final Viewer viewer, Object oldInput, Object newInput ) {

        if(newInput == null) {
            getCompositeTable( this.viewer ).removeRowContentProvider( this );
            return;
        }

        Assert.isTrue( viewer instanceof MoleculeTableViewer );
        Assert.isTrue( newInput instanceof IAdaptable );

        final IFile file = ( IFile )( ( IAdaptable )newInput )
                                    .getAdapter( IFile.class );
        if ( file != null ) {
            try {
                if ( file.getProject().equals( net.bioclipse.core.Activator
                                               .getVirtualProject() ) ) {
                    loadMoleculesFromManager( file , FileType.SDF);
                } else

                    if ( file.getContentDescription().getContentType().getId()
                            .equals( "net.bioclipse.contenttypes.sdf" ) ) {
                        (new SDFileMoleculesEditorModel( this )).init( file );
                        model = createSDFTemporaryModel( file );

                    } else {
                        if(file.getContentDescription().getContentType().getId()
                                .equals( "net.bioclipse.contenttypes.smi" ) ) {
                            loadMoleculesFromManager( file , FileType.SMI);
                        }
                    }

            } catch ( CoreException e ) {
                logger.warn( "Failed to load file: " + e.getMessage() );
                LogUtils.debugTrace( logger, e );
            } catch ( IOException e ) {
                logger.warn( "Failed to load file: " + e.getMessage() );
                LogUtils.debugTrace( logger, e );
            }
        } else {

            if(model == null) {
                model = (IMoleculesEditorModel)
                ((IAdaptable)newInput).getAdapter( IMoleculesEditorModel.class );
            }
        }
        if(viewer != this.viewer) {

            Viewer oldViewer = this.viewer;
            this.viewer = (MoleculeTableViewer)viewer;
            if(oldInput != null) {

                getCompositeTable( oldViewer ).removeRowContentProvider( this );
            }
            getCompositeTable( viewer ).addRowContentProvider( this );
        }

        updateSize( (model!=null?model.getNumberOfMolecules():0) );
    }

    private CompositeTable getCompositeTable(Viewer viewer) {

        return (CompositeTable)(viewer).getControl();
    }

    public void refresh( CompositeTable sender, int currentObjectOffset,
                         Control rowControl ) {

        Row row = (Row) rowControl;
        Control[] columns = row.getChildren();
        ((Label)columns[0]).setText( ""+(currentObjectOffset+1 ));
        try {
        ICDKMolecule molecule = getMoleculeAt( currentObjectOffset );

                    Image image;

                    image = melp.getColumnImage( molecule ,1);
        //             image = new Image(rowControl.getDisplay(), "icons/many_molecules.png");
                    ((Label)columns[1]).setImage( image );
        //            children[0].setSize( 100,100);



                } catch ( Exception e ) {
                    ((Label)columns[1]).setImage( null );
                    ((Label)columns[1]).setText( "no structure" );
                    logger.debug( "Failed to generate iamge" );
                 }
    }
    enum FileType {
        SDF,SMI
    }
    private void loadMoleculesFromManager(IFile file,FileType type)
                                            throws CoreException, IOException {
        BioclipseUIJob< List<ICDKMolecule>> uiJob =
            new BioclipseUIJob<List<ICDKMolecule>>() {
            @Override
            public void runInUI() {
                final List<ICDKMolecule> bioList = getReturnValue();

                model = new IMoleculesEditorModel() {
                    List<ICDKMolecule> molecules;
                    {
                        molecules = bioList;
                    }
                    public Object getMoleculeAt( int index ) {

                        return molecules.get( index );
                    }

                    public int getNumberOfMolecules() {

                        return molecules.size();
                    }

                    public void save() {
                        throw new UnsupportedOperationException();
                    }

                };

                CompositeTable cTable = getCompositeTable( viewer );
                int firstVisibleRow = cTable.getTopRow();
                cTable.setNumRowsInCollection(bioList.size());
                cTable.setTopRow( firstVisibleRow );

            }

            @Override
            public boolean runInBackground() {
                return true;
            }
        };

        switch(type) {
            case SDF:
                Activator.getDefault().getCDKManager()
                .loadMolecules( file,uiJob);
                break;
            case SMI:
                Activator.getDefault().getCDKManager()
                .loadSMILESFile( file,uiJob );

        }

    }
    private IMoleculesEditorModel createSDFTemporaryModel(final IFile file) {
        return new IMoleculesEditorModel() {
            int rowSize;
            {
                rowSize = numberOfEntries( READ_AHEAD );
            }

            public Object getMoleculeAt( int index ) {

                return readMoleculeWithIterator( index );
            }

            public int getNumberOfMolecules() {
                return rowSize;
            }

            public void save() {
                throw new UnsupportedOperationException();
            }

            private ICDKMolecule readMoleculeWithIterator( int index ) {

                Iterator<ICDKMolecule> iter;
                try {
                    iter = Activator.getDefault().getCDKManager()
                            .createMoleculeIterator( file );

                    ICDKMolecule molecule;
                    int count = 0;
                    while ( iter.hasNext() ) {
                        molecule = iter.next();
                        if ( count++ == index ) {
                            return molecule;
                        }
                    }
                } catch ( CoreException e ) {
                    return null;
                }
                return null;
            }

            private int numberOfEntries( int max ) {

                try {
                    int count = 0;
                    BufferedReader reader =
                       new BufferedReader(
                          new InputStreamReader( file.getContents() ) );

                    String line;
                    while ( count < max
                            && (line = reader.readLine()) != null ) {

                        if ( line.contains( "$$$$" ) ) {
                            count++;
                        }
                    }
                    reader.close();
                    return count;

                } catch ( CoreException e ) {

                } catch ( IOException e ) {

                }
                return 0;
            }
        };
    }
    private void setProperties( Label properties, IAtomContainer ac ) {

        if(ac == null) {
            properties.setText( "No properties found");
            return;
        }
        StringBuilder b = new StringBuilder();
        int count = 0;
        Map<Object, Object> proper = ac.getProperties();
        for ( Object o : proper.keySet() ) {
            // b = new StringBuilder();
            String key = o.toString();
            String value = proper.get( o ).toString();
            b.append( key ).append( ": " ).append( value ).append( ", \n" );
            // properties.add( b.toString() );
            // FIXME dirty hack to make it look good
            if ( count++ >= 5 )
                break;

        }
        properties.setText( b.toString() );
    }

    private void updateSize(int size) {
        getCompositeTable( viewer ).setNumRowsInCollection( size );
    }


    public void updateElement( int index ) {

    }

    static class SDFileMoleculesEditorModel implements IMoleculesEditorModel {
        Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );
        RandomAccessSDFReader reader;
        MoleculeTableContentProvider provider;

        public SDFileMoleculesEditorModel(
                                       MoleculeTableContentProvider provider) {
            this.provider = provider;

        }
        public void init(IFile file) {
            createIndex( file );
        }
        public Object getMoleculeAt( int index ) {


            IChemObject chemObject;
            try {
                chemObject = reader
                .readRecord( index );

                IMolecule ret = (IMolecule) chemObject;

                return (ret!=null?new CDKMolecule(ret):null);
            } catch ( Exception e ) {
               logger.debug( "Failed to read molecule for SDFile." );
               return null;
            }
        }

        public int getNumberOfMolecules() {

            return reader.size();
        }

        public void save() {

            // TODO Auto-generated method stub

        }

        private void createIndex(final IFile file) {
            Activator.getDefault().getCDKManager()
            .createSDFileIndex( file, new BioclipseUIJob<Integer>() {
                @Override
                public void runInUI() {

                    IChemObjectBuilder builder = DefaultChemObjectBuilder
                    .getInstance();

                    IPath location = file.getLocation();
                    try {
                    java.io.File jFile = (location!=null?location.toFile():null);
                    if(jFile == null) return;
                    reader = new RandomAccessSDFReader( jFile, builder );
                    provider.model = SDFileMoleculesEditorModel.this;
                    CompositeTable cTable = provider
                                        .getCompositeTable( provider.viewer );

                    int firstVisibleRow = cTable.getTopRow();
                    cTable.setNumRowsInCollection(
                                                  getNumberOfMolecules() );
                    cTable.setTopRow( firstVisibleRow );
                    } catch (IOException e ) {
                        LogUtils.debugTrace( logger, e );
                    }
                }

                @Override
                public boolean runInBackground() {
                    return true;
                }
            });
        }
    }
    public void dispose() {

    }
}
