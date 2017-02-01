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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.RadicalGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;

/**
 * Preference page for the CDK cheminformatics functionality.
 */
public class JChemPaintPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

    private BooleanFieldEditor showAromaticityField;
    private BooleanFieldEditor showEndCarbons;
    private BooleanFieldEditor showExplicitHydrogens;
    private BooleanFieldEditor showImplicitHydrogens;
    private BooleanFieldEditor showNumbers;
    
    private DoubleFieldEditor atomRadius;
    private DoubleFieldEditor bondLength;
    private DoubleFieldEditor bondDistance;
    private DoubleFieldEditor highlightAtomDistance;
    private DoubleFieldEditor highlightBondDistance;
    private DoubleFieldEditor margin;
    private DoubleFieldEditor wedgeWidth;
    
    public JChemPaintPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("JChemPaint Preferences");
	}
	
    public static List<IGenerator<IAtomContainer>> getGenerators() {
        List<IGenerator<IAtomContainer>> generatorList = new ArrayList<IGenerator<IAtomContainer>>();
        // generatorList.add( new AtomContainerBoundsGenerator() );
        generatorList.add( new BasicSceneGenerator() );
        generatorList.add( new RingGenerator() );
        // generatorList.add( new ExtendedAtomGenerator());
        generatorList.add( new StandardGenerator( java.awt.Font.decode( null ) ) );
        // generatorList.add( new BasicAtomGenerator());
        generatorList.add( new RadicalGenerator() );
        generatorList.add( new HighlightAtomGenerator() );
        generatorList.add( new HighlightBondGenerator() );
        return generatorList;
    }

    private void generateFieldEditors() {

        for ( IGeneratorParameter<?> param : new RendererModel().getRenderingParameters() ) {
            generateFieldEditor( param );
        }
        for ( IGenerator<IAtomContainer> prp : getGenerators() ) {
            String generatorName = prp.getClass().getSimpleName();
            addField( new LabelFieldEditor( "------" + generatorName + "------", getFieldEditorParent() ) );
            for ( IGeneratorParameter<?> param : prp.getParameters() ) {
                generateFieldEditor( param );
            }
            addField( new LabelFieldEditor( "", getFieldEditorParent() ) );
        }
    }

    protected void generateFieldEditor( IGeneratorParameter<?> param ) {

        Class<?> clazz = param.getDefault().getClass();
        // org/openscience/cdk/renderer/generators/standard/StandardGenerator$BondSeparation.class
        String name = param.getClass().getName();
        String[] names = name.substring( name.lastIndexOf( '.' ) + 1 ).split( "\\$", 2 );
        if ( clazz.isAssignableFrom( Integer.class ) ) {
            FieldEditor editor = new IntegerFieldEditor( name, names[1], getFieldEditorParent() );
            addField( editor );
        } else if ( clazz.isAssignableFrom( Double.class ) ) {
            FieldEditor editor = new DoubleFieldEditor( name, names[1], getFieldEditorParent() );
            addField( editor );
        } else if ( clazz.isAssignableFrom( Boolean.class ) ) {
            FieldEditor editor = new BooleanFieldEditor( name, names[1], getFieldEditorParent() );
            addField( editor );
        } else if ( clazz.isAssignableFrom( Color.class ) ) {
            FieldEditor editor = new ColorFieldEditor( name, names[1], getFieldEditorParent() );
            addField( editor );
        } else {
            FieldEditor editor = new StringFieldEditor( name, names[1], getFieldEditorParent() );
            editor.setEnabled( false, getFieldEditorParent() );
            addField( editor );
        }
    }

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	    
        generateFieldEditors();
        if ( true )
            return;

	    showAromaticityField = new BooleanFieldEditor(
	        PreferenceConstants.SHOW_AROMATICITY_BOOL,
	        "Show &Aromaticity",
	        getFieldEditorParent()
	    );
		addField(showAromaticityField);
		
        showEndCarbons = new BooleanFieldEditor(
            PreferenceConstants.SHOW_END_CARBONS_BOOL,
            "Show &End Carbons",
            getFieldEditorParent()
        );
        addField(showEndCarbons);
        
        showExplicitHydrogens = new BooleanFieldEditor(
                PreferenceConstants.SHOW_EXPLICIT_HYDROGENS_BOOL,
                "Show E&xplicit Hydrogens",
                getFieldEditorParent()
        );
        addField(showExplicitHydrogens);
        
        showImplicitHydrogens = new BooleanFieldEditor(
                PreferenceConstants.SHOW_IMPLICIT_HYDROGENS_BOOL,
                "Show &Implicit Hydrogens",
                getFieldEditorParent()
        );
        addField(showImplicitHydrogens);
        
        showNumbers = new BooleanFieldEditor(
                PreferenceConstants.SHOW_NUMBERS_BOOL,
                "Show &Numbers",
                getFieldEditorParent()
        );
        addField(showNumbers);
        
        atomRadius = new DoubleFieldEditor(
                PreferenceConstants.ATOM_RADIUS_DOUBLE,
                "Atom &Radius",
                getFieldEditorParent()
        );
        addField(atomRadius);
        
        bondLength = new DoubleFieldEditor(
                PreferenceConstants.BOND_LENGTH_DOUBLE,
                "Bond &Length",
                getFieldEditorParent()
        );
        addField(bondLength);
        
        bondDistance = new DoubleFieldEditor(
                PreferenceConstants.BOND_DISTANCE_DOUBLE,
                "Bond &Distance",
                getFieldEditorParent()
        );
        addField(bondDistance);
        
        highlightAtomDistance = new DoubleFieldEditor(
                PreferenceConstants.HIGHLIGHT_ATOM_DISTANCE_DOUBLE,
                "&Highlight Atom Distance",
                getFieldEditorParent()
        );
        addField(highlightAtomDistance);
        
        highlightBondDistance = new DoubleFieldEditor(
                PreferenceConstants.HIGHLIGHT_BOND_DISTANCE_DOUBLE,
                "&Highlight Bond Distance",
                getFieldEditorParent()
        );
        addField(highlightBondDistance);
        
        margin = new DoubleFieldEditor(
                PreferenceConstants.MARGIN_DOUBLE,
                "&Margin",
                getFieldEditorParent()
        );
        addField(margin);
        
        wedgeWidth = new DoubleFieldEditor(
                PreferenceConstants.WEDGE_WIDTH_DOUBLE,
                "&Wedge Width",
                getFieldEditorParent()
        );
        addField(wedgeWidth);

        BooleanFieldEditor showGenerated =
            new BooleanFieldEditor( PreferenceConstants.SHOW_LABEL_GENERATED,
                                    "Show Generated Label",
                                    getFieldEditorParent() );
        addField( showGenerated );

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	public boolean performOk() {
	    boolean isOK = super.performOk();

	    if (isOK) {
	        IJChemPaintGlobalPropertiesManager jcpProp =
	            net.bioclipse.cdk.jchempaint.Activator.getDefault().
	            getJCPPropManager();
	        try {
	            jcpProp.applyGlobalProperties();
	        } catch (BioclipseException e) {
	            e.printStackTrace();
	        }
	    }
	    return isOK;
	}
	
	
	
}