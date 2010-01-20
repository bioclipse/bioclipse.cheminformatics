/*******************************************************************************
 * Copyright (c) 2009  Carl Masak <carl@masak.org>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.jmol.views;

import net.bioclipse.scripting.ui.views.ScriptingConsoleView;
import net.bioclipse.jmol.Activator;


public class JmolConsoleView extends ScriptingConsoleView {

    @Override
    protected String executeCommand( String command ) {
        Activator.getDefault().getJmolManager().run( command );
        return null;
    }

    protected void waitUntilCommandFinished() {

        // TODO Auto-generated method stub
        
    }

}
