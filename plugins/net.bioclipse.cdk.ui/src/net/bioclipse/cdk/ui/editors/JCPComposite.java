/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors;

import org.eclipse.swt.widgets.Composite;

public class JCPComposite extends Composite {

	private boolean hasFocus = false;

	public JCPComposite(Composite parent, int style) {
		super(parent, style);
	}

	public void setHasFocus(boolean b) {
		this.hasFocus = b;
		
	}
	public boolean getFocus() {
		return hasFocus;
	}

	@Override
	public boolean setFocus() {
		this.setHasFocus(true);
		return super.setFocus();
	}

}
