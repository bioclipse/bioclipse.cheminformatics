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

import java.util.List;

/**
 * An abstract base class for objects in the JmolContentOutline
 * @author ola
 */
public abstract class JmolObject implements IJmolObject {

    private String name;
    private List<IJmolObject> children;
    private IJmolObject parent;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<IJmolObject> getChildren() {
        if (children==null) createChildren();

        if (children!=null) 
            return children;
        else return null;
    }
    public void setChildren(List<IJmolObject> children) {
        this.children = children;
    }
    public IJmolObject getParent() {
        return parent;
    }
    public void setParent(IJmolObject parent) {
        this.parent = parent;
    }

    /**
     * This method creates the children of an JmolObject
     */
    public abstract void createChildren();

    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
