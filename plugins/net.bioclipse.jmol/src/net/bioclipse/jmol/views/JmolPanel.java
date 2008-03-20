/*******************************************************************************
 * Copyright (c) 2007 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/

package net.bioclipse.jmol.views;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;
import org.apache.log4j.Logger;

/**
 * Extends Jpanel with a JmolViewer
 * 
 * @author ola
 *
 */
public class JmolPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(JmolPanel.class);
	//TODO remove
	/*private static final org.apache.log4j.Logger logger = Activator
	.getLogManager().getLogger(JmolPanel.class.toString());*/

	final Dimension currentSize = new Dimension();
	private JmolViewer jmolViewer;
	private Viewer viewer;

	private ISelectionProvider part;
	
//	public JmolPanel() {
//		jmolViewer = createViewer(new SmarterJmolAdapter());
//		viewer = (Viewer)jmolViewer;
//	}

	public JmolPanel(ISelectionProvider part) {
		this.part=part;
		jmolViewer = createViewer(new SmarterJmolAdapter());
		viewer = (Viewer)jmolViewer;
	}

	private JmolViewer createViewer(JmolAdapter adapter) {
		JmolViewer viewer = Viewer.allocateViewer(this, adapter);
		viewer.setColorBackground("white");
		viewer.setAutoBond(true);
		viewer.setJmolStatusListener(new StatusListener(part));
		return viewer;
	}

	public Viewer getViewer() {
		return viewer;
	}


	public void paint(Graphics g) {
		viewer.setScreenDimension(getSize(currentSize));
		Rectangle rectClip = new Rectangle();
		g.getClipBounds(rectClip);
		viewer.renderScreenImage(g, currentSize, rectClip);
	}
}
