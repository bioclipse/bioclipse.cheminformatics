/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sourceforge.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeUtils;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.CDKHydrogenAdder;


/**
 * @author arvid
 *
 */
public class SDFIndexEditorModel implements IFileMoleculesEditorModel,
                                            Iterable<ICDKMolecule> {

    Logger logger = Logger.getLogger( SDFIndexEditorModel.class );

    SDFileIndex input = SDFileIndex.emptyIndex();

    protected ISimpleChemObjectReader chemReader;
    protected IChemObjectBuilder builder;

    Map<Integer,ICDKMolecule> cache 
        = new HashMap<Integer, ICDKMolecule>();
    List<Integer> cacheQueue = new LinkedList<Integer>();

//    List<Object> visibleProperties= new ArrayList<Object>(10);
    private Set<Object> availableProperties;

    Map<Integer, ICDKMolecule> edited = new HashMap<Integer, ICDKMolecule>();

    private Map<Integer, Map<String,Object>> molProps;

    private Map<String,Class<?>> propertyList;

    private Map<String,IPropertyCalculator<?>> calculators;

    private boolean dirty = false;

    private SDFIndexEditorModel() {
        molProps = new HashMap<Integer, Map<String,Object>>();
        propertyList = new HashMap<String, Class<?>>();
        chemReader = new MDLV2000Reader();
        builder = DefaultChemObjectBuilder.getInstance();
        calculators = new TreeMap<String, IPropertyCalculator<?>>();
        Collection<IPropertyCalculator<?>> calcs = retriveCalculatorContributions();
        for(IPropertyCalculator<?> p:calcs) {
            calculators.put( p.getPropertyName(), p );
        }
    }

    public SDFIndexEditorModel(SDFileIndex input) {
        this();
        this.input = input;
        if(getNumberOfMolecules()>0) getMoleculeAt( 0 );
    }

    public IFile getResource() {
        return input.file();
    }

    public boolean isDirty() {
        return dirty || edited.size()!=0;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#getMoleculeAt(int)
     */
    public synchronized ICDKMolecule getMoleculeAt( int index ) {

        ICDKMolecule mol = edited.get( index );
        if(mol == null) {
            if ( cache.containsKey( index ) )  {
                mol = cache.get( index );
            }
            else {
                try {
                    StringReader reader = new StringReader(input.getRecord( index ));
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

            sanatizeMDLV2000MolFileInput(mol);

            for(IPropertyCalculator<?> calculator:calculators.values()) {
                Object propertyValue = getPropertyFor( index,
                                                 calculator.getPropertyName() );
                if(propertyValue != null)
                    CDKMoleculeUtils.setProperty( mol,
                                                  calculator.getPropertyName(),
                                                  propertyValue);
            }
        }
        return mol;
    }

    /* copy form CDKManager to fix bug 2052 */
    // TODO bug 2053 Remove this code
    private void sanatizeMDLV2000MolFileInput(ICDKMolecule molecule) {
        IAtomContainer container = molecule.getAtomContainer();
        if (container != null && container.getAtomCount() > 0) {
            CDKHydrogenAdder hAdder =
                CDKHydrogenAdder.getInstance(container.getBuilder());
            CDKAtomTypeMatcher matcher =
                CDKAtomTypeMatcher.getInstance(container.getBuilder());
            try {
                // perceive atom types
                IAtomType[] types = matcher.findMatchingAtomType(container);
                for (int i=0; i<container.getAtomCount(); i++) {
                    if (types[i] != null) {
                        IAtom atom = container.getAtom(i);
                        // set properties needed for H adding and aromaticity
                        atom.setAtomTypeName(types[i].getAtomTypeName());
                        atom.setHybridization(types[i].getHybridization());
                        hAdder.addImplicitHydrogens(container, atom);
                    }
                }
                // perceive aromaticity
                CDKHueckelAromaticityDetector.detectAromaticity(container);
            } catch ( CDKException e ) {
                e.printStackTrace();
            }
        }
    }
    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#getNumberOfMolecules()
     */
    public int getNumberOfMolecules() {

        return input.size();
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#save(int, net.bioclipse.cdk.domain.ICDKMolecule)
     */
    public void markDirty( int index, ICDKMolecule moleculeToSave ) {
        assert(moleculeToSave!=null);
        moleculeToSave.getAtomContainer().setFlag( 7, true );
        edited.put( index, moleculeToSave );
        Collection<IPropertyCalculator<?>> propCalcs = retriveCalculatorContributions();

        for(IPropertyCalculator<?> calc : propCalcs) {
            String key = calc.getPropertyName();
            Object o = moleculeToSave.getProperty( key, Property.USE_CACHED );
            if(o!=null)
                setPropertyFor( index, key, o );
        }
    }

    public void save() {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();

    }

    protected IChemObject processContent() throws CDKException {

        IChemObject co = chemReader.read(builder.newInstance(IChemFile.class));
        if (co instanceof IChemFile) {
            for(IChemSequence chemSeq:((IChemFile) co).chemSequences()) {
                for(IChemModel chemModel:chemSeq.chemModels()) {
                  for(IAtomContainer ac:chemModel.getMoleculeSet().molecules()) {
                      co = ac;
                      break;
                    }
                  break;
                }
                break;
            }
        }
        return co;
    }

    private void setLastRead( int index, ICDKMolecule mol ) {
        if ( mol != null ) {
            if ( cache.size() + 1 > 10 ) {
                Integer i = cacheQueue.remove( cacheQueue.size() - 1 );
                cache.remove( i );
            }
            cacheQueue.add( index );
            cache.put( index, mol );
        }
    }

    private void readProperties(ICDKMolecule molecule) {
        if(availableProperties==null) {
            availableProperties = new HashSet<Object>();
        }
            availableProperties.addAll(
                        molecule.getAtomContainer()
                        .getProperties().keySet());
    }

    public Collection<Object> getAvailableProperties() {
        if(availableProperties==null) return Collections.emptySet();
        availableProperties.addAll( propertyList.keySet() );
        return new HashSet<Object>(availableProperties);
    }

    public void addPropertyKey( String name ) {
        if(availableProperties == null)
            availableProperties = new HashSet<Object>();
        availableProperties.add( name );
    }

    public void removePropertyKey(Object key) {
        if(availableProperties == null) return;
        availableProperties.remove( key );
        propertyList.remove( key );
        setDirty( true );
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyFor(int moleculeIndex,String property) {
        Map<String,Object> props = molProps.get(moleculeIndex);
        Class<?> c = propertyList.get( property );
        if(props!=null && c != null) {
            Object val = props.get( property );

            return (T)val;
        }
        return null;
    }

    public <T extends Object>void setPropertyFor( int moleculeIndex,
                                                  String property,
                                                  T value) {
        Map<String,Object> props = molProps.get( moleculeIndex );
        if(value == null) {
            if(props!=null) 
                setDirty( props.remove( property )!=null );
            return;
        }
        if(value!=null)
            propertyList.put( property, value.getClass() );
        if(props==null)
            molProps.put( moleculeIndex, props = new HashMap<String, Object>() );
        props.put( property, value );
        setDirty( true );
    }

    public Iterator<ICDKMolecule> iterator() {

        return new Iterator<ICDKMolecule>() {

            int pos = 0;
            public boolean hasNext() {
                return pos < getNumberOfMolecules();
            }

            public ICDKMolecule next() {

                return getMoleculeAt( ++pos );
            }

            public void remove() {
                throw new UnsupportedOperationException(
                                        "remove is not supported for SDFIndex");
            }

        };
    }

    long getPropertyPositionFor(int index) {
        return input.getPropertyStart( index );
    }

    int getPropertyCountFor(int index) {
        return input.getPropertyCount( index );
    }

    public IPropertyCalculator<?> getCalculator(String property) {
        IPropertyCalculator<?> calculator = calculators.get( property );
        return calculator;
    }

    public static Collection<IPropertyCalculator<?>> retriveCalculatorContributions() {
        List<IPropertyCalculator<?>> calculators
                        = new ArrayList<IPropertyCalculator<?>>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint generatorExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.cdk.propertyCalculator");
        if(generatorExtensionPoint != null ) {
            IExtension[] generatorExtensions
            = generatorExtensionPoint.getExtensions();

            for(IExtension extension : generatorExtensions) {

                for( IConfigurationElement element
                        : extension.getConfigurationElements() ) {
                    try {
                        final IPropertyCalculator<?> generator =
                            (IPropertyCalculator<?>)
                            element.createExecutableExtension("class");
                        calculators.add( generator);
                    } catch (CoreException e) {
                        LogUtils.debugTrace(
                              Logger.getLogger( SDFIndexEditorModel.class ) ,e);
                    }
                }
            }
        }
        return calculators;
    }

    public void insert( int index, ICDKMolecule... molecules ) {
        throw new UnsupportedOperationException();
    }

    public void move( int indexFrom, int indexTo ) {
        throw new UnsupportedOperationException();
    }

    public void instert( ICDKMolecule... molecules ) {
        throw new UnsupportedOperationException();
    }

    public void delete( int index ) {
        throw new UnsupportedOperationException();
    }
}