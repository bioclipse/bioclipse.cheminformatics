/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.domain;
import java.util.BitSet;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
/**
 * An intertace for CDKMolecule
 * @author ola
 *
 */
public interface ICDKMolecule extends IMolecule{
    /**
     * Calculate CDK fingerprint and cache the result.
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public BitSet getFingerprint(boolean force) throws BioclipseException;
    /**
     * AtomContainer is the CDK model for a molecule
     * @return
     */
    public IAtomContainer getAtomContainer();
    String getName();
    void setName( String name );
}
