/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at <http://www.eclipse.org/legal/epl-v10.html>.
 * Contributors: Arvid goglepox@users.sf.net
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.vecmath.Point2d;

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
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.RenderingModel;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicGenerator;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    IAtomContainer  atomContainer;
    Renderer2DModel renderer2DModel;
    Transform currentTransform;

    public JChemPaintWidget(Composite parent, int style) {

        super( parent, style );
        currentTransform = new Transform(getDisplay());
        renderer2DModel = new Renderer2DModel();

        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event );
            }
        } );
    }

    private void paintControl( PaintEvent event ) {

        if ( atomContainer == null )
            return;
        RenderingModel model = new RenderingModel();
        Point size = getSize();
        Dimension dimension = new Dimension( size.x, size.y );
        double[] scalse = model.getDimensions( atomContainer, dimension );

        RenderingModel renderingModel = generateRenderingModel( model );
        Point2D center = model.center( atomContainer, dimension );
        
        
        
        currentTransform.identity();        
        currentTransform.translate( (float) center.getX(), (float) center.getY() );
        float scale = (float) Math.min( scalse[0], scalse[1] );
        currentTransform.scale( scale, -scale );
        currentTransform.invert();
        
        Transform transform = new Transform( event.gc.getDevice() );
        transform.translate( (float) center.getX(), (float) center.getY() );
        
        event.gc.setTransform( transform );
        
        SWTRenderer renderer = new SWTRenderer( event.gc, renderer2DModel, scalse );
        renderingModel.accept( renderer );
    }
    
    public Point2d getCoorFromScreen(int screenX, int screenY) {
        float[] pointArray = new float[] {screenX,screenY};
        currentTransform.transform( pointArray );
        return new Point2d(pointArray[0],pointArray[1]);
    }

    private RenderingModel generateRenderingModel( RenderingModel model ) {

        BasicBondGenerator gen2 = new BasicBondGenerator( atomContainer,
                                                          renderer2DModel );
        for ( IBond bond : atomContainer.bonds() ) {
            IRenderingElement element = gen2.generate( bond );
            if ( element != null )
                model.add( element );
        }

        BasicGenerator generator = new BasicGenerator( atomContainer,
                                                       renderer2DModel );
        for ( IAtom atom : atomContainer.atoms() ) {
            IRenderingElement element = generator.generate( atom );
            if ( element != null )
                model.add( element );
        }

        return model;
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
    
    @Override
    public void dispose() {
        currentTransform.dispose();
        super.dispose();
    }
}
