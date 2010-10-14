/*******************************************************************************
 * Copyright (c) 2009-2010  Egon Willighagen <egonw@user.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.business.preferences;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.logging.BioclipseLoggingTool;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.openscience.cdk.tools.LoggingToolFactory;

/**
 * Preference page for the CDK cheminformatics functionality.
 */
public class CDKPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private BooleanFieldEditor bioclipseLogging;

	public CDKPreferencePage() {
		super(GRID);
		ScopedPreferenceStore store 
		    = new ScopedPreferenceStore( new InstanceScope(), 
                                         PreferenceConstants.NODEQUALIFIER );
		store.setDefault( PreferenceConstants.PRETTY_CML,        true  );
        store.setDefault( PreferenceConstants.BIOCLIPSE_LOGGING, false );
		setPreferenceStore(store);
		setDescription("Cheminformatics");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.PRETTY_CML,
				"&Pretty print CML",
				getFieldEditorParent()));
		bioclipseLogging = new BooleanFieldEditor(
			PreferenceConstants.BIOCLIPSE_LOGGING,
			"&Use Bioclipse Logging",
			getFieldEditorParent());
		addField(bioclipseLogging);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	private void setBioclipseLogging(boolean use) {
		if (use) {
			LoggingToolFactory.setLoggingToolClass(BioclipseLoggingTool.class);
		} else {
			LoggingToolFactory.setLoggingToolClass(null);
		}
	}
	
	public boolean performOk() {
		BioclipseLoggingTool.useBioclipseLogging =
			bioclipseLogging.getBooleanValue();
		return super.performOk();
	}
	
	protected void performApply() {
		super.performApply();
		BioclipseLoggingTool.useBioclipseLogging =
			bioclipseLogging.getBooleanValue();
	}
	
	protected void performDefaults() {
		super.performDefaults();
		BioclipseLoggingTool.useBioclipseLogging =
			bioclipseLogging.getBooleanValue();
	}
	
}