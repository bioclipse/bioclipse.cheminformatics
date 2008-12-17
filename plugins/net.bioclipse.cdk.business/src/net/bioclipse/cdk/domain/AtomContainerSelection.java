/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cdk.domain;
import org.openscience.cdk.interfaces.IAtomContainer;
/**
 * 
 * @author ola
 *
 */
public class AtomContainerSelection {
    IAtomContainer selection;
    public IAtomContainer getSelection() {
        return selection;
    }
    public void setSelection( IAtomContainer selection ) {
        this.selection = selection;
    }
    /**
     * Constructor
     * @param selection
     */
    public AtomContainerSelection(IAtomContainer selection) {
        super();
        this.selection = selection;
    }
}
