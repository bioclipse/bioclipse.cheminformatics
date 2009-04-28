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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.AtomContainerBoundsGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    static protected class Message {
        enum Alignment{
            TOP_LEFT,
            TOP_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT,
        }

        public static Message DIRTY = new Message( "Changed",
                                                   Alignment.BOTTOM_LEFT);

        public static Message GENERATED = new Message( "Generated",
                                                   Alignment.BOTTOM_RIGHT);
        public final Font font;
        public final Color color;
        public final Alignment alignment;

        public final String text;


        private Message(String text, Alignment alignment) {
            this.text = text;
            this.alignment = alignment;
            font = Display.getDefault().getSystemFont();
            color = Display.getDefault().getSystemColor( SWT.COLOR_DARK_MAGENTA );

        }

        public void dispose() {
            font.dispose();
            color.dispose();
        }
    }

    //protected IAtomContainer  atomContainer;
    protected IChemModel model;

    protected RendererModel rendererModel = new RendererModel();

    protected ChoiceGenerator extensionGenerator;

    private Renderer renderer;

    private SWTFontManager fontManager;

    protected Set<Message> messages = new HashSet<Message>();

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

        if ( model == null ) {
            setBackground( getParent().getBackground() );
            return;
        } else
            setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

        Rectangle c = getClientArea();
        Rectangle2D drawArea = new Rectangle2D.Double( c.x, c.y,
                                                         c.width, c.height);

        for(Message message: messages) {
            paintMessage( event.gc, message );
        }

        SWTRenderer visitor = new SWTRenderer( event.gc );

        renderer.paintChemModel( model, visitor,drawArea,true );

        if( ChemModelManipulator.getAtomCount( model )!=0) {
            isNew = false;
        }
    }

    public void setModel(IChemModel model) {
        if(   model!=null
          ) {
            if(this.model !=model)
                isNew = true;
            this.model = model;
            updateView( true );
        } else {
            this.model = null;
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

    protected void paintMessage( GC gc, Message message ) {
        Font oldFont = gc.getFont();
        Color oldColor = gc.getForeground();

        Rectangle clientRect = getClientArea();
        gc.setFont(message.font);

        int x = 0;
        switch(message.alignment) {
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                x = clientRect.width - gc.textExtent( message.text ).x;
        }

        int y = 0;
        switch(message.alignment) {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                y = clientRect.height-gc.getFontMetrics().getHeight();
        }

        gc.setForeground( message.color );
        gc.drawText( message.text, x , y );

        gc.setFont( oldFont );
        gc.setForeground( oldColor );
    }

    public void add(Message message) {
        messages.add( message );
        redraw();
    }

    public void remove(Message message) {
        messages.remove( message );
        redraw();
    }
}
