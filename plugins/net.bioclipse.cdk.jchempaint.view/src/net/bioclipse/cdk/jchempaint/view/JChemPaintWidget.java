/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *      Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
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

    private int margin = 20;

    protected IAtomContainer  atomContainer;
    
    protected RendererModel renderer2DModel = new RendererModel();
    
    private Renderer renderer;
    
    private SWTFontManager fontManager;

    public JChemPaintWidget(Composite parent, int style) {

        super( parent, style );
        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeView();
            }
        });

        fontManager = new SWTFontManager(this.getDisplay());

        List<IGenerator> set = createGenerators();
        renderer = new Renderer(set,fontManager);

        renderer2DModel = renderer.getRenderer2DModel();
      
        renderer2DModel.setFitToScreen( true );
        renderer2DModel.setShowImplicitHydrogens( true );
        renderer2DModel.setShowEndCarbons( true );
        renderer2DModel.setShowExplicitHydrogens( true );

        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event );
            }
        } );
    }

    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();
        generatorList.add( new AtomContainerBoundsGenerator() );
        generatorList.add( new HighlightGenerator() );
        generatorList.add( new BasicBondGenerator() );
        generatorList.add( new BasicAtomGenerator());

        return generatorList;
    }

    private void paintControl( PaintEvent event ) {
        drawBackground( event.gc, 0, 0, getSize().x, getSize().y );
        if ( atomContainer == null ) {
            setBackground( getParent().getBackground() );
            return;
        } else setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

        Rectangle c = getClientArea();
        Rectangle2D clientArea =
            new Rectangle2D.Double(c.x, c.y, c.width, c.height); 
        SWTRenderer visitor = new SWTRenderer( event.gc);

        renderer.paintMolecule(atomContainer, visitor, clientArea, true);
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

    public Renderer getRenderer() {
        return renderer;
    }

    private void disposeView() {

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
