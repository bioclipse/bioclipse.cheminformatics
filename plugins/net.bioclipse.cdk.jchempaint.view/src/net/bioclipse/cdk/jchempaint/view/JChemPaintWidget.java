/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at <http://www.eclipse.org/legal/epl-v10.html>.
 * Contributors: Arvid goglepox@users.sf.net
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.AtomContainerBoundsGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    int margin = 20;

    protected IAtomContainer  atomContainer;
    protected RendererModel renderer2DModel;
    Transform currentTransform;
    Renderer renderer;
    SWTFontManager fontManager;

    public JChemPaintWidget(Composite parent, int style) {

        super( parent, style );
        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeView();
            }
        });
        
        fontManager = new SWTFontManager(this.getDisplay());
        currentTransform = new Transform(getDisplay());
        renderer2DModel = new RendererModel();
        renderer2DModel.setAtomRadius( 20 );
        renderer2DModel.setHighlightRadiusModel( .4 );
        renderer2DModel.setBondDistance( .05 );

        renderer2DModel.setShowImplicitHydrogens( true );
        renderer2DModel.setShowEndCarbons( true );
        renderer2DModel.setShowExplicitHydrogens( true );

        Collection<IGenerator> set =createGenerators();


        renderer = new Renderer(set);
        renderer.setRenderer2DModel( renderer2DModel );
        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event );
            }
        } );
    }

    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();
        generatorList.add( new AtomContainerBoundsGenerator() );
        generatorList.add( new HighlightGenerator(renderer2DModel) );
        generatorList.add( new BasicBondGenerator(renderer2DModel) );
        generatorList.add( new BasicAtomGenerator(renderer2DModel));

        return generatorList;
    }

    private void paintControl( PaintEvent event ) {
        drawBackground( event.gc, 0, 0, getSize().x, getSize().y );
        if ( atomContainer == null ) {
            setBackground( getParent().getBackground() );
            return;
        } else setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
        renderer.setBounds( new Rectangle2D.Double(margin,margin,this.getSize().x-margin*2,this.getSize().y-margin*2 ));

        SWTRenderer visitor = new SWTRenderer( event.gc,fontManager, renderer2DModel);
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

    public void setRenderer2DModel( RendererModel renderer2DModel ) {

        this.renderer2DModel = renderer2DModel;
        updateView( renderer2DModel!=null );
    }

    public RendererModel getRenderer2DModel() {
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

            public RendererModel getRenderer2DModel() {

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

            public void setRenderer2DModel( RendererModel model ) {

                throw new UnsupportedOperationException("setRenderer2DModel not supported from Controller2DHub");

            }

        };
    }

    private void disposeView() {
        currentTransform.dispose();
        fontManager.dispose();
    }


    public int getMargin() {

        return margin;
    }


    public void setMargin( int margin ) {

        this.margin = margin;
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {

        int width = 0, height = 0;

        height = Math.max(100,wHint);
        width = height;

        return new Point(width + 2, height + 2);

     }
}
