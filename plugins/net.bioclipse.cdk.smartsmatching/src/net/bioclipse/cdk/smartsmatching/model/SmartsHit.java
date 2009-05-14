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

import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.core.domain.BioObject;

/**
 * Model object for a SMARTS hit
 * @author ola
 *
 */
public class SmartsHit extends BioObject implements ISubStructure{

    private String name;
    private IAtomContainer ac;
    private SmartsWrapper parent;
    private IPropertySource propertySource;
    
    /**
     * This is the molecule the hit is in
     */
    private ICDKMolecule hitMolecule;

    
    public SmartsHit(String name, IAtomContainer ac) {
        this.ac=ac;
        this.name=name;
    }

    public IAtomContainer getAtomContainer() {
        return ac;
    }
    public void setAtomContainer( IAtomContainer ac ) {
        this.ac = ac;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }

    public void setParent( SmartsWrapper parent ) {

        this.parent = parent;
    }

    public SmartsWrapper getParent() {

        return parent;
    }
    
    @Override
    public Object getAdapter( Class adapter ) {

        if (adapter == IPropertySource.class){
            return propertySource!=null 
                ? propertySource : new SmartsHitPropertySource(this);
        }

        return super.getAdapter( adapter );
    }

    
    public ICDKMolecule getHitMolecule() {
    
        return hitMolecule;
    }

    
    public void setHitMolecule( ICDKMolecule hitMolecule ) {
    
        this.hitMolecule = hitMolecule;
    }


}
