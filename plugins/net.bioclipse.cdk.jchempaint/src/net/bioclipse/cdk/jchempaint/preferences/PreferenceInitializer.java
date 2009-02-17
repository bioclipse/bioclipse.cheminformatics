/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@user.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.preferences;

import net.bioclipse.cdk.jchempaint.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.renderer.RendererModel;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// inherit the defaults from the JChemPaintManager
		RendererModel model = new RendererModel();

		store.setDefault(
		    PreferenceConstants.SHOWAROMATICITY_BOOL,
		    model.getShowAromaticity()
		);
        store.setDefault(
            PreferenceConstants.SHOWENDCARBONS_BOOL,
            model.getShowEndCarbons()
        );
        store.setDefault(
            PreferenceConstants.MARGIN_DOUBLE,
            model.getMargin()
        );
        store.setDefault(
                PreferenceConstants.BOND_LENGTH_DOUBLE,
                model.getBondLength()
        );
            
	}

}
