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

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.jchempaint.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomRadius;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowEndCarbons;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowExplicitHydrogens;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.BondLength;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.WedgeWidth;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator.ShowImplicitHydrogens;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator.HighlightBondDistance;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
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
        List<IGeneratorParameter<?>> params = new ArrayList<>();
        params.addAll( model.getRenderingParameters() );
        for ( IGenerator<IAtomContainer> p : JChemPaintPreferencePage.getGenerators() ) {
            params.addAll( p.getParameters() );
        }

        for ( IGeneratorParameter<?> param : params ) {
            initializeParam( param, store );
        }


            
	}

    private <T> void initializeParam( IGeneratorParameter<T> param, IPreferenceStore store ) {

        Class<?> clazz = param.getClass();
        String name = clazz.getName();
        Object value = param.getDefault();
        if ( value instanceof Double && ((Double) value).isNaN() ) {
            System.out.println( "NANNANNANNANAN" );
        }
        if ( value instanceof Integer ) {
            store.setDefault( name, ((Integer) value).intValue() );
        } else if ( value instanceof Double ) {
            store.setDefault( name, ((Double) value).doubleValue() );
        } else if ( value instanceof Boolean ) {
            store.setDefault( name, ((Boolean) value).booleanValue() );
        } else if (value instanceof java.awt.Color) {
            store.setDefault( name, colorToHex( (java.awt.Color) value ) );
        } else {
            store.setDefault( name, value.toString() );
        }
    }

    private String colorToHex( java.awt.Color color ) {
        return String.format("%d,%d,%d",color.getRed(), color.getGreen(), color.getBlue() );
        //return String.format( "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue() );
    }
}
