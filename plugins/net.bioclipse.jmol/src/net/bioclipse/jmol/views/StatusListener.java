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


import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.scripting.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.jmol.api.JmolStatusListener;
import org.jmol.viewer.JmolConstants;

/**
 * Class that listens to Jmol events and promotes them into the 
 * Bioclipse framework.
 * @author ola
 *
 */
public class StatusListener implements JmolStatusListener {

    private static final Logger logger 
        = Logger.getLogger(StatusListener.class);

    private ISelectionProvider part;
    
    public StatusListener(ISelectionProvider part) {
        this.part=part;
    }
    
    public void handlePopupMenu(int x, int y) {
        logger.debug( "StatusListener.handlePopupMenu( " +
                          "x=" + x + ", " +
                          "y= " + y + " )" );
    }

//    public void notifyAtomPicked(int atomIndex, String strInfo) {
//        Activator.getDefault()
//                 .getJsConsoleManager()
//                 .print("Jmol atom picked: " + atomIndex + ". " +
//                 		    "Info: " + strInfo);
//        
////        if (strInfo.charAt(0)=='['){
////            if (strInfo.charAt(4)==']'){
//        
//        //PolymerSelection
//        if (strInfo.contains("[")){
//        	
//        	 //[LYS]17:B.N #1314 24.648 17.987 18.03
//
//        	//[LYS]15.CD/1 #229 -6.294 -5.529 6.598)
//            
//            /*
//           15 is the residue number 
//           . separates that from an atom name
//           CD is the atom name (in this case the fourth carbon on the lysine sidechain)
//           /1 means "in model 1", number as assigned by the PDB MODEL record or just 1 if only one model. When multiple files are loaded, then this might looke like
//
//           /3.2
//
//           where 3 is the file number, starting with 1, and 2 is the model number within that file -- disregarding MODEL records -- just assigned sequentially starting with 1.
//           */
//        	
//        	try{
//        		
//        		//We start with [XXX] now pick out left and right of colon 
//        		int colonIndex = strInfo.indexOf(":");
//
//        		//start first char after ]
//        		int startindex=strInfo.indexOf(']')+1;
//
//        		String no;
//        		int dotIndex;
//        		String chain;
//        		//End before colon
//        		if (colonIndex<=startindex){
//        			//No colon, hence a model without chain
//
//
//            		//Find first dot after colon
//            		dotIndex=strInfo.indexOf('.', 0);
//
//            		no=strInfo.substring(startindex,dotIndex);
//            		chain="";
//            		int a=0;
//        			
//        		}else{
//
//            		no=strInfo.substring(startindex,colonIndex);
//
//            		//Find first dot after colon
//            		dotIndex=strInfo.indexOf('.', colonIndex-1);
//
//            		chain=strInfo.substring(colonIndex+1,dotIndex);
//        		}
//
//
//
//        		//Create new selection
//        		JmolPolymerSelection selection=new JmolPolymerSelection(no,chain);
//
//        		//Throw a new SelectionEvent
//        		part.setSelection(selection);
//
//        	} catch (Exception e){
//        		logger.debug("Could not create JmolSelecion, " +
//        				         "probably something unselectable selected in Jmol");
//        		LogUtils.debugTrace(logger, e);
//        		return;
//        	}
//        }
//
//        //AtomSelection
//        else{
//    		//Create new selection
//    		JmolAtomSelection selection=new JmolAtomSelection(""+(atomIndex+1));
//
//    		//Throw a new SelectionEvent
//    		part.setSelection(selection);
//        	
//        }
//                
////            }
////        }
//        
//    }

    public void showUrl(String url) {
        Activator.getDefault().getJsConsoleManager().print("Show URL: " + url);
    }

    public void showConsole(boolean showConsole) {
        logger.debug( "StatusListener.showConsole( " +
        		              "showConsole=" + showConsole + " )" );
    }

    public void setCallbackFunction( String callbackType, 
                                     String callbackFunction ) {
        logger.debug( "StatusListener.setCallbackFunction( " +
        		              "callbackType=" + callbackType + "," +
        		              "callbackFunction=" + callbackFunction + " )" );
    }

    public String eval(String strEval) {
        logger.debug( "StatusListener.eval( strEval=" + strEval + " )" );
        return null;
    }

    public float[][] functionXY(String functionName, int x, int y) {
        logger.debug( "StatusListener.functionXY( " +
                          "functionName=" + functionName + "," +
                          "x=" + x + ", y=" + y + " )" );
        return new float[0][0];
    }

    public String createImage( String fileName, 
                               String type, 
                               Object text_or_bytes, 
                               int quality ) {
        logger.debug( "StatusListener.createImage( " +
                          "fileName=" + fileName + "," +
                          "type=" + type + "," +
                          "Object=" + text_or_bytes + "," +
                          "quality=" + quality + " )" );
        return null;
    }

    public String dialogAsk( String type, String fileName ) {
        logger.debug( "dialogAsk( " +
                          "type=" + type + ", " +
                          "fileName= "+ fileName + " )" );
        return null;
    }

    public Hashtable<?,?> getRegistryInfo() {
        logger.debug( "StatusListener.getRegistryInfo() called" );
        return null;
    }

    public void notifyCallback(int type, Object[] data) {
        logger.debug( "StatusListener.notifyCallback( type = " + type + ", " +
             		     "data = " + Arrays.deepToString(data) + ") called" );
        
        switch (type) {
            case JmolConstants.CALLBACK_SCRIPT:
                if ( data.length > 2 &&
                     data[2] instanceof String ) {
                    
                    Pattern p = Pattern.compile(
                                "select selected tog \\(atomindex=(\\d+)\\)");
                    Matcher m = p.matcher( (String) data[2] );
                
                    if ( m.matches() ) {
                        fireSelectionChanged();
                    }
                }
                break;
        }
    }

    private void fireSelectionChanged() {
//        part.setSelection( new JmolAtomSelection( 
//                           "" + ( Integer.parseInt( (m.group( 1 ) ) ) + 1 ) 
//                                               ) 
//                                         );
    }

    public boolean notifyEnabled(int callback_pick) {
        logger.debug( "StatusListener.notifyEnabled( callback_pick = " 
                      + callback_pick + ") called" );
        return false;
    }
}
