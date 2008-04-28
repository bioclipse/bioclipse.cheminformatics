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
import net.bioclipse.cdk.ui.widgets.JChemPaintWidget;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.openscience.cdk.CDKConstants;

public class MoleculeListLabelProviderNew extends OwnerDrawLabelProvider{


	@Override
	protected void measure(Event event, Object element) {

		//Hardcoded to 200x200
		event.setBounds(new Rectangle(event.x, event.y, 200,
				200));
		
	}

	@Override
	protected void paint(Event event, Object element) {
		if (element instanceof StructureTableEntry) {
			StructureTableEntry entry = (StructureTableEntry) element;
			entry.draw(event);
		}
		
	}
	
}
