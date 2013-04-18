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

import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.editors.IJmolEditor;
import net.bioclipse.jmol.editors.JmolEditor;
import net.bioclipse.jmol.model.IJmolMolecule;
import net.bioclipse.jmol.views.JmolConsoleView;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class JmolManager implements IBioclipseManager {

    private IJmolEditor jmolEditor;
    private ICDKManager cdk = net.bioclipse.cdk.business.Activator
            .getDefault().getJavaCDKManager();
    
    public String getManagerName() {
        return "jmol";
    }

    public void run(String script, boolean reportErrorToJSConsole) {

        if (script == null || script.length() <= 0)
            throw new IllegalArgumentException(
                "Script parameter cannot be empty" );

        //Run script in editor
        IJmolEditor editor = findActiveJmolEditor();
        if (editor == null) {
            throw new IllegalStateException(
                "Could not find any Jmol editor to run the script in" );
        }
        editor.runScript(script, reportErrorToJSConsole);
    }
    
    public void load(IFile file) throws CoreException {
        if (jmolEditor == null) {
            throw new CoreException( new Status(SWT.ERROR, Activator.PLUGIN_ID,
                "Cannot load a file into active Jmol editor.") );
        }
        jmolEditor.load(file);
    }

    public void load(IMolecule molecule) throws BioclipseException {
        if (!cdk.has2d( molecule ) || !cdk.has3d( molecule )){
            try {
                cdk.generate2dCoordinates( molecule );
            } catch ( Exception e ) {
               throw new BioclipseException( "Can't load molecule: "+e.getMessage() );
            }
        }
        net.bioclipse.ui.business.Activator.
         getDefault().getUIManager().
          open( molecule, "net.bioclipse.jmol.editors.JmolEditor");
    }

    /**
     * @return Active editor or null if not instance of JmolEditor
     */
    private IJmolEditor findActiveJmolEditor() {

        final Display display = PlatformUI.getWorkbench().getDisplay();
        setActiveJmolEditor(null);
        display.syncExec( new Runnable() {
            public void run() {
                IEditorPart activeEditor 
                    = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow()
                                .getActivePage()
                                .getActiveEditor();
                
                if (activeEditor instanceof IJmolEditor) {
                    setActiveJmolEditor( (IJmolEditor)activeEditor );
                } else {
                	System.out.println("Found a non-JmolEditor: " + activeEditor.getClass().getName());
                }
            }
        });
        return jmolEditor;
    }

    private JmolConsoleView getJmolConsoleView() {
        try {
            return (JmolConsoleView)
                PlatformUI.getWorkbench()
                          .getActiveWorkbenchWindow()
                          .getActivePage()
                          .showView("net.bioclipse.jmol.views.JmolConsoleView");
        } catch ( PartInitException e ) {
            throw new RuntimeException(
                "The Jmol console could not be opened"
            );
        }
    }

    protected void setActiveJmolEditor( IJmolEditor activeEditor ) {
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

    public void print( final String message ) {
        Display.getDefault().asyncExec( new Runnable() {
            public void run() { getJmolConsoleView().printMessage( message ); }
        } );
    }
    
    public boolean selectionIsEmpty() {
    	IJmolEditor editor = findActiveJmolEditor();
        return editor.getSelection() == null 
            || editor.getSelection().isEmpty();
    }

    public void run( String script ) {
        run( script, true );
    }

    public void select( String selection ) {
        run( "select " + selection );
    }

    public void color( String color ) {
        run( "color " + color );
    }

    public void append( IFile file ) {
       findActiveJmolEditor().append(file);
    }

    public List<IJmolMolecule> getMolecules() {
        return findActiveJmolEditor().getJmolMolecules();
    }

    public void append(IMolecule molecule) {
        IJmolEditor editor = findActiveJmolEditor();
        if (editor != null && editor instanceof JmolEditor) {
          ((JmolEditor)editor).append(molecule);
        }
    }

}
