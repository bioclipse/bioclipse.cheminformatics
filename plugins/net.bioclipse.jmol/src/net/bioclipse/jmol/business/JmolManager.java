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

import net.bioclipse.jmol.editors.JmolEditor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class JmolManager implements IJmolManager{

    public String getNamespace() {
        return "jmol";
    }

    public void run(String script){

        if (script==null || script.length()<=0)
            throw new IllegalArgumentException("Script parameter cannot be empty");

        //Run script in editor
        JmolEditor editor=findActiveJmolEditor();
        if (editor!=null){
            editor.runScript(script);
        }

        //Run script in view (if open)
        //TODO: Implement later if/when JmolView exists

    }


    public void load(String path){
        
        System.out.println("jmol load called with input: " + path);
        System.out.println("Not implemented");
        
    }

    /**
     * @return Active editor or null if not instance of JmolEditor
     */
    private JmolEditor findActiveJmolEditor() {

        IEditorPart activeEditor=PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        if (activeEditor instanceof JmolEditor) {
            return (JmolEditor) activeEditor;
        }

        return null;
    }
    
}
