/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.scripting;
import net.bioclipse.jmol.editors.JmolEditor;
import net.bioclipse.scripting.INamespaceProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
/**
 * 
 * Tools for simplified Jmol scripting
 * 
 * @author ola
 *
 */
public class JmolScriptingTools implements INamespaceProvider{
    /**
     * Constructor
     */
    public JmolScriptingTools() {
    }
    /**
     * Pipe the argument as string to Jmol as a script command
     * @param command
     */
    public void run(String command){
        //Basic checks
        if (command==null) return;
        else if ("".equals(command)) return;
        //Find active editor
        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (part instanceof JmolEditor) {
            JmolEditor jedit = (JmolEditor) part;
            jedit.runScript(command);
        }
                IViewReference[] views=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
    }
}