/*******************************************************************************
 * Copyright (c) 2006-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.pubchem.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import net.bioclipse.model.BioResourceType;
import net.bioclipse.model.IBioResource;
import net.bioclipse.model.resources.StringResource;
import net.bioclipse.scripting.ui.Activator;
import net.bioclipse.scripting.ui.business.IJsConsoleManager;
import net.bioclipse.util.BioclipseConsole;
import net.bioclipse.util.FetchURLContentJob;
import net.bioclipse.util.IFetchURLContentDoneListener;
import net.bioclipse.util.folderUtils;
import net.bioclipse.views.BioResourceView;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;

import org.eclipse.core.runtime.jobs.IJobStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewFromPubChemWizard extends Wizard implements INewWizard{
	
	private final static String PUBCHEM_FOLDER_NAME = "PubChem Results";
	
	private final static String utilsURLBase = "http://www.ncbi.nlm.nih.gov/entrez/eutils";
	private final static String pubchemURLBase = "http://pubchem.ncbi.nlm.nih.gov/";
	
	private final static String TOOL = "bioclipse.net";

	private NewFromPubChemWizardPage newMolPage;
	protected String molecule;
	
	private String query = "";
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
	}
	
	
	public void addPages()  
	{  
		// create and add first page
		newMolPage=new NewFromPubChemWizardPage();
		addPage(newMolPage);
		newMolPage.setQuery(query);
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
		if (newMolPage != null) newMolPage.setQuery(query);
	}
	
	public boolean canFinish() {
		if (query == null || query.length() == 0) {
			// make sure we have the latest
			this.query = newMolPage.getQuery();
		}
		return (query != null && query.compareTo("") != 0);
	}
	
	public boolean performFinish() {
		
        IJsConsoleManager js = Activator.getDefault().getJsConsoleManager();
        
		try {
//			do the PubChem magic here
			String db = "pccompound";
			String query = replaceSpaces(molecule);

			String esearch = utilsURLBase + "/esearch.fcgi?" +
				"db=" + db + "&retmax=50&usehistory=y&tool=" + TOOL + "&term=" + query;

			URL queryURL = new URL(esearch);
			URLConnection connection = queryURL.openConnection();
			
	        Builder parser = new Builder();
			Document doc = parser.build(connection.getInputStream());
			
			int count = 0;
			Nodes countNodes = doc.query("/eSearchResult/Count");
			if (countNodes.size() > 0) {
				System.out.println(countNodes.get(0).toString());
				count = Integer.parseInt(countNodes.get(0).getValue());
				js.print("PubChem: #compounds found -> " + count);
			} else {
			    js.print("No results found");
				return false;
			}
		
			Nodes cidNodes = doc.query("/eSearchResult/IdList/Id");
			
			int max = cidNodes.size();
			if (max > 15) {
			    js.print("Found more than 15 molecules, only downloading the first 15");
				max = 15;
			}
			
			for (int cidCount=0; cidCount<max; cidCount++) {
				String cid = cidNodes.get(cidCount).getValue();
				js.print("Downloading CID: " + cid);
				String efetch = pubchemURLBase + "summary/summary.cgi?cid=" + cid + "&disopt=DisplaySDF";
			
				URL fetchURL = new URL(efetch);
				Job downloadJobs = new FetchURLContentJob(
					fetchURL, new DownloadListener(folder, cid)
				);
				downloadJobs.schedule();
			}

		} catch (Exception e) {
		    js.print("Downloading the search results failed: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private String replaceSpaces(String molecule2) {
		StringBuffer result = new StringBuffer();
		for (int i=0; i<molecule2.length(); i++) {
			if (Character.isWhitespace(molecule2.charAt(i))) {
				result.append("+");
			} else {
				result.append(molecule2.charAt(i));
			}
		}
		return result.toString();
	}
	
	public IBioResource createVirtualFolder() {
		final IBioResource root = BioResourceView.getRootFolder();
		BioResourceType virtualFolderType = folderUtils.getVirtualFolderType();
		// find a folder name which is not used yet
		int counter = 1;
		String folderName = PUBCHEM_FOLDER_NAME + " " + counter;
		while (existsFolder(BioResourceView.getRootFolder(), folderName)) {
			counter++;
			folderName = PUBCHEM_FOLDER_NAME + " " + counter;
		}
		BioclipseConsole.writeToConsole("New virtual folder name: " + folderName);
		
		// OK, no SENECA virtual folder created yet...
		final IBioResource virtualFolder = virtualFolderType.newResource(
			folderName, folderName
		);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				root.addChild(virtualFolder);
			}
		});
		return virtualFolder;
	}

	private boolean existsFolder(IBioResource root, String folderName) {
		Iterator children = root.getChildren().iterator();
		while (children.hasNext()) {
			Object child = children.next();
			if (child instanceof IBioResource &&
				((IBioResource)child).isFolder() &&
				((IBioResource)child).getName().equals(folderName)) {
				return true;
			}
		}
		return false;
	}

	class DownloadListener implements IFetchURLContentDoneListener {

		private IBioResource parent;
		private String cid;
		
		public DownloadListener(IBioResource parent, String cid) {
			this.parent = parent;
			this.cid = cid;
		}
		
		public void processDownloadedContent(byte[] content) {
			// need to clean up the MOL format
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					new ByteArrayInputStream(content)
				)
			);
			boolean done = false;
			StringBuffer result = new StringBuffer();
			String line = null;
			while (!done) {
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// just ignore
					done = true;
				}
				if (line == null || line.startsWith("$$$$")) done = true;
				if (!done) {
					result.append(line);
					result.append('\n');
				}
			}
			
			// then create a new resource
			try {
				IBioResource newResource = StringResource.createResourceFromObject(
					result.toString(), cid + ".mol"
				);
				newResource.setParent(parent);
				parent.addChild(newResource);
			} catch (Exception exception) {
				parent.setParsed(false);
			}
		}
		
	}

}
