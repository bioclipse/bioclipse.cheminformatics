/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egon.willighagen@gmail.com> & Ola Spjuth
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemspider;

import net.bioclipse.chemspider.business.IChemspiderManager;
import net.bioclipse.chemspider.business.IJavaChemspiderManager;
import net.bioclipse.chemspider.business.IJavaScriptChemspiderManager;

import org.apache.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    private static final Logger logger = Logger.getLogger(Activator.class);

	public static final String PREF_SECURITY_TOKEN = "chemspider.security.preference";
	public static final String PREF_SERVER_ENDPOINT = "chemspider.server.endpoint";

    // The shared instance
    private static Activator plugin;

    // Trackers for getting the managers
    private ServiceTracker javaFinderTracker;
    private ServiceTracker jsFinderTracker;

    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        //Set default prefs
        Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_SECURITY_TOKEN, "");

        Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_SERVER_ENDPOINT, 
        												"http://cs.dev.rsc-us.org/Search.asmx");

        javaFinderTracker
            = new ServiceTracker( context,
                                  IJavaChemspiderManager.class.getName(),
                                  null );

        javaFinderTracker.open();
        jsFinderTracker
            = new ServiceTracker( context,
                                  IJavaScriptChemspiderManager.class.getName(),
                                  null );

        jsFinderTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public IChemspiderManager getJavaChemspiderManager() {
        IChemspiderManager manager = null;
        try {
            manager = (IChemspiderManager)
                      javaFinderTracker.waitForService(1000*10);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(
                          "Could not get the Java ChemspiderManager",
                          e );
        }
        if (manager == null) {
            throw new IllegalStateException(
                          "Could not get the Java ChemspiderManager");
        }
        return manager;
    }

    public IJavaScriptChemspiderManager getJavaScriptChemspiderManager() {
        IJavaScriptChemspiderManager manager = null;
        try {
            manager = (IJavaScriptChemspiderManager)
                      jsFinderTracker.waitForService(2000*10);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(
                          "Could not get the JavaScript ChemspiderManager",
                          e );
        }
        if (manager == null) {
            throw new IllegalStateException(
                          "Could not get the JavaScript ChemspiderManager");
        }
        return manager;
    }
}
