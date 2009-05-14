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
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.props.BioObjectPropertySource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IChemObject;

public class CDKMoleculePropertySource extends BioObjectPropertySource {

    protected static final String PROPERTY_HAS2D = "Has 2D Coords";
    protected static final String PROPERTY_HAS3D = "Has 3D Coords";
    protected static final String PROPERTY_FORMAT = "Molecular Format";
    protected static final String PROPERTY_FORMULA = "Molecular Formula";
    protected static final String PROPERTY_MASS = "Molecular Mass";

    private final Object cdkPropertiesTable[][] =
    {
        { PROPERTY_HAS2D,
            new TextPropertyDescriptor(PROPERTY_HAS2D,PROPERTY_HAS2D)},
        { PROPERTY_HAS3D,
            new TextPropertyDescriptor(PROPERTY_HAS3D,PROPERTY_HAS3D)},
        { PROPERTY_FORMAT,
            new TextPropertyDescriptor(PROPERTY_FORMAT,PROPERTY_FORMAT)},
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
        
        cdkProperties = setupProperties(item.getAtomContainer());
        cdkValueMap = getPropertyValues(item);
    }

    /**
     * @param item
     */
    private HashMap<String, Object> getPropertyValues(CDKMolecule item) {
        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(
            PROPERTY_HAS2D,
            GeometryTools.has2DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        valueMap.put(
            PROPERTY_HAS3D,
            GeometryTools.has3DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        valueMap.put(
            PROPERTY_FORMULA, cdk.molecularFormula(item)
        );
        try {
            valueMap.put(
                PROPERTY_MASS, cdk.calculateMass(item)
            );
        } catch (BioclipseException e) {
            valueMap.put(PROPERTY_MASS, "N/A");
        }
        IResource resource = item.getResource();
        if (resource instanceof IFile) {
            IFile fileRes = (IFile)resource;
            try {
                valueMap.put(
                    PROPERTY_FORMAT, fileRes.getContentDescription().getContentType().getName()
                );
            } catch (CoreException e) {
                valueMap.put(PROPERTY_FORMAT, "error");
            }
        } else {
            valueMap.put(PROPERTY_FORMAT, "N/A");
        }
        // IChemObject.getProperties()
        Map<Object,Object> objectProps = item.getAtomContainer().getProperties();
        for (Object propKey : objectProps.keySet()) {
            String label = ""+propKey;
            valueMap.put(label, ""+objectProps.get(propKey));
        }
        return valueMap;
    }

    private ArrayList<IPropertyDescriptor> setupProperties(IChemObject object) {
        ArrayList<IPropertyDescriptor> cdkProperties =
            new ArrayList<IPropertyDescriptor>();
        // default properties
        for (int i=0;i<cdkPropertiesTable.length;i++) {
            PropertyDescriptor descriptor;
            descriptor = (PropertyDescriptor)cdkPropertiesTable[i][1];
            descriptor.setCategory("General");
            cdkProperties.add(descriptor);
        }
        // IChemObject.getProperties()
        Map<Object,Object> objectProps = object.getProperties();
        for (Object propKey : objectProps.keySet()) {
            PropertyDescriptor descriptor;
            String label = ""+propKey;
            descriptor = new TextPropertyDescriptor(label,label);
            descriptor.setCategory("Molecular Properties");
            cdkProperties.add(descriptor);
        }
        return cdkProperties;
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
