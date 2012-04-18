/*******************************************************************************
  * Copyright (c) 2011 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.chemspider.ui;

 import net.bioclipse.chemspider.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * @author Ola Spjuth
 */
public class ChemspiderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

    public void init(IWorkbench workbench) {
        //Initialize the preference store we wish to use
        setPreferenceStore(Activator.getDefault().getPreferenceStore());

      }
    

    @Override
    protected void createFieldEditors() {

    	StringFieldEditor token = new StringFieldEditor(Activator.PREF_SECURITY_TOKEN, 
    					"Chemspider Security Token" +
    					"(get one at \nhttp://www.chemspider.com/AboutServices.aspx)", 
    				    getFieldEditorParent());

    	StringFieldEditor endpoint = new StringFieldEditor(Activator.PREF_SERVER_ENDPOINT, 
				"Chemspider server endpoint", 
				getFieldEditorParent());

    	addField( token );
    	addField( endpoint );
    }
}
