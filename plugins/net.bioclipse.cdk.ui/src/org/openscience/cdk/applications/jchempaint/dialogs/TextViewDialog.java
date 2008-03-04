/* $RCSfile: TextViewDialog.java,v $
 * $Author: egonw $
 * $Date: 2005/10/22 16:25:37 $
 * $Revision: 1.5 $
 *
 * Copyright (C) 2003-2005  The JChemPaint Development Team
 *
 * Contact: jchempaint-devel@lists.sourceforge.net
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

import java.awt.Dimension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple text viewing dialog for general use.
 *
 * @cdk.module jchempaint
 * @cdk.created 2003-08-24
 */
public class TextViewDialog extends Dialog {

    private Dimension dimension;
	private Shell shell;
	private int width;
	private int height;
	private Text textArea;
	private String areaText;
    
        
    /**
     * Constructs a new JTextViewDialog.
     *
     */
    public TextViewDialog(Shell parent, int style, String title, int width, int height) {
        super(parent, style);
        this.setText(title);
        this.width = width;
        this.height = height;
    }
    
    public Object open(){
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(getText());
		final Display display = parent.getDisplay();
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		shell.setLayout(layout);
		textArea = new Text(shell, SWT.NONE|SWT.WRAP);
		//TODO setSize isnt changing anything at the moment...
//		textArea.setSize(width, height);
		if (areaText != null) {
			textArea.setText(areaText);
		}
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return display;
	}
    
    public void setMessage(String text) {
    	this.areaText = text;
    }
    
}
