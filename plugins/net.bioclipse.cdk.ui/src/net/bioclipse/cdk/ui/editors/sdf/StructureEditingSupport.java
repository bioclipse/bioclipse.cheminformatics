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
package net.bioclipse.cdk.ui.editors.sdf;

import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public class StructureEditingSupport extends EditingSupport {

	public StructureEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		return false;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof ICDKMolecule) {
			ICDKMolecule mol = (ICDKMolecule) element;
			
			
		}
		return null;
	}

	@Override
	protected Object getValue(Object element) {
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
	}

}
