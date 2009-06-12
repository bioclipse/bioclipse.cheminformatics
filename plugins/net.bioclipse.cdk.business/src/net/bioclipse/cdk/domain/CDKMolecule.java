/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *
 ******************************************************************************/

package net.bioclipse.cdk.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.preferences.PreferenceConstants;
import net.bioclipse.cdk.domain.CDKMoleculeUtils.MolProperty;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.IInChIManager;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.xmlcml.cml.element.CMLMolecule;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The CKMolecule wraps an IAtomContainer and is able to cache SMILES
 * @author ola
 *
 */
public class CDKMolecule extends BioObject implements ICDKMolecule {

    private String name;
    private IAtomContainer atomContainer;

    // cached properties
    private BitSet cachedFingerprint;
    private InChI cachedInchi;

    private Map<String,Object> cachedProperties;

    private static Preferences prefs;

    /*
     * Needed by Spring
     */
    CDKMolecule() {
        super();
        if (prefs == null && Activator.getDefault() != null) {
            prefs = Activator.getDefault().getPluginPreferences();
        }
    }
    
    public CDKMolecule(IAtomContainer atomContainer) {
        this();
        this.atomContainer=atomContainer;
    }
    
    /**
     * Used for creating a CDKMolecule if all values already are calculated
     * 
     * @param name
     * @param atomContainer
     * @param smiles
     * @param fingerprint
     */
    public CDKMolecule( String name, 
                        IAtomContainer atomContainer, 
                        BitSet fingerprint ) {
        this(atomContainer);
        this.name = name;
        this.cachedFingerprint = fingerprint;
    }


    public String toSMILES() throws BioclipseException {
        if (getAtomContainer() == null) return "";
        IAtomContainer container = getAtomContainer();

        //Operate on a clone with removed hydrogens
        org.openscience.cdk.interfaces.IMolecule hydrogenlessClone =
            container.getBuilder().newMolecule(
                AtomContainerManipulator.removeHydrogens(container)
            );

        return new SmilesGenerator().createSMILES(hydrogenlessClone);
    }

    public IAtomContainer getAtomContainer() {
        return atomContainer;
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private final static Properties cmlPrefs = new Properties();
    static {
        cmlPrefs.put("XMLDeclaration", "false");
    }

    public String toCML() throws BioclipseException {

        if (getAtomContainer()==null) throw new BioclipseException("No molecule to " +
        "get CML from!");

        if (prefs != null && prefs.getBoolean(PreferenceConstants.P_BOOLEAN)) {
            ByteArrayOutputStream bo=new ByteArrayOutputStream();

            CMLWriter writer=new CMLWriter(bo);
            writer.addChemObjectIOListener(new PropertiesListener(cmlPrefs));
            try {
                writer.write(getAtomContainer());
                writer.close();
            } catch (CDKException e) {
                throw new BioclipseException("Could not convert molecule to CML: "
                        + e.getMessage());
            } catch (IOException e) {
                throw new BioclipseException("Could not write molecule to CML: "
                        + e.getMessage());
            }
            return bo.toString();
        }

        Convertor convertor = new Convertor(true, null);
        CMLMolecule cmlMol = convertor.cdkAtomContainerToCMLMolecule(getAtomContainer());
        return cmlMol.toXML();
    }

    /**
     * Calculate CDK fingerprint and cache the result.
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public BitSet getFingerprint(IMolecule.Property urgency) throws BioclipseException {
        if (urgency == IMolecule.Property.USE_CACHED) return cachedFingerprint;
        
        if (urgency != IMolecule.Property.USE_CALCULATED) {
            if (cachedFingerprint != null) {
                return cachedFingerprint;
            }
        }
        Fingerprinter fp=new Fingerprinter();
        try {
            BitSet fingerprint=fp.getFingerprint(getAtomContainer());
            cachedFingerprint=fingerprint;
            return fingerprint;
        } catch (Exception e) {
            throw new BioclipseException("Could not create fingerprint: "
                    + e.getMessage());
        }

    }

    public boolean has3dCoords() throws BioclipseException {
        if (getAtomContainer()==null) throw new BioclipseException("Atomcontainer is null!");
        return GeometryTools.has3DCoordinates(getAtomContainer());
    }
    
    @Override
    public Object getAdapter( Class adapter ) {
    
        if (adapter == IMolecule.class){
            return this;
        }
        
        if (adapter.isAssignableFrom(IAtomContainer.class)) {
            return this.getAtomContainer();
        }
        
        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new CDKMoleculePropertySource(this);
        }
        
        // TODO Auto-generated method stub
        return super.getAdapter( adapter );
    }

    public List<IMolecule> getConformers() {

        throw new NotImplementedException();
        
    }

    public String toString() {
        if ( getName() != null )
            return getClass().getSimpleName() + ":" + getName();
        if (this.getAtomContainer().getAtomCount() == 0) {
            return getClass().getSimpleName() + ": no atoms";
        }
        if (Activator.getDefault() == null)
            return getClass().getSimpleName() + ":" + hashCode();

        return getClass().getSimpleName() + ":" 
               + Activator.getDefault().getJavaCDKManager().molecularFormula(this);
    }

    public String getInChI(IMolecule.Property urgency) throws BioclipseException {
        if (urgency == IMolecule.Property.USE_CACHED) {
            return cachedInchi == null ? "" : cachedInchi.getValue();
        }
        
        if (urgency != IMolecule.Property.USE_CALCULATED) {
            if (cachedInchi != null) {
                return cachedInchi.getValue();
            }
        }
        IInChIManager inchi = net.bioclipse.inchi.business.Activator.
            getDefault().getJavaInChIManager();
        try {
            cachedInchi = inchi.generate(this);
            return cachedInchi.getValue();
        } catch (Exception e) {
            throw new BioclipseException("Could not create InChI: "
                    + e.getMessage(), e);
        }
    }

    public String getInChIKey(IMolecule.Property urgency) throws BioclipseException {
        if (urgency == IMolecule.Property.USE_CACHED)
            return cachedInchi == null ? "" : cachedInchi.getKey();
        
        if (urgency != IMolecule.Property.USE_CALCULATED) {
            if (cachedInchi != null) {
                return cachedInchi.getKey();
            }
        }
        IInChIManager inchi = net.bioclipse.inchi.business.Activator.
            getDefault().getJavaInChIManager();
        try {
            cachedInchi = inchi.generate(this);
            return cachedInchi.getKey();
        } catch (Exception e) {
            throw new BioclipseException("Could not create InChIKey: "
                    + e.getMessage(), e);
        }
    }

    public Object getProperty(String propertyKey, Property urgency) {
        switch (urgency) {
            case USE_CALCULATED:
                // TODO get calculator for property
            case USE_CACHED_OR_CALCULATED:
                // TODO if cached use it otherwise calculate
            case USE_CACHED:
                if(cachedProperties !=null)
                    return cachedProperties.get( propertyKey );
        }
        return null;
    }

    void setProperty(String propertyKey, Object value) {
        if(MolProperty.InChI.name().equals( propertyKey ))
            cachedInchi = (InChI) value;
        else if(MolProperty.Fingerprint.name().equals( propertyKey ))
            cachedFingerprint = (BitSet) value;
        else {
            if(cachedProperties == null)
                cachedProperties = new HashMap<String, Object>();
            cachedProperties.put( propertyKey, value );
        }
    }

    void clearProperty( String key ) {
        if(MolProperty.InChI.name().equals( key ))
            cachedInchi = null;
        else if(MolProperty.Fingerprint.name().equals( key ))
            cachedFingerprint = null;
        else {
            if(cachedProperties!= null)
                cachedProperties.remove( key );
        }
    }
}
