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
public class ModelPropertySource implements IPropertySource {
    private static final String PROPERTY_NAME = "jmol.model.name";     
    private static final String PROPERTY_BPOL_COUNT = "jmol.model.bpols";    
    private static final String PROPERTY_CHAIN_COUNT = "jmol.model.chainss";    
    private JmolModel jmolModel;                        //The model
    private IPropertyDescriptor[] propertyDescriptors;    //Cached descriptors
    public ModelPropertySource(JmolModel jmolModel) {
        this.jmolModel=jmolModel;
    }
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            // Create a descriptor and set a category
            PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "name");
            nameDescriptor.setCategory("Jmol");
            PropertyDescriptor bpolDescriptor = new PropertyDescriptor(PROPERTY_BPOL_COUNT, "Biopolymers");
            bpolDescriptor.setCategory("Jmol");
            PropertyDescriptor chainDescriptor = new PropertyDescriptor(PROPERTY_CHAIN_COUNT, "Chains");
            chainDescriptor.setCategory("Jmol");
            propertyDescriptors = new IPropertyDescriptor[] {
                    nameDescriptor,   // Read-only (instance of PropertyDescriptor)
                    bpolDescriptor,   // Read-only (instance of PropertyDescriptor)
                    chainDescriptor   // Read-only (instance of PropertyDescriptor)
            };
        }
        return propertyDescriptors;
    }
    public Object getEditableValue() {
        return null;
    }
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return jmolModel.getName();
        else if (id.equals(PROPERTY_BPOL_COUNT)) {
            return jmolModel.getBiopolymerCount();
        }
        else if (id.equals(PROPERTY_CHAIN_COUNT)) {
            return jmolModel.getChainCount();
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
