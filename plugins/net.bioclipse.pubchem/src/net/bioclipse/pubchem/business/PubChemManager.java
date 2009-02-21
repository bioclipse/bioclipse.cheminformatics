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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PubChemManager implements IPubChemManager {

    private final static String EUTILS_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/eutils";
    private final static String PUBCHEM_URL_BASE = "http://pubchem.ncbi.nlm.nih.gov/";

    private final static String TOOL = "bioclipse.net";

    public String getNamespace() {
        return "pubchem";
    }

    public void loadCompound(int cid, String target) throws IOException,
            BioclipseException, CoreException {
        loadCompound(cid, ResourcePathTransformer.getInstance().transform(target), null);
    }

    public void loadCompound(int cid, IFile target, IProgressMonitor monitor)
    throws IOException, BioclipseException, CoreException {
        loadCompoundAny(cid, target, monitor, "DisplayXML");
    }

    public void loadCompound3d(int cid, String target) throws IOException,
        BioclipseException, CoreException {
        loadCompound3d(cid, ResourcePathTransformer.getInstance().transform(target), null);
    }

    public void loadCompound3d(int cid, IFile target, IProgressMonitor monitor)
        throws IOException, BioclipseException, CoreException {
        loadCompoundAny(cid, target, monitor, "3DDisplaySDF");
    }

    private void loadCompoundAny(int cid, IFile target,
            IProgressMonitor monitor, String type)
            throws IOException, BioclipseException, CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (target == null) {
            throw new BioclipseException("Cannot save to a NULL file.");
        }
        
        monitor.beginTask("Downloading CID " + cid, 2);

        try {                
            String efetch = PUBCHEM_URL_BASE + "summary/summary.cgi?cid=" +
                            cid + "&disopt=" + type;
            monitor.subTask("Downloading from " + efetch);
            URL rawURL = new URL(efetch);
            URLConnection rawConn = rawURL.openConnection();
            if (target.exists()) {
                target.setContents(rawConn.getInputStream(), true, false, null);
            } else {
                target.create(rawConn.getInputStream(), false, null);
            }

            monitor.worked(1);
        } catch (PatternSyntaxException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid Pattern.", exception);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid URL.", exception);
        }
        monitor.done();
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

    public List<Integer> search(String query) throws IOException,
            BioclipseException, CoreException {
        return search(query, null);
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

}
