/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views;

import java.awt.event.MouseEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Listen to mouse events and activate editor if clicked upon
 * @author ola
 *
 */
public class JmolCompMouseListener implements java.awt.event.MouseListener {

    private IWorkbenchPart part;
    private Composite comp;

    public JmolCompMouseListener(Composite composite) {
        this.comp = composite;
    }
    public JmolCompMouseListener(Composite composite, IWorkbenchPart part) {
        this.comp = composite;
        this.part = part;
    }

    public void mouseClicked(MouseEvent e) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate((IWorkbenchPart) part);
//                JmolCompMouseListener.this.comp.setFocus();
            }
        });
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }

    public void mousePressed(MouseEvent e) {
//        Display.getDefault().syncExec(new Runnable() {
//            public void run() {
//                JmolCompMouseListener.this.comp.setFocus();
//            }
//        });
    }

    public void mouseReleased(MouseEvent e) {
        
    }

}
