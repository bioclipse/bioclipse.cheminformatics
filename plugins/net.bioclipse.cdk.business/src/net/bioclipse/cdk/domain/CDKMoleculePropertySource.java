/*******************************************************************************
 * Copyright (c) 2009 Egon Willighagen <egonw@users.sf.net>
 *               2009 Arvid Berg <goglepox@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import java.util.ArrayList;
import java.util.HashMap;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.props.BioObjectPropertySource;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.openscience.cdk.geometry.GeometryTools;

public class CDKMoleculePropertySource extends BioObjectPropertySource {

    protected static final String PROPERTY_SMILES = "SMILES";
    protected static final String PROPERTY_HAS2D = "Has 2D Coords";
    protected static final String PROPERTY_HAS3D = "Has 3D Coords";
    protected static final String PROPERTY_FORMULA = "Molecular Formula";
    protected static final String PROPERTY_MASS = "Molecular Mass";

    private final Object cdkPropertiesTable[][] =
    {
        { PROPERTY_SMILES,
            new TextPropertyDescriptor(PROPERTY_SMILES,PROPERTY_SMILES)},
        { PROPERTY_HAS2D,
            new TextPropertyDescriptor(PROPERTY_HAS2D,PROPERTY_HAS2D)},
        { PROPERTY_HAS3D,
            new TextPropertyDescriptor(PROPERTY_HAS3D,PROPERTY_HAS3D)},
        { PROPERTY_FORMULA,
            new TextPropertyDescriptor(PROPERTY_FORMULA,PROPERTY_FORMULA)},
        { PROPERTY_MASS,
            new TextPropertyDescriptor(PROPERTY_MASS,PROPERTY_MASS)}
    };

    private CDKMolecule cdkMol;
    private ArrayList<IPropertyDescriptor> cdkProperties;
    private HashMap<String, Object> cdkValueMap;

    public CDKMoleculePropertySource(CDKMolecule item) {
        super(item);
        cdkMol = item;
        
        cdkProperties = new ArrayList<IPropertyDescriptor>();
        cdkValueMap = new HashMap<String, Object>();

        for (int i=0;i<cdkPropertiesTable.length;i++) {
            PropertyDescriptor descriptor;
            descriptor = (PropertyDescriptor)cdkPropertiesTable[i][1];
            descriptor.setCategory("General");
            cdkProperties.add(descriptor);
        }

        String smiles = null;
        try {
            smiles = cdkMol.getSMILES();
        } catch (BioclipseException e) {
            // FIXME: use a logger
            e.printStackTrace();
        }
        cdkValueMap.put(
            PROPERTY_SMILES,
            smiles == null ? "N/A" : smiles
        );

        cdkValueMap.put(
            PROPERTY_HAS2D,
            GeometryTools.has2DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        cdkValueMap.put(
            PROPERTY_HAS3D,
            GeometryTools.has3DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        ICDKManager cdk = Activator.getDefault().getCDKManager();
        cdkValueMap.put(
            PROPERTY_FORMULA, cdk.molecularFormula(item)
        );
        try {
            cdkValueMap.put(
                PROPERTY_MASS, cdk.calculateMass(item)
            );
        } catch (BioclipseException e) {
            cdkValueMap.put(PROPERTY_MASS, "N/A");
        }
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        // Create the property vector.

        IPropertyDescriptor[] propertyDescriptors =
            new IPropertyDescriptor[cdkProperties.size()];
        for (int i=0; i< cdkProperties.size();i++){
            propertyDescriptors[i]=(IPropertyDescriptor) cdkProperties.get(i);
        }

        // Return it.
        return propertyDescriptors;
    }

    public Object getPropertyValue(Object id) {
        if (cdkValueMap.containsKey(id))
            return cdkValueMap.get(id);

        return super.getPropertyValue(id);
    }

    public ArrayList<IPropertyDescriptor> getProperties() {
        return cdkProperties;
    }

    public void setProperties(ArrayList<IPropertyDescriptor> properties) {
        this.cdkProperties = properties;
    }

    public HashMap<String, Object> getValueMap() {
        return cdkValueMap;
    }

    public void setValueMap(HashMap<String, Object> valueMap) {
        this.cdkValueMap = valueMap;
    }

}
