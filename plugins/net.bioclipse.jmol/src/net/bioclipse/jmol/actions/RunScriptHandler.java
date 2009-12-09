/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.jmol.actions;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.business.IJmolManager;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

public class RunScriptHandler extends AbstractHandler implements IHandler {

    private static final Logger logger 
        = Logger.getLogger(RunScriptHandler.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {

        String script 
            = event.getParameter("net.bioclipse.jmol.scriptParameter");
        IJmolManager manager = Activator.getDefault().getJmolManager();

        // If running non select script and nothing is selected, 
        // select all before the script and select none after
        if ( manager.selectionIsEmpty() 
             && !script.matches( "(?i:select).*") ) {
            script = "select all;" + script + ";select none;";
        }
        manager.run( script );

        return null;
    }
}
