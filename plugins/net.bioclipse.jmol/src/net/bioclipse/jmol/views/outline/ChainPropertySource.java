/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
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
import org.jmol.modelset.Chain;

/**
 * Properties for the JmolChain view object
 * @author ola
 *
 */
public class ChainPropertySource implements IPropertySource {

    private static final String PROPERTY_NAME = "jmol.chain.name";     
    private static final String PROPERTY_SEQUENCE = "jmol.chain.sequence";    

    private JmolChain jmolChain;                        //The model

    private IPropertyDescriptor[] propertyDescriptors;    //Cached descriptors

    public ChainPropertySource(JmolChain jmolChain) {
        this.jmolChain=jmolChain;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            // Create a descriptor and set a category
            PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "name");
            nameDescriptor.setCategory("Jmol");

            PropertyDescriptor sequenceDescriptor = new PropertyDescriptor(PROPERTY_SEQUENCE, "Sequence");
            sequenceDescriptor.setCategory("Jmol");


            propertyDescriptors = new IPropertyDescriptor[] {
                    nameDescriptor,   // Read-only (instance of PropertyDescriptor)
                    sequenceDescriptor   // Read-only (instance of PropertyDescriptor)
            };
        }
        return propertyDescriptors;
    }


    public Object getEditableValue() {
        return null;
    }

    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return jmolChain.getName();
        else if (id.equals(PROPERTY_SEQUENCE)) {
            return jmolChain.getSequence();
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
