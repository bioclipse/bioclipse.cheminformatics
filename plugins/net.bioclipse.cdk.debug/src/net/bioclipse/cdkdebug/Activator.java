/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 ******************************************************************************/
package net.bioclipse.cdkdebug;

import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.cdkdebug.business.IJavaCDKDebugManager;
import net.bioclipse.cdkdebug.business.IJavaScriptCDKDebugManager;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator extends AbstractUIPlugin {

    private static final Logger logger = Logger.getLogger(Activator.class);

    // The plug-in ID
    public static final String PLUGIN_ID = "net.bioclipse.cdk.debug";

    // The shared instance
    private static Activator plugin;
    
    private ServiceTracker javaFinderTracker;
    private ServiceTracker jsFinderTracker;
    
    public Activator() {}

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        javaFinderTracker = new ServiceTracker(
                context,
                IJavaCDKDebugManager.class.getName(), 
                null
            );
            javaFinderTracker.open();
            jsFinderTracker = new ServiceTracker(
                context,
                IJavaScriptCDKDebugManager.class.getName(), 
                null
            );
            jsFinderTracker.open();
    }

    public ICDKDebugManager getJavaManager() {
        ICDKDebugManager manager = null;
        try {
            manager = (ICDKDebugManager)javaFinderTracker.waitForService(1000*30);
        } catch (InterruptedException e) {
        	LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the CDKDebug manager.");
        }
        return manager;
    }

    public ICDKDebugManager getJavaScriptManager() {
        ICDKDebugManager manager = null;
        try {
            manager = (ICDKDebugManager)jsFinderTracker.waitForService(1000*30);
        } catch (InterruptedException e) {
        	LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the CDKDebug manager.");
        }
        return manager;
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
}
