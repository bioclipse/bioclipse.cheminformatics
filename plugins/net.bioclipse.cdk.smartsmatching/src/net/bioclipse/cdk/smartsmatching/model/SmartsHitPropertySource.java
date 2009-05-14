/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching.model;

import java.util.ArrayList;

import net.bioclipse.cdk.smartsmatching.views.SmartsMatchingView;
import net.bioclipse.core.domain.props.BioObjectPropertySource;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.openscience.cdk.interfaces.IAtom;


public class SmartsHitPropertySource extends BioObjectPropertySource {

    protected static final String PROPERTY_NAME = "Name";
    protected static final String PROPERTY_SMARTS = "SMARTS";
    protected static final String PROPERTY_ATOMS = "Atoms";

    private final Object PropertiesTable[][] =
    {
        { PROPERTY_NAME,
            new TextPropertyDescriptor(PROPERTY_NAME,PROPERTY_NAME)},
        { PROPERTY_SMARTS,
            new TextPropertyDescriptor(PROPERTY_SMARTS,PROPERTY_SMARTS)},
        { PROPERTY_ATOMS,
            new TextPropertyDescriptor(PROPERTY_ATOMS,PROPERTY_ATOMS)}
    };

    private SmartsHit hit;
    private ArrayList<IPropertyDescriptor> properties;
    
    public SmartsHitPropertySource(SmartsHit hit) {
        super( hit );
        this.hit=hit;
        
        properties=new ArrayList<IPropertyDescriptor>();
//        valueMap=new HashMap<String, Object>();

        //Build the arraylist of propertydescriptors
        for (int i=0;i<PropertiesTable.length;i++) {
            // Add each property supported.
            PropertyDescriptor descriptor;
            descriptor = (PropertyDescriptor)PropertiesTable[i][1];
            descriptor.setCategory("Match");
            properties.add(descriptor);
        }

    }


    public IPropertyDescriptor[] getPropertyDescriptors() {
        // Create the property vector.

        IPropertyDescriptor[] propertyDescriptors =
            new IPropertyDescriptor[properties.size()];
        for (int i=0; i< properties.size();i++){
            propertyDescriptors[i]=(IPropertyDescriptor) properties.get(i);
        }

        // Return it.
        return propertyDescriptors;
    }

    public Object getPropertyValue(Object id) {
        
        if (PROPERTY_NAME.equals( id )){
            return hit.getName();
        }
        else if (PROPERTY_SMARTS.equals( id )){
            return hit.getParent().getSmartsString();
        }
        else if (PROPERTY_ATOMS.equals( id )){
            String ret="";
            if (hit.getAtomContainer()!=null){
                for (IAtom atom : hit.getAtomContainer().atoms()){
                    ret=ret+atom.getSymbol() + hit.getHitMolecule().getAtomContainer().getAtomNumber( atom )+", ";
                }
                if (ret.length()>2)
                    ret=ret.substring( 0,ret.length()-2 );
            }

            return ret;
        }
        
        return super.getPropertyValue(id);
    }

    public ArrayList<IPropertyDescriptor> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<IPropertyDescriptor> properties) {
        this.properties = properties;
    }


}
