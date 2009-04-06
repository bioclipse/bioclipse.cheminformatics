/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.chemoinformatics.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.ui.prefs.UpdateSitesPreferencePage;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.update.search.BackLevelFilter;
import org.eclipse.update.search.EnvironmentFilter;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

public class CheatSheetAction extends Action implements ICheatSheetAction {

  private static final Logger logger = Logger.getLogger(CheatSheetAction.class);
  
	public void run(String[] params, ICheatSheetManager manager) {
		if(params[0].equals("runupdate")){
		    BusyIndicator.showWhile(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay(), new Runnable() {
		        public void run() {
		          UpdateJob job = new UpdateJob("Software Updates",
		              getSearchRequest());
		          UpdateManagerUI.openInstaller(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), job);
		        }
		      });
		}
	}
	
	 private UpdateSearchRequest getSearchRequest() {
	     UpdateSearchRequest result = new UpdateSearchRequest(
	         UpdateSearchRequest.createDefaultSiteSearchCategory(),
	         new UpdateSearchScope());
	     result.addFilter(new BackLevelFilter());
	     result.addFilter(new EnvironmentFilter());
	     UpdateSearchScope scope = new UpdateSearchScope();
	       //Get prefs from update site store
	       List<String[]> list=UpdateSitesPreferencePage.getPreferencesFromStore();
	       if (list==null){
	         return null;
	       }

	       //Add them one by one
	       Iterator<String[]> iter=list.iterator();
	       while (iter.hasNext()) {
	         String[] entry = iter.next();
	         try {
	         scope.addSearchSite(entry[0], new URL(entry[1]), null);
	         System.out.println("Added entry site: " + entry[0] + " - " + entry[1]);
	         } catch (MalformedURLException e) {
	           System.out.println("ERROR: Skipping site: " + entry[0] + " - " + entry[1] + ": Malformed URL.");
	         }
	       }   
	       
//	       scope.addSearchSite("Bioclipse site", new URL(BioclipseConstants.UPDATE_SITE), null);
	     result.setScope(scope);
	     return result;
	   }
}
