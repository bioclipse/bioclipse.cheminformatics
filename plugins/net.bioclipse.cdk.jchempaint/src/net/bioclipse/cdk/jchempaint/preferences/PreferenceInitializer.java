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
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomRadius;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowEndCarbons;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowExplicitHydrogens;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondLength;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.WedgeWidth;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator.ShowImplicitHydrogens;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator.HighlightBondDistance;
import org.openscience.cdk.renderer.generators.RingGenerator.ShowAromaticity;

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
		        PreferenceConstants.SHOW_AROMATICITY_BOOL,
		        model.getRenderingParameter(ShowAromaticity.class).getDefault()
		);
        store.setDefault(
                PreferenceConstants.SHOW_END_CARBONS_BOOL,
                model.getRenderingParameter(ShowEndCarbons.class).getDefault()
        );
        store.setDefault(
                PreferenceConstants.SHOW_EXPLICIT_HYDROGENS_BOOL,
                model.getRenderingParameter(ShowExplicitHydrogens.class).getValue()
        );
        store.setDefault(
                PreferenceConstants.SHOW_IMPLICIT_HYDROGENS_BOOL,
                model.getRenderingParameter(ShowImplicitHydrogens.class).getValue()
        );
        store.setDefault(
                PreferenceConstants.SHOW_NUMBERS_BOOL,
                model.getDefaultRenderingParameter(WillDrawAtomNumbers.class)
        );


        store.setDefault(
                PreferenceConstants.ATOM_RADIUS_DOUBLE,
                (Double)model.getRenderingParameter(AtomRadius.class).getDefault()
        );
        store.setDefault(
                PreferenceConstants.BOND_LENGTH_DOUBLE,
                model.getRenderingParameter(BondLength.class).getValue()
        );
        store.setDefault(
                PreferenceConstants.BOND_DISTANCE_DOUBLE,
                model.getRenderingParameter( BondDistance.class ).getValue()
        );
        store.setDefault(
                PreferenceConstants.HIGHLIGHT_ATOM_DISTANCE_DOUBLE,
                model.getDefaultRenderingParameter(HighlightAtomDistance.class)
        );
        store.setDefault(
                PreferenceConstants.HIGHLIGHT_BOND_DISTANCE_DOUBLE,
                model.getDefaultRenderingParameter(HighlightBondDistance.class)
        );
        store.setDefault(
                PreferenceConstants.MARGIN_DOUBLE,
                model.getRenderingParameter(Margin.class).getDefault()
        );
        store.setDefault(
                PreferenceConstants.WEDGE_WIDTH_DOUBLE,
                model.getDefaultRenderingParameter(WedgeWidth.class)
        );
            
	}

}
