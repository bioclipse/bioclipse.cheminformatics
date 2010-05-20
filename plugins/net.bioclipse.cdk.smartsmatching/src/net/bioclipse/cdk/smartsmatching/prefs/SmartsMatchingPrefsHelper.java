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

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.smartsmatching.Activator;
import net.bioclipse.cdk.smartsmatching.model.SmartsFile;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;


public class SmartsMatchingPrefsHelper {
    
    private static final Logger logger = Logger.getLogger(SmartsMatchingPrefsHelper.class);

    public static final String SMARTSMATCHING_PREFS_SMARTS = "SmartsMatchingSmarts";
    public static final String PREFS_FILE_DELIMITER = "-SPFILE-";
    public static final String PREFS_DELIMITER = "-SP-";
    public static final String PREFS_PART_DELIMITER = "-SPSP-";

    public static List<SmartsFile> getPreferences(){

        IPreferenceStore store=Activator.getDefault().getPreferenceStore();

        String entireString=store.getString( SMARTSMATCHING_PREFS_SMARTS );

        logger.debug("Read Smarts prefs string: " + entireString);
        
        List<SmartsFile> retlist = splitPrefsString( entireString);

        //If no hits, initialize default ones.
        if (retlist.size()<=0){
            logger.debug( "No SMARTS could be read, initializing default prefs." );
            entireString=store.getDefaultString( SMARTSMATCHING_PREFS_SMARTS );
            logger.debug("Read default Smarts prefs string: " + entireString);

            //Store def val
            store.setValue( SMARTSMATCHING_PREFS_SMARTS, entireString );

            //Try again
            retlist = splitPrefsString( entireString);

        }
        
        return retlist;

    }

    /**
     * file1
     *    S1 - CC
     *    S2 - N[N]
     * file1
     *    S3 - OOO
     *    S4 - As
     * 
     * Form: file1-SP-s1-SPSP-CC-SP-s2-SPSP-N[N]-SPFILE-file2-SP-s3-SPSP-OOO-SP-s4-SPSP-As
     * Split on SPFILE
     *      file1-SP-s1-SPSP-CC-SP-s2-SPSP-N[N]
     *      file2-SP-s3-SPSP-OOO-SP-s4-SPSP-As
     * Split on SP
     *      file1
     *      s1-SPSP-CC
     *      s2-SPSP-N[N]
     * Split on SPSP
     *      file1
     *      
     *      s1
     *      CC
     * 
     * @param entireString
     * @return
     */
    private static List<SmartsFile> splitPrefsString( String entireString) {

        List<SmartsFile> retFiles=new ArrayList<SmartsFile>();
        

        //Split in parts
        String[] files=entireString.split(PREFS_FILE_DELIMITER);
        if (files.length<=0) return retFiles;
        if (files.length==1 && files[0].equals( "" )) return retFiles;
        
        for (int f = 0; f < files.length; f++) {
            
            SmartsFile sfile=new SmartsFile();
            List<SmartsWrapper> rtlst=new ArrayList<SmartsWrapper>();
            sfile.setSmarts( rtlst );

            String[] ret=files[f].split(PREFS_DELIMITER);

            for (int i = 0; i < ret.length; i++) {

                logger.debug("Part " + i + " extracted: " + ret[i]);

                String[] subparts = ret[i].split(PREFS_PART_DELIMITER);

              if (subparts.length==2){
                  SmartsWrapper sw=new SmartsWrapper(subparts[0], subparts[1]);
                  rtlst.add(sw);
              }else if (subparts.length==1){
                  
                  //The filename sinc eonly one
                  sfile.setName( subparts[0] );
              }else{
                  logger.error( "SmartsMatchingPrefs part: " + ret[i] + "could not " +
                      "be parsed into name and smartsstring. Skipped.");
              }
            }
            
            retFiles.add(sfile);
            
        }
        
        return retFiles;
    }
    
    public static void setPreferences(List<SmartsFile> smartsList){

        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        
        String prefsToSave="";
        for (SmartsFile sf : smartsList){

            prefsToSave = prefsToSave + sf.getName() + PREFS_DELIMITER;
            for (SmartsWrapper sw : sf.getSmarts()){
                if (sw!=null){
                    if (sw.getName()==null || "".equals( sw.getName())){
                        logger.error( "SmartsWrapper has no name: " + sw.getName());
                    }else{
                        if (sw.getSmartsString()==null || "".equals( sw.getSmartsString())){
                            logger.error( "SmartsWrapper has no SMARTS: " + sw.getSmartsString());
                        }else{
                            String substr=sw.getName()+PREFS_PART_DELIMITER + sw.getSmartsString();
                            prefsToSave=prefsToSave + substr + PREFS_DELIMITER;
                        }
                    }
                }
            }
            //Remove trailing parts delimiter
            if (prefsToSave.length()>4)
                prefsToSave=prefsToSave.substring( 0, prefsToSave.length()-(PREFS_DELIMITER.length()));

            prefsToSave=prefsToSave + PREFS_FILE_DELIMITER;

        }

        //Remove trailing file delimiter
        if (prefsToSave.length()>4)
            prefsToSave=prefsToSave.substring( 0, prefsToSave.length()-(PREFS_FILE_DELIMITER.length()));

        logger.debug( "Saving smartsmatching prefs: " + prefsToSave );

        store.setValue(  SMARTSMATCHING_PREFS_SMARTS , prefsToSave);

        Activator.getDefault().savePluginPreferences();

    }
}
