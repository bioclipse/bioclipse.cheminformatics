/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bioclipse.cdk.business.CDKManager.SDFileIndex;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;
import net.sourceforge.nattable.data.IDataProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;


/**
 * @author arvid
 *
 */
public class MoleculeViewerContentProvider implements IDataProvider,
        IContentProvider, IMoleculesEditorModel{

    private static final int NUMBER_OF_PROPERTIES = 10;

    Logger logger = Logger.getLogger( MoleculeViewerContentProvider.class );

    MoleculeTableViewer viewer;
    SDFileIndex input = new SDFileIndex(null,new ArrayList<Long>()) {
        @Override
        public int size() {
            return 0;
        }

    };

    protected ISimpleChemObjectReader chemReader;
    protected IChemObjectBuilder builder;

    int lastIndex = -1;
    ICDKMolecule lastRead = null;

    List<Object> visibleProperties= new ArrayList<Object>(10);
    Set<Object> availableProperties= new HashSet<Object>();

    Map<Integer, ICDKMolecule> edited = new HashMap<Integer, ICDKMolecule>();

    public MoleculeViewerContentProvider() {
        chemReader = new MDLV2000Reader();
        builder = DefaultChemObjectBuilder.getInstance();
    }

    public int getColumnCount() {

        return visibleProperties.size()+1;
    }

    public int getRowCount() {

        return getNumberOfMolecules();
    }
    public Object getValue( int index, int column ) {

        ICDKMolecule molecule = (ICDKMolecule)getMoleculeAt( index );
        if(column == 0) {
            return molecule;
        }else {
            return molecule.getAtomContainer()
                    .getProperty( visibleProperties.get( column-1 ) );
        }
    }

    public void dispose() {


    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {

        Assert.isTrue( viewer instanceof MoleculeTableViewer );

        this.viewer = (MoleculeTableViewer) viewer;

        input = (SDFileIndex) newInput;
        setLastRead(-1 , null );

        if(input.size()>0) {
            availableProperties.clear();
            availableProperties.addAll( getMoleculeAt( 0 ).getAtomContainer()
                                        .getProperties().keySet() );
        }
        fillVisibleProperties();
    }

    private void fillVisibleProperties() {
     // fill properties with elements from availableProperties
        visibleProperties.clear();
        Iterator<Object> iter = availableProperties.iterator();
        for(int i=0;i<NUMBER_OF_PROPERTIES;i++) {
            if(iter.hasNext())
                visibleProperties.add(iter.next());
        }
    }

    private String getRecord(int index) throws CoreException, IOException {
        InputStream in = input.file().getContents();
        long start = input.start( index );
        int length = (int) (input.start( index +1 )-start);
        in.skip( start );
        byte[] bytes= new byte[length];
        in.read( bytes , 0  , length );
        in.close();
        String result = new String( bytes );
        return result.substring( 0,result.indexOf( "$$$$" ));
    }

    private void setLastRead(int index,ICDKMolecule mol) {
        if(mol==null) {
            lastIndex = -1;
        }else {
            lastIndex = index;
        }
        lastRead = mol;
    }

    protected IChemObject processContent() throws CDKException {

          IChemObject co = chemReader.read(builder.newChemFile());
          if (co instanceof IChemFile) {
              for(IChemSequence chemSeq:((IChemFile) co).chemSequences()) {
                  for(IChemModel chemModel:chemSeq.chemModels()) {
                    for(IAtomContainer ac:chemModel.getMoleculeSet().molecules()) {
                        co = (IMolecule) ac;
                        break;
                      }
                    break;
                  }
                  break;
              }
          }
          return co;
      }

    public ICDKMolecule getMoleculeAt( int index ) {
        ICDKMolecule mol = edited.get( index );
        if(mol == null) {
            if(index == lastIndex) {
                mol = lastRead;
            }
            else {
                try {
                    StringReader reader = new StringReader(getRecord( index ));
                    chemReader.setReader( reader );
                    IChemObject chemObj = processContent();
                    mol = new CDKMolecule((IAtomContainer)chemObj);
                    setLastRead( index, mol );
                    readProperties( mol );
                } catch ( CoreException e ) {
                    logger.warn( "Failed to read record "+index);
                    LogUtils.debugTrace( logger, e );
                    setLastRead( index, null );
                    return null;
                } catch ( IOException e ) {
                    logger.warn( "Failed to read record "+index);
                    LogUtils.debugTrace( logger, e );
                    setLastRead( index, null );
                    return null;
                } catch ( CDKException e ) {
                    logger.warn( "Failed to read record "+index);
                    LogUtils.debugTrace( logger, e );
                    setLastRead( index, null );
                    return null;
                }
            }
        }
        return mol;
    }

    public int getNumberOfMolecules() {

        return input.size();
    }

    public void save(int index,ICDKMolecule molecule) {
        edited.put( index, molecule);
    }

    public void save() {

        // TODO Auto-generated method stub

    }

    private void readProperties(ICDKMolecule molecule) {

        availableProperties.addAll(
                        molecule.getAtomContainer()
                        .getProperties().keySet());
    }

    public List<Object> getProperties() {

        return new ArrayList<Object>(visibleProperties);
    }

    public void setVisibleProperties( List<Object> visibleProperties ) {
        this.visibleProperties.clear();
        this.visibleProperties.addAll( visibleProperties );
        if(viewer != null)
            viewer.refresh();
    }

    public Collection<Object> getAvailableProperties() {
        return new HashSet<Object>(availableProperties);
    }

    void updateHeaders() {

        viewer.refresh();
    }
}
