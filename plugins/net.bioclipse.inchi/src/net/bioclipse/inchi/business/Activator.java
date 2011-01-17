/*******************************************************************************
 * Copyright (c) 2008  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contact: http://www.bioclipse.net/    
 ******************************************************************************/
package net.bioclipse.inchi.business;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author jonalv
 */
public class Activator extends Plugin {

    private static final Logger logger = Logger.getLogger(Activator.class);
    public static final String PLUGIN_ID = "net.bioclipse.inchi";

    private static Activator myself;
    
    private ServiceTracker javaFinderTracker;
    private ServiceTracker jsFinderTracker;
    
    public Activator() {}

    public void start(BundleContext context) throws Exception {
        super.start(context);
        myself = this;
        
        javaFinderTracker = new ServiceTracker(
            context,
            IJavaInChIManager.class.getName(), 
            null
        );
        javaFinderTracker.open();
        jsFinderTracker = new ServiceTracker(
            context,
            IJavaScriptInChIManager.class.getName(), 
            null
        );
        jsFinderTracker.open();
    }

    public IInChIManager getJavaInChIManager() {
        IInChIManager inchiManager = null;
        try {
            inchiManager = (IInChIManager)
                javaFinderTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            logger.log( Level.DEBUG, "Caught exception", e );
        }
        if(inchiManager == null) {
            throw new IllegalStateException("Could not get java inchi manager");
        }
        return inchiManager;
    }

    public IInChIManager getJavaScriptInChIManager() {
        IInChIManager inchiManager = null;
        try {
            inchiManager = (IInChIManager)
                jsFinderTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            logger.log( Level.DEBUG, "Caught exception", e );
        }
        if(inchiManager == null) {
            throw new IllegalStateException("Could not get JS inchi manager");
        }
        return inchiManager;
    }

    public void stop(BundleContext context) throws Exception {
        myself = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return myself;
    }
}
