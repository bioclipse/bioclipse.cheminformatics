/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.tools.LoggingTool;

/**
 * @cdk.module render
 */
public class Renderer {

    LoggingTool logger = new LoggingTool(Renderer.class);

    RendererModel renderer2DModel;

    AffineTransform transform;
    List<IGenerator> generators;

    boolean dirty = true;
    Rectangle2D bounds;
    Point2d center = new Point2d(0, 0);

    public Renderer(Collection<IGenerator> gen) {
        renderer2DModel = new RendererModel();
        generators = new ArrayList<IGenerator>(gen);
        dirty = true;
    }

    public Point2d getCoorFromScreen(int screenX, int screenY) {
//         if(dirty) {
//             
//             return null; // what should the behavior when it is dirty
//         }
        try {
            double[] points = new double[2];
            if (transform != null) {
                transform.inverseTransform(new double[] { screenX, screenY }, 0,
                                           points, 0, 1);
                return new Point2d(points[0], points[1]);
            } else {
                return new Point2d(0,0);
            }
        } catch (NoninvertibleTransformException e) {
            logger.debug("Can not invert transform");
            return new Point2d(0,0);
        }
    }

    public RendererModel getRenderer2DModel() {

        return renderer2DModel;
    }

    public void paintMolecule(IAtomContainer atomCon,
            IRenderingVisitor renderingVisitor) {
        ElementGroup diagram = new ElementGroup();
        if (dirty)
            calculateTransform(atomCon);
        renderingVisitor.setTransform( transform );
        for (IGenerator generator : generators) {
            diagram.add(generator.generate(atomCon));
        }
        // renderingVisitor.render(diagram);
        diagram.accept(renderingVisitor);
    }

    public void setRenderer2DModel(RendererModel model) {
        renderer2DModel = model;
    }

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
        dirty = true;
    }

    /*
     * Right now we do all the scaling in one transform
     * Dose this work with generate(..., Point2d) idea?
     */
    protected void calculateTransform(IAtomContainer atomCon) {

        Rectangle2D rect = getBounds( atomCon );

        if( Double.isInfinite( rect.getX())
                || Double.isNaN( rect.getX() )) {
            rect.setFrame(-1, rect.getY(), rect.getWidth(), rect.getHeight());
        }
        if( Double.isInfinite( rect.getY())
                || Double.isNaN( rect.getY() )) {
            rect.setFrame(rect.getX(), -1, rect.getWidth(), rect.getHeight());
        }
        if( Double.isInfinite( rect.getWidth())
                || Double.isNaN( rect.getWidth() )) {
            rect.setFrame(rect.getX(), rect.getY(), 2, rect.getHeight());
        }
        if( Double.isInfinite( rect.getHeight())
                || Double.isNaN( rect.getHeight() )) {
            rect.setFrame(rect.getX(), rect.getY(), rect.getWidth(), 2 );
        }
        AffineTransform trans = new AffineTransform();
//        trans.translate(-rect.getWidth()/2,-rect.getHeight()/2 );
        double xScale = (bounds.getWidth())/(rect.getWidth());
        double yScale = (bounds.getHeight())/(rect.getHeight());
        double scale = Math.min( xScale, yScale );
//
        trans.translate( (bounds.getX() + bounds.getWidth()-rect.getWidth()*scale)/2,
                         (bounds.getY()+  bounds.getHeight()-rect.getHeight()*-scale)/2);


//        trans.translate( -rect.getX()*scale + (bounds.getWidth()-rect.getWidth()*scale)/2,
//                         -(rect.getY())*-scale + (bounds.getHeight()-rect.getHeight()*-scale)/2 );
        trans.scale( scale , -scale );
        trans.translate( -rect.getX(), -rect.getY() );

        transform = trans;
        dirty = false;
    }

    Rectangle2D getBounds(IAtomContainer ac) {
        double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymin =
                Double.POSITIVE_INFINITY , ymax = Double.NEGATIVE_INFINITY;

        for ( IAtom atom : ac.atoms() ) {
            if(atom.getPoint2d() == null) {
                logger.warn( "Atom has no 2D-coordianates" );
                logger.debug( atom );
            } else {
                double x = atom.getPoint2d().x;
                double y = atom.getPoint2d().y;
                xmin = Math.min( xmin, x );
                xmax = Math.max( xmax, x );
                ymin = Math.min( ymin, y );
                ymax = Math.max( ymax, y );
            }
        }
        return  new Rectangle2D.Double(xmin,ymin,xmax-xmin,ymax-ymin);
    }
}