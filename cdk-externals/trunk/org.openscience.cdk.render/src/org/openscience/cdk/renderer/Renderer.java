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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.generators.BoundsGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.MappingGenerator;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

/**
 * A general renderer for ChemModels, Reactions, and Molecules. The chem object
 * is converted into a 'diagram' made up of {@link IRenderingElement}s. It takes
 * an {@link IDrawVisitor} to do the drawing of the generated diagram. Various
 * display properties can be set using the {@link RendererModel}.
 * 
 * @author maclean
 * @cdk.module render
 */
public class Renderer {

	private IFontManager fontManager;

	/**
	 * The renderer model is final as it is not intended to be replaced.
	 */
	private final RendererModel rendererModel = new RendererModel();

	private List<IGenerator> generators;

	private AffineTransform transform;

	/**
	 * The center of the model coordinates 
	 */
	private Point2d modelCenter = new Point2d(0, 0);

	/**
	 * The center point of the diagram on screen
	 */
	private Point2d drawCenter = new Point2d(200, 200);

	/**
	 * The rotation applied to the diagram
	 */
	private double theta = 0.0; 

	/**
	 * The default scale is used when the model is empty.
	 */
	public static final double DEFAULT_SCALE = 30.0;

	private double scale = DEFAULT_SCALE;
	
	private double zoom = 1.0;

	private IRenderingElement cachedDiagram;

    /**
     * A renderer that generates diagrams using the specified
     * generators and manages fonts with the supplied font manager.
     * 
     * @param generators
     *            a list of classes that implement the IGenerator interface
     * @param fontManager
     *            a class that manages mappings between zoom and font sizes
     */
	public Renderer(List<IGenerator> generators, 
	        IFontManager fontManager) {
        this.generators = generators;
        this.fontManager = fontManager;
        
        this.setup();
    }
	
	/**
     * Paint a ChemModel.
     *
     * @param chemModel
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds of the area to paint on.
     * @param resetCenter 
     *     if true, set the modelCenter to the center of the ChemModel's bounds.
     */
    public void paintChemModel(IChemModel chemModel, 
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {
        // check for an empty model
        IMoleculeSet moleculeSet = chemModel.getMoleculeSet();
        IReactionSet reactionSet = chemModel.getReactionSet();
        
        // nasty, but it seems that reactions can be read in as ChemModels
        // with BOTH a ReactionSet AND a MoleculeSet...
        if (moleculeSet == null || reactionSet != null) {
            if (reactionSet != null) {
                paintReactionSet(reactionSet, drawVisitor, bounds, resetCenter);
            }
            return;
        }
    
        // calculate the total bounding box
        Rectangle2D modelBounds = Renderer.calculateBounds(moleculeSet);
        
        this.setupTransform(bounds, modelBounds,
                this.calculateAverageBondLength(chemModel), resetCenter);
    
        // generate the elements
        IRenderingElement diagram = this.generateDiagram(moleculeSet);
    
        // paint it
        this.paint(drawVisitor, diagram);
    }
    
    /**
     * Paint a set of reactions.
     * 
     * @param reaction the reaction to paint
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds on the screen
     * @param resetCenter 
     *     if true, set the draw center to be the center of bounds
     */
    public void paintReactionSet(IReactionSet reactionSet,
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {
        
        // total up the bounding boxes
        Rectangle2D totalBounds = null;
        for (IReaction reaction : reactionSet.reactions()) {
            Rectangle2D modelBounds = Renderer.calculateBounds(reaction);  
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }
        
        this.setupTransform(bounds, totalBounds,
                this.calculateAverageBondLength(reactionSet), resetCenter);
        
        ElementGroup diagram = new ElementGroup();
        for (IReaction reaction : reactionSet.reactions()) {
            diagram.add(this.generateDiagram(reaction));
        }
        
        // paint them all
        this.paint(drawVisitor, diagram);
    }

    /**
	 * Paint a reaction.
	 * 
	 * @param reaction the reaction to paint
	 * @param drawVisitor the visitor that does the drawing
	 * @param bounds the bounds on the screen
	 * @param resetCenter 
	 *     if true, set the draw center to be the center of bounds
	 */
	public void paintReaction(IReaction reaction, IDrawVisitor drawVisitor, 
            Rectangle2D bounds, boolean resetCenter) {
        
	    // calculate the bounds
        Rectangle2D modelBounds = Renderer.calculateBounds(reaction);
        
        this.setupTransform(bounds, modelBounds,
                this.calculateAverageBondLength(reaction), resetCenter);
        
        // generate the elements
        IRenderingElement diagram = this.generateDiagram(reaction);  
        
        // paint it
        this.paint(drawVisitor, diagram);
    }
	
	/**
     * Paint a set of molecules.
     * 
     * @param reaction the reaction to paint
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds on the screen
     * @param resetCenter 
     *     if true, set the draw center to be the center of bounds
     */
    public void paintMoleculeSet(IMoleculeSet molecules,
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {
        
        // total up the bounding boxes
        Rectangle2D totalBounds = null;
        for (IAtomContainer molecule : molecules.molecules()) {
            Rectangle2D modelBounds = Renderer.calculateBounds(molecule);  
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }
        
        this.setupTransform(bounds, totalBounds,
                this.calculateAverageBondLength(molecules), resetCenter); 
        
        ElementGroup diagram = new ElementGroup();
        for (IAtomContainer molecule : molecules.molecules()) {
            diagram.add(this.generateDiagram(molecule));
        }
        
        this.paint(drawVisitor, diagram);
    }

	/**
     * Paint a molecule (an IAtomContainer).
     * 
     * @param atomContainer the molecule to paint
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds on the screen
     * @param resetCenter 
     *     if true, set the draw center to be the center of bounds
     */
    public void paintMolecule(IAtomContainer atomContainer, 
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {

        // the bounds of the model
    	Rectangle2D modelBounds = Renderer.calculateBounds(atomContainer);
    	
    	this.setupTransform(bounds, modelBounds, 
    	        GeometryTools.getBondLengthAverage(atomContainer), resetCenter);
    	
    	// the diagram to draw
    	IRenderingElement diagram = this.generateDiagram(atomContainer);

    	this.paint(drawVisitor, diagram);
    }

    /**
     * Repaint using the cached diagram
     *
     * @param drawVisitor the wrapper for the graphics object that draws
     */
    public void repaint(IDrawVisitor drawVisitor) {
        this.paint(drawVisitor, cachedDiagram);
    }

    /**
	 * Given a chem model, calculates the bounding rectangle in screen space.
	 *
	 * @param model the model to draw.
	 * @return a rectangle in screen space.
	 */
	public Rectangle calculateScreenBounds(IChemModel model) {
        IMoleculeSet moleculeSet = model.getMoleculeSet();
        IReactionSet reactionSet = model.getReactionSet();
        if ((moleculeSet == null && reactionSet == null)
                || this.drawCenter == null) {
            return new Rectangle();
        }

        Rectangle2D modelBounds;
        if (moleculeSet == null) {
            modelBounds = Renderer.calculateBounds(reactionSet);
        } else {
            modelBounds = Renderer.calculateBounds(moleculeSet);
        }
        
        return this.calculateScreenBounds(modelBounds);
    }
	
	public Rectangle calculateScreenBounds(IReactionSet reactionSet) {
        return this.calculateScreenBounds(
                Renderer.calculateBounds(reactionSet));
	}
	
	public Rectangle calculateScreenBounds(IReaction reaction) {
        return this.calculateScreenBounds(
                Renderer.calculateBounds(reaction));
	}
	
	public Rectangle calculateScreenBounds(IMoleculeSet moleculeSet) {
	    return this.calculateScreenBounds(
	               Renderer.calculateBounds(moleculeSet));
	}
	
	public Rectangle calculateScreenBounds(IAtomContainer atomContainer) {
	   return this.calculateScreenBounds(
	           Renderer.calculateBounds(atomContainer));
	}
	
	public Rectangle calculateScreenBounds(Rectangle2D modelBounds) {
	    double margin = this.rendererModel.getMargin();
        Point2d modelScreenCenter
            = this.toScreenCoordinates(modelBounds.getCenterX(),
                                       modelBounds.getCenterY());
        double w = (scale * zoom * modelBounds.getWidth()) + (2 * margin);
        double h = (scale * zoom * modelBounds.getHeight()) + (2 * margin);
        return new Rectangle((int) (modelScreenCenter.x - w / 2),
                             (int) (modelScreenCenter.y - h / 2),
                             (int) w,
                             (int) h);
	}

	public RendererModel getRenderer2DModel() {
		return this.rendererModel;
	}

    public Point2d toModelCoordinates(int screenX, int screenY) {
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
    
    /**
     * Set the zoom to use; a zoom of 1.0 is 100%, 0.5 is 50%, 2.0 is 200%.
     * 
     * @param zoom the zoom
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    
    public double getZoom() {
        return this.zoom;
    }

    /**
	 * Set the rotation for drawing; NOTE - should be unset by calling 
	 * <code>setRotation(0)</code>.
	 *
	 * @param degrees the angle to rotate by in degrees.
	 */
	public void setRotation(double degrees) {
	    this.theta = Math.toRadians(degrees);
	}

	public void setModelCenter(double x, double y) {
	    this.modelCenter = new Point2d(x, y);
	}

	public void setDrawCenter(double x, double y) {
        this.drawCenter = new Point2d(x, y);
    }

    /**
     * The target method for paintChemModel, paintReaction, and paintMolecule.
     * 
     * @param drawVisitor
     *            the visitor to draw with
     * @param diagram
     *            the IRenderingElement tree to render
     */
	private void paint(IDrawVisitor drawVisitor, 
	                   IRenderingElement diagram) {
	    if (diagram == null) return;
	    
	    // cache the diagram for quick-redraw
	    this.cachedDiagram = diagram;
	    
	    this.fontManager.setFontName(this.rendererModel.getFontName());
	    this.fontManager.setFontStyle(this.rendererModel.getFontStyle());
	    
	    drawVisitor.setFontManager(this.fontManager);
	    drawVisitor.setTransform(this.transform);
	    drawVisitor.setRendererModel(this.rendererModel);
	    diagram.accept(drawVisitor);
	}

    /**
     * Sets the transformation needed to draw the model on the canvas.
     * 
     * @param drawBounds
     *            the bounding box of the draw area
     * @param modelBounds
     *            the bounding box of the model
     * @param bondLen
     *            the average bond length of the model
     * @param resetCenter
     *            if true, model center will be set to the modelBounds center
     */
	private void setupTransform(Rectangle2D drawBounds, 
	                            Rectangle2D modelBounds, 
	                            double bondLen,
	                            boolean resetCenter) {
	    double scaledWidth  = modelBounds.getWidth() * this.scale;
	    double scaledHeight = modelBounds.getHeight() * this.scale;
	    
	    if (drawBounds == null) {
	        this.scale = Renderer.DEFAULT_SCALE;
	        this.setDrawCenter(scaledWidth / 2, scaledHeight / 2);
	    } else {
	        
	        // calculate the scale 
	        if (Double.isNaN(bondLen)) {
	            this.scale = Renderer.DEFAULT_SCALE;
	        } else {
	            this.scale = this.rendererModel.getBondLength() / bondLen;
	        }
	        
	        this.setDrawCenter(
	                drawBounds.getCenterX(), 
	                drawBounds.getCenterY());
	        
	        if (rendererModel.isFitToScreen()) {
	            double m = 2 * this.rendererModel.getMargin();
	            double widthRatio  = (drawBounds.getWidth() - m) / scaledWidth;
	            double heightRatio = 
	                (drawBounds.getHeight() - m) / scaledHeight;

	            // the area is contained completely within the target
	            if (widthRatio > 1 && heightRatio > 1) {
	                this.zoom = Math.min(widthRatio, heightRatio);
	            }

	            // the area is wider than the target, but fits the height
	            else if (widthRatio < 1 && heightRatio > 1) {
	                this.zoom = widthRatio;
	            }

	            // the area is taller than the target, but fits the width
	            else if (widthRatio > 1 && heightRatio < 1) {
	                this.zoom = heightRatio;
	            }

	            // the target is completely contained by the area
	            else if (widthRatio > 1 && heightRatio > 1) {
	                this.zoom = heightRatio;
	            }

	            else {
	                // the bounds are equal
	                this.zoom = widthRatio; // either is fine
	            }
	        }
	        this.fontManager.setFontForZoom(this.zoom);
	        this.rendererModel.setZoomFactor(zoom);
	    }
	    
	    // this controls whether editing a molecule causes it to re-center
	    // with each change or not
	    if (resetCenter) {
            this.setModelCenter(
                    modelBounds.getCenterX(), modelBounds.getCenterY());
        }
	    
	    // set the scale in the renderer model for the generators
	    this.rendererModel.setScale(scale);
	  
	    this.setup();
	}
	
	private void setup() {
	    
        // set the transform
        try {
            this.transform = new AffineTransform();
            this.transform.translate(this.drawCenter.x, this.drawCenter.y);
            this.transform.scale(this.scale, this.scale);
            this.transform.scale(this.zoom, this.zoom);
            this.transform.rotate(this.theta);
            this.transform.translate(-this.modelCenter.x, -this.modelCenter.y);
        } catch (NullPointerException npe) {
            // one of the drawCenter or modelCenter points have not been set!
            System.err.println(String.format(
                    "null pointer when setting transform: " +
                    "drawCenter=%s scale=%s zoom=%s rotation=%s modelCenter=%s",
                    this.drawCenter,
                    this.scale,
                    this.zoom,
                    this.theta,
                    this.modelCenter));
        }
	}

    /**
     * 
     * 
     *  @param model the model for which to calculate the average bond length
     */
    private double calculateAverageBondLength(IChemModel model) {
                
        // empty models have to have a scale
        IMoleculeSet moleculeSet = model.getMoleculeSet();
        if (moleculeSet == null) {
            IReactionSet reactionSet = model.getReactionSet();
            if (reactionSet != null) {
                return this.calculateAverageBondLength(reactionSet);
            }
            return 0.0;
        }
        
        return this.calculateAverageBondLength(moleculeSet);
    }
    
    private double calculateAverageBondLength(IReactionSet reactionSet) {
        double averageBondModelLength = 0.0;
        for (IReaction reaction : reactionSet.reactions()) {
            averageBondModelLength +=
                this.calculateAverageBondLength(reaction);
        }
        return averageBondModelLength / reactionSet.getReactionCount();
    }
    
    private double calculateAverageBondLength(IReaction reaction) {
        
        IMoleculeSet reactants = reaction.getReactants();
        double reactantAverage = 0.0;
        if (reactants != null) {
            reactantAverage = 
                this.calculateAverageBondLength(reactants) /
                reactants.getAtomContainerCount();
        }
        
        IMoleculeSet products = reaction.getProducts();
        double productAverage = 0.0;
        if (products != null) {
            productAverage = 
                this.calculateAverageBondLength(products) /
                products.getAtomContainerCount();
        }
        
        if (productAverage == 0.0 && reactantAverage == 0.0) {
            return 1.0;
        } else {
            return (productAverage + reactantAverage) / 2.0;
        }
    }
    
    private double calculateAverageBondLength(IMoleculeSet moleculeSet) {
        double averageBondModelLength = 0.0;
        for (IAtomContainer atomContainer : moleculeSet.molecules()) {
            averageBondModelLength += 
                GeometryTools.getBondLengthAverage(atomContainer);
        }
        return averageBondModelLength / moleculeSet.getAtomContainerCount();
    }

    private IRenderingElement generateDiagram(IReaction reaction) {
	    ElementGroup diagram = new ElementGroup();
	    
	    // generate the bounds first, so that they are to the back
	    BoundsGenerator boundsGenerator = new BoundsGenerator();
	    diagram.add(boundsGenerator.generate(reaction, this.rendererModel));
	    
	    // now make the molecules
	    diagram.add(generateDiagram(reaction.getReactants()));
	    diagram.add(generateDiagram(reaction.getProducts()));
	    
	    // specialised reaction-specific generators
	    MappingGenerator mapper = new MappingGenerator();
	    diagram.add(mapper.generate(reaction, this.rendererModel));
	    
	    return diagram;
	}

	private IRenderingElement generateDiagram(IMoleculeSet moleculeSet) {
	    ElementGroup diagram = new ElementGroup();
        for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
            IAtomContainer ac = moleculeSet.getAtomContainer(i);
            for (IGenerator generator : this.generators) {
                diagram.add(generator.generate(ac, this.rendererModel));
            }
        }
        return diagram;
	}

	private IRenderingElement generateDiagram(IAtomContainer ac) {
	    ElementGroup diagram = new ElementGroup();
        for (IGenerator generator : this.generators) {
            diagram.add(generator.generate(ac, this.rendererModel));
        }
        return diagram;
	}
	
	public static Rectangle2D calculateBounds(IReactionSet reactionSet) {
	    Rectangle2D totalBounds = new Rectangle2D.Double();
	    for (IReaction reaction : reactionSet.reactions()) {
	        Rectangle2D reactionBounds = Renderer.calculateBounds(reaction);
	        if (totalBounds.isEmpty()) {
	            totalBounds = reactionBounds;
	        } else {
	            Rectangle2D.union(totalBounds, reactionBounds, totalBounds);
	        }
	    }
	    return totalBounds;
	}
	
	public static Rectangle2D calculateBounds(IReaction reaction) {
	    // get the participants in the reaction
        IMoleculeSet reactants = reaction.getReactants();
        IMoleculeSet products = reaction.getProducts();
        if (reactants == null || products == null) return null;
        
        // determine the bounds of everything in the reaction
        Rectangle2D reactantsBounds = Renderer.calculateBounds(reactants);
        return reactantsBounds.createUnion(Renderer.calculateBounds(products));
	}

	public static Rectangle2D calculateBounds(IMoleculeSet moleculeSet) {
	    Rectangle2D totalBounds = new Rectangle2D.Double();
        for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
            IAtomContainer ac = moleculeSet.getAtomContainer(i);
            Rectangle2D acBounds = Renderer.calculateBounds(ac);
            if (totalBounds.isEmpty()) {
                totalBounds = acBounds;
            } else {
                Rectangle2D.union(totalBounds, acBounds, totalBounds);
            }
        }
        return totalBounds;
	}
	
	public static Rectangle2D calculateBounds(IAtomContainer ac) {
        // this is essential, otherwise a rectangle
        // of (+INF, -INF, +INF, -INF) is returned!
        if (ac.getAtomCount() == 0) {
            return new Rectangle2D.Double();
        } else if (ac.getAtomCount() == 1) {
            Point2d p = ac.getAtom(0).getPoint2d();
            return new Rectangle2D.Double(p.x, p.y, 10, 10);
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
   
}
