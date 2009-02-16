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
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for the CDK cheminformatics functionality.
 */
public class JChemPaintPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

    private BooleanFieldEditor showAromaticityField;
    private BooleanFieldEditor showEndCarbons;
    
	public JChemPaintPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("JChemPaint Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	    showAromaticityField = new BooleanFieldEditor(
	        PreferenceConstants.SHOWAROMATICITY_BOOL,
	        "Show &Aromaticity",
	        getFieldEditorParent()
	    );
		addField(showAromaticityField);
        showEndCarbons = new BooleanFieldEditor(
            PreferenceConstants.SHOWENDCARBONS_BOOL,
            "Show &End Carbons",
            getFieldEditorParent()
        );
        addField(showEndCarbons);
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