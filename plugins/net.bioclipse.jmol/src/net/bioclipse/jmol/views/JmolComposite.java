/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.jmol.views;

import org.eclipse.swt.widgets.Composite;

public class JmolComposite extends Composite {

    public JmolComposite(Composite parent, int style) {
        super(parent, style);
    }

    //TODO: override if the composite is dirty and add explicit 
    //repaint call to Jmol
    
    
}
