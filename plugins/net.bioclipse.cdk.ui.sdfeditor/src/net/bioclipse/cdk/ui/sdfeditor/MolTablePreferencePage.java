/*******************************************************************************
* Copyright (c) 2010 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * @author arvid
 *
 */
public class MolTablePreferencePage extends FieldEditorPreferencePage implements
                                                IWorkbenchPreferencePage {
    
    public MolTablePreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new IntegerFieldEditor( Activator.STRUCTURE_COLUMN_WIDTH , 
                                         "Structure Column Size:",
                                         getFieldEditorParent()));
    }

    public void init( IWorkbench workbench ) {
    }
}
