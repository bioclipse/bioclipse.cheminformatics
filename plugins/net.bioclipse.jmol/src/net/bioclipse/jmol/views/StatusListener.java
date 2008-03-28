/*******************************************************************************
 * Copyright (c) 2007 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.jmol.views;


import java.util.List;

import org.apache.log4j.Logger;
import net.bioclipse.core.util.LogUtils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;
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
		logger.debug("Jmol atom picked: " + atomIndex + ". Info: " + strInfo);
	
		//Pick out 15 and B from e.g. [LYS]17:B.N #1314 24.648 17.987 18.034
//		if (strInfo.charAt(0)=='['){
//			if (strInfo.charAt(4)==']'){
				try{
				//We start with [XXX] now pick out left and right of colon 
				int colonIndex = strInfo.indexOf(":");
				
				//start first char after ]
				int startindex=strInfo.lastIndexOf(']', colonIndex)+1;
				
				//End before colon
				if (colonIndex<=startindex) return;
				String no=strInfo.substring(startindex,colonIndex);
				
				//Find first dot after colon
				int dotIndex=strInfo.indexOf('.', colonIndex-1);
				
				//get after colon to before first dot
				if (dotIndex<=(colonIndex-1)) return;
				String chain=strInfo.substring(colonIndex+1,dotIndex);

				//Create new selection
				JmolSelection selection=new JmolSelection(no,chain);
				
				//Throw a new SelectionEvent
				part.setSelection(selection);

				} catch (Exception e){
					logger.debug("Could not create JmolSelecion, probably something unselectable selected in Jmol");
					LogUtils.debugTrace(logger, e);
					return;
				}

				
//			}
//		}

		
		
		
	}

	public void notifyFileLoaded(String fullPathName, String fileName, String modelName, Object clientFile, String errorMessage) {
		logger.debug("Jmol file loaded: " + fullPathName + ". Modelname: " + modelName);
		if ((errorMessage!=null) && (!(errorMessage.equals("")))){
			logger.error("JmolError: " + errorMessage);
		}
	}

	public void notifyFrameChanged(int frameNo) {
		logger.debug("Jmol frame changed to: " + frameNo);
	}

	public void notityNewDefaultModeMeasurement(int count, String strInfo) {
		logger.debug("New default measurement mode: " + count + ". Info: " + strInfo);
	}

	public void notifyNewPickingModeMeasurement(int iatom, String strMeasure) {
		logger.debug("Jmol atom picked: " + iatom + ". Measure: " + strMeasure);
	}

	public void notifyScriptStart(String statusMessage, String additionalInfo) {
		logger.info("JmolScript started. Status: " + statusMessage + ". Info: " + additionalInfo);
	}

	public void notifyScriptTermination(String statusMessage, int msWalltime) {
		logger.info("JmolScript ended. Status: " + statusMessage + ". Time: " + msWalltime + " ms");
	}

	public void sendConsoleEcho(String strEcho) {
		sendConsoleMessage(strEcho);
	}

	public void sendConsoleMessage(String strStatus) {
		logger.info(strStatus);
	}

	public void sendSyncScript(String script, String appletName) {
	}

	public void showUrl(String url) {
		logger.debug("Show URL: " + url);
	}

	public void showConsole(boolean showConsole) {
	}

	public void setStatusMessage(String statusMessage) {
		logger.debug("Jmol status message: " + statusMessage);
	}

	public void scriptEcho(String strEcho) {
		logger.debug("Jmol ScriptEcho: " + strEcho);
	}

	public void scriptStatus(String strStatus) {
		logger.debug("Jmol ScriptStatus: " + strStatus);
	}

	public void notifyMeasurementsChanged() {
		logger.debug("Jmol measurements have changed.");
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

	public void notifyFrameChanged(int arg0, int arg1, int arg2, int arg3, int arg4) {
	}

	public void notifyResized(int arg0, int arg1) {
	}

	public float[][] functionXY(String arg0, int arg1, int arg2) {
		return new float[0][0];
	}

	public void createImage(String file, Object type_or_text_or_bytes,
			int quality) {
	}

}
