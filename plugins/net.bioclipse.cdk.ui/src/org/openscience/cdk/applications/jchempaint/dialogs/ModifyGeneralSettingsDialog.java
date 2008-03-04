/* $RCSfile: ModifyGeneralSettingsDialog.java,v $    
 * $Author: egonw $    
 * $Date: 2005/10/22 16:25:37 $    
 * $Revision: 1.3 $
 * 
 * Copyright (C) 2005  The JChemPaint project
 *
 * Contact: jchempaint-devel@lists.sf.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.applications.jchempaint.dialogs;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.applications.jchempaint.JCPPropertyHandler;

/**
 * @cdk.module jchempaint.application
 */
public class ModifyGeneralSettingsDialog extends Dialog {

    private Button okButton;
	private Shell shell;
	private Button applyButton;
	private Button cancelButton;
	private Button checkButton;
    
	/**
	 * Displays the Info Dialog for JChemPaint.
	 */
    public ModifyGeneralSettingsDialog(Shell parent, int style) {
    	super(parent, style);
    	this.setText("Modify General Settings Dialog");
    }
    public Object open() {
    	Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(getText());
		
		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		shell.setLayout(gridLayout);
 		
 		
 		checkButton = new Button(shell, SWT.CHECK);
 		checkButton.setText("Ask for IO settings");
 		GridData gridData = new GridData();
 		gridData.horizontalSpan = 3;
 		checkButton.setLayoutData(gridData);
 		
 		readSettings();
 		
 		okButton = new Button(shell, SWT.PUSH);
 		okButton.setText("OK");
 		okButton.addSelectionListener(new GeneralSettingsSelectionListener());
 		
 		applyButton = new Button(shell, SWT.PUSH);
 		applyButton.setText("Apply");
 		applyButton.addSelectionListener(new GeneralSettingsSelectionListener());
 		
 		cancelButton = new Button(shell, SWT.PUSH);
 		cancelButton.setText("Cancel");
 		cancelButton.addSelectionListener(new GeneralSettingsSelectionListener());
 		
		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return display;
    }
    
    private class GeneralSettingsSelectionListener extends SelectionAdapter {
    	
    	public void widgetSelected(SelectionEvent e) {
    		if (e.getSource() == okButton) {
    			setSettings();
    			shell.dispose();
    		}
    		else if (e.getSource() == applyButton) {
    			setSettings();
    		}
    		else if (e.getSource() == cancelButton) {
    			shell.dispose();
    		}
    		
    	}

		private void setSettings() {
			 Properties props = JCPPropertyHandler.getInstance().getJCPProperties();
		        props.setProperty("askForIOSettings",
		        		new String("" + checkButton.getSelection()));
		        JCPPropertyHandler.getInstance().saveProperties();
			
		}
    }
    
    private void readSettings() {
		Properties props = JCPPropertyHandler.getInstance().getJCPProperties();
		checkButton.setSelection(props.getProperty("askForIOSettings", "true").equals("true"));
	}
}
