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

import net.bioclipse.jmol.adapter.cdk.CdkJmolAdapter;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jmol.adapter.smarter.AtomSetCollection;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
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
    
    final Dimension currentSize = new Dimension();
    private JmolViewer jmolViewer;
    JmolViewer cdkViewer;
    private Viewer viewer;

    private ISelectionProvider part;
    
//    public JmolPanel() {
//        jmolViewer = createViewer(new SmarterJmolAdapter());
//        viewer = (Viewer)jmolViewer;
//    }

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
    
    public void openClientFile(String string, String string2, Object obj) {
//        if (obj instanceof IAtomContainer) {
//            IAtomContainer ac=(IAtomContainer)obj;
//
//            ChemFile cf=new ChemFile();
//            ChemSequence seq=new ChemSequence();
//            cf.addChemSequence(seq);
//            ChemModel model=new ChemModel();
//            seq.addChemModel(model);
//            
//            IMoleculeSet set=new MoleculeSet();
//            set.addAtomContainer(ac);
//            model.setMoleculeSet(set);

        if (obj instanceof IChemFile) {
            
            cdkViewer = createViewer(new CdkJmolAdapter());
            viewer = (Viewer)cdkViewer;
            viewer.openClientFile(string, string2, (IChemFile)obj);

        } else if (obj instanceof AtomSetCollection) {
            viewer = (Viewer)jmolViewer;
            viewer.openClientFile(string, string2, obj);
        } else {
            logger.debug("Object neither CDK or Jmol. " +
                    "JmolPanel Can't open client file.");
            return;
        }
    }

    public String getOpenFileError() {
        return viewer.getOpenFileError();
    }

}
