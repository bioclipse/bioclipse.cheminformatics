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
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    protected IAtomContainer  atomContainer;

    protected RendererModel rendererModel = new RendererModel();

    protected ChoiceGenerator extensionGenerator;

    private Renderer renderer;

    private SWTFontManager fontManager;

    /**
     * A new model has to reset the center
     */
    protected boolean isNew = true;

    public JChemPaintWidget(Composite parent, int style) {

        super( parent, style|SWT.DOUBLE_BUFFERED );
        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeView();
            }
        });

        fontManager = new SWTFontManager(this.getDisplay());

        renderer = new Renderer(createGenerators(), fontManager);

        rendererModel = renderer.getRenderer2DModel();

//        rendererModel.setFitToScreen( true );
//        rendererModel.setShowImplicitHydrogens( true );
//        rendererModel.setShowEndCarbons( true );
//        rendererModel.setShowExplicitHydrogens( true );
//        rendererModel.setHighlightShapeFilled( true );
//        rendererModel.setShowAromaticity( true );
//        rendererModel.setShowAromaticityCDKStyle( false );
        setupPaintListener();
    }

    protected void setupPaintListener() {
        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event );
            }
        } );
    }

    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();

        generatorList.add( extensionGenerator
                           = ChoiceGenerator.getGeneratorsFromExtensionPoint());

        generatorList.add( new AtomContainerBoundsGenerator() );
        generatorList.add( new HighlightAtomGenerator() );
        generatorList.add( new HighlightBondGenerator() );
        generatorList.add( new RingGenerator() );
        generatorList.add( new BasicAtomGenerator());

        return generatorList;
    }

    private void paintControl( PaintEvent event ) {

        //drawBackground( event.gc, 0, 0, getSize().x, getSize().y );

        if ( atomContainer == null ) {
            setBackground( getParent().getBackground() );
            return;
        } else
            setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

        Rectangle c = getClientArea();
        Rectangle2D drawArea = new Rectangle2D.Double( c.x, c.y,
                                                         c.width, c.height);

        SWTRenderer visitor = new SWTRenderer( event.gc );

        renderer.paintMolecule(atomContainer, visitor, drawArea,true);

        if (!(atomContainer.getAtomCount() == 0)) {
            isNew = false;
        }
    }

    public void setAtomContainer( IAtomContainer atomContainer ) {

        if( (atomContainer!=null)
                    && (GeometryTools.has2DCoordinates( atomContainer ))) {
            if(this.atomContainer != atomContainer)
                isNew = true;
            this.atomContainer = atomContainer;

            updateView( true );
        }else {
            this.atomContainer = null;
            updateView( false );
        }
    }

    public void setRenderer2DModel( RendererModel renderer2DModel ) {

        this.rendererModel = renderer2DModel;
        updateView( renderer2DModel!=null );
    }

    public RendererModel getRenderer2DModel() {
        return rendererModel;
    }

    private void updateView(boolean show) {
        if (isVisible() != show) {
            setVisible( show );
        }
        redraw();
    }

    public Renderer getRenderer() {
        return renderer;
    }

    private void disposeView() {

        fontManager.dispose();
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {

        int width = 0, height = 0;

        height = Math.max(100, wHint);
        width = height;

        return new Point(width + 2, height + 2);

     }

    public void setUseExtensionGenerators( boolean useExtensionGenerators ) {
        extensionGenerator.setUse( useExtensionGenerators);
        this.redraw();
    }
}
