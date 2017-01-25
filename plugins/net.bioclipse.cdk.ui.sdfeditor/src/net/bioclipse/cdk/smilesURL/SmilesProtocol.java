/*******************************************************************************
 * Copyright (c) 2013  Klas Jšnsson <klas.joensson@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.smilesURL;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.business.IJavaCDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chart.ChartUtils;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;


public class SmilesProtocol extends AbstractURLStreamHandlerService implements
        BundleActivator {
    
    @Override
    public void start( BundleContext context ) throws Exception {
        Hashtable<String, String[]> properties = 
                new Hashtable<String, String[]>();
        properties.put( URLConstants.URL_HANDLER_PROTOCOL,
                        new String[] { "smiles" } );
        context.registerService( URLStreamHandlerService.class.getName(), this,
                                 properties );
    }

    @Override
    public void stop( BundleContext context ) throws Exception {    }

    @Override
    public URLConnection openConnection( URL url ) throws IOException {
        return new SmilesURL( url );
    }

    public static ICDKMolecule smilesToMolecule(String smiles) 
            throws BioclipseException {
        ICDKManager cdk = getManager( IJavaCDKManager.class );
        ICDKMolecule mol = cdk.fromSMILES( smiles );
        if (!cdk.has2d( mol ))
            try {
                mol = cdk.generate2dCoordinates( mol );
            } catch ( Exception e ) {
               System.out.println("Could not generate 2D coordinates for the " +
               		"tooltip molecule:"+e.getMessage());
            }
        
        return mol;
    }
       
    public static <T extends IBioclipseManager> T getManager(Class<T> clazz) {
        Bundle bundle = FrameworkUtil.getBundle( ChartUtils.class );
        BundleContext context = bundle.getBundleContext();
        ServiceReference<T> sRef = context.getServiceReference( clazz );
        if(sRef!=null) {
            T manager = context.getService( sRef );
            if(manager!=null){
                return manager;
            }

        }
        throw new IllegalStateException("Could not get the "+clazz.getName());
    }
}
