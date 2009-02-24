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
package net.bioclipse.jmol.business;

import net.bioclipse.core.Activator;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.jmol.editors.JmolEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class JmolManager implements IJmolManager {

    private JmolEditor jmolEditor;

    public String getNamespace() {
        return "jmol";
    }

    public void run(String script) {

        if (script == null || script.length() <= 0)
            throw new IllegalArgumentException(
                "Script parameter cannot be empty" );

        //Run script in editor
        JmolEditor editor = findActiveJmolEditor();
        if (editor != null){
            editor.runScript(script);
        }

    }

    public void load(String path) throws CoreException {
        load(ResourcePathTransformer.getInstance().transform(path));
    }
    
    public void load(IFile file) throws CoreException {
        if (jmolEditor == null) {
            throw new CoreException(new Status(SWT.ERROR,Activator.PLUGIN_ID,
            "Cannot load a file into active Jmol editor."));
        }
    	jmolEditor.load(file);
    }
    
    /**
     * @return Active editor or null if not instance of JmolEditor
     */
    private JmolEditor findActiveJmolEditor() {

        final Display display = PlatformUI.getWorkbench().getDisplay();
        setActiveJmolEditor(null);
        display.syncExec( new Runnable() {
            public void run() {
                IEditorPart activeEditor 
                    = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow()
                                .getActivePage()
                                .getActiveEditor();

                if (activeEditor instanceof JmolEditor) {
                    setActiveJmolEditor( (JmolEditor)activeEditor );
                }
            }
        });

        return jmolEditor;
    }
    

    protected void setActiveJmolEditor( JmolEditor activeEditor ) {
        jmolEditor = activeEditor;
    }

    public void spinOff() {
        run( "spin off" );
    }

    public void spinOn() {
        run( "spin" );
    }

    public void minimize() {
        run("minimize");
    }

	public void snapshot(String filepath) {
		IFile file = ResourcePathTransformer.getInstance().transform(filepath);
		this.findActiveJmolEditor().snapshot(file);
	}
}
