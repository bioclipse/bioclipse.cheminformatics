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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * @author arvid
 *
 */
public class MolTablePreferenceInitializer extends
                                          AbstractPreferenceInitializer {

    /**
     * 
     */
    public MolTablePreferenceInitializer() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {

       IPreferenceStore store = Activator.getDefault().getPreferenceStore();
       store.setDefault( Activator.STRUCTURE_COLUMN_WIDTH, 100);

    }

}
