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
package net.bioclipse.cdk.ui.sdfeditor.business;

import net.bioclipse.cdk.ui.sdfeditor.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class MoleculeTableManagerFactory implements IExecutableExtension,
        IExecutableExtensionFactory {

    private Object jsConsoleManager;

    public void setInitializationData( IConfigurationElement config,
                                       String propertyName, Object data )
                                                                         throws CoreException {

        jsConsoleManager = Activator.getDefault().getJSMoleculeTableManager();

    }

    public Object create() throws CoreException {
        return jsConsoleManager;
    }

}
