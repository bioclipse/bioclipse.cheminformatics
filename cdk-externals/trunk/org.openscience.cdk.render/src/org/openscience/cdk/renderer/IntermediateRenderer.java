/* $Revision$ $Author$ $Date$
*
*  Copyright (C) 2008 Gilleain Torrance <gilleain.torrance@gmail.com>
*
*  Contact: cdk-devel@list.sourceforge.net
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU Lesser General Public License
*  as published by the Free Software Foundation; either version 2.1
*  of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.openscience.cdk.renderer;


import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.generators.SelectionGenerator;
import org.openscience.cdk.renderer.visitor.TransformingDrawVisitor;

/**
 * @cdk.module render
 */
public class IntermediateRenderer implements IJava2DRenderer {
	
	private AWTFontManager fontManager;
	
	private RendererModel rendererModel;
	
	private ArrayList<IGenerator> generators;
	
	private Stroke stroke;
	
	private AffineTransform transform;
	
	private Point2d modelCenter;
	
	private Point2d drawCenter;
	
	private double theta = 0.0;    // the rotation
	
	public static final double NATURAL_SCALE = 30.0;
	
	private double scale = NATURAL_SCALE;
	
	private IRenderingElement cachedDiagram;
	
	/**
	 * If true, the renderer will calculate a scale that will fit
	 * the molecule into the drawBounds, as well as centring it there.
	 */
	private boolean fitToScreen = false;
	
	public IntermediateRenderer() {
		this.rendererModel = new RendererModel();
		this.rendererModel.setHighlightRadiusModel(2.5);
		this.rendererModel.setBondDistance(0.15);
		this.rendererModel.setMargin(0.25);
		
		this.generators = new ArrayList<IGenerator>();
		this.generators.add(new RingGenerator(this.rendererModel));
		this.generators.add(new BasicAtomGenerator(this.rendererModel));
		this.generators.add(new HighlightGenerator(this.rendererModel));
		this.generators.add(new SelectionGenerator(this.rendererModel));
		this.stroke = new BasicStroke(2);
		this.fontManager = new AWTFontManager();
		this.transform = new AffineTransform();
	}
	
	/**
	 * Given a chem model, calculates the bounding rectangle in screen space.
	 * 
	 * @param model the model to draw.
	 * @return a rectangle in screen space.
	 */
	public Rectangle calculateScreenBounds(IChemModel model) {
        IMoleculeSet moleculeSet = model.getMoleculeSet();
        if (moleculeSet == null || this.drawCenter == null) {
            return new Rectangle();
        }
        
        Rectangle2D modelBounds = this.calculateBounds(moleculeSet);
        double margin = this.rendererModel.getMargin();
        
        // this is the center on the screen where the model will be drawn
        Point2d modelScreenCenter 
                = this.toScreenCoordinates(modelBounds.getCenterX(),
                                           modelBounds.getCenterY());
        double w = scale * (modelBounds.getWidth() + 2 * margin);
        double h = scale * (modelBounds.getHeight() + 2 * margin); 
        return new Rectangle((int) (modelScreenCenter.x - w / 2), 
                             (int) (modelScreenCenter.y - h / 2),
                             (int) w,
                             (int) h);
    }

    public void setFitToScreen(boolean fitToScreen) {
	    this.fitToScreen = fitToScreen;
	}

	public RendererModel getRenderer2DModel() {
		return this.rendererModel;
	}
	
	/**
	 * Set the rotation for drawing - should be unset by calling <code>setRotation(0)</code>.
	 * 
	 * @param degrees the angle to rotate by in degrees.
	 */
	public void setRotation(double degrees) {
	    this.theta = Math.toRadians(degrees);
	}
	
	public void setModelCenter(double x, double y) {
//	    System.err.println("setting model center to " + x + " " + y);
	    this.modelCenter = new Point2d(x, y);
	}
	
	public void setDrawCenter(double x, double y) {
//	    System.err.println("setting draw center to " + x + " " + y);
        this.drawCenter = new Point2d(x, y);
    }
	
	private IRenderingElement generateDiagram(IMoleculeSet moleculeSet) {
	    ElementGroup diagram = new ElementGroup();
        for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
            IAtomContainer ac = moleculeSet.getAtomContainer(i);
            for (IGenerator generator : this.generators) {
                diagram.add(generator.generate(ac));
            }
        }
        return diagram;
	}
	
	private IRenderingElement generateDiagram(IAtomContainer ac) {
	    ElementGroup diagram = new ElementGroup();
        for (IGenerator generator : this.generators) {
            diagram.add(generator.generate(ac));
        }
        return diagram;
	}
	
	private Rectangle2D calculateBounds(IMoleculeSet moleculeSet) {
	    Rectangle2D totalBounds = new Rectangle2D.Double();
        for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
            IAtomContainer ac = moleculeSet.getAtomContainer(i);
            Rectangle2D acBounds = this.calculateBounds(ac);
            if (totalBounds.isEmpty()) {
                totalBounds = acBounds;
            } else {
                Rectangle2D.union(totalBounds, acBounds, totalBounds);
            }
        }
        return totalBounds;
	}
	
	/**
	 * Paint an entire ChemModel.
	 * 
	 * @param chemModel
	 * @param g
	 * @param bounds the bounds of the area to paint on.
	 * @param resetCenter if true, set the modelCenter to the center of the ChemModel's bounds. 
	 */
	public void paintChemModel(IChemModel chemModel, Graphics2D g, Rectangle2D bounds, 
	        boolean resetCenter) {
	    if (chemModel.getMoleculeSet() == null) return;
	    
	    // calculate the total bounding box
	    IMoleculeSet moleculeSet = chemModel.getMoleculeSet(); 
	    Rectangle2D modelBounds = this.calculateBounds(moleculeSet);
	    
	    // generate the elements
	    IRenderingElement diagram = this.generateDiagram(moleculeSet);
	    
	    // cache the diagram for quick-redraw
	    this.cachedDiagram = diagram;
	    
	    // setup the scale and translation for the transform
	    this.setupDrawArea(bounds, modelBounds);
	    if (resetCenter) {
	        this.setModelCenter(modelBounds.getCenterX(), modelBounds.getCenterY());
	    }
	    
	    // finally, paint it
	    this.paint(g, diagram);
	}
	
	/**
	 * Repaint using the cached diagram
	 * 
	 * @param g a Graphics2D object to paint with
	 */
	public void repaint(Graphics2D g) {
	    this.paint(g, cachedDiagram);
	}

	/**
	 * Paint an atom container at the natural scale, with 
	 * its upper left corner at the origin.
	 * 
	 * @param ac the atom container
	 * @param g the graphics context
	 */
	public void paintMolecule(IAtomContainer ac, Graphics2D g) {
	    this.paintMolecule(ac, g, null, true);
	}

	public void paintMolecule(IAtomContainer atomCon, Graphics2D graphics,
            Rectangle2D bounds) {
        // TODO Auto-generated method stub
        
    }

    public void paintMolecule(IAtomContainer ac, Graphics2D g, Rectangle2D bounds,
	        boolean resetCenter) {
		if (ac.getAtomCount() == 0) return;	// XXX
		
		Rectangle2D modelBounds = this.calculateBounds(ac);
		IRenderingElement diagram = this.generateDiagram(ac);
		
		this.setupDrawArea(bounds, modelBounds);
		if (resetCenter) {
		    this.setModelCenter(modelBounds.getCenterX(), modelBounds.getCenterY());
		}
		this.paint(g, diagram);
	}
    
    private void setupDrawArea(Rectangle2D drawBounds, Rectangle2D modelBounds) {
        if (drawBounds == null) {
            this.scale = IntermediateRenderer.NATURAL_SCALE;
            double scaledWidth  = modelBounds.getWidth() * this.scale;
            double scaledHeight = modelBounds.getHeight() * this.scale;
            this.setDrawCenter(scaledWidth / 2, scaledHeight / 2);
        } else {
            this.setDrawCenter(drawBounds.getCenterX(), drawBounds.getCenterY());
            this.setScale(drawBounds, modelBounds);
        }
    }
	
	private void setScale(Rectangle2D bounds, Rectangle2D atomicBounds) {
        // alter the width slightly to allow for atom symol text
        double w = bounds.getWidth() * 0.9;
        double h = bounds.getHeight() * 0.9;
        double toFitScale = this.calculateScale(atomicBounds, w, h); 
        if (!fitToScreen || toFitScale > IntermediateRenderer.NATURAL_SCALE) {
            this.scale = IntermediateRenderer.NATURAL_SCALE;
        } else {
            this.scale = toFitScale; 
        }
        double fractionalScale = (1 - h / (h * this.scale));
        this.fontManager.setFontForScale(fractionalScale);
    }

    /**
     * Calculates the multiplication factor that will fit the model completely within
     * the dimensions w * h (not accounting for a border).
     * 
     * @param modelBounds
     * @param displayBounds
     * @return
     */
    private double calculateScale(Rectangle2D modelBounds, double w, double h) {
    	double widthRatio = w / modelBounds.getWidth();
    	double heightRatio = h / modelBounds.getHeight();
    
    	// the area is contained completely within the target
    	if (widthRatio > 1 && heightRatio > 1) {
    		return Math.min(widthRatio, heightRatio);
    	}
    
    	// the area is wider than the target, but fits the height
    	if (widthRatio < 1 && heightRatio > 1) {
    		return widthRatio;
    	}
    
    	// the area is taller than the target, but fits the width
    	if (widthRatio > 1 && heightRatio < 1) {
    		return heightRatio;
    	}
    
    	// the target is completely contained by the area
    	if (widthRatio > 1 && heightRatio > 1) {
    		return heightRatio;
    	}
    
    	// the bounds are equal
    	return widthRatio;	// could return either...
    }

    private void paint(Graphics2D g, IRenderingElement diagram) {

	    try {
            this.setTransform(this.drawCenter.x,
                              this.drawCenter.y, 
                              this.scale,
                              this.theta,
                              this.modelCenter.x,
                              this.modelCenter.y);
	    } catch (NullPointerException npe) {
	        // one of the drawCenter or modelCenter points have not been set!
	        System.err.println(String.format(
	                "problem in transform %s %s %s %s",
	                this.drawCenter,
                    this.scale,
                    this.theta,
                    this.modelCenter));
	    }
        
        g.setStroke(this.stroke);
        
//      diagram.accept(new org.openscience.cdk.renderer.visitor.PrintVisitor());
        diagram.accept(
                new TransformingDrawVisitor(g, transform, fontManager, rendererModel));
	}
	
	private void setTransform(double dx, double dy, double scale, double theta, 
	        double cx, double cy) {
	    this.transform = new AffineTransform();
        this.transform.translate(dx, dy);
        this.transform.scale(scale, scale);
        this.transform.rotate(theta);
        this.transform.translate(-cx, -cy);
	}
	
	private Rectangle2D calculateBounds(IAtomContainer ac) {
	    // this is essential, otherwise a rectangle 
	    // of (+INF, -INF, +INF, -INF) is returned!  
	    if (ac.getAtomCount() == 0) {
	        return new Rectangle2D.Double();
	    } else if (ac.getAtomCount() == 1) {
	        Point2d p = ac.getAtom(0).getPoint2d();
	        return new Rectangle2D.Double(p.x, p.y, 0, 0);
	    }
	    
		double xmin = Double.POSITIVE_INFINITY;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;

		for (IAtom atom : ac.atoms()) {
			Point2d p = atom.getPoint2d();
			xmin = Math.min(xmin, p.x);
			xmax = Math.max(xmax, p.x);
			ymin = Math.min(ymin, p.y);
			ymax = Math.max(ymax, p.y);
		}
		double w = xmax - xmin;
		double h = ymax - ymin;
		return new Rectangle2D.Double(xmin, ymin, w, h);
	}

	public void setRenderer2DModel(RendererModel model) {
		this.rendererModel = model;
	}

	public Point2d getCoorFromScreen(int screenX, int screenY) {
	    try {
	        double[] dest = new double[2];
	        double[] src = new double[] { screenX, screenY };
	        transform.inverseTransform(src, 0, dest, 0, 1);
	        return new Point2d(dest[0], dest[1]);
	    } catch (NoninvertibleTransformException n) {
	        return new Point2d(0,0);
	    }
	}

	public Point2d toScreenCoordinates(double modelX, double modelY) {
	    double[] dest = new double[2];
	    transform.transform(new double[] { modelX, modelY }, 0, dest, 0, 1);
	    return new Point2d(dest[0], dest[1]);
	}
	
	public void setTransform(AffineTransform transform) {
	    this.transform = transform;
    }
}
