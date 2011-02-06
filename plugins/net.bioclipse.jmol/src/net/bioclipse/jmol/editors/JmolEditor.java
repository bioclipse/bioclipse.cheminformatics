/*******************************************************************************
 * Copyright (c) 2007-2008 Bioclipse Project
 *               2011      Kalishenko Evgeny <ydginster@gmail.com> 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.jmol.editors;

import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.vecmath.Point3f;

import net.bioclipse.core.IResourcePathTransformer;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.model.IJmolMolecule;
import net.bioclipse.jmol.model.JmolMolecule;
import net.bioclipse.jmol.views.JmolCompMouseListener;
import net.bioclipse.jmol.views.JmolPanel;
import net.bioclipse.jmol.views.JmolSelection;
import net.bioclipse.jmol.views.outline.JmolContentOutlinePage;
import net.bioclipse.jmol.views.outline.JmolModel;
import net.bioclipse.jmol.views.outline.JmolModelString;
import net.bioclipse.jmol.views.outline.JmolObject;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLMolecule;

/**
 * An Editor with Jmol
 */
public class JmolEditor extends EditorPart 
                        implements IResourceChangeListener, 
                                   IAdaptable, 
                                   ISelectionListener, 
                                   ISelectionProvider {

    public static final String EDITOR_ID 
        = "net.bioclipse.jmol.editors.JmolEditor";

    private static final Logger logger = Logger.getLogger(JmolEditor.class);

    /** The ContentOutlinePage for the Outline View */
    JmolContentOutlinePage fOutlinePage;

    /** 
     * The JmolPanel that we can get the JmolViewer 
     * from to e.g. call scripts 
     */
    JmolPanel jmolPanel;

    /** Registered listeners */
    private volatile List<ISelectionChangedListener> selectionListeners;

    /** Store last selection */
    private volatile JmolSelection selection;

    /**
     * Creates a multi-page editor example.
     */
    public JmolEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        selectionListeners = new ArrayList<ISelectionChangedListener>();
    }

    /**
     * Creates page 0 of the multi-page editor,
     * which consists of Jmol.
     */
    public void createPartControl(Composite parent) {
    	
        /*
         * Set a Windows specific AWT property that prevents heavyweight
         * components from erasing their background. Note that this is a global
         * property and cannot be scoped. It might not be suitable for your
         * application.
         */
        try {
        	System.setProperty("sun.awt.noerasebackground", "true");
        } catch (NoSuchMethodError error) {
        }

        //Set the layout for parent
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        parent.setLayoutData(layoutData);

        //Add the Jmol composite to the top
        Composite composite = new Composite( parent, SWT.NO_BACKGROUND 
                                                   | SWT.EMBEDDED
                                                   | SWT.DOUBLE_BUFFERED );
        layout = new GridLayout();
        composite.setLayout(layout);
        layoutData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(layoutData);

        java.awt.Frame awtFrame = SWT_AWT.new_Frame(composite);
        java.awt.Panel awtPanel 
            = new java.awt.Panel(new java.awt.BorderLayout());
        awtFrame.add(awtPanel);

        jmolPanel = new JmolPanel(this);
        JScrollPane scrollPane = new JScrollPane(jmolPanel);
        awtPanel.add(scrollPane);

        jmolPanel.addMouseListener((MouseListener) new
                JmolCompMouseListener(composite,this));

        final float scaleFactor = 0.3f;
        jmolPanel.addKeyListener( new KeyAdapter() {
           @Override
            public void keyPressed( KeyEvent e ) {

               if(e.getKeyCode() != KeyEvent.VK_W)
                   return;

               int onmask;
               String vers = System.getProperty( "os.name" ).toLowerCase();
               if( vers.indexOf( "mac" ) != -1)
                   onmask = KeyEvent.META_DOWN_MASK;
               else
                   onmask = KeyEvent.CTRL_DOWN_MASK;
               int offmask = KeyEvent.SHIFT_DOWN_MASK
                            |KeyEvent.ALT_DOWN_MASK;
               if ( (e.getModifiersEx() & (onmask | offmask)) == onmask) {
                   final IEditorPart editor = JmolEditor.this;
                   Display.getDefault().syncExec(new Runnable() {
                       public void run() {
                           editor.getSite().getPage().closeEditor(editor, true);
                       };
                   });
               }
            }
        });
        
        jmolPanel.requestFocusInWindow();
        jmolPanel.addMouseWheelListener(
            new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int clicks = e.getWheelRotation();
                    float z = jmolPanel.getViewer().getZoomPercentFloat();
                    if (clicks > 0) {
                        float newZ = z * (1 - scaleFactor);
                        if (newZ > 5.0) {
                            runScriptSilently("zoom " + newZ);
                        }
                    } else {
                        float newZ = z * (1 + scaleFactor);
                        if (newZ < 5000.0) {
                            runScriptSilently("zoom " + newZ);
                        }
                    }
                }
            }
        );

        loadFile();
        initJMolPanel();
        
        // Post selections in Jmol to Eclipse
        getSite().setSelectionProvider(this);
        
        // Register help context for this editor
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "net.bioclipse.jmol.jmolEditor");
        
        getSite().getPage().addSelectionListener(this);
    }

    /**
     * Read file by JMol and show it
     */
    private void loadFile()
    {
      String content; 
      
      content = getContentsFromEditor();
      
      if (content == null)
      {
        logger.error("Could not get FILE in jmol editor");
        content = "";
      }
      
      jmolPanel.getViewer().openStringInline(content);
    }
    
    /**
     * JMol panel view initialization
     */
    private void initJMolPanel()
    {
      // Use halos as selection marker
      jmolPanel.getViewer().setSelectionHalos(true);
      
      // display all frames, then use 'display'
      runScript("frame 0.0");
      runScript("display 1.1");

      runScript("select none");
      
      //make clicking on elemtents select those elements
      if (jmolPanel.getViewer().getPolymerCount() == 0)
          runScript("set picking select atoms");
      else
          runScript("set picking select group");      
    }
    
    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors.
     * Subclasses may extend.
     */
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }
    
    public void load(IFile file) throws CoreException {
        BufferedReader contentStream = 
            new BufferedReader(new InputStreamReader(file.getContents()));
        StringBuffer stringModel = new StringBuffer();
        try {
            String line;
            while ((line = contentStream.readLine()) != null) {
                stringModel.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        jmolPanel.getViewer().loadInline(stringModel.toString());
    }
    
    /**
     * Take a snapshot of the editor contents and save as a png file
     */
    public void snapshot(IFile file) {
        Image image = new BufferedImage(jmolPanel.getWidth(),
                                        jmolPanel.getHeight(),
                                        BufferedImage.TYPE_INT_RGB);
        jmolPanel.paint(image.getGraphics());

        try {
            // this seems like a convoluted way to do things...
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
            ImageIO.write((RenderedImage) image, "PNG", outputStream);
            ByteArrayInputStream input = 
                new ByteArrayInputStream(outputStream.toByteArray());
            if (file.exists()) {
                file.setContents(input, false, true, null);
            } else {
                file.create(input, false, null);
            }
        } catch (IOException ioe) {
            LogUtils.debugTrace( logger, ioe );
        } catch (CoreException e) {
            LogUtils.debugTrace( logger, e );
        }
    }
    
    /**
     * Saves the multi-page editor's document.
     */
    public void doSave(IProgressMonitor monitor) {
        //Not implemented
    }
    
    /**
     * Saves the multi-page editor's document as another file.
     * Also updates the text for page 0's tab, and updates this multi-page 
     * editor's input to correspond to the nested editor's.
     */
    public void doSaveAs() {
        //Not implemented
    }
    
    /* (non-Javadoc)
     * Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        IDE.gotoMarker(this, marker);
    }
    
    public void init(IEditorSite site, IEditorInput editorInput)
                                                    throws PartInitException {
        setPartName(editorInput.getName());
        setSite(site);
        setInput(editorInput);
    }

    /* (non-Javadoc)
     * Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    /**
     * Handle resource changes
     */
    public void resourceChanged(final IResourceChangeEvent event){

        final IEditorInput input=getEditorInput();
        if (!( input instanceof IFileEditorInput ))
            return;
        final IFile jmolfile=((IFileEditorInput)input).getFile();
        
        /*
         * Closes editor if resource is deleted
         */
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            
            IResourceDelta rootDelta = event.getDelta();
            //get the delta, if any, for the documentation directory
            
            final List<IResource> deletedlist = new ArrayList<IResource>();
            
            IResourceDelta docDelta = rootDelta.findMember(jmolfile.getFullPath());
            if (docDelta != null){
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                    public boolean visit(IResourceDelta delta) {
                       //only interested in removal changes
                       if ((delta.getFlags() & IResourceDelta.REMOVED) == 0){
                           deletedlist.add( delta.getResource() );
                       }
                       return true;
                    }
                 };
                 try {
                    docDelta.accept(visitor);
                 } catch (CoreException e) {
                    LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
                 }
            }
                
            if (deletedlist.size()>0 && deletedlist.contains( jmolfile )){
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (getSite()==null) 
                            return;
                        if (getSite().getWorkbenchWindow()==null) 
                            return;
                        
                        IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                                                          .getPages();
                        for (int i = 0; i<pages.length; i++) {
                                IEditorPart editorPart
                                  = pages[i].findEditor(input);
                                pages[i].closeEditor(editorPart,true);
                        }
                    }
                });
            }
            

            
        }

        /*
         * Closes all editors with this editor input on project close.
         */
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    if (getSite()==null) 
                        return;
                    if (getSite().getWorkbenchWindow()==null) 
                        return;
                    
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                                                      .getPages();
                    for (int i = 0; i<pages.length; i++) {
                        if ( jmolfile.getProject()
                                .equals( event.getResource() )) {
                            IEditorPart editorPart
                              = pages[i].findEditor(input);
                            pages[i].closeEditor(editorPart,true);
                        }
                    }
                }
            });
        }
    }

    private String getContentsFromEditor() {

        IEditorInput input=getEditorInput();
        
        if(input == null)
          return null;
        
        IResourcePathTransformer transformer 
                                    = ResourcePathTransformer.getInstance();

        try {
            String val = (String) input.getAdapter( String.class );
            if(val != null) return val;
            
            IMolecule mol = (IMolecule) input.getAdapter( IMolecule.class );
            if(mol!=null) return mol.toCML();

            if ((input instanceof IFileEditorInput) && 
                    ((IFileEditorInput)input).getFile().exists()) {
                return readFile( ((IFileEditorInput)input)
                                                     .getFile().getContents());
            }
            if ( input instanceof IPathEditorInput) {
                IFile file = transformer
                                    .transform( ((IPathEditorInput)input)
                                    .getPath().toOSString());
                return readFile( file.getContents() );
            }
            if( input instanceof IURIEditorInput) {
                URI uri = ((IURIEditorInput)input).getURI();
                IFile file = transformer.transform( uri.toString() );
                return readFile( file.getContents() );
            }
            
            logger.debug("Can't read input");
            //TODO: Close editor?
            return null;

        } catch (CoreException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        } catch ( BioclipseException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        }

        return null;
    }

    private String readFile(InputStream instream) throws IOException {        
        StringBuilder builder = new StringBuilder();

        // read bytes until eof
        for(int i = instream.read(); i != -1; i = instream.read())
        {
            builder.append((char)i);
        }
        instream.close();

        return builder.toString();
    }

    /**
     *
     * Provide Adapters for the JmolEditor
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {

        //Adapter for Outline
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage
                    = new JmolContentOutlinePage(getEditorInput(), this);
//                if (getEditorInput() != null)
//                fOutlinePage.setInput(getEditorInput());
            }
            return fOutlinePage;
        }
        
        if (required == JmolModelString.class) {
            String jms = (String) jmolPanel.getViewer()
                                           .getProperty( "String", 
                                                         "stateinfo", 
                                                         "" );
            JmolModelString jmso = new JmolModelString(jms);
            return jmso;
        }
        
        return super.getAdapter(required);
    }

    public void runScript(String script){
        logger.debug("Running jmol script: '" + script + "'");
        String res = jmolPanel.getViewer().evalString(script);
        if (res!=null)
            logger.debug("Jmol said: '" + res + "'");
        
        if ( fOutlinePage != null &&
             //UGLY HACK to make the outline model only update on load appends
             script.toLowerCase().contains( "append" ) ) {
            
            Display.getDefault().asyncExec( new Runnable() {
                public void run() {
                    fOutlinePage.updateTreeViewerModel();                    
                }
            });
        }
    }
    
    public void runScriptSilently(String script){
        logger.debug("Running jmol script: '" + script + "'");
        jmolPanel.getViewer().evalString(script);
        if ( fOutlinePage != null ) {
            Display.getDefault().asyncExec( new Runnable() {
                public void run() {
                    fOutlinePage.updateTreeViewerModel();                    
                }
            });
        }
    }

    public List<IJmolMolecule> getJmolMolecules() {
        List<IJmolMolecule> mols = new ArrayList<IJmolMolecule>();
        ModelSet set = jmolPanel.getViewer().getModelSet();
        System.out.println("model count: " + set.getModelCount());
        Map<Integer,CMLMolecule> models = new HashMap<Integer,CMLMolecule>();
        for (int atomIndex=0; atomIndex<set.getAtomCount(); atomIndex++) {
            int modelIndex = set.getAtomModelIndex(atomIndex);
            System.out.println("modelindex: " + modelIndex);
            CMLMolecule model = models.get(modelIndex);
            if (model == null) {
                model = new CMLMolecule();
                models.put(modelIndex, model);
            }
            CMLAtom atom = new CMLAtom(set.getAtomName(atomIndex));
            atom.setElementType(set.getAtomLabel(atomIndex));
            Point3f coord = set.getAtomPoint3f(atomIndex);
            atom.setX3(coord.x);
            atom.setY3(coord.y);
            atom.setZ3(coord.z);
            model.addAtom(atom);
            System.out.println("Atom: " + atom.toXML());
        }
        for (int bondIndex=0; bondIndex<set.getBondCount(); bondIndex++) {
            int modelIndex = set.getBondModelIndex(bondIndex);
            System.out.println("modelindex: " + modelIndex);
            CMLMolecule model = models.get(modelIndex);
            if (model == null) {
                model = new CMLMolecule();
                models.put(modelIndex, model);
            }
            System.out.println("model: " + model.toXML());
            System.out.println("bond: " + bondIndex);
            System.out.println(" atom1: " + set.getBondAtom1(bondIndex).getAtomName());
            System.out.println(" atom2: " + set.getBondAtom2(bondIndex).getAtomName());
            CMLBond bond = new CMLBond(
                model.getAtomById(set.getBondAtom1(bondIndex).getAtomName()),
                model.getAtomById(set.getBondAtom2(bondIndex).getAtomName())
            );
            short order = set.getBondOrder(bondIndex);
            if (order == 1) bond.setOrder("s");
            if (order == 2) bond.setOrder("d");
            if (order == 3) bond.setOrder("t");
            model.addBond(bond);
        }
        for (CMLMolecule model : models.values())
            mols.add(new JmolMolecule(model));
        return mols;
    }

    public JmolPanel getJmolPanel() {
        return jmolPanel;
    }

    public void setJmolPanel(JmolPanel jmolPanel) {
        this.jmolPanel = jmolPanel;
    }

    @SuppressWarnings("unchecked")
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {

        //Check what part it is selected in
        //Only listen for the outline with our created JmolPage (IAdapter)
        if (part instanceof ContentOutline) {
            ContentOutline outline = (ContentOutline) part;
            IPage pg=outline.getCurrentPage();
            if (fOutlinePage==null) return;
            if (!(fOutlinePage.equals(pg)))
                return;
        }

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection selection2 = (IStructuredSelection) selection;

            if (selection2==null){
                runScript("select all; halos off;");
                return;
            }

            List<String> selectedModelsList=new ArrayList<String>();
            List<String> selectedObjects=new ArrayList<String>();

            for (Iterator selIt=selection2.iterator();selIt.hasNext();){
                Object element=selIt.next();

                //Add models separately as they use display instead of select
                if (element instanceof JmolModel) {
                    JmolModel jmodel = (JmolModel) element;
                    Model model=(Model) jmodel.getObject();

                    //Add +1 as jmol uses base 1 and arrays 0
                    selectedModelsList.add(
                        String.valueOf(model.getModelIndex()+1) );
                }

                else if (element instanceof JmolObject) {
                    JmolObject jobj = (JmolObject) element;
                    selectedObjects.add(jobj.getSelectString());
                }
            }

            /*
            //List used to collect monomers/polymers to select
            List<String> selectedMonomersList=new ArrayList<String>();
            List<String> selectedModelsList=new ArrayList<String>();

            for (Iterator selIt=selection2.iterator();selIt.hasNext();){
                Object element=selIt.next();

                //Collect all selected objects
                if (element instanceof Model) {
                    Model model=(Model)element;
                    int currentModelIndex=jmolPanel.getViewer().getDisplayModelIndex();
                    if (model.getModelIndex()!=currentModelIndex){
                        logger.debug("Should change to model: " + model.getModelIndex());

                        selectedModelsList.add(String.valueOf(model.getModelIndex()+1));
                        //Add +1 as jmol uses base 1 and arrays 0
                        runScript("display 1." + (model.getModelIndex()+1));
                    }
                }

                //Replace BioPolymer with Chain: TODO
                else if (element instanceof BioPolymer) {
                    //We should select all entire BioPplymer
                    BioPolymer bpol = (BioPolymer) element;
                    for (int i=0; i<bpol.getMonomerCount();i++){
                        Monomer mono=bpol.getMonomers()[i];
                        String selStr=String.valueOf(mono.getSeqNumber());
                        int c=mono.getChainID();
                        if (c>0)
                            selStr+=":"+mono.getChainID();
                            //Add this monomer to selection
                        selectedMonomersList.add(selStr);
                    }

                }
                else if (element instanceof Monomer) {
                    Monomer mono = (Monomer) element;
                    String selStr=String.valueOf(mono.getSeqNumber());
                    int c=mono.getChainID();
                    if (c>0)
                        selStr+=":"+mono.getChainID();
                        //Add this monomer to selection
                    selectedMonomersList.add(selStr);
                }
                //Handle proteinStructures?
             */

//            Polymers are collections of Monomers, which extend Group. Each Group
//            is a member of a chain.

//            Not all groups are monomers, because not all groups are DNA, RNA,
//            carbohydrate, or protein (for example, H2O).

//            Chains are very specifically the sets defined in PDB and mmCIF
//            files based on the character in the chain field. If this field
//            is blank, it's still a chain -- the blank chain. For a file that
//            is not PDB or mmCIF, there is one chain,
//            and it's designation is blank.

            //A polymer is smaller than a chain. "Chain" refers to PDB chain.
            //water molecules in a PDB file are part of a chain, not a polymer.
            //polymers are only DNA, RNA, protein, carbohydrate


//            else if (element instanceof Strands) {
//            PDBStrand strand = (PDBStrand) chobj;
//            selectionList.add("*:" + strand.getStrandName());
//            }



            //Handle selection of Frames by DISPLAY in Jmol
            if (selectedModelsList.size() > 0) {
                if (selectedModelsList.get(0) != null) {

                    selectedModelsList=removeDuplicates(selectedModelsList);

                    //Collect all Select commands into one string
                    String collectedSelects="Display ";
                    for ( Iterator<String> it = selectedModelsList.iterator(); 
                          it.hasNext(); ) {
                        String sel = it.next();
                        collectedSelects+="1." +sel+",";
                    }

                    //Remove last comma
                    collectedSelects = 
                        collectedSelects
                            .substring( 0, collectedSelects.length() - 1 );
                    logger.debug("Collected display string: '" 
                                 + collectedSelects + "'");

                    runScript(collectedSelects);
                }
            }

            //Handle selection of JmolObjects by SELECT in Jmol
            if (selectedObjects.size() > 0) {
                if (selectedObjects.get(0) !=null) {

                    //Sort list of monomers and remove duplicates
                    selectedObjects=removeDuplicates(selectedObjects);

                    //Collect all Select commands into one string
                    String collectedSelects="Select ";
                    for (Iterator<String> it = selectedObjects.iterator(); 
                          it.hasNext();) {
                        String sel = it.next();
                        collectedSelects+=sel+",";
                    }

                    //Remove last comma
                    collectedSelects 
                        = collectedSelects
                          .substring( 0, collectedSelects.length()-1);
                    logger.debug("Collected select string: '" 
                                 + collectedSelects + "'");

                    runScript(collectedSelects);
                }
            } // else: nothing to select
        }
    }

    /**
     * Convenience method to remove duplicates in a list
     * @param items
     * @return
     */
    @SuppressWarnings("unchecked")
    public List removeDuplicates(List items) {
        Set set = new LinkedHashSet();
        set.addAll(items);
        return new ArrayList(set);
    }

    /* Below are for setting selections in Bioclipse from Jmol, e.g when
     clicked on an Atom*/

    public void addSelectionChangedListener(
                    ISelectionChangedListener listener) {
        if ( !selectionListeners.contains(listener) ) {
            selectionListeners.add(listener);
        }
    }

    public ISelection getSelection() {
        return selection;
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionListeners.contains(listener))
            selectionListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        if (!(selection instanceof JmolSelection)) 
            return;

        this.selection = (JmolSelection)selection;

        if ( this.selection.updateJmolSelection() ) {
            String selStr;
            if ( selection.isEmpty() ) {
                selStr = "Select none";
            }
            else {
                StringBuilder builder = new StringBuilder();
                builder.append( "Select " );
                for ( Object s : this.selection) {
                    builder.append( (String)s );
                    builder.append( ", " );
                }
                // -2 to get rid of last ", " part
                selStr = builder.substring( 0, builder.length() - 2 );
            }
            runScript(selStr);
        }

        if (selectionListeners==null) 
            return;
        
        java.util.Iterator<ISelectionChangedListener> iter 
            = selectionListeners.iterator();
        while ( iter.hasNext() ) {
            final ISelectionChangedListener listener = iter.next();
            final SelectionChangedEvent e 
                = new SelectionChangedEvent(this, this.selection);
            //Does SWT stuff so this has to be called on SWT's thread
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    listener.selectionChanged(e);
                }
            });

        }
    }

    public IEditorPart getPart(){
        return this;
    }
    
    public void setDataInput(IEditorInput input){  
        String data = (String) input.getAdapter( String.class );
        if(jmolPanel != null && data != null) {
            jmolPanel.getViewer().openStringInline( data );
        }
    }

    public void runScript( String script, boolean repportErrorToJSConsole ) {
        if ( repportErrorToJSConsole ) {
            jmolPanel.getJmolListener().toggleReportErrorToJSConsole();
        }
        runScript(script);
    }

    /**
     * Create file filled with scanner contents
     * @param scanner Serialized molecule
     * @return Created temporary file
     */
    private File createMolecularFile(Scanner scanner)
    {
      File tmpFile = null;
      FileWriter fileWriter = null;
      
      try {
        tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".cml");
        fileWriter = new FileWriter(tmpFile);
        
        while (scanner.hasNext()) {
          fileWriter.append(scanner.nextLine()).append('\n');
        }
      } catch (Exception exc) {
        throw new RuntimeException(
            "Exception when creating temp file.", exc
        );
      }
      finally { 
        try {
          fileWriter.close();          
        } catch (Exception exc2) {
          throw new RuntimeException(
              "Exception when creating temp file.", exc2
          );
        }
      }
      
      return tmpFile;
    }
    
    /**
     * @param file
     */
    public void append( IFile file ) {
        File    tempFile = null;
        Scanner scanner = null;
        
        try {
          scanner = new Scanner(file.getContents());          
          tempFile = createMolecularFile(scanner);
        }
        catch (CoreException e) {
            throw new RuntimeException("Exception when creating temp file", e);
        }
        finally
        {
          scanner.close();
        }

        runScript( "load append " + tempFile.getAbsolutePath() );
        runScript( "frame all" );
    }
    
    /**
     * @param mol {@link IMolecule} to show
     * @param toAppend append molecule or load
     */
    private void showMolecule(IMolecule mol, Boolean toAppend) {
      File    tempFile = null;
      Scanner scanner = null;
      
      try {
        scanner = new Scanner(mol.toCML());          
        tempFile = createMolecularFile(scanner);
      }
      catch (Exception e) {
          throw new RuntimeException("Exception when creating temp file", e);
      }
      finally
      {
        scanner.close();
      }
      
      if(toAppend == true)
        runScript("load append " + tempFile.getAbsolutePath());
      else
        runScript("load " + tempFile.getAbsolutePath());
        
      runScript("frame all");
    }
    
    /**
     * @param mol {@link IMolecule} to append
     */
    public void append(IMolecule mol) {
      showMolecule(mol, true);
    }
    
    /**
     * Show the molecule, the previous one will be erased
     * @param mol {@link IMolecule} to load 
     */
    public void loadMolecule(IMolecule mol) {
      showMolecule(mol, false);
    }

    @Override
    public boolean isDirty() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void setFocus() {
      // TODO Auto-generated method stub
      
    }
}
