 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.business;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.log4j.Logger;
import net.bioclipse.core.util.LogUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    private static final Logger logger = Logger.getLogger(Activator.class);
    
	// The plug-in ID
	public static final String PLUGIN_ID = "net.bioclipse.cdk.business";

	// The shared instance
	private static Activator plugin;

	//For Spring
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
				ICDKManager.class.getName(), 
				null );
		
		finderTracker.open();
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

	public ICDKManager getCDKManager() {
		ICDKManager manager = null;
		try {
			manager = (ICDKManager) finderTracker.waitForService(1000*10);
		} catch (InterruptedException e) {
			LogUtils.debugTrace(logger, e);
		}
		if(manager == null) {
			throw new IllegalStateException("Could not get the CDK manager");
		}
		return manager;
	}

}
