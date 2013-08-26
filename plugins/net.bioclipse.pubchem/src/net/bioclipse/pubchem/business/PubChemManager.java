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
package net.bioclipse.pubchem.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.RecordableList;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PubChemManager implements IBioclipseManager {

    private final static String EUTILS_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/eutils";
    private final static String PUBCHEM_URL_BASE = "http://pubchem.ncbi.nlm.nih.gov/";
    private final static String PUBCHEMRDF_URL_BASE = "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/";

    private final static String TOOL = "bioclipse.net";
    
    private static final CDKManager cdk = new CDKManager();
    private static final RDFManager rdf = new RDFManager();

    public String getManagerName() {
        return "pubchem";
    }

    public IFile loadCompound(int cid, IFile target, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
        return loadCompoundAny(cid, target, monitor, "DisplayXML");
    }

    public IFile loadCompound3d(int cid, IFile target, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        return loadCompoundAny(cid, target, monitor, "3DDisplaySDF");
    }

    private IFile loadCompoundAny(int cid, IFile target,
            IProgressMonitor monitor, String type)
            throws IOException, BioclipseException, CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (target == null) {
            throw new BioclipseException("Cannot save to a NULL file.");
        }

        String molString = downloadAsString(cid, type, monitor);
        
        if (target.exists()) {
            target.setContents(
                new ByteArrayInputStream(molString.getBytes()),
                true, false, null
            );
        } else {
            target.create(
                new ByteArrayInputStream(molString.getBytes()),
                false, null
            );
        }
        
        monitor.done();
        return target;
    }

    public IFile loadCompoundRDF(int cid, IFile target, IProgressMonitor monitor)
    		throws IOException, BioclipseException, CoreException {        
        if (target == null) {
            throw new BioclipseException("Cannot save to a NULL file.");
        }
        if (monitor == null) monitor = new NullProgressMonitor();

        String downloadURI = PUBCHEMRDF_URL_BASE + "CID" + cid;
    	String rdfString = downloadAsString(downloadURI, "application/rdf+xml", monitor);        

    	if (target.exists()) {
            target.setContents(
                new ByteArrayInputStream(rdfString.getBytes()),
                true, false, null
            );
        } else {
            target.create(
                new ByteArrayInputStream(rdfString.getBytes()),
                false, null
            );
        }
        
        monitor.done();
        return target;
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

    public List<Integer> search(String query, IProgressMonitor monitor)
            throws IOException, BioclipseException, CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        List<Integer> results = new ArrayList<Integer>();
        monitor.beginTask("Searching PubChem for '" + query + "'...", 1);

        String db = "pccompound";
        query = replaceSpaces(query);

        String esearch = EUTILS_URL_BASE + "/esearch.fcgi?" +
            "db=" + db + "&retmax=50&usehistory=y&tool=" + TOOL + "&term=" + query;

        System.out.println("URL: " + esearch);
        URL queryURL = new URL(esearch);
        URLConnection connection = queryURL.openConnection();

        monitor.subTask("Processing search results...");
        Builder parser = new Builder();
        Document doc;
        try {
            doc = parser.build(connection.getInputStream());
            Nodes countNodes = doc.query("/eSearchResult/Count");
            if (countNodes.size() > 0) {
                System.out.println(countNodes.get(0).toString());
            } else {
                monitor.done();
                return results;
            }

            Nodes cidNodes = doc.query("/eSearchResult/IdList/Id");

            int max = cidNodes.size();
            if (max > 15) max = 15;

            for (int cidCount=0; cidCount<max; cidCount++) {
                String cidStr = cidNodes.get(cidCount).getValue();
                int cid = Integer.parseInt(cidStr);
                results.add(cid);
            }
        } catch (ValidityException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            e.printStackTrace();
        } finally {
            monitor.done();
        }

        return results;
    }

    public IMolecule download(Integer cid, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        monitor.beginTask("Downloading Compound from PubChem...", 2);
        String molstring = downloadAsString(cid, monitor);
        if (monitor.isCanceled()) return null;
        
        ICDKMolecule molecule = cdk.fromString(molstring);
        monitor.worked(1);
        return molecule;
    }

    public IMolecule download3d(Integer cid, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException{
        monitor.beginTask("Downloading Compound 3D from PubChem...", 2);
        String molstring = download3dAsString(cid, monitor);
        if (monitor.isCanceled()) return null;

        // convert the returned SD file into a MDL molfile by stripping the
        // $$$$ and beyond
        molstring = molstring.substring(0, molstring.indexOf("$$$$"));

        ICDKMolecule molecule = cdk.fromString(molstring);
        monitor.worked(1);
        return molecule;
    }

    private String downloadAsString(Integer cid, String type,
                                   IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        if (monitor == null) monitor = new NullProgressMonitor();
        
        monitor.subTask("Downloading CID " + cid);
    	String efetch = PUBCHEM_URL_BASE + "summary/summary.cgi?cid=" +
                cid + "&disopt=" + type;
    	return downloadAsString(efetch, null, monitor);
    }
    
    private String downloadAsString(String URL, String accepts, IProgressMonitor monitor)
            throws IOException, BioclipseException, CoreException {
        if (monitor == null) monitor = new NullProgressMonitor();
        
        String fileContent = ""; 
        try {                
            monitor.subTask("Downloading from " + URL);
            HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(URL);
			if (accepts != null) {
				method.setRequestHeader("Accept", accepts);
				method.setRequestHeader("Content-Type", accepts);
			}
			client.executeMethod(method);

			InputStream responseStream = method.getResponseBodyAsStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = responseStream.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
			buffer.flush();
			responseStream.close();
			method.releaseConnection();
			fileContent = new String(buffer.toByteArray());
            monitor.worked(1);
        } catch (PatternSyntaxException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid Pattern.", exception);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid URL.", exception);
        }
        return fileContent;
    }

    public String downloadAsString(Integer cid, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        return downloadAsString(cid, "DisplayXML", monitor);
    }

    public String download3dAsString(Integer cid, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException{
        return downloadAsString(cid, "3DDisplaySDF", monitor);
    }

    public List<IMolecule> download(List<Integer> cids,
                                    IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask(
            "Downloading Compounds from PubChem...",
            cids.size()
        );
        List<IMolecule> results = new RecordableList<IMolecule>();
        for (Integer cid : cids) {
            if (monitor.isCanceled()) return null;
            results.add(download(cid, monitor));
            monitor.worked(1);
        }
        return results;
    }

    public List<IMolecule> download3d(List<Integer> cids,
                                      IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask(
            "Downloading Compounds 3D from PubChem...",
            cids.size()
        );
        List<IMolecule> results = new RecordableList<IMolecule>();
        for (Integer cid : cids) {
            if (monitor.isCanceled()) return null;
            results.add(download3d(cid, monitor));
            monitor.worked(1);
        }
        return results;
    }

    public IRDFStore downloadRDF(Integer cid, IRDFStore store,
    		IProgressMonitor monitor)
    				throws IOException, BioclipseException, CoreException {
    	if (monitor == null) monitor = new NullProgressMonitor();

    	String downloadURI = PUBCHEMRDF_URL_BASE + "CID" + cid;
    	String rdfContent = downloadAsString(downloadURI, "application/rdf+xml", monitor);
    	rdf.importFromString(store, rdfContent, "RDF/XML", monitor);
    	return store;
    }
}
