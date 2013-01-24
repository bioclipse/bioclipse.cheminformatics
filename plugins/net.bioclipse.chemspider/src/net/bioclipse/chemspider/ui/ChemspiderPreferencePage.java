/*******************************************************************************
  * Copyright (c) 2011 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.chemspider.ui;

 import java.net.MalformedURLException;
import java.net.URL;

import net.bioclipse.chemspider.Activator;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Ola Spjuth, Klas Jšnsson
 */
public class ChemspiderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

    private static final Logger logger = Logger.getLogger(ChemspiderPreferencePage.class);
    
    public void init(IWorkbench workbench) {
        //Initialize the preference store we wish to use
        setPreferenceStore(Activator.getDefault().getPreferenceStore());

      }
    
    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
    	StringFieldEditor token = new StringFieldEditor(Activator.PREF_SECURITY_TOKEN, 
    					"Chemspider Security Token (get one at:", parent);
    	Link link = new Link(parent, SWT.UNDERLINE_LINK);
    	link.setText( "<a>http://www.chemspider.com/AboutServices.aspx</a>)" );
    	link.addMouseListener( new MouseAdapter() {
    	    @Override
    	    public void mouseDown(MouseEvent me) {
    	        try {
                    //  Open default external browser 
                    PlatformUI.getWorkbench().getBrowserSupport().
                    getExternalBrowser().
                    openURL(new URL("http://www.chemspider.com/AboutServices.aspx"));
                } 
                catch (PartInitException ex) {
                    logger.error( ex );
                    ex.printStackTrace();
                } 
                catch (MalformedURLException ex) {
                    logger.error( ex );
                    ex.printStackTrace();
                }
    	    }
        } );

    	StringFieldEditor endpoint = new StringFieldEditor(Activator.PREF_SERVER_ENDPOINT, 
				"Chemspider server endpoint", 
				getFieldEditorParent());

    	addField( token );
    	addField( endpoint );
    }
}
