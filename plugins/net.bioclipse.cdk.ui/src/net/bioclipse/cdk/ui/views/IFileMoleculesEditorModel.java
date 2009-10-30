/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
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

import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.core.resources.IFile;


public interface IFileMoleculesEditorModel extends IMoleculesEditorModel {

    public IFile getResource();

    public void insert(int index, ICDKMolecule... molecules);

    public void move(int indexFrom, int indexTo);

}
