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
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector2d;

import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openscience.cdk.event.ICDKChangeListener;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.IRenderer;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.SWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.RadicalGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.AtomNumberTextColor;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

/**
 * @author arvid
 */
public class JChemPaintWidget extends Canvas {

    static public class Message {
        public static enum Alignment{
            TOP_LEFT,
            TOP_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT,
        }

        public static Message DIRTY = new Message( "Changed",
                                                   Alignment.TOP_LEFT);

        public static Message GENERATED = new Message( "Generated",
                                                   Alignment.BOTTOM_RIGHT);
        public final Font font;
        public final Color color;
        public final Alignment alignment;

        public final String text;

        public Message(String text, Alignment alignment) {
            this.text = text;
            this.alignment = alignment;
            Font f = Display.getDefault().getSystemFont();
            FontData fd = f.getFontData()[0];
            fd.setHeight(16);
            fd.setStyle(SWT.BOLD);
            font = new Font(Display.getDefault(),fd);
            color = Display.getDefault().getSystemColor( SWT.COLOR_DARK_MAGENTA );

        }

        public void dispose() {
            font.dispose();
            color.dispose();
        }
    }

    protected ICDKMolecule source;

    protected IChemModel model;

    protected RendererModel rendererModel = new RendererModel();

    protected ChoiceGenerator extensionGenerator;
    protected ChoiceGenerator drawNumbers;

    private Renderer renderer;

    private SWTFontManager fontManager;

    private Set<Message> messages = new HashSet<Message>();

    public JChemPaintWidget(Composite parent, int style) {
        super( parent, style|SWT.DOUBLE_BUFFERED );
        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeView();
            }
        });

        fontManager = new SWTFontManager(this.getDisplay());

        List<IGenerator<IAtomContainer>> generators =  createGenerators();
        generators.add( drawNumbers = new ChoiceGenerator(

          new AtomNumberGenerator(new Vector2d(7,-7))
        ) );

        renderer = new Renderer(generators, fontManager);
        rendererModel = renderer.getRenderer2DModel();
        setupPaintListener();
        setupPreferenceListener( renderer );
        setAtomNumberColors( drawNumbers );
    }

    private void setAtomNumberColors(IGenerator<IAtomContainer> generator) {
        for(IGeneratorParameter<?> p:generator.getParameters()) {
            if(p instanceof AtomNumberTextColor) {
                ((AtomNumberTextColor)p).setValue( java.awt.Color.MAGENTA );
            }
        }
    }

    private void setupPreferenceListener(IRenderer renderer) {
      renderer.getRenderer2DModel().addCDKChangeListener( new ICDKChangeListener() {

        public void stateChanged( EventObject event ) {
            if(event.getSource() instanceof RendererModel) {
                drawNumbers.setUse( JChemPaintWidget.this.renderer
                                       .getRenderer2DModel().drawNumbers());
            }
        }
    });
    }

    protected void setupPaintListener() {
        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintWidget.this.paintControl( event.gc );
            }
        } );
    }

    protected List<IGenerator<IAtomContainer>> createGenerators() {
        List<IGenerator<IAtomContainer>> generatorList =
        	new ArrayList<IGenerator<IAtomContainer>>();

        generatorList.addAll(ChoiceGenerator.getGeneratorsFromExtension());
        // This generator can be used for debugging partitioning problems
        //generatorList.add( new AtomContainerBoundsGenerator() );
        generatorList.add(new BasicSceneGenerator());
        generatorList.add( new RingGenerator() );
        generatorList.add( new BasicAtomGenerator());
        generatorList.add( new RadicalGenerator());
        generatorList.add( new HighlightAtomGenerator() );
        generatorList.add( new HighlightBondGenerator() );

        return generatorList;
    }

    public Image snapshot() {

        Rectangle area = getClientArea();
        Image image = new Image( getDisplay(), area.width, area.height );
        paintControl(  new GC( image ) );
        return image;
    }

    protected void paint(IDrawVisitor visitor) {

            Rectangle c = getClientArea();
            Rectangle2D drawArea = new Rectangle2D.Double( c.x, c.y,
                                                           c.width, c.height);
            renderer.paintChemModel( model, visitor,drawArea,true );
    }

    private void paintControl( GC gc ) {

        if(model != null ) {
            for(Message message: messages) {
            paintMessage( gc, message ,getClientArea());
            }
            SWTRenderer visitor = new SWTRenderer( gc );
            paint(visitor);
        }
    }

    public void setModel(IChemModel model) {
        if ( model != null ) {
            this.model = model;
            setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
            renderer.setup(model, adaptRectangle(getClientArea()));
            updateView( true );
        } else {
            this.model = null;
            setBackground( getParent().getBackground() );
            updateView( false );
        }
    }

    private java.awt.Rectangle adaptRectangle(Rectangle rect) {
    	return new java.awt.Rectangle(rect.x,rect.y,rect.width,rect.height);
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

    protected void disposeView() {
        fontManager.dispose();
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {

        int size = Math.max(100, wHint) +2;
        return new Point(size, size);
     }

    public void setUseExtensionGenerators( boolean useExtensionGenerators ) {
        if(extensionGenerator!=null) {
            extensionGenerator.setUse( useExtensionGenerators);
            this.redraw();
        }
    }

    public static void paintMessage( GC gc, Message message , Rectangle rect) {
        Font oldFont = gc.getFont();
        Color oldColor = gc.getForeground();
        gc.setFont(message.font);

        int x = 0;
        switch(message.alignment) {
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                x = rect.x + rect.width - gc.textExtent( message.text ).x;
        }

        int y = 0;
        switch(message.alignment) {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                y = rect.y + rect.height-gc.getFontMetrics().getHeight();
        }

        gc.setForeground( message.color );
        gc.drawText( message.text, x , y );

        gc.setFont( oldFont );
        gc.setForeground( oldColor );
    }

    public void add(Message message) {
        messages.add( message );
        if(!isDisposed())
            redraw();
    }

    public void remove(Message message) {
        messages.remove( message );
        if(!isDisposed())
            redraw();
    }

    public ICDKMolecule getMolecule() {
        return source;
    }
}
