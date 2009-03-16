/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching.prefs;

import net.bioclipse.cdk.smartsmatching.Activator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class SmartsMatchingPrefsInitializer extends AbstractPreferenceInitializer {

    private static final Logger logger = Logger.getLogger(SmartsMatchingPrefsInitializer.class);
    
    @Override
    public void initializeDefaultPreferences() {
      
      IPreferenceStore store=Activator.getDefault().getPreferenceStore();

//      store.setDefault(SmartsMatchingPrefsHelper.SMARTSMATCHING_PREFS_SMARTS, 
//                       "Aromatic" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[a]" 
//                       + SmartsMatchingPrefsHelper.PREFS_DELIMITER + "Fragment 1" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[CH2](*(N))" 
//                       + SmartsMatchingPrefsHelper.PREFS_DELIMITER + "Fragment 2" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[N](*(*(O)))"
//                       );

      store.setDefault(SmartsMatchingPrefsHelper.SMARTSMATCHING_PREFS_SMARTS, 
                       "t-Butyl" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[*][C]([CH3])([CH3])[CH3]" 
                       + SmartsMatchingPrefsHelper.PREFS_DELIMITER 
                       + "Epoxide" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[*][C](O1)[C]1[*]" 
                       + SmartsMatchingPrefsHelper.PREFS_DELIMITER 
                       + "Amino acid" + SmartsMatchingPrefsHelper.PREFS_PART_DELIMITER + "[*][C](C(=O)[OH])[NH2]"
                       );

        logger.info("SmartsMatching default preferences initialized");
    }
    
}
