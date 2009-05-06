/*******************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *******************************************************************************/

package net.bioclipse.cdk.domain;

import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IBioObject;

import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IChemObject;

/**
 * Wraps an IChemObject as GUI object
 * 
 * 
 * @author ola
 *
 */
public class CDKChemObject<T extends IChemObject> extends BioObject implements IBioObject{

    private T chemobj;
//    private ChemObjectPropertySource propSource;
    private String name;

    private IPropertySource propSource;
    
    /**
     * Used to look up e.g. the molecule of an Atom
     */
    private IChemObject parentChemobj;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CDKChemObject(String name, T chemobj) {
        this.name=name;
        this.chemobj = chemobj;
    }

    //Set name=ID from chemobj
    public CDKChemObject(T chemobj) {
        this.chemobj = chemobj;
        name=chemobj.toString();
    }

    public T getChemobj() {
        return chemobj;
    }
    public void setChemobj(T chemobj) {
        this.chemobj = chemobj;
    }

    public IChemObject getParentChemobj() {
        return parentChemobj;
    }

    public void setParentChemobj(IChemObject parentChemobj) {
        this.parentChemobj = parentChemobj;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter ==IPropertySource.class){
            if (propSource ==null){
                propSource=new ChemObjectPropertySource(this);
            }
            return propSource;
        }
        if (adapter.isAssignableFrom( chemobj.getClass() )) {
            return chemobj;
        }
        return super.getAdapter(adapter);
    }

}
