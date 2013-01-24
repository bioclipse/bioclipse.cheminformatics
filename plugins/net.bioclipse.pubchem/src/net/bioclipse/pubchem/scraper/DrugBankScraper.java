/* *****************************************************************************
 * Copyright (c) 2010  Ola Spjuth <ospjuth@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.pubchem.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.browser.scraper.IBrowserScraper;
import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.SMILESMolecule;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * @author valyo
 *
 */
public class DrugBankScraper implements IBrowserScraper {
    
    
    public DrugBankScraper() {
    }

    /**
     * Check base URL
     */
    public boolean matchesURL( String url ) {

        if (url.startsWith("http://www.drugbank.ca/")){
            return true;
        }
        return false;
    }

    /**
     * Accept all content for now
     */
    public boolean matchesContent( String content ) {
        return true;
    }

    /**
     * Deliver different URLs to different extractors
     */
    public List<? extends IBioObject> extractObjects( String url, String content ) {

        if (url.startsWith("http://www.drugbank.ca/drugs/")){
            System.out.println("Processing DrugBank page");
            return extractDBMolecules(content);
        }else {
        	System.out.println("Not a PubChem page.");
        	return null;
        }

    }

    private List<IMolecule> extractDBMolecules( String content ) {
        Pattern slp = Pattern.compile("DB(\\d+)");
//        Set<Integer> cids=new HashSet<Integer>();
        Matcher slm = slp.matcher(content);
        String dban = null;
        while (slm.find()) {
            dban=slm.group( 1 );
        }
        String smiles = null;
		try {
			smiles = downloadDBSMILES(dban);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BioclipseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //Get SMILESMolecule and convert it to ICDKmolecule
        List<IMolecule> mols=new ArrayList<IMolecule>();
            IMolecule mol = new SMILESMolecule( smiles );
            ICDKMolecule cdkmol;
			try {
				cdkmol = Activator.getDefault().getJavaCDKManager().asCDKMolecule( (IMolecule)mol );
				mols.add(cdkmol);
			} catch (BioclipseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        System.out.println("Found no DrugBank mols: " + mols.size());

        return mols;
    }
    
  //fetch SMILES method starts here - TODO refactor
    private String downloadDBSMILES(String dban1)
    throws IOException, BioclipseException, CoreException {
    	IProgressMonitor monitor = new NullProgressMonitor();
    	
    	monitor.subTask("Downloading SMILES " + dban1);
    	StringBuffer fileContent = new StringBuffer(); 
    	try {                
    		String efetch = "http://www.drugbank.ca/drugs/DB" +
    		dban1 + ".smiles";
    		monitor.subTask("Downloading from " + efetch);
    		URL rawURL = new URL(efetch);
    		URLConnection rawConn = rawURL.openConnection();
    		
    		BufferedReader reader = new BufferedReader(
    				new InputStreamReader(rawConn.getInputStream())
    		);
    		String line = reader.readLine();
    		while (line != null && !monitor.isCanceled()) {
    			fileContent.append(line);
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
    	System.out.println(fileContent.toString());
    	return fileContent.toString();
    }

}
