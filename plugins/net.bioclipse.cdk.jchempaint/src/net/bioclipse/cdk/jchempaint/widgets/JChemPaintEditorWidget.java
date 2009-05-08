/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *      Arvid Berg
 *
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.widgets;

import static net.bioclipse.cdk.jchempaint.outline.StructureContentProvider.createCDKChemObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.cdk.jchempaint.editor.SWTMouseEventRelay;
import net.bioclipse.cdk.jchempaint.undoredo.SWTUndoRedoFactory;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.jchempaint.view.SWTRenderer;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.ControllerModel;
import org.openscience.cdk.controller.IChemModelEventRelayHandler;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.MoveModule;
import org.openscience.cdk.controller.undoredo.IUndoListener;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.ExternalHighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.SelectionGenerator;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


public class JChemPaintEditorWidget extends JChemPaintWidget
    implements ISelectionProvider, IViewEventRelay, IUndoListener {

    Logger logger = Logger.getLogger( JChemPaintEditorWidget.class );

    private Collection<ISelectionChangedListener> listeners =
                                    new ArrayList<ISelectionChangedListener>();

    private IAtom prevHighlightedAtom;
    private IBond prevHighlightedBond;

    private ICDKMolecule cdkMolecule;

    private ControllerHub hub;
    private ControllerModel c2dm;
    private SWTMouseEventRelay relay;

    private boolean isdirty = false;
    private boolean isScrolling = false;

    private IOperationHistory operationHistory =
        OperationHistoryFactory.getOperationHistory();

    private final IUndoContext undoContext = new IUndoContext() {

        public final String label = "JChemPaintEditorWidget";

        public String getLabel() {
            return label;
        }

        public boolean matches(IUndoContext context) {
            return context.getLabel().equals(label);
        }

    };

    public JChemPaintEditorWidget(Composite parent, int style) {
        super( parent,  style
                       |SWT.H_SCROLL
                       |SWT.V_SCROLL
                       |SWT.DOUBLE_BUFFERED);

        setupScrollbars();

        java.awt.Color color =
            createFromSWT(
                    getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION ));
        getRenderer().getRenderer2DModel().setSelectedPartColor(color);

        setupControllerHub();

       addControlListener( new ControlAdapter() {

           public void controlResized( ControlEvent e ) {

               resizeControl();
               redraw();
           }
       });

    }

    public Image snapshot() {
        Rectangle area = getClientArea();
        Image image = new Image(getDisplay(), area.width, area.height);
        paint(new GC(image));
        return image;
    }

    private void setupControllerHub( ) {
        IChemModel chemModel =
            NoNotificationChemObjectBuilder.getInstance().newChemModel();

        c2dm = new ControllerModel();
        UndoRedoHandler undoRedoHandler = new UndoRedoHandler();
        undoRedoHandler.addIUndoListener(this);
        hub = new ControllerHub(c2dm,
                                getRenderer(),
                                chemModel,
                                this,
                                undoRedoHandler,
                                new SWTUndoRedoFactory(this.undoContext)
        );

        hub.setEventHandler(
                new IChemModelEventRelayHandler() {

                    public void coordinatesChanged() {
                        setDirty(true);
                        setSelection(getSelection());
                        //TODO update selection => properties changed
                    }

                    public void selectionChanged() {
                        setSelection(getSelection());
                    }

                    public void structureChanged() {
                        Display.getDefault().syncExec( new Runnable() {
                            public void run() {
                                JChemPaintEditorWidget.this.structureChanged();
                            }
                        });
                        setDirty(true);
                    }

                    public void structurePropertiesChanged() {
                        JChemPaintEditorWidget.this.structurePropertiesChanged();
                        setDirty(true);
                    }

                    public void zoomChanged() {
                        resizeControl();
                        redraw();
                    }
                }
        );

        setupListeners();
        addListener( SWT.MouseHover, new Listener() {
           public void handleEvent( Event event ) {

               if(event.type == SWT.MouseHover) {
                   updateToolTip();
               }

            }
        });
        hub.setActiveDrawModule(new MoveModule(hub));

        applyGlobalProperties();

    }

    private static final int[] EVENTS = new int[]{
        SWT.MouseDoubleClick,
        SWT.MouseDown,
        SWT.MouseEnter,
        SWT.MouseExit,
//        SWT.MouseHover,
        SWT.MouseMove,
        SWT.MouseUp,
        SWT.MouseWheel
    };

    private void setupListeners() {
        relay = new SWTMouseEventRelay(hub);
        for(int event:EVENTS) {
            addListener( event, relay );
        }
    }

    private void setupScrollbars() {
        final ScrollBar hBar = getHorizontalBar();
        hBar.setEnabled(true);
        hBar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ScrollBar bar = (ScrollBar) event.widget;
                int d = bar.getSelection();
                Rectangle rect = getDiagramBounds();
                int dx = -d - rect.x;
                setIsScrolling(true);
                scroll(-d, rect.y, rect.x, rect.y, rect.width, rect.height, false);
                getRenderer().shiftDrawCenter( dx, 0 );
                setIsScrolling(false);
                update();
            }
        });

        final ScrollBar vBar = getVerticalBar();
        vBar.setEnabled(true);
        vBar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ScrollBar bar = (ScrollBar) event.widget;
                int d = bar.getSelection();
                Rectangle rect = getDiagramBounds();
                int dy = -d - rect.y;
                setIsScrolling(true);
                scroll(rect.x, -d, rect.x, rect.y, rect.width, rect.height, false);
                getRenderer().shiftDrawCenter( 0, dy);
                setIsScrolling(false);
                update();
            }
        });
    }

    private void paintControl( PaintEvent event ) {
        paint(event.gc);
    }

    private void paint(GC gc) {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        // drawBackground( event.gc, 0, 0, getSize().x, getSize().y );

        for(Message message: messages) {
            paintMessage( gc, message ,getClientArea());
        }

        if (model != null) {
            SWTRenderer visitor = new SWTRenderer(gc);
            Renderer renderer = getRenderer();

            if (isScrolling) {
                renderer.repaint(visitor);
            } else {
                renderer.paintChemModel(model, visitor);
            }
        }
    }

    private Rectangle getDiagramBounds() {
        java.awt.Rectangle r =
            getRenderer().calculateDiagramBounds(hub.getIChemModel());
        return new Rectangle(r.x, r.y, r.width, r.height);
    }

    private void applyGlobalProperties() {
        // apply the global JCP properties
        IJChemPaintGlobalPropertiesManager jcpprop =
            Activator.getDefault().getJCPPropManager();
        try {
            jcpprop.applyProperties(hub.getRenderer().getRenderer2DModel());
        } catch (BioclipseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateToolTip() {
        RendererModel rendererModel = getRenderer2DModel();
        IAtom atom = rendererModel.getHighlightedAtom();
        IBond bond = rendererModel.getHighlightedBond();

        if (atom != prevHighlightedAtom || bond != prevHighlightedBond) {
            prevHighlightedAtom = atom;
            prevHighlightedBond = bond;
            if (prevHighlightedAtom != null) {
                setToolTipText(
                        rendererModel.getToolTipText(prevHighlightedAtom));
            } else if (prevHighlightedBond != null) {
             // put getToolTipText(prevHighlightedBond) here
                setToolTipText( null );
            } else {
                setToolTipText( "" );
            }
        }
    }

    public void setIsScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    @Override
    protected void setupPaintListener() {
        addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent event ) {

                JChemPaintEditorWidget.this.paintControl( event );
            }
        } );

    }

    public void reset() {
        this.isNew = true;
    }

    public void updateView() {
        //updateSelection();
        redraw();
    }

    @Override
    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();

        generatorList.add( extensionGenerator
                           = ChoiceGenerator.getGeneratorsFromExtensionPoint());
        extensionGenerator.setUse( true );
        generatorList.add(new ExternalHighlightGenerator());
        generatorList.addAll( super.createGenerators() );
        generatorList.add(new SelectionGenerator());

        return generatorList;
    }

    @Override
    public void setModel( IChemModel model ) {
        hub.setChemModel(model);
        this.applyGlobalProperties();
        Rectangle clientRect = getClientArea();
        java.awt.Rectangle rect = new java.awt.Rectangle( clientRect.x,
                                                          clientRect.y,
                                                          clientRect.width,
                                                          clientRect.height);
        getRenderer().setup( model, rect );

        resizeControl();
        super.setModel( model );
        setDirty( false );
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        if( atomContainer != null) {

            IChemModel model = ChemModelManipulator.newChemModel( atomContainer );
            setModel( model );
        }else {
            setModel( null );
        }

    }
    public void setInput( Object element ) {

        if(element instanceof IAdaptable) {
            ICDKMolecule molecule =
                (ICDKMolecule)
                ((IAdaptable)element).getAdapter( ICDKMolecule.class );

            if (molecule != null) {
                cdkMolecule = molecule;
                IAtomContainer atomContainer = cdkMolecule.getAtomContainer();
                if(atomContainer.getAtomCount() > 0 &&
                        GeometryTools.has2DCoordinatesNew( atomContainer )<2) {
                         cdkMolecule = generate2Dfrom3D( cdkMolecule );
                         if(cdkMolecule!=null) // FIXME do what else dose empty chem model
                             atomContainer= cdkMolecule.getAtomContainer();
                         else
                             atomContainer = null;
                         setDirty( true );
                         add( Message.GENERATED );
                     }else {
                         IAtomContainer oldAC = atomContainer;
                         atomContainer = atomContainer.getBuilder()
                                 .newAtomContainer( atomContainer );
                         atomContainer.setProperties( new HashMap<Object, Object>(
                                 oldAC.getProperties()) );
                     }
                setAtomContainer(atomContainer);
            }
            else {
                IChemModel model = NoNotificationChemObjectBuilder.getInstance()
                                    .newChemModel();
                setModel( model );
            }
        }
    }

    /*
     * Utility method for copying 3D x,y to 2D coordinates
     */
    public static ICDKMolecule generate2Dfrom3D( ICDKMolecule cdkMolecule ) {

        ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        ICDKMolecule newMolecule;
        try {
            newMolecule = cdk.generate2dCoordinates( cdkMolecule );
        } catch ( Exception e ) {
            // FIXME make non static
            return null;
        }
        newMolecule.getAtomContainer().setProperties( new HashMap<Object, Object>(
                cdkMolecule.getAtomContainer().getProperties()) );

        return newMolecule;
    }

    public ControllerHub getControllerHub() {
        return hub;
    }

    public void addSelectionChangedListener( ISelectionChangedListener listener ) {

        listeners.add( listener );

    }

    public ISelection getSelection() {
        RendererModel rendererModel = getRenderer2DModel();
        if (rendererModel == null)
            if(cdkMolecule != null)
                return new StructuredSelection(cdkMolecule);
            else
               return StructuredSelection.EMPTY;

        List<CDKChemObject<?>> selection = new LinkedList<CDKChemObject<?>>();

        IChemObjectSelection sel = rendererModel.getSelection();
        IAtomContainer modelSelection = sel.getConnectedAtomContainer();

        if (modelSelection != null) {
            for (IAtom atom : modelSelection.atoms()) {
                selection.add(createCDKChemObject(atom));
            }

            for (IBond bond : modelSelection.bonds()) {
                selection.add(createCDKChemObject(bond));
            }
        }

        if (selection.isEmpty() && cdkMolecule != null) {
            return new StructuredSelection(cdkMolecule);
        }

        return new StructuredSelection(selection);
    }

    public void removeSelectionChangedListener(
                                          ISelectionChangedListener listener ) {

        listeners.remove( listener );

    }

    public void setSelection( ISelection selection ) {
        final SelectionChangedEvent e =
            new SelectionChangedEvent(this, selection);
        Object[] listenersArray = listeners.toArray();

        for (int i = 0; i < listenersArray.length; i++) {
            final ISelectionChangedListener l = (ISelectionChangedListener)
                                                              listenersArray[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(e);
                }
            });
        }

    }

    public void setActiveDrawModule(IControllerModule activeDrawModule){
		hub.setActiveDrawModule(activeDrawModule);
	}

    protected void structureChanged() {
        resizeControl();
    }

    protected void structurePropertiesChanged() {
    }

	public void setDirty( boolean dirty) {
	    this.isdirty = dirty;
	    if(!this.isDisposed()) {
	        Display.getDefault().asyncExec( new Runnable() {
	            public void run() {
	                if(isdirty) {
	                    add(Message.DIRTY);
	                }
	                else
	                    remove( Message.DIRTY );
	                redraw();
	            }
	        });
	    }
	}

	public boolean getDirty() {
	    return isdirty;
	}

	private java.awt.Color createFromSWT(org.eclipse.swt.graphics.Color color) {
	    return new java.awt.Color( color.getRed(),
	                               color.getGreen(),
	                               color.getBlue());
	}

	public void undo() throws ExecutionException {
	    if (this.operationHistory.canUndo(this.undoContext)) {
	        this.operationHistory.undo(undoContext, null, null);
	    }
	}


	public void redo() throws ExecutionException {
	    if (this.operationHistory.canRedo(this.undoContext)) {
            this.operationHistory.redo(undoContext, null, null);
        }
	}


    public void doUndo(IUndoRedoable undoredo) {
        operationHistory.add((IUndoableOperation)undoredo);
    }

    private void resizeControl() {
        final ScrollBar hBar = getHorizontalBar();
        final ScrollBar vBar = getVerticalBar();

        Integer xVal=null,
                yVal=null;

        Rectangle rect = getDiagramBounds();
        Rectangle client = getClientArea();

        hBar.setMaximum (rect.width);
        vBar.setMaximum (rect.height);
        hBar.setThumb (Math.min (rect.width, client.width));
        vBar.setThumb (Math.min (rect.height, client.height));
        int hPage = rect.width - client.width;
        int vPage = rect.height - client.height;
        int hSelection = hBar.getSelection ();
        int vSelection = vBar.getSelection ();
        if (hSelection >= hPage) {
          if (hPage <= 0) hSelection = 0;// negative width fits in should center
          xVal = -hSelection+client.width/2-rect.width/2;
        }
        if (vSelection >= vPage) {
          if (vPage <= 0) vSelection = 0;
          yVal = -vSelection + client.height/2-rect.height/2;
        }
        int dx = xVal!=null?xVal-rect.x:0;
        int dy = yVal!=null?yVal-rect.y:0;

        if(dx!=0 || dy!=0)
            getRenderer().shiftDrawCenter( dx, dy);
    }

    public ICDKMolecule getMolecule() {
        return cdkMolecule;
    }
}