/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cml.managers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import net.bioclipse.cml.managers.Activator;

/**
 * Factory for the exampleManager 
 * 
 * @author jonalv
 *
 */
public class ValidateCMLManagerFactory implements IExecutableExtension, 
                                              IExecutableExtensionFactory {

    private Object exampleManager;
    
    public void setInitializationData( IConfigurationElement config,
                                       String propertyName, 
                                       Object data) throws CoreException {
    
        exampleManager = Activator.getDefault().getValidateCMLManager();
    }
    
    public Object create() throws CoreException {
        return exampleManager;
    }
}