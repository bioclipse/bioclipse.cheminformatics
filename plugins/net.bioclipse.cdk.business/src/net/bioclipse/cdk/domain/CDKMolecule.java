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
import java.util.List;
import java.util.Properties;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.preferences.PreferenceConstants;
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
    private String cachedSMILES;
    private BitSet cachedFingerprint;
    private InChI cachedInchi;

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
                        String smiles, 
                        BitSet fingerprint ) {
        this(atomContainer);
        this.name = name;
        this.cachedFingerprint = fingerprint;
        this.cachedSMILES = smiles;
    }


    public String getSMILES(IMolecule.Property urgency) throws BioclipseException {

        //TODO: wrap in job?
        if (urgency == IMolecule.Property.USE_CACHED) return cachedSMILES;

        if (cachedSMILES != null &&
            urgency == IMolecule.Property.USE_CACHED_OR_CALCULATED) {
            return cachedSMILES;
        }

        if (getAtomContainer() == null)
            throw new BioclipseException("Unable to calculate SMILES: Molecule is empty");

        if (!(getAtomContainer() instanceof org.openscience.cdk.interfaces.IMolecule))
            throw new BioclipseException("Unable to calculate SMILES: Not a molecule.");

        if (getAtomContainer().getAtomCount() > 100)
            throw new BioclipseException("Unable to calculate SMILES: Molecule has more than 100 atoms.");

        if (getAtomContainer().getBondCount() == 0)
            throw new BioclipseException("Unable to calculate SMILES: Molecule has no bonds.");

        org.openscience.cdk.interfaces.IMolecule molecule=(org.openscience.cdk.interfaces.IMolecule)getAtomContainer();

        // Create the SMILES
        SmilesGenerator generator = new SmilesGenerator();
        
        //Operate on a clone with removed hydrogens
        org.openscience.cdk.interfaces.IMolecule newMol;
        newMol=(org.openscience.cdk.interfaces.IMolecule)
                           AtomContainerManipulator.removeHydrogens( molecule );
        cachedSMILES = generator.createSMILES(newMol);

        return cachedSMILES;
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

    public String getCML() throws BioclipseException {

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
        if (Activator.getDefault() == null)
            return getClass().getSimpleName() + ":" + hashCode();

        return getClass().getSimpleName() + ":" 
               + Activator.getDefault().getCDKManager().molecularFormula(this);
    }

    public String getInChI(IMolecule.Property urgency) throws BioclipseException {
        if (urgency == IMolecule.Property.USE_CACHED)
            return cachedInchi.getValue();
        
        if (urgency != IMolecule.Property.USE_CALCULATED) {
            if (cachedInchi != null) {
                return cachedInchi.getValue();
            }
        }
        IInChIManager inchi = net.bioclipse.inchi.business.Activator.
            getDefault().getInChIManager();
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
            return cachedInchi.getKey();
        
        if (urgency != IMolecule.Property.USE_CALCULATED) {
            if (cachedInchi != null) {
                return cachedInchi.getKey();
            }
        }
        IInChIManager inchi = net.bioclipse.inchi.business.Activator.
            getDefault().getInChIManager();
        try {
            cachedInchi = inchi.generate(this);
            return cachedInchi.getKey();
        } catch (Exception e) {
            throw new BioclipseException("Could not create InChIKey: "
                    + e.getMessage(), e);
        }
    }
}
