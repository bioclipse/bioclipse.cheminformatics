/* *****************************************************************************
* Copyright (c) 2010 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor.painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;

/**
 * @author arvid
 *
 */
public class JmolCellPainter extends BackgroundPainter {
    public Logger logger = Logger.getLogger(JmolCellPainter.class );
    Graphics2DRenderer renderer = new Graphics2DRenderer();
    JmolViewer viewer;
    public JmolCellPainter() {
        JPanel panel = new JPanel();
        panel.setSize(100, 100);
        viewer = Viewer.allocateViewer(panel, new SmarterJmolAdapter());
        viewer.setColorBackground("black");
        viewer.setAutoBond(true);
    }
    
    @Override
    public void paintCell(LayerCell cell, GC gc, Rectangle bounds,
            IConfigRegistry configRegistry) {
        super.paintCell(cell, gc, bounds, configRegistry);
        Rectangle originalClipping = gc.getClipping();
        gc.setClipping( bounds.intersection( originalClipping ) );
        IStyle cellStyle = CellStyleUtil.getCellStyle( cell, configRegistry );
        try{
        paintImage(gc,bounds,cell.getDataValue());
        }catch (Exception e) {
            logger.debug("Jmol painting error",e);
        }
        gc.setClipping( originalClipping );
    }
    
    private void paintImage(GC gc, Rectangle bounds,Object o) {
        if(!setupViewer(o)) return;
        java.awt.Rectangle rectClip = new java.awt.Rectangle( bounds.x, bounds.y, 
                bounds.width,bounds.height);
        Dimension currentSize = new Dimension(bounds.width,bounds.height);
        BufferedImage image = new BufferedImage(bounds.width,
                bounds.height,
                BufferedImage.TYPE_INT_RGB);
        viewer.renderScreenImage( image.getGraphics(),currentSize,
                    new java.awt.Rectangle(0,0,bounds.width,bounds.height));
        
        ImageData swtImageData= SWTUtils.convertToSWT(image);
        Image swtImage = new Image(gc.getDevice(),swtImageData);
        gc.drawImage(swtImage, bounds.x,bounds.y);
        swtImage.dispose();
    }
    
    private void paintToGraphics(GC gc, Rectangle bounds,Object o) {
        if(!setupViewer(o)) return;
        renderer.prepareRendering(gc);
        
        Dimension currentSize = new Dimension(bounds.width,bounds.height);
        viewer.setScreenDimension(currentSize);
        java.awt.Rectangle rectClip = new java.awt.Rectangle( bounds.x, bounds.y, 
                                            bounds.width,bounds.height);
        Graphics g = renderer.getGraphics2D();
        //g.getClipBounds(rectClip);
        viewer.renderScreenImage(g, currentSize, rectClip);
        renderer.render(gc);
    }

    private boolean setupViewer(Object o) {
        if(o instanceof ICDKMolecule) {
            try {
                String cml = ((ICDKMolecule)o).toCML();
                viewer.loadInline(cml);
                return true;
            } catch (BioclipseException e) {
                logger.debug("Could not render 3d molecule",e);
                return false;
            }
        }
        return false;
    }
    
}