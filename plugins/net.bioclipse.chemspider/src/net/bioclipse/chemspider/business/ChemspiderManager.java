/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemspider.business;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.core.api.managers.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ChemspiderManager implements IBioclipseManager {

	private static final CDKManager cdk = new CDKManager();

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "chemspider";
    }

    public IFile loadCompound(int csid, IFile target, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
    	if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

    	monitor.beginTask(
    		"Downloading CSID " + csid + " from ChemSpider.", 2
    	);
    	URL url = new URL("http://www.chemspider.com/mol/" + csid);
    	BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				url.openConnection().getInputStream()
			)
		);
    	String line = reader.readLine();
    	StringBuffer molString = new StringBuffer(); 
		while (line != null) {
			molString.append(line).append('\n');
			line = reader.readLine();
		}

		monitor.worked(1);
		if (target.exists()) {
            target.setContents(
                new ByteArrayInputStream(molString.toString().getBytes()),
                true, false, null
            );
        } else {
            target.create(
                new ByteArrayInputStream(molString.toString().getBytes()),
                false, null
            );
        }
		monitor.worked(1);

        monitor.done();
        return target;
    }

    public List<Integer> resolve(String inchiKey, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
    	if (monitor == null) {
    		monitor = new NullProgressMonitor();
    	}

    	Set<Integer> results = new HashSet<Integer>();
    	monitor.beginTask(
    		"Resolving the InChIKey '" + inchiKey + "' on ChemSpider...", 1
    	);

    	URL url = new URL("http://www.chemspider.com/InChIKey/" + inchiKey);
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				url.openConnection().getInputStream()
			)
		);
		String line = reader.readLine();
		Pattern pattern = Pattern.compile("Chemical-Structure.(\\d*).html");
		String csid = "";
		while (line != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				csid = matcher.group(1);
				results.add(Integer.valueOf(csid));
			}
			line = reader.readLine();
		}

		List<Integer> uniqueResults = new ArrayList<Integer>();
		uniqueResults.addAll(results);
    	return uniqueResults;
    }

    public String downloadAsString(Integer csid, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
    	if (monitor == null) monitor = new NullProgressMonitor();

    	monitor.subTask("Downloading CSID " + csid);
    	StringBuffer fileContent = new StringBuffer(); 
    	try {                
    		monitor.beginTask(
    	    		"Downloading CSID " + csid + " from ChemSpider.", 2
    	    	);
    	    	URL url = new URL("http://www.chemspider.com/mol/" + csid);
    	    	BufferedReader reader = new BufferedReader(
    				new InputStreamReader(
    					url.openConnection().getInputStream()
    				)
    			);
    	    	String line = reader.readLine();
    			while (line != null) {
    				fileContent.append(line).append('\n');
    				line = reader.readLine();
    			}
    			reader.close();
    			monitor.worked(1);
    	} catch (PatternSyntaxException exception) {
    		exception.printStackTrace();
    		throw new BioclipseException("Invalid Pattern.", exception);
    	} catch (MalformedURLException exception) {
    		exception.printStackTrace();
    		throw new BioclipseException("Invalid URL.", exception);
    	}
    	return fileContent.toString();
    }

    public IMolecule download(Integer csid, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
    	monitor.beginTask("Downloading Compound from ChemSpider...", 2);
    	String molstring = downloadAsString(csid, monitor);
    	if (monitor.isCanceled()) return null;

    	ICDKMolecule molecule = cdk.fromString(molstring);
    	monitor.worked(1);
    	return molecule;
    }

}
