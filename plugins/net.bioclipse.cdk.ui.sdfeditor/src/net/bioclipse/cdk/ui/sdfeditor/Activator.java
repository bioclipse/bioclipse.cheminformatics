/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/

package net.bioclipse.cdk.ui.sdfeditor;

import net.bioclipse.cdk.ui.sdfeditor.business.IJSMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.bioclipse.cdk.ui.sdfeditor";

	// The shared instance
	private static Activator plugin;

	private ServiceTracker finderTracker;
  private ServiceTracker jsFinderTracker;

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
                                        IMoleculeTableManager.class.getName(),
                                        null );
		finderTracker.open();
		jsFinderTracker = new ServiceTracker( context,
                                          IJSMoleculeTableManager.class.getName(),
                                          null );
		jsFinderTracker.open();
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

	public IMoleculeTableManager getMoleculeTableManager() {
	    IMoleculeTableManager molTableManager;

	     try {
	       molTableManager
	           = (IMoleculeTableManager) finderTracker.waitForService(1000*30);
	     }
	     catch (InterruptedException e) {
	       throw
	         new IllegalStateException("Could not get molTable manager", e);
	     }
	     if (molTableManager == null) {
	       throw new IllegalStateException("Could not get molTable manager");
	     }
	     return molTableManager;
	   }

	public IMoleculeTableManager getJSMoleculeTableManager() {
	     IJSMoleculeTableManager jsMolTableManager;

	     try {
	       jsMolTableManager
	           = (IJSMoleculeTableManager) jsFinderTracker.waitForService(1000*30);
	     }
	     catch (InterruptedException e) {
	       throw
	         new IllegalStateException("Could not get js molTable manager", e);
	     }
	     if (jsMolTableManager == null) {
	       throw new IllegalStateException("Could not get js molTable manager");
	     }
	     return jsMolTableManager;
	   }
}
