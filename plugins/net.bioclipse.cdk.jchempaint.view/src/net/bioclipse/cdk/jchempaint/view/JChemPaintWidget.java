/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at <http://www.eclipse.org/legal/epl-v10.html>.
 * Contributors: Arvid goglepox@users.sf.net
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.renderer.Java2DRenderer;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.generators.AtomContainerBoundsGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    public static int MARGIN = 20;

    IAtomContainer  atomContainer;
    Renderer2DModel renderer2DModel;
    Transform currentTransform;
    Renderer renderer;

    public JChemPaintWidget(Composite parent, int style) {

        super( parent, style );
        setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
        currentTransform = new Transform(getDisplay());
        renderer2DModel = new Renderer2DModel();
        renderer2DModel.setAtomRadius( 20 );
        renderer2DModel.setHighlightRadiusModel( .4 );

        renderer2DModel.setShowImplicitHydrogens( true );
        renderer2DModel.setShowEndCarbons( true );
        renderer2DModel.setShowExplicitHydrogens( true );

        Collection<IGenerator> set = new ArrayList<IGenerator>();
        set.add( new AtomContainerBoundsGenerator() );
        set.add( new BasicBondGenerator(renderer2DModel) );
        set.add( new BasicAtomGenerator(renderer2DModel));
        set.add( new HighlightGenerator(renderer2DModel) );
        

        renderer = new Renderer(set);
        renderer.setRenderer2DModel( renderer2DModel );
        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event );
            }
        } );
    }

    private void paintControl( PaintEvent event ) {

        if ( atomContainer == null )
            return;
        renderer.setBounds( new Rectangle2D.Double(0,0,this.getSize().x,this.getSize().y ));
        
        SWTRenderer visitor = new SWTRenderer( event.gc, renderer2DModel);
        renderer.paintMolecule( atomContainer, visitor );
    }

    public Point2d getCoorFromScreen(int screenX, int screenY) {
        float[] pointArray = new float[] {screenX,screenY};
        currentTransform.transform( pointArray );
        return new Point2d(pointArray[0],pointArray[1]);
    }

    public void setAtomContainer( IAtomContainer atomContainer ) {

        this.atomContainer = atomContainer;
        updateView( (atomContainer!=null)
                    && (GeometryTools.has2DCoordinates( atomContainer )) );
    }

    public void setRenderer2DModel( Renderer2DModel renderer2DModel ) {

        this.renderer2DModel = renderer2DModel;
        updateView( renderer2DModel!=null );
    }

    public Renderer2DModel getRenderer2DModel() {
        return renderer2DModel;
    }

    private void updateView(boolean show) {
        if(isVisible() != show)
            setVisible( show );
        redraw();
    }

    public IJava2DRenderer getRenderer() {
        return new IJava2DRenderer() {

            public Point2d getCoorFromScreen( int screenX, int screenY ) {

                return renderer.getCoorFromScreen( screenX, screenY );
            }

            public Renderer2DModel getRenderer2DModel() {

                return JChemPaintWidget.this.getRenderer2DModel();
            }

            public void paintMolecule( IAtomContainer atomCon,
                                       Graphics2D graphics ) {

                throw new UnsupportedOperationException("paintMolecule not supported from Controller2DHub");

            }

            public void paintMolecule( IAtomContainer atomCon,
                                       Graphics2D graphics, Rectangle2D bounds ) {

                throw new UnsupportedOperationException("paintMolecule not supported from Controller2DHub");

            }

            public void setRenderer2DModel( Renderer2DModel model ) {

                throw new UnsupportedOperationException("setRenderer2DModel not supported from Controller2DHub");

            }

        };
    }

    @Override
    public void dispose() {
        currentTransform.dispose();
        super.dispose();
    }
}
