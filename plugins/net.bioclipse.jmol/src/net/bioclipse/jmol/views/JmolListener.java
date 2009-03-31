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
import java.util.BitSet;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.scripting.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.jmol.api.JmolSelectionListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.JmolConstants;

/**
 * Class that listens to Jmol events and promotes them into the 
 * Bioclipse framework.
 * @author ola
 *
 */
public class JmolListener implements JmolStatusListener, 
                                     JmolSelectionListener {

    private static final Logger logger 
        = Logger.getLogger(JmolListener.class);

    private ISelectionProvider part;

    private JmolViewer viewer;
    
    /**
     * @param part
     * @param viewer whether the ouline is showing atoms
     */
    public JmolListener(ISelectionProvider part, JmolViewer viewer) {
        this.part  = part;
        this.viewer = viewer;
        
    }
    
    public void handlePopupMenu(int x, int y) {
        logger.debug( "JmolListener.handlePopupMenu( " +
                          "x=" + x + ", " +
                          "y= " + y + " )" );
    }

    public void showUrl(String url) {
        Activator.getDefault().getJsConsoleManager().print("Show URL: " + url);
    }

    public void showConsole(boolean showConsole) {
        logger.debug( "JmolListener.showConsole( " +
        		              "showConsole=" + showConsole + " )" );
    }

    public void setCallbackFunction( String callbackType, 
                                     String callbackFunction ) {
        logger.debug( "JmolListener.setCallbackFunction( " +
        		              "callbackType=" + callbackType + "," +
        		              "callbackFunction=" + callbackFunction + " )" );
    }

    public String eval(String strEval) {
        logger.debug( "JmolListener.eval( strEval=" + strEval + " )" );
        return null;
    }

    public float[][] functionXY(String functionName, int x, int y) {
        logger.debug( "JmolListener.functionXY( " +
                          "functionName=" + functionName + "," +
                          "x=" + x + ", y=" + y + " )" );
        return new float[0][0];
    }

    public String createImage( String fileName, 
                               String type, 
                               Object text_or_bytes, 
                               int quality ) {
        logger.debug( "JmolListener.createImage( " +
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
        logger.debug( "JmolListener.getRegistryInfo() called" );
        return null;
    }

    public void notifyCallback(int type, Object[] data) {
        logger.debug( "JmolListener.notifyCallback( type = " + type + ", " +
             		     "data = " + Arrays.deepToString(data) + ") called" );
    }

    public boolean notifyEnabled(int callback_pick) {
        logger.debug( "JmolListener.notifyEnabled( callback_pick = " 
                      + callback_pick + ") called" );
        return false;
    }

    public void selectionChanged( BitSet selection ) {
        if ( viewer.getPolymerCount() == 0 &&
             viewer.getChainCount() == 1 ) {
            part.setSelection( new JmolAtomSelection(selection) );
        }
    }
}
