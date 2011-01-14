/* Copyright (c) 2011  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/epl-v10.html/.
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opsin.ui;

import net.bioclipse.ui.BioclipseActivator;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author egonw
 *
 */
public class Activator extends BioclipseActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.bioclipse.opsin.ui";

    
    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
