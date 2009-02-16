/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 *               2009  Egon Willighagen <egonw@user.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.business;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import net.bioclipse.cdk.jchempaint.Activator;

/**
 * Factory for the {@link JChemPaintGlobalPropertiesManager}. 
 * 
 * @author jonalv
 */
public class JChemPaintGlobalPropertiesManagerFactory implements IExecutableExtension, 
                                              IExecutableExtensionFactory {

    private Object exampleManager;
    
    public void setInitializationData( IConfigurationElement config,
                                       String propertyName, 
                                       Object data) throws CoreException {
    
        exampleManager = Activator.getDefault().getJCPPropManager();
    }
    
    public Object create() throws CoreException {
        return exampleManager;
    }
}
