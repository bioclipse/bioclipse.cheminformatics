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
import java.util.List;

import net.bioclipse.core.api.domain.BioObject;


public class SmartsFile extends BioObject{
    
    List<SmartsWrapper> smarts;
    String name;
    
    public List<SmartsWrapper> getSmarts() {
    
        return smarts;
    }
    
    public void setSmarts( List<SmartsWrapper> smarts ) {
    
        this.smarts = smarts;
    }
    
    public String getName() {
    
        return name;
    }
    
    public void setName( String name ) {
    
        this.name = name;
    }

    public void addSmartsWrapper( SmartsWrapper sw ) {
        if (smarts==null) smarts= new ArrayList<SmartsWrapper>();
        smarts.add( sw );
    }
    
}
