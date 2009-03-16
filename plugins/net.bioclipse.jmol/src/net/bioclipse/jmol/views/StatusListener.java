/*******************************************************************************
 * Copyright (c) 2007-2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.jmol.views;


import java.util.Hashtable;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.scripting.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.jmol.api.JmolStatusListener;

/**
 * Class that listens to Jmol events and promotes them into the 
 * Bioclipse framework.
 * @author ola
 *
 */
public class StatusListener implements JmolStatusListener {

    private static final Logger logger = Logger.getLogger(StatusListener.class);

    private ISelectionProvider part;
    
    public StatusListener(ISelectionProvider part) {
        this.part=part;
    }
    
    public void handlePopupMenu(int x, int y) {
    }

    public void notifyAtomPicked(int atomIndex, String strInfo) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol atom picked: " + atomIndex + ". " +
                 		    "Info: " + strInfo);
        
//        if (strInfo.charAt(0)=='['){
//            if (strInfo.charAt(4)==']'){
        
        //PolymerSelection
        if (strInfo.contains("[")){
        	
        	 //[LYS]17:B.N #1314 24.648 17.987 18.03

        	//[LYS]15.CD/1 #229 -6.294 -5.529 6.598)
            
            /*
           15 is the residue number 
           . separates that from an atom name
           CD is the atom name (in this case the fourth carbon on the lysine sidechain)
           /1 means "in model 1", number as assigned by the PDB MODEL record or just 1 if only one model. When multiple files are loaded, then this might looke like

           /3.2

           where 3 is the file number, starting with 1, and 2 is the model number within that file -- disregarding MODEL records -- just assigned sequentially starting with 1.
           */
        	
        	try{
        		
        		//We start with [XXX] now pick out left and right of colon 
        		int colonIndex = strInfo.indexOf(":");

        		//start first char after ]
        		int startindex=strInfo.indexOf(']')+1;

        		String no;
        		int dotIndex;
        		String chain;
        		//End before colon
        		if (colonIndex<=startindex){
        			//No colon, hence a model without chain


            		//Find first dot after colon
            		dotIndex=strInfo.indexOf('.', 0);

            		no=strInfo.substring(startindex,dotIndex);
            		chain="";
            		int a=0;
        			
        		}else{

            		no=strInfo.substring(startindex,colonIndex);

            		//Find first dot after colon
            		dotIndex=strInfo.indexOf('.', colonIndex-1);

            		chain=strInfo.substring(colonIndex+1,dotIndex);
        		}



        		//Create new selection
        		JmolPolymerSelection selection=new JmolPolymerSelection(no,chain);

        		//Throw a new SelectionEvent
        		part.setSelection(selection);

        	} catch (Exception e){
        		logger.debug("Could not create JmolSelecion, " +
        				         "probably something unselectable selected in Jmol");
        		LogUtils.debugTrace(logger, e);
        		return;
        	}
        }

        //AtomSelection
        else{
    		//Create new selection
    		JmolAtomSelection selection=new JmolAtomSelection(""+(atomIndex+1));

    		//Throw a new SelectionEvent
    		part.setSelection(selection);
        	
        }
                
//            }
//        }
        
    }

    public void notifyFileLoaded( String fullPathName, 
                                  String fileName, 
                                  String modelName, 
                                  Object clientFile, 
                                  String errorMessage ) {
        logger.debug("Jmol file loaded: " + fullPathName 
                     + ". Modelname: " + modelName);
        if ((errorMessage!=null) && (!(errorMessage.equals("")))) {
            logger.error("JmolError: " + errorMessage);
        }
    }

    public void notifyFrameChanged(int frameNo) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol frame changed to: " + frameNo);
    }

    public void notityNewDefaultModeMeasurement(int count, String strInfo) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("New default measurement mode: " 
                        + count + ". Info: " + strInfo);
    }

    public void notifyNewPickingModeMeasurement(int iatom, String strMeasure) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol atom picked: " + iatom 
                        + ". Measure: " + strMeasure);
    }

    public void notifyScriptStart( String statusMessage, 
                                   String additionalInfo ) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("JmolScript started. Status: " + 
                        statusMessage + ". Info: " + additionalInfo);
    }

    public void notifyScriptTermination(String statusMessage, int msWalltime) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("JmolScript ended. Status: " + statusMessage 
                        + ". Time: " + msWalltime + " ms");
    }

    public void sendConsoleEcho(String strEcho) {
        sendConsoleMessage(strEcho);
    }

    public void sendConsoleMessage(String strStatus) {
        logger.debug(strStatus);
    }

    public void sendSyncScript(String script, String appletName) {
    }

    public void showUrl(String url) {
        Activator.getDefault().getJsConsoleManager().print("Show URL: " + url);
    }

    public void showConsole(boolean showConsole) {
    }

    public void setStatusMessage(String statusMessage) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol status message: " + statusMessage);
    }

    public void scriptEcho(String strEcho) {
        Activator.getDefault()
                 .getJsConsoleManager().print("Jmol ScriptEcho: " + strEcho);
    }

    public void scriptStatus(String strStatus) {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol ScriptStatus: " + strStatus);
    }

    public void notifyMeasurementsChanged() {
        Activator.getDefault()
                 .getJsConsoleManager()
                 .print("Jmol measurements have changed.");
    }

    public void notifyAtomHovered(int arg0, String arg1) {
    }

    public void notifyNewDefaultModeMeasurement(int arg0, String arg1) {
    }

    public void setCallbackFunction(String arg0, String arg1) {
    }

    public void createImage(String file, String type, int quality) {
    }

    public String eval(String arg0) {
        return null;
    }

    public void notifyFrameChanged( int arg0, int arg1, 
                                    int arg2, int arg3, int arg4) {
    }

    public void notifyResized(int arg0, int arg1) {
    }

    public float[][] functionXY(String arg0, int arg1, int arg2) {
        return new float[0][0];
    }

    public void createImage(String file, Object type_or_text_or_bytes,
            int quality) {
    }

    public String createImage( String arg0, String arg1, 
                               Object arg2, int arg3 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public String dialogAsk( String arg0, String arg1 ) {
        // FIXME: Auto-generated method stub
        return null;
    }

    public Hashtable getRegistryInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void notifyCallback( int arg0, Object[] arg1 ) {
        // TODO Auto-generated method stub
        
    }

    public boolean notifyEnabled( int arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

}
