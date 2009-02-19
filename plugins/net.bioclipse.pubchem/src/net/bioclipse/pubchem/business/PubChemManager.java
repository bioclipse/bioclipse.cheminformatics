/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
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
import java.util.List;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PubChemManager implements IPubChemManager {

    private final static String PUBCHEM_URL_BASE = "http://pubchem.ncbi.nlm.nih.gov/";
    
    public String getNamespace() {
        return "pubchem";
    }

    public void loadCompound(int cid, String target) throws IOException,
            BioclipseException, CoreException {
        loadCompound(cid, ResourcePathTransformer.getInstance().transform(target), null);
    }

    public void loadCompound(int cid, IFile target, IProgressMonitor monitor)
            throws IOException, BioclipseException, CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (target == null) {
            throw new BioclipseException("Cannot save to a NULL file.");
        }
        
        monitor.beginTask("Downloading CID " + cid, 2);

        try {                
            String efetch = PUBCHEM_URL_BASE + "summary/summary.cgi?cid=" + cid + "&disopt=DisplayXML";
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

    public List<Integer> search(String query) throws IOException,
            BioclipseException, CoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Integer> search(String query, IProgressMonitor monitor)
            throws IOException, BioclipseException, CoreException {
        // TODO Auto-generated method stub
        return null;
    }

}
