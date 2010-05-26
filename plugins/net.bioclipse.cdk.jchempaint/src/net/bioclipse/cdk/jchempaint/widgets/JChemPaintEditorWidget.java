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
import static org.openscience.cdk.geometry.GeometryTools.has2DCoordinatesNew;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.cdk.jchempaint.editor.SWTMouseEventRelay;
import net.bioclipse.cdk.jchempaint.preferences.GenerateLabelPrefChangedLisener;
import net.bioclipse.cdk.jchempaint.preferences.PreferenceConstants;
import net.bioclipse.cdk.jchempaint.undoredo.SWTUndoRedoFactory;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.ControllerModel;
import org.openscience.cdk.controller.IChemModelEventRelayHandler;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.MoveModule;
import org.openscience.cdk.controller.PhantomBondGenerator;
import org.openscience.cdk.controller.undoredo.IUndoListener;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.ExternalHighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.MergeAtomsGenerator;
import org.openscience.cdk.renderer.generators.SelectAtomGenerator;
import org.openscience.cdk.renderer.generators.SelectBondGenerator;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JChemPaintEditorWidget extends JChemPaintWidget
    implements ISelectionProvider, IViewEventRelay, IUndoListener {

    Logger logger = Logger.getLogger( JChemPaintEditorWidget.class );

    private ListenerList listeners = new ListenerList();

    private IAtom prevHighlightedAtom;
    private IBond prevHighlightedBond;

    private ControllerHub hub;
    private ControllerModel c2dm;
    private SWTMouseEventRelay relay;

    private boolean new2Dcoordinates = false;
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

    private PhantomBondGenerator phantomGenerator;

    private GenerateLabelPrefChangedLisener prefListener;

    public JChemPaintEditorWidget(Composite parent, int style) {
        super( parent,  style
                       |SWT.H_SCROLL
                       |SWT.V_SCROLL
                       |SWT.DOUBLE_BUFFERED);

        setupScrollbars();

        RendererModel rModel = getRenderer().getRenderer2DModel();
        // Commented becaus of bug 1100 selectioncolor not good on windows
        //java.awt.Color color = createFromSWT( SWT.COLOR_LIST_SELECTION );
        java.awt.Color color = Color.BLUE;//new java.awt.Color(0xc2deff);
        color = new java.awt.Color( color.getRed(),
                                    color.getGreen(),
                                    color.getBlue(),
                                    128);
        rModel.setSelectedPartColor(color);
        rModel.setSelectionRadius( 8 );

        rModel.setHighlightShapeFilled( true );
        rModel.setHoverOverColor( new Color( Color.GRAY.getRed(),
                                             Color.GRAY.getGreen(),
                                             Color.GRAY.getBlue(),
                                             128) );

        setupControllerHub();

        addControlListener( new ControlAdapter() {

            public void controlResized( ControlEvent e ) {

                resizeControl();
                redraw();
            }
        });

        int ops = DND.DROP_COPY;
        final TextTransfer textTransfer = TextTransfer.getInstance();
        Transfer[] transfers = new Transfer[] { textTransfer};
        this.addDropSupport(ops, transfers, new DropTargetListener() {

            public void dragEnter( DropTargetEvent event ) {
                event.detail = DND.DROP_COPY;
            }

            public void dragLeave( DropTargetEvent event ) {

                // TODO Auto-generated method stub

            }

            public void dragOperationChanged( DropTargetEvent event ) {
                event.detail = DND.DROP_COPY;
            }

            public void dragOver( DropTargetEvent event ) {

                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
                if (textTransfer.isSupportedType(event.currentDataType)) {
                    // NOTE: on unsupported platforms this will return null
                    Object o = textTransfer.nativeToJava(event.currentDataType);
                    String t = (String)o;
                    if (t != null) System.out.println(t);
                }

            }

            public void drop( DropTargetEvent event ) {
                if (textTransfer.isSupportedType(event.currentDataType)) {
                    String text = (String)event.data;

//                    ICDKManager cdk = net.bioclipse.cdk.business.Activator
//                                        .getDefault().getJavaCDKManager();
//                    try {
//                        ICDKMolecule mol = cdk.fromSMILES( text );
//                        IAtomContainer ac = mol.getAtomContainer();
//                        if(ac.getAtomCount()==1) {
//                            IJChemPaintManager jcp = Activator.getDefault()
//                                                            .getJavaManager();
//                            Point2d point = new Point2d(event.x,event.y);
//                            jcp.addAtom( ac.getAtom( 0 ), point);
//                        }
//                    } catch ( BioclipseException e ) {
//                        logger.debug( "Could not create molecuel form text" );
//                    }
                    IJChemPaintManager jcp = Activator.getDefault()
                    .getJavaManager();
                    Point p = JChemPaintEditorWidget.this.toControl( event.x,
                                                                     event.y );
                    Point2d point = getRenderer().toModelCoordinates( p.x,
                                                                      p.y );
                    jcp.addAtom( text, point);
                }
            }

            public void dropAccept( DropTargetEvent event ) {

                // TODO Auto-generated method stub

            }

        });


     // create a tooltip
        ToolTip tooltip = new DefaultToolTip(this) {
            @Override
            protected String getText( Event event ) {

                RendererModel rmodel = getRenderer2DModel();
                IAtom atom = rmodel.getHighlightedAtom();
                if(atom == null) {
                    return null;
                }else {
                    if(rmodel.getToolTipTextMap().isEmpty()) {
                        List<IAtomContainer> acs= ChemModelManipulator
                                            .getAllAtomContainers(
                                            getControllerHub().getIChemModel());
                        int num =-1;
                        for(IAtomContainer ac:acs) {
                            num = ac.getAtomNumber( atom );
                            if(num!=-1) break;
                        }
                        if(num<0) return null;
                        String atomType = atom.getAtomTypeName();
                        if(atomType!= null)
                            atomType= atomType.replaceFirst(  "^[^\\.]+\\.","" );
                        return String.format( "%s%d, [%s]",
                                              atom.getSymbol(),
                                              num,
                                              atomType);
                    }else {
                        String text = rmodel.getToolTipText( atom );
                        return text.length()!=0?text:null;
                    }
                }
            }

            @Override
            protected boolean shouldCreateToolTip( Event event ) {
                RendererModel rmodel = getRenderer2DModel();
                IAtom atom = rmodel.getHighlightedAtom();
                return atom!=null && super.shouldCreateToolTip( event );
            }
        };
        tooltip.setShift( new Point(10,0) );
        tooltip.setPopupDelay(200);


        prefListener = new GenerateLabelPrefChangedLisener( this );
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener( prefListener );
        addUndoRedoListener();
    }

    private void addUndoRedoListener() {
        operationHistory.addOperationHistoryListener(new IOperationHistoryListener() {

            public void historyNotification(OperationHistoryEvent event) {
                if(operationHistory.canUndo(undoContext)) {
                    setDirty(true);
                }else setDirty(false);

                if(!isDisposed()) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            hub.getRenderModel().setSelection( AbstractSelection.EMPTY_SELECTION );
                            hub.select( AbstractSelection.EMPTY_SELECTION );
                            structureChanged();
                            redraw();
                        }
                    });
                }
            }
        });
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

        phantomGenerator.setControllerHub( hub );
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
//        addListener( SWT.MouseHover, new Listener() {
//           public void handleEvent( Event event ) {
//               if(event.type == SWT.MouseHover) {
//                   updateToolTip();
//               }
//            }
//        });


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

    @Override
    protected void paint( IDrawVisitor visitor ) {

        Renderer renderer = getRenderer();

        if ( isScrolling ) {
            renderer.repaint( visitor );
        } else {
            renderer.paintChemModel( model, visitor );
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
            LogUtils.debugTrace( logger, e );
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
                setToolTipText( null );
            }
        }
    }

    public void setIsScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void reset() {
        if(hub!=null && !isDisposed()) {
            Rectangle clientRect = getClientArea();
            java.awt.Rectangle rect = new java.awt.Rectangle( clientRect.x,
                                                              clientRect.y,
                                                              clientRect.width,
                                                              clientRect.height);
            getRenderer().setup( hub.getIChemModel(), rect );
            resizeControl();
        }
    }

    public void updateView() {
        redraw();
    }

    @Override
    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();

        generatorList.add(new ExternalHighlightGenerator());
        generatorList.addAll( super.createGenerators() );
        generatorList.add( phantomGenerator = new PhantomBondGenerator());
        generatorList.add(new SelectAtomGenerator());
        generatorList.add(new SelectBondGenerator());
        generatorList.add( new MergeAtomsGenerator());

        return generatorList;
    }

    @Override
    public void setModel( IChemModel model ) {
        this.applyGlobalProperties();
        hub.setChemModel(model);
       // if(!this.isDisposed())
            reset();
        super.setModel( model );
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        if( atomContainer != null) {
            IChemModel model = ChemModelManipulator.newChemModel( atomContainer );
            setModel( model );
        }else {
            setModel( null );
        }
    }
    public void setReaction(IReaction reaction) {
        if( reaction != null) {

            IChemModel model = reaction.getBuilder().newChemModel();
            IReactionSet reactionSet = reaction.getBuilder().newReactionSet();
            reactionSet.addReaction( reaction );
            model.setReactionSet( reactionSet );
            setModel( model );
        }else {
            setModel( null );
        }

    }

    public void setReactionSet(IReactionSet reactionSet) {
        if( reactionSet != null) {

            IChemModel model = reactionSet.getBuilder().newChemModel();
            model.setReactionSet( reactionSet );
            setModel( model );
        }else {
            setModel( null );
        }
    }
    public void setInput( IAdaptable element ) {
            new2Dcoordinates = false;
            ICDKMolecule molecule = null;
            if(element != null)
                molecule =
                    (ICDKMolecule)
                    ((IAdaptable)element).getAdapter( ICDKMolecule.class );

            if (molecule != null) {
                source = molecule;
                IAtomContainer atomContainer = molecule.getAtomContainer();
                if( atomContainer.getAtomCount() > 0
                        && has2DCoordinatesNew( atomContainer )<2) {

                    molecule = generate2Dfrom3D( molecule );
                    if(molecule!=null) // FIXME do what else dose empty chem model
                        atomContainer= molecule.getAtomContainer();
                    else
                        atomContainer = null;
                    new2Dcoordinates = true;
                    // Editor not dirty when generated coordinates see bug 1372
                    setDirty( false );
                    if(GenerateLabelPrefChangedLisener.showGeneratedLabel())
                        add( Message.GENERATED );
                }else {
                    IAtomContainer oldAC = atomContainer;
                    atomContainer = atomContainer.getBuilder()
                    .newAtomContainer( atomContainer );
                    atomContainer.setProperties( new HashMap<Object, Object>(
                            oldAC.getProperties()) );
                    setDirty( oldAC.getFlag( 7 ) );
                }
                setAtomContainer(atomContainer);
            }
            else {
                IChemModel model = NoNotificationChemObjectBuilder.getInstance()
                .newChemModel();
                source = null;
                setModel( model );
                setDirty( false );
            }
            setSelection( getSelection() );
    }

    /*
     * Utility method for copying 3D x,y to 2D coordinates
     */
    public static ICDKMolecule generate2Dfrom3D( ICDKMolecule cdkMolecule ) {

        ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
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
            if(source != null)
                return new StructuredSelection(source);
            else
               return StructuredSelection.EMPTY;

        List<CDKChemObject<?>> selection = new LinkedList<CDKChemObject<?>>();

        IChemObjectSelection sel = rendererModel.getSelection();
        IAtomContainer modelSelection = null;
        if (sel != null)
        	modelSelection = sel.getConnectedAtomContainer();

        if (modelSelection != null) {
            for (IAtom atom : modelSelection.atoms()) {
                selection.add(createCDKChemObject(atom));
            }

            for (IBond bond : modelSelection.bonds()) {
                selection.add(createCDKChemObject(bond));
            }
        }

        if (selection.isEmpty() && source != null) {
            return new StructuredSelection(source);
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
        Object[] listenersArray = listeners.getListeners();

        for (int i = 0; i < listenersArray.length; i++) {
            final ISelectionChangedListener l = (ISelectionChangedListener)
                                                              listenersArray[i];
            Display.getDefault().asyncExec( new Runnable() {
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
        IChemModel model = hub.getIChemModel();
        updateAtomTypesAndHCounts( model );
        if(!this.isDisposed())
            resizeControl();
    }

    private void updateAtomTypesAndHCounts( IChemModel model ) {
        CDKHydrogenAdder hAdder =
            CDKHydrogenAdder.getInstance(model.getBuilder());
        CDKAtomTypeMatcher matcher =
            CDKAtomTypeMatcher.getInstance(model.getBuilder());
        for (IAtomContainer container :
             ChemModelManipulator.getAllAtomContainers(model)) {
            // erase old information
            for (IAtom atom : container.atoms()) {
                atom.setAtomTypeName(null);
                atom.setHybridization(null);
                atom.setHydrogenCount(0);
                atom.setFlag(CDKConstants.ISAROMATIC, false);
            }
            for (IBond bond : container.bonds())
                bond.setFlag(CDKConstants.ISAROMATIC, false);
            // determine new information
            try {
                IAtomType[] types = matcher.findMatchingAtomType(container);
                for (int i=0; i<container.getAtomCount(); i++) {
                    IAtom atom = container.getAtom(i);
                    if (types[i] != null) {
                        atom.setAtomTypeName(types[i].getAtomTypeName());
                        atom.setHybridization(types[i].getHybridization());
                        hAdder.addImplicitHydrogens(container, atom);
                    }
                }
                CDKHueckelAromaticityDetector.detectAromaticity(container);
            } catch ( CDKException e ) {
                e.printStackTrace();
            }
        }
    }

    protected void structurePropertiesChanged() {
        IChemModel model = hub.getIChemModel();
        updateAtomTypesAndHCounts( model );
    }

    public void setDirty( boolean dirty) {
        if(!dirty) {
            new2Dcoordinates = false;

        }
        this.isdirty = dirty;
        if(!this.isDisposed()) {
            Display.getDefault().asyncExec( new Runnable() {
                public void run() {
                    if(isdirty) {
                        add(Message.DIRTY);
                        remove(Message.GENERATED);
                    }
                    else {
                        remove( Message.DIRTY );
                        remove( Message.GENERATED);
                    }
                    if(!JChemPaintEditorWidget.this.isDisposed())
                        redraw();
                }
            });
        }
    }

    public boolean is2Dnew() {
        return new2Dcoordinates;
    }

    public boolean getDirty() {
        return isdirty;
    }

    public void setProperty(Object key,Object value) {
        IAtomContainer ac = model.getMoleculeSet().getAtomContainer(0);
        if(value == null)
            ac.removeProperty(key);
        else
            ac.setProperty(key, value);
    }

   @Override
   protected void disposeView() {
       Activator.getDefault().getPreferenceStore()
                                  .removePropertyChangeListener( prefListener );
       super.disposeView();
   }

    @Override
    public void add( Message message ) {
        boolean showGenerate = Platform.getPreferencesService().getBoolean(
                                 Activator.PLUGIN_ID,
                                 PreferenceConstants.SHOW_LABEL_GENERATED,
                                 true, null );
        // Don't show 'Generated' message when preference is not set
        if(!showGenerate && message.equals( Message.GENERATED ))
            return;
        super.add( message );
    }

    private java.awt.Color createFromSWT(org.eclipse.swt.graphics.Color color) {
        return new java.awt.Color( color.getRed(),
                                   color.getGreen(),
                                   color.getBlue());
    }

    private java.awt.Color createFromSWT(int swt_color_constant) {
        return createFromSWT( getDisplay().getSystemColor(swt_color_constant ));
    }

    public void undo() throws ExecutionException {
        if (this.operationHistory.canUndo(this.undoContext)) {
            this.operationHistory.undo(undoContext, null, null);
            if(!this.operationHistory.canUndo( this.undoContext )) {
                setDirty( false );
            }
            hub.getRenderModel().setSelection( AbstractSelection.EMPTY_SELECTION );
            hub.select( AbstractSelection.EMPTY_SELECTION );
            structureChanged();
        }
    }

    public IUndoContext getUndoContext() {
        return undoContext;
    }

    public void redo() throws ExecutionException {
        if (this.operationHistory.canRedo(this.undoContext)) {
            this.operationHistory.redo(undoContext, null, null);
            if(!getDirty()) {
                setDirty( true );
            }
            hub.getRenderModel().setSelection( AbstractSelection.EMPTY_SELECTION );
            hub.select( AbstractSelection.EMPTY_SELECTION );
            structureChanged();
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

    public void addDropSupport(int operations, Transfer[] transferTypes,
                               final DropTargetListener listener) {
        Control control = this;
        DropTarget dropTarget = new DropTarget(control, operations);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(listener);
    }
}
