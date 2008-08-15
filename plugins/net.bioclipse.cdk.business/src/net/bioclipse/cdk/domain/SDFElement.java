/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import org.eclipse.core.resources.IFile;

import net.bioclipse.core.domain.BioObject;


/**
 * An element in an sdf file
 * 
 * @author jonalv
 *
 */
public class SDFElement extends BioObject {

    private String name;
    private long position;
    
    public SDFElement(IFile file, String name, long position) {

        super();
        this.name = name;
        this.position = position;
        this.resource = file;
    }

    public String getName() {
    
        return name;
    }
    
    public void setName( String name ) {
    
        this.name = name;
    }
    
    public long getPosition() {
    
        return position;
    }
    
    public void setPosition( long position ) {
    
        this.position = position;
    }
}
