/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Properties for the JmolChain view object
 * @author ola
 *
 */
public class MonomerPropertySource implements IPropertySource {

    private static final String PROPERTY_NAME = "jmol.model.name";     
    private static final String PROPERTY_PROTEIN_STRUCTURE = "jmol.model.proteinstructure";    
    private static final String PROPERTY_ATOMCOUNT = "jmol.model.atomcount";    

    private JmolMonomer jmolMonomer;                        //The model

    private IPropertyDescriptor[] propertyDescriptors;    //Cached descriptors

    public MonomerPropertySource(JmolMonomer jmolMonomer) {
        this.jmolMonomer=jmolMonomer;
    }


	public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            // Create a descriptor and set a category
            PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Name");
            nameDescriptor.setCategory("Jmol");

            PropertyDescriptor structureDescriptor = new PropertyDescriptor(PROPERTY_PROTEIN_STRUCTURE, "Structure");
            structureDescriptor.setCategory("Jmol");

            PropertyDescriptor atomsDescriptor = new PropertyDescriptor(PROPERTY_ATOMCOUNT, "Atoms");
            atomsDescriptor.setCategory("Jmol");


            propertyDescriptors = new IPropertyDescriptor[] {
                    nameDescriptor,   // Read-only (instance of PropertyDescriptor)
                    structureDescriptor,   // Read-only (instance of PropertyDescriptor)
                    atomsDescriptor   // Read-only (instance of PropertyDescriptor)
            };
        }
        return propertyDescriptors;
    }


    public Object getEditableValue() {
        return null;
    }

    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return jmolMonomer.getName();
        else if (id.equals(PROPERTY_PROTEIN_STRUCTURE)) {
            return jmolMonomer.getProteinStructure();
        }
        else if (id.equals(PROPERTY_ATOMCOUNT)) {
            return jmolMonomer.getAtomCount();
        }
        return null;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public void setPropertyValue(Object id, Object value) {
    }

}
