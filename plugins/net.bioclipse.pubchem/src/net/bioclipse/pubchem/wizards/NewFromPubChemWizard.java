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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.pubchem.business.IPubChemManager;
import net.bioclipse.scripting.ui.Activator;
import net.bioclipse.scripting.ui.business.IJsConsoleManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewFromPubChemWizard extends BasicNewResourceWizard {
	
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
	    if (newMolPage != null) this.query = newMolPage.getQuery();
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
		if (newMolPage != null) newMolPage.setQuery(query);
	}
	
	public boolean canFinish() {
	    getQuery();
		if (query == null || query.length() == 0) {
			// make sure we have the latest
			this.query = newMolPage.getQuery();
		}
		return (query != null && query.compareTo("") != 0);
	}

	public boolean performFinish() {
	    try {
            getContainer().run(true, true, new FinishRunnable());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException exception) {
            return false; // return, but keep wizard
        }
        return true;
	}

    class FinishRunnable implements IRunnableWithProgress {

        public FinishRunnable() {}

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            downloadGist(monitor);
        }

        private void downloadGist(IProgressMonitor monitor) throws InvocationTargetException {
            IJsConsoleManager js = Activator.getDefault().getJsConsoleManager();
            IPubChemManager pubchem = net.bioclipse.pubchem.Activator.getDefault()
                .getManager();

            try {
                System.out.println("query: " + query);
                List<Integer> searchResults = pubchem.search(query);
                js.print("Found hits: " + searchResults.size() + "\n");

                int max = Math.min(15, searchResults.size());
                for (int i=0; i<max; i++) {
                  int cid = searchResults.get(i);
                  String filename = "/Virtual/" + query.replace(" ", "") + "." + cid + ".xml";
                  js.print("downloading CID " + cid + "...\n");
                  pubchem.loadCompound(cid, filename);
                }
            } catch (IOException e) {
                js.print("Downloading the search results failed: " + e.getMessage());
                e.printStackTrace();
            } catch (BioclipseException e) {
                js.print("Downloading the search results failed: " + e.getMessage());
                e.printStackTrace();
            } catch (CoreException e) {
                js.print("Downloading the search results failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
