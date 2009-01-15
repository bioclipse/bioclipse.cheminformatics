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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

/**
 * 
 * @author jonalv
 */
public class CDKManagerFactory implements IExecutableExtension, 
                                          IExecutableExtensionFactory {

    private Object cdkManager;
    
    public void setInitializationData( IConfigurationElement config,
                                       String propertyName, 
                                       Object data) throws CoreException {
        
        cdkManager = Activator.getDefault().getJSCDKManager();
        
        if (cdkManager == null ) {
            cdkManager = new Object();
        }
    }

    public Object create() throws CoreException {
        return cdkManager;
    }
}
