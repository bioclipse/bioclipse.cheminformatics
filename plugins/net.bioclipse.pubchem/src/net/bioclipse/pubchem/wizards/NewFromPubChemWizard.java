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
import java.util.List;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.pubchem.business.PubChemManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewFromPubChemWizard extends BasicNewResourceWizard {
	
	private NewFromPubChemWizardPage newMolPage;
	private static Logger logger = Logger.getLogger("my.package.classname");

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

	private IFolder getResultsFolder(String results, IProgressMonitor monitor)
	  throws CoreException {
	    IProject project = net.bioclipse.core.Activator.getVirtualProject();
	    int counter = 1;
	    String resultsBase = results;
	    while (project.findMember(results) != null) {
	        results = resultsBase + counter;
	    }
	    IFolder folder = project.getFolder(results);
	    return folder;
	}

    public boolean performFinish() {
	    Job job = new Job("Searching PubChem...") {
	        protected IStatus run(IProgressMonitor monitor) {
	            IFolder resultsFolder;
	            try {
                    resultsFolder = getResultsFolder(
                        query.replace(' ', '_'),
                        monitor
                    );
                    resultsFolder.create(true, true, monitor);
                } catch ( CoreException e1 ) {
                    LogUtils.handleException(e1, logger,
                         "Could not set up a results folder: " +
                         e1.getMessage()
                    );
                    e1.printStackTrace();
                    monitor.done();
                    return Status.CANCEL_STATUS;
                }

	            PubChemManager pubchem = new PubChemManager();

	            try {
	                List<Integer> searchResults =
	                    pubchem.search(query, monitor);

//                    new UIManager().revealAndSelect(resultsFolder);
	                monitor.subTask("Downloading search results...");
	                int max = Math.min(15, searchResults.size());
	                for (int i=0; i<max && !monitor.isCanceled(); i++) {
	                    int cid = searchResults.get(i);
	                    String filename =
	                        resultsFolder.getFullPath().toString() + "/" +
	                        "cid" + cid + ".xml";
	                    pubchem.loadCompound(
	                        cid,
	                        ResourcePathTransformer.getInstance()
	                            .transform(filename),
	                        monitor
	                    );
	                    monitor.worked(1);
	                }
	            } catch (IOException e) {
                    LogUtils.handleException(e, logger,
                        "Downloading the search results failed: " +
                        e.getMessage()
                    );
                    e.printStackTrace();
                    monitor.done();
                    return Status.CANCEL_STATUS;
	            } catch (BioclipseException e) {
                    LogUtils.handleException(e, logger,
                        "Downloading the search results failed: " +
                        e.getMessage()
                    );
	                e.printStackTrace();
                    monitor.done();
                    return Status.CANCEL_STATUS;
	            } catch (CoreException e) {
                    LogUtils.handleException(e, logger,
                        "Downloading the search results failed: " +
                        e.getMessage()
                    );
	                e.printStackTrace();
                    monitor.done();
                    return Status.CANCEL_STATUS;
	            }
                monitor.done();
	            return Status.OK_STATUS;
	        }
	    };
	    job.setPriority(Job.SHORT);
	    job.schedule();
        return true;
	}

}
