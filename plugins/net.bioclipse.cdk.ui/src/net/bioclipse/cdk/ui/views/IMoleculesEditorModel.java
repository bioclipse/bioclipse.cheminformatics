/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     Arvid Berg
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.views;
/**
 * @author arvid
 *
 */
public interface IMoleculesEditorModel {
    public Object getMoleculeAt(int index);
    public int getNumberOfMolecules();
    public void save();
}
