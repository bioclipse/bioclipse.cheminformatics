/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.periodictable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.bioclipse.cdk.domain.CDKChemObject;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.XMLIsotopeFactory;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.periodictable.PeriodicTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author arvid
 *
 */
@SuppressWarnings("serial")
public class PeriodicTableView extends ViewPart implements ISelectionProvider{

    private static final Logger logger = LoggerFactory.getLogger( PeriodicTableView.class );

    final int[] atomicNumbers;
    Inset inset = new Inset();

    ColorAWTtoSWTConverter colorConverter;

    Canvas canvas;

    CDKChemObject<IElement> selection;

    ListenerList listeners = new ListenerList();
    ISelection theSelection = StructuredSelection.EMPTY;

    static final Map<String,java.awt.Color> colorMap = new HashMap<String, java.awt.Color>(){
        {
            put("Alkali Metals",new java.awt.Color(0xFF6666));
            put("Alkali Earth Metals",new java.awt.Color(0xFFDEAD));
            put("Lanthanides",new java.awt.Color(0xFFBFFF));
            put("Actinides",new java.awt.Color(0xFF99CC));
            put("Transition metals",new java.awt.Color(0xFFC0C0));
            put("Metals",new java.awt.Color(0xCCCCCC));
            put("Metalloids",new java.awt.Color(0xCCCC99));
            put("Nonmetals",new java.awt.Color(0xA0FFA0));
            put("Halogens",new java.awt.Color(0xFFFF99));
            put("Noble Gasses",new java.awt.Color(0xC0FFFF));
        }
    };

    static final Map<String,java.awt.Color> stateColorMap = new HashMap<String, java.awt.Color>(){
        {
            put("Gas",java.awt.Color.GREEN);
            put("Synthetic",new java.awt.Color(0xEEEEEE));
            put("Liquid",java.awt.Color.BLUE);
            put("Solid",java.awt.Color.BLACK);

        }
    };

    Point extent = new Point(0,0);
    private Font font;


    public PeriodicTableView() {
        SortedSet<Integer> candidates = new TreeSet<Integer>();
        for(int i=1;i<1000;i++) {
            if( null != PeriodicTable.getSymbol( i )) {
                candidates.add( i );
            }
        }
        atomicNumbers = new int[candidates.size()];
        int i = 0;
        for(int number:candidates) {
            atomicNumbers[i++] = number;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl( Composite parent ) {

        canvas = new Canvas(parent,SWT.NONE);
        canvas.setBackground( canvas.getDisplay().getSystemColor( SWT.COLOR_WHITE ) ) ;
        colorConverter = new ColorAWTtoSWTConverter(canvas.getDisplay());
        canvas.addDisposeListener( new DisposeListener() {

            public void widgetDisposed( DisposeEvent e ) {

                colorConverter.dispose();
                if(font!=null)
                    font.dispose();
            }

        });

        canvas.addControlListener( new ControlAdapter() {
            @Override
            public void controlResized( ControlEvent e ) {
                resizeControl(e);
            }
        });

        canvas.addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent e ) {
                PeriodicTableView.this.paintControl( e );
            }

        });

       initSelection();
       getSite().setSelectionProvider( this );

       int ops = DND.DROP_COPY;
       Transfer[] transfers = new Transfer[] { TextTransfer.getInstance()};
       this.addDragSupport(ops, transfers, new DragSourceListener() {

        public void dragFinished( DragSourceEvent event ) {
        }

        public void dragSetData( DragSourceEvent event ) {

            // Provide the data of the requested type.
            if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                event.data = selection.getChemobj().getSymbol();
            }
        }

        public void dragStart( DragSourceEvent event ) {
            if(selection==null)
                event.doit = false;
        }

       });

    }

    private String checkHit(Point p) {
        Rectangle rect = new Rectangle(0,0,extent.x,extent.y);

        for(int number: atomicNumbers) {
            String symbol = PeriodicTable.getSymbol( number );
            Point gp = getGroupPeriodFor( symbol );
            if( gp == null) continue;

            rect.x = gp.x;
            rect.y = gp.y;

            if(rect.contains( p )) return symbol;
        }
        return null;
    }

    private void initSelection() {

        canvas.addMouseListener( new MouseAdapter() {
           @Override
            public void mouseDown( MouseEvent e ) {

               String s = checkHit( new Point(e.x,e.y) );
               if(s!=null) {
                   try {
                       IsotopeFactory isf;
                        isf = XMLIsotopeFactory.getInstance( SilentChemObjectBuilder.getInstance() );
                       IElement element = isf.getElement( s );
                       element.setProperty( CDKConstants.TITLE, PeriodicTable.getName( s ));
                       selection = new CDKChemObject<IElement>(element);
                       setSelection( new StructuredSelection(selection) );
                   } catch (IOException ex) {
                       logger.error("Could not get element from atomic symbol",ex);
                   }
               }else {
                   selection = null;
                   setSelection(  StructuredSelection.EMPTY );
               }
               canvas.redraw();
           }
        });

    }

    protected void resizeControl(ControlEvent event) {
        Rectangle clientArea = canvas.getClientArea();
        int width = clientArea.width-1;
        int height = clientArea.height-1;
        int rowHeight = (int) Math.floor(height/9.5);
        int columnWidth = (int) Math.floor( width/18d);

        int xExtra = (int)(( width-columnWidth*18)/2d);
        int yExtra = (int)(( height-rowHeight*9.5)/2d);
        inset.left = inset.right = xExtra;
        inset.top = inset.bottom = yExtra;

        extent = new Point(columnWidth,rowHeight);
        if(font !=null && !font.isDisposed()) {
            Font newFont = font;
            font.dispose();
            FontData fd = newFont.getFontData()[0];
            fd.setHeight( (int) (.50 * width/18d) );
            font = new Font(canvas.getDisplay(),fd);
        }

    }

    private Point getGroupPeriodFor(String symbol) {
        Integer gTmp = PeriodicTable.getGroup( symbol );
        Integer pTmp = PeriodicTable.getPeriod(symbol);
        if(symbol.equals( "Ds" )) {
            gTmp = 10;
            pTmp = 7;
        }
        double group,period;

        if ( (gTmp != null && gTmp > 0) && pTmp != null && !(symbol.equals( "La" ) || symbol.equals( "Ac" )) ) {
            group = gTmp;
            period = pTmp;
        }
        else if(pTmp != null){
            int atomNumber = PeriodicTable.getAtomicNumber( symbol );
            if(atomNumber<89) {
                group = 4+atomNumber-58;
            }else
                group = 4+atomNumber-90;
            period = pTmp + 2.5;
        }else {
            return null;
        }
        int x = (int) ((group-1)*extent.x)+inset.left;
        int y = (int) ((period-1)*extent.y)+inset.top;

        return new Point(x,y);
    }

    protected void paintControl(PaintEvent e) {

        GC gc = e.gc;

        if(font!=null)
            gc.setFont( font );

        for(int number:atomicNumbers) {
            String symbol = PeriodicTable.getSymbol( number );
            Point gP = getGroupPeriodFor( symbol );
            if(gP == null)
                continue;
            String ser = PeriodicTable.getChemicalSeries( symbol );
            String state = PeriodicTable.getPhase( symbol );

            int x = gP.x;
            int y = gP.y;

            java.awt.Color color = colorMap.get( ser );
            if(color != null) {
                gc.setBackground( colorConverter.toSWTColor( color ) );
                gc.fillRectangle( x,y , extent.x, extent.y );
            }
            //gc.setForeground( colorConverter.toSWTColor( stateColorMap.get( state ) ) );
            gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
            Point ext2 = gc.textExtent( symbol );
            int width   = extent.x-ext2.x;
            int height = extent.y-ext2.y;
            gc.drawText( symbol, x+width/2,y+height/2,true);
            gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

            gc.drawRectangle( x,y , extent.x, extent.y );
        }
        if( selection != null ) {
            int oldWidth = gc.getLineWidth();
            Color oldColor = gc.getForeground();
            gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_LIST_SELECTION ) );
            Point gP = getGroupPeriodFor( selection.getChemobj().getSymbol() );
            gc.setLineWidth( 3 );
            gc.drawRectangle( gP.x,gP.y , extent.x, extent.y );
            gc.setLineWidth( oldWidth );
            gc.setForeground( oldColor );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {

        // TODO Auto-generated method stub

    }

    static class ColorAWTtoSWTConverter {

        Device device;
        Color DEFAULT_COLOR;
        HashMap<java.awt.Color, org.eclipse.swt.graphics.Color> lookup;
        boolean isDisposing = false;
        public ColorAWTtoSWTConverter(Device device) {
            this.device=device;
            lookup = new HashMap<java.awt.Color, org.eclipse.swt.graphics.Color>();
            DEFAULT_COLOR = this.device.getSystemColor(SWT.COLOR_MAGENTA);
        }

        public Color toSWTColor(java.awt.Color color) {
            if(isDisposing) {
                throw new SWTError(SWT.ERROR_WIDGET_DISPOSED);
            }
            if (color == null) {
                return DEFAULT_COLOR;
            }

            assert(color != null);

            Color otherColor = lookup.get(color);
            if (otherColor == null) {
                otherColor = new Color(device,
                                       color.getRed(),
                                       color.getGreen(),
                                       color.getBlue());
                lookup.put(color,otherColor);
            }
            return otherColor;
        }

        public void dispose() {
            isDisposing = true;
            for(Color c:lookup.values()) {
                c.dispose();
            }
        }
    }

    public void addSelectionChangedListener( ISelectionChangedListener listener ) {

        if(listeners==null)
            listeners = new ListenerList();

       listeners.add(listener);
    }

    public ISelection getSelection() {

       return theSelection;
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {

        listeners.remove( listener );
    }

    public void setSelection( ISelection selection ) {
        theSelection = selection;
        final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);

        Object[] listenersArray = listeners.getListeners();

        for(Object listener:listenersArray) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listener;
            SafeRunner.run( new ISafeRunnable() {

                public void handleException( Throwable exception ) {

                }

                public void run() throws Exception {
                    l.selectionChanged( e );
                }
            });
        }
    }

    public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {

        Control myControl = canvas;
        final DragSource dragSource = new DragSource(myControl, operations);
        dragSource.setTransfer(transferTypes);
        dragSource.addDragListener(listener);
      }
}

class Inset {
    int left,right,top,bottom;

    public Inset() {
        left=right=top=bottom=2;
    }
}