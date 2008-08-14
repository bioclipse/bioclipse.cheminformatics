/*******************************************************************************
 * Copyright (c) 2005-2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn <shk3@users.sf.net> - original implementation
 *     Carl <carl_marak@users.sf.net>  - converted into table
 *     Ola Spjuth                      - minor fixes
 *     Egon Willighagen                - made into a SWT widget
 *     Arvid Berg                      - rewrite of rendering
 *******************************************************************************/
package net.bioclipse.cdk.jchempaint.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2d;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.renderer.Renderer2DModel;

/**
 * SWT widget that views molecules using CDK's JChemPaint viewing engine.
 */
public class JChemPaintSWTWidget extends Canvas {
    
    private SWTRenderer renderer;
    private IAtomContainer molecule;
    private Map<IAtom,Point2d> coordinates=new HashMap<IAtom,Point2d>();
    private final static int compactSize = 200;
    
    /**
     * The constructor.
     */
    public JChemPaintSWTWidget(Composite parent, int style) {
        super(parent, style);
        
        renderer = new SWTRenderer(new Renderer2DModel());
        Dimension screenSize = new Dimension(this.getSize().x, this.getSize().y);
        renderer.getRenderer2DModel().setBackgroundDimension(screenSize);
        renderer.getRenderer2DModel().setDrawNumbers(false);
        setCompactedNess(screenSize);
        renderer.getRenderer2DModel().setBondWidth(10);
        renderer.getRenderer2DModel().setForeColor(Color.BLACK);
        
        
        
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                JChemPaintSWTWidget.this.widgetDisposed(event);
            }
        });
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                JChemPaintSWTWidget.this.paintControl(event);
            }
        });
        addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent event) {
                JChemPaintSWTWidget.this.controlResized(event);
            }
        });
    }

    public void setAtomContainer(IAtomContainer molecule) throws IllegalArgumentException {
        if (!GeometryTools.has2DCoordinates(molecule)) {
            throw new IllegalArgumentException("The AtomContainer does not contain 2D coordinates.");
        }
        this.molecule = molecule;
    }
    
    public Renderer2DModel getRendererModel() {
        return renderer.getRenderer2DModel();
    }
    
    public IJava2DRenderer getRenderer(){
    	return renderer;
    }
    
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return new Point(200, 200);
    }
    
    private void widgetDisposed(DisposeEvent event) {
        renderer.dispose();
        molecule = null;
        renderer = null;
    }
    
    private void controlResized(ControlEvent event) {
    	int xsize = this.getSize().x;
        int ysize = this.getSize().y;
        Dimension newDimensions=new Dimension(xsize,ysize);
        updateOnReize(newDimensions);
    	setCompactedNess(newDimensions);
    }
    private void updateOnReize(Dimension newSize){
        GeometryTools.translateAllPositive(molecule);
        GeometryTools.scaleMolecule(molecule, newSize, 0.8);          
        GeometryTools.center(molecule, newSize);
//            GeometryTools.translateAllPositive(molecule, coordinates);
//            GeometryTools.scaleMolecule(molecule, oldDimensions, 0.8, coordinates);          
//            GeometryTools.center(molecule, oldDimensions, coordinates);

        renderer.getRenderer2DModel().setRenderingCoordinates(coordinates);
        renderer.getRenderer2DModel().setBackgroundDimension(newSize);
//            Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, oldDimensions.getWidth(), oldDimensions.getHeight());
//            renderer.paintMolecule(
//                molecule, 
//                (Graphics2D)graphics,
//                rect
//            );
    }
    private void paintControl(PaintEvent event) {
        this.setBackground(new org.eclipse.swt.graphics.Color(event.gc.getDevice(),255,255,230));
    	renderer.paintMolecule(molecule,event.gc,new Rectangle2D.Double(0,0,this.getSize().x,this.getSize().y));
    }

	private void setCompactedNess(Dimension dimensions) {
        if (dimensions.height < compactSize ||
            dimensions.width < compactSize) {
            renderer.getRenderer2DModel().setIsCompact(true);
        } else {
            renderer.getRenderer2DModel().setIsCompact(false);
        }
	}
}