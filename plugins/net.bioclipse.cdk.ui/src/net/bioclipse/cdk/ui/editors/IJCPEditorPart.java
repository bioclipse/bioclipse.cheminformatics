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
import org.openscience.cdk.applications.jchempaint.DrawingPanel;
import org.openscience.cdk.applications.jchempaint.JCPScrollBar;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;

public interface IJCPEditorPart {

	
	public JChemPaintModel getJCPModel();
	
	public Composite getJcpComposite();
	
	public DrawingPanel getDrawingPanel();
	
	public JCPScrollBar getJcpScrollBar();
}
