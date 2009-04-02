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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ui.jobs.BioclipseUIJob;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.random.RandomAccessSDFReader;

/**
 * @author arvid
 */
public class MoleculeTableContentProvider implements
        ILazyContentProvider, IDataProvider {

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    public static int READ_AHEAD = 100;
    static final int NUMBER_OF_PROPERTIES =10;

    private MoleculeTableViewer viewer;
    IMoleculesEditorModel   model       = null;
    List<Object> properties = new ArrayList<Object>(NUMBER_OF_PROPERTIES);
    Collection<Object> availableProperties = new HashSet<Object>();

    MoleculesEditorLabelProvider melp = new MoleculesEditorLabelProvider(
                                    MoleculeTableViewer.STRUCTURE_COLUMN_WIDTH);

    public MoleculesEditorLabelProvider getLabelProvider() {
        return melp;
    }

    public List<Object> getProperties() {

        return new ArrayList<Object>(properties);
    }

    public Collection<Object> getAvailableProperties() {
        return new HashSet<Object>(availableProperties);
    }


    private void setModel(IMoleculesEditorModel model) {
        this.model = model;
        if(viewer !=null)
            viewer.refresh();
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        ICDKMolecule molecule = null;
        IMoleculesEditorModel lModel;
        synchronized ( this ) {
            lModel = model;
        }
        if ( lModel != null ) {
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
            return;
        }

        if(viewer != this.viewer) {
            this.viewer = (MoleculeTableViewer)viewer;
        }

//        Assert.isTrue( viewer instanceof MoleculeTableViewer );
        Assert.isTrue( newInput instanceof IAdaptable );

        final IFile file = ( IFile )( ( IAdaptable )newInput )
                                    .getAdapter( IFile.class );
        if ( file != null ) {
            try {
                readProperties( file );
                if ( file.getProject().equals( net.bioclipse.core.Activator
                                               .getVirtualProject() ) ) {
                    loadMoleculesFromManager( file , FileType.SDF);
                } else

                    if ( file.getContentDescription().getContentType().getId()
                            .equals( "net.bioclipse.contenttypes.sdf" ) ) {
                        (new SDFileMoleculesEditorModel( this )).init( file );
                        setModel( createSDFTemporaryModel( file ));

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
            setModel( (IMoleculesEditorModel)
            ((IAdaptable)newInput).getAdapter( IMoleculesEditorModel.class ));
            readProperties( model );
        }


        if(this.viewer != null)
            updateSize( (model!=null?model.getNumberOfMolecules():0) );

        // fill properties with elements from availableProperties
        properties.clear();
        Iterator<Object> iter = availableProperties.iterator();
        for(int i=0;i<NUMBER_OF_PROPERTIES;i++) {
            if(iter.hasNext())
                properties.add(iter.next());
        }
        updateHeaders();
    }

    private void readProperties(IFile file) throws CoreException{
        Iterator<ICDKMolecule> iter = Activator.getDefault()
                                .getCDKManager().createMoleculeIterator( file );
        if(iter.hasNext()) {
            ICDKMolecule moleucle = iter.next();
            availableProperties = moleucle.getAtomContainer().getProperties()
                        .keySet();
        }
    }

    private void readProperties(IMoleculesEditorModel model) {
        ICDKMolecule moleucle = (ICDKMolecule)model.getMoleculeAt( 0 );
        availableProperties = moleucle.getAtomContainer().getProperties()
                    .keySet();
    }

    void updateHeaders() {

        viewer.refresh();
    }

    private NatTable getCompositeTable(Viewer viewer) {

        return (NatTable)viewer.getControl();
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

                setModel( new IMoleculesEditorModel() {
                    List<ICDKMolecule> molecules;
                    {
                        molecules = bioList;
                    }
                    public ICDKMolecule getMoleculeAt( int index ) {

                        return molecules.get( index );
                    }

                    public int getNumberOfMolecules() {

                        return molecules.size();
                    }

                    public void save() {
                        throw new UnsupportedOperationException();
                    }

                });

                NatTable cTable = getCompositeTable( viewer );
                cTable.redraw();// TODO selection when size changes?
                cTable.reset();
                cTable.updateResize();

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

            public ICDKMolecule getMoleculeAt( int index ) {

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

    private void updateSize(int size) {
        getCompositeTable( viewer ).redraw();
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
        public ICDKMolecule getMoleculeAt( int index ) {


            IChemObject chemObject;
            try {
                chemObject = reader
                .readRecord( index );

                IMolecule ret = (IMolecule) chemObject;

                return (ret!=null?new CDKMolecule(ret):null);
            } catch ( Exception e ) {
               logger.warn("Failed to read molecule for SDFile.", e);
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
            try {
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
                            provider.setModel( SDFileMoleculesEditorModel.this);

                        } catch (IOException e ) {
                            LogUtils.debugTrace( logger, e );
                        }
                    }

                    @Override
                    public boolean runInBackground() {
                        return true;
                    }
                });

            }catch( OperationCanceledException e) {
                logger.error("Failed to create inded because, "+e.getMessage());
                LogUtils.debugTrace( logger, e );
            }
        }
    }
    public void dispose() {

    }

    public void setVisibleProperties( List<Object> visibleProperties ) {
        properties.clear();
        properties.addAll( visibleProperties );
        updateHeaders();
    }

    public int getColumnCount() {
        return properties.size()+1;
    }

    public int getRowCount() {
        IMoleculesEditorModel tModel = model;
        if(tModel != null)
            return tModel.getNumberOfMolecules();
        return 0;
    }

    public Object getValue( int row, int col ) {
        IMoleculesEditorModel tModel = model;
        ICDKMolecule molecule =  (ICDKMolecule) tModel.getMoleculeAt( row );
//        if(col == 0) {
//            return ""+(row+1);
//        }
        if(col == 0) {
//            Image image;
//            image = melp.getColumnImage( molecule ,1);
//            return image;
            return molecule;
        }
        int i = col;
        if( properties != null && i<properties.size()+1) {
            Object value = molecule.getAtomContainer()
            .getProperty( properties.get(i-1));
            return  value!=null?value.toString():"";
        } else
            return "";
    }
}
