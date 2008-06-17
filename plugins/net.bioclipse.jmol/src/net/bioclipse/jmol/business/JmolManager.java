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
import net.bioclipse.jmol.views.JmolView;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

public class JmolManager implements IJmolManager{

    private JmolEditor jmolEditor;
    private JmolView jmolView;

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
        JmolView view=findActiveJmolView();
        if (view!=null){
            view.runScript(script);
        }

    }


    public void load(String path){
        
        System.out.println("jmol load called with input: " + path);
        System.out.println("Not implemented");
        
    }

    /**
     * @return Active editor or null if not instance of JmolEditor
     */
    private JmolEditor findActiveJmolEditor() {

        final Display display = PlatformUI.getWorkbench().getDisplay();
        setActiveJmolEditor(null);
        display.syncExec(new Runnable() {
            public void run() {
                IEditorPart activeEditor=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

                if (activeEditor instanceof JmolEditor) {
                    setActiveJmolEditor((JmolEditor)activeEditor);
                }
            }
        });

        return jmolEditor;
    }
    
    /**
     * @return Active editor or null if not instance of JmolEditor
     */
    private JmolView findActiveJmolView() {

        final Display display = PlatformUI.getWorkbench().getDisplay();
        setActiveJmolView( null );
        display.syncExec(new Runnable() {
            public void run() {
                IViewReference[] views=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();

                for (IViewReference viewref: views){
                    if (viewref.getId().equals( JmolView.ID )) {
                        IViewPart part=viewref.getView( false );
                        if (part!=null){
                            setActiveJmolView((JmolView)part);
                        }
                    }
                }
                
            }
        });

        return jmolView;
    }


    protected void setActiveJmolView( JmolView view ) {
        jmolView=view;
    }

    protected void setActiveJmolEditor( JmolEditor activeEditor ) {
        jmolEditor=activeEditor;
    }
    
}
