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

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class MoleculeListLabelProvider implements ITableLabelProvider {

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ICDKMolecule) {
			ICDKMolecule cdkmol = (ICDKMolecule) element;
			
			if (columnIndex==0){
				try {
					String smiles = Activator.getDefault().getCDKManager()
						.calculateSmiles(cdkmol);
					return smiles;
				} catch (BioclipseException e) {
					return "Error calculating SMILES";
				}
			}
			
			return "Not implemented";
//			return cdkmol.getAtomContainer().getID();
		}
		else if (element instanceof IMolecule) {
			IMolecule mol = (IMolecule) element;
			return mol.getUID();
		}
		return element.toString();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
