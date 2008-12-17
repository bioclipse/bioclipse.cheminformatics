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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "net.bioclipse.cdk.debug";
    // The shared instance
    private static Activator plugin;
    // tracks the example manager
    private ServiceTracker finderTracker;
    /**
     * The constructor
     */
    public Activator() {
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        finderTracker = new ServiceTracker( context, 
                                            ICDKDebugManager.class.getName(), 
                                            null );
        finderTracker.open();
    }
    /**
     * Returns a reference to the example manager object
     * 
     * @return the exampleManager
     */
    public ICDKDebugManager getManager() {
        ICDKDebugManager exampleManager = null;
        try {
            exampleManager = (ICDKDebugManager) finderTracker.waitForService(1000*30);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Could not get example manager", e);
        }
        if(exampleManager == null) {
            throw new IllegalStateException("Could not get example manager");
        }
        return exampleManager;
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
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
}
