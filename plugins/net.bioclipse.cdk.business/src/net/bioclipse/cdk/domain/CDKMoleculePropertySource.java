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

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.props.BioObjectPropertySource;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class CDKMoleculePropertySource extends BioObjectPropertySource {

    protected static final String PROPERTY_SMILES = "SMILES";

    private final Object cdkPropertiesTable[][] =
    {
        { PROPERTY_SMILES,
            new TextPropertyDescriptor(PROPERTY_SMILES,PROPERTY_SMILES)}
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
