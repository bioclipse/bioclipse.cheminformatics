/*******************************************************************************
* Copyright (c) 2009  Ola Spjuth <ospjuth@users.sourceforge.net>
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
*
* Contact: http://www.bioclipse.net/
******************************************************************************/
package net.bioclipse.cdk.domain;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * An interface to provide a substructure
 * @author ola
 *
 */
public interface ISubStructure {

    public IAtomContainer getAtomContainer();
    
}
