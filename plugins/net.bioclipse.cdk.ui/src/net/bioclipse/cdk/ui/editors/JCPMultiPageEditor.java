/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contributors:
 *     CDK Project Team - initial API and implementation
 *     Ola Spjuth - adaptation for Bioclipse2
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.undo.UndoableEdit;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.jchempaint.DrawingPanel;
import org.openscience.cdk.applications.jchempaint.JCPScrollBar;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.applications.jchempaint.action.UndoableAction;
import org.openscience.cdk.applications.undoredo.AddAtomsAndBondsEdit;
import org.openscience.cdk.applications.undoredo.RemoveAtomsAndBondsEdit;
import org.openscience.cdk.event.ICDKChangeListener;
import org.openscience.cdk.event.ICDKSelectionChangeListener;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.PDBReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.cml.MDMoleculeConvention;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.nonotify.NNChemFile;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class JCPMultiPageEditor extends MultiPageEditorPart implements ISelectionListener, ISelectionProvider, ICDKSelectionChangeListener, ICDKChangeListener {

    public static final String EDITOR_ID="net.bioclipse.plugins.bc_jchempaint.editors.JCPMultiPageEditor";

    private static final Logger logger = Logger.getLogger(JCPMultiPageEditor.class);
    //TODO remove
    /*    private static final Logger logger = Activator.getLogManager()
    .getLogger(JCPMultiPageEditor.class.toString());*/
//    private IEditorInput editorInput;
    private JCPScrollBar jcpScrollBar;
    private JCPEditor jcpEditor;
    private TextEditor textEditor;


    private JCPMultiPageEditorContributor contributor;
    private IUndoContext undoContext=null;
    private Clipboard clipboard;
    private boolean focus;

    private Object syncParsedRes;

    private boolean validInput;

    private boolean ignoreSelectionChange;

    private final ListenerList fListeners= new ListenerList();

    private IFile underlyingFile;


    /**
     * This is the resource the model should synch with
     */
    private IChemFile resource;


    public IUndoContext getUndoContext() {
        return undoContext;
    }
    /**
     * Creates a multi-page editor example.
     */
    public JCPMultiPageEditor() {
        super();
        this.setPartName("JCPMultiPageEditor");
    }
    /**
     * Creates page 0 of the multi-page editor,
     * which contains jcp.
     * @throws OpenEditorException
     */

    void createPage0(){

        //No page0 so far
        if (true) return;

        String content=getContentsFromEditor();

        //Parse content into ChemFile
        IChemFile chemFile=null;

        ByteArrayInputStream bs = new ByteArrayInputStream(content.getBytes());

        long time = System.currentTimeMillis();

        IChemObjectReader reader;

        try {
            reader = new ReaderFactory().createReader(bs);
            if (reader==null){
                logger.error("Error parsing file in JCP ");
            }

            chemFile = new org.openscience.cdk.ChemFile();

            // Do some customizations...
            customizeReading(reader, chemFile);

            chemFile=(IChemFile)reader.read(chemFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }




        if (ChemFileManipulator.getAtomCount((IChemFile) chemFile) != 0 ) {
            List acsL = ChemFileManipulator.getAllAtomContainers((IChemFile) chemFile);
            Iterator iterator = acsL.iterator();
            while(iterator.hasNext()){
                IAtomContainer ac = (IAtomContainer)iterator.next();
                if (!GeometryTools.has2DCoordinates(ac)) {
                    logger.debug("In JCP: no 2D coords");
                    return;
                }
            }
        }

        String name =underlyingFile.getName();
        this.setPartName(name);
        try{
            jcpEditor = new JCPEditor();
            int index = this.addPage((IEditorPart) jcpEditor, getEditorInput());
            setPageText(index, "JChemPaint");
            this.setActivePage(index);
            jcpEditor.getJCPModel().getRendererModel().addCDKChangeListener(this);
        } catch (PartInitException e){
            e.printStackTrace();
        }

    }


    private void customizeReading(IChemObjectReader reader, IChemFile chemFile) {
        logger.info("customingIO, reader found: " + reader.getClass().getName());
        logger.info("Found # IO settings: " + reader.getIOSettings().length);
        if (reader instanceof PDBReader) {
            chemFile = new NNChemFile();

            Properties customSettings = new Properties();
            customSettings.setProperty("DeduceBonding", "false");

            PropertiesListener listener = new PropertiesListener(customSettings);
            reader.addChemObjectIOListener(listener);
        }

        if (reader instanceof CMLReader) {
            ((CMLReader)reader).registerConvention("md:mdMolecule", new MDMoleculeConvention(new ChemFile()));
            System.out.println("****** CmlReader, registered MDMoleculeConvention");

        }

    }



    /**
     * Creates page 1 of the multi-page editor,
     * which contains a text editor.
     *
     */
    void createPage1(){

        textEditor = new TextEditor();

        int index;
        try {
            index = this.addPage((IEditorPart) textEditor, getEditorInput());
            setPageText(index,"Source");
//            textEditor.setBioresource(resource);
            if (this.jcpEditor == null || this.jcpEditor.getJCPModel() == null) {
                this.setActivePage(index);
            }
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        undoContext=textEditor.getUndoContext();
    }


    /**
     * Creates the pages of the multi-page editor.
     */
    protected void createPages() {
        if (this.validInput) {
            createPage0();
        }
        createPage1();
        getEditorSite().getPage().addSelectionListener(this);
        //Register the treeViewer so that others may react upon it
        getSite().setSelectionProvider(this);

    }
    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors.
     * Subclasses may extend.
     */
    public void dispose() {
        if(jcpEditor != null && jcpEditor.isDirty()){
            //TODO: propagate changes
        }
        super.dispose();
    }
    /**
     * Saves the multi-page editor's document.
     */
    public void doSave(IProgressMonitor monitor) {

        //From b1, need update in Bioclispe2

        IEditorPart editor = this.getActiveEditor();
        boolean savePossible = true;
        if (editor == textEditor) {
//            editor.doSave(monitor);

            IDocument doc = textEditor.getDocumentProvider().getDocument(getEditorInput());
            String docString = doc.get();

            //Set new content and force a parse
            //TODO

        }
        if (editor == jcpEditor) {
            editor.doSave(monitor);
        }
        this.jcpEditor.setDirty(false);
        textEditor.doRevertToSaved();
//        textEditor.setInput(getEditorInput());



        /*
        IEditorPart editor = this.getActiveEditor();
        boolean savePossible = true;
        if (editor == textEditor) {
            IDocument doc = textEditor.getDocumentProvider().getDocument(getEditorInput());
            String docString = doc.get();
            ByteArrayInputStream bs = new ByteArrayInputStream(docString.getBytes());
            try {
                IChemObjectReader reader = new ReaderFactory().createReader(bs);
                IChemFile chemFile = new org.openscience.cdk.ChemFile();
                chemFile=(IChemFile)reader.read(chemFile);
            } catch (IOException e) {
                savePossible = false;
            } catch (CDKException e) {
                savePossible = false;
            }
        }
        if (savePossible) {
            editor.doSave(monitor);
            //sync syncParsedRes with parsedRes
            try {
                syncParsedRes = ((IChemFile)((BioResourceEditorInput)getEditorInput()).getBioResource().getParsedResource()).clone();
            } catch (CloneNotSupportedException e) {
    //            FIXME: should at least report to logger, but not available!!!
            }
            //set both editors dirty flag to false & set XMLEditor input new, because it otherwise isnt holding the actual model for some reason
            this.jcpEditor.setDirty(false);
            textEditor.doRevertToSaved();
        }
        else {
            MessageDialog.openError(this.getContainer().getShell(), "Save failed", "It was not possible to save the file. Propably the source is no valid " + ((CDKResource)((BioResourceEditorInput)getEditorInput()).getBioResource()).getChemFormatName() + "\nPlease correct the file content!");
            //FIXME: should at least report to logger, but not available!!!
        }
        textEditor.setInput(getEditorInput());

         */

    }
    /**
     * Saves the multi-page editor's document as another file.
     * Also updates the text for page 0's tab, and updates this multi-page editor's input
     * to correspond to the nested editor's.
     */
    public void doSaveAs() {

        getActiveEditor().doSaveAs();
        JCPEditor jcpEditor=(JCPEditor)getEditor(0);
        jcpEditor.setDirty(false);

        /*
        MoleculeSaveAsDialog saveAsDialog = new MoleculeSaveAsDialog(this.getSite().getShell(), ((BioResourceEditorInput)getEditorInput()).getBioResource(), null);
        saveAsDialog.open();
        if (saveAsDialog.getReturnCode()==0){
            //Close this editor as a new one will be open
            this.dispose();
        }

        //Needed?
        saveAsDialog.close();
         */

//        MoleculeSaveAsWizard molwiz = new MoleculeSaveAsWizard(((BioResourceEditorInput)editorInput).getBioResource());
//        molwiz.init(this.getSite().getPage().getWorkbenchWindow().getWorkbench(), new StructuredSelection(((BioResourceEditorInput)editorInput).getBioResource()));
//        WizardDialog dialog = new WizardDialog(this.getSite().getShell(), molwiz);
//        dialog.create();
//        dialog.open();
//        JCPSaveAsDialog dialog = new JCPSaveAsDialog(this.getSite().getShell(),((BioResourceEditorInput)editorInput).getBioResource());
//        dialog.open();
    }

    /**
     * Init and validate input.
     */
    public void init(IEditorSite site, IEditorInput editorInput)
    throws PartInitException {
        super.init(site, editorInput);
        if (isValidInput(editorInput)==false){
            this.validInput = false;
//            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Open in JChemPaint Editor failed", "It was not possible to open the file " + ((BioResourceEditorInput)editorInput).resource.getName() + " in the JCPEditor. " + "\nIt will be opened in a text editor!");
        }
        else {
            this.validInput = true;

//            try {
//                syncParsedRes = ((IChemFile)((BioResourceEditorInput)editorInput).getBioResource().getParsedResource()).clone();
//            } catch (CloneNotSupportedException e) {
//                // FIXME: should at least report to logger, but not available!!!
//            }
        }
        clipboard = new Clipboard(getSite().getShell().getDisplay());
        IActionBars bars = ((IEditorSite)getSite()).getActionBars();
        bars.setGlobalActionHandler(ActionFactory.CUT.getId(),new CutAction(this,clipboard));
        bars.setGlobalActionHandler(ActionFactory.COPY.getId(),new CopyAction(this,clipboard));
        bars.setGlobalActionHandler(ActionFactory.PASTE.getId(),new PasteAction(this,clipboard));
        bars.setGlobalActionHandler(ActionFactory.PRINT.getId(),new PrintAction(this));
        bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),new SelectAllAction(this));
        bars.setGlobalActionHandler(ActionFactory.FIND.getId(),new FindAction(this));
    }

    /* (non-Javadoc)
     * Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    public void setContributor(JCPMultiPageEditorContributor con){
        this.contributor=con;
    }

    public IEditorPart getEditor(int index){
        return super.getEditor(index);
    }

    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        if (this.getPageCount() > 1) {
            if(newPageIndex==1){
                JCPEditor jcpEditor=(JCPEditor)getEditor(0);

                //Only save model from editor to resource if really changed
                if (jcpEditor.isDirty()){
                    IChemModel cModel = jcpEditor.getChemModel();
                    if (cModel != null) {
                        IChemObjectBuilder builder = cModel.getBuilder();
                        IChemSequence cs = builder.newChemSequence();
                        cs.addChemModel(cModel);
                        IChemFile cf = builder.newChemFile();
                        cf.addChemSequence(cs);

                        //                String text=jcpEditor.getStringFromJCPEditor();
//                        ((BioResourceEditorInput)getEditorInput()).getBioResource().setParsedResource(cf);
                    }
                }
//                boolean success = ((BioResourceEditorInput)getEditorInput()).getBioResource().updateParsedResourceFromString(text);
//                ((BioResourceDocumentProvider) textEditor.getDocumentProvider()).reloadDocumentContent(getEditorInput());
                if (!jcpEditor.isDirty()) {
                    textEditor.doRevertToSaved();
                }
//                textEditor.setInput(getEditorInput(),null);
            }
            if(newPageIndex==0){
                //This should not be able to happen, but probably due to synchr issues it is needed
                if (textEditor==null){
                    //                logger.debug("textEditor is NULL! NOT GOOD!");
                    return;
                }
                if (textEditor.isDirty()){
                    IDocument doc=textEditor.getDocumentProvider().getDocument(getEditorInput());
//                    ((BioResourceEditorInput)getEditorInput()).getBioResource().updateParsedResourceFromString(doc.get());
                    jcpEditor.setDirty(true);
                }
//                else {
//                jcpEditor.setDirty(false);
//                }
                if(!((JCPEditor)getEditor(0)).updateModel() && ((JCPEditor)getEditor(0)).getJCPModel() != null){
                    MessageBox messageBox=new MessageBox(getEditorSite().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    messageBox.setText("Parse failed");
                    messageBox.setMessage("Could not parse edited content; input is probably corrupt. Do you want to continue and loose any changes?");
                    int response = messageBox.open();
                    if (response == SWT.NO){
                        this.setActivePage(1);
                    }
                }else{
                    JChemPaintModel jcpModel = ((JCPEditor)getEditor(0)).getJCPModel();
                    if (jcpModel != null){
                        contributor.updateModel(jcpModel.getChemModel());
                        jcpEditor.getJcpComposite().setSize(jcpEditor.getJcpComposite().getSize().x,jcpEditor.getJcpComposite().getSize().y-1);
                        if(jcpModel.getControllerModel().getAutoUpdateImplicitHydrogens()){
                            CDKHydrogenAdder hydrogenAdder = CDKHydrogenAdder.getInstance(jcpModel.getChemModel().getBuilder());
                            IChemModel chemModel = ((JCPEditor)getEditor(0)).getJCPModel().getChemModel();

                            List moleculeL = ChemModelManipulator.getAllAtomContainers(chemModel);
                            Iterator molsI = moleculeL.iterator();
                            while(molsI.hasNext()){
                                IMolecule molecule = (IMolecule)molsI.next();
                                if (molecule != null)
                                {
                                    try{
                                        hydrogenAdder.addImplicitHydrogens(molecule);
                                    }catch(Exception ex){
                                        //do nothing
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public JChemPaintModel getJcpModel() {
        if (jcpEditor != null) {
            return jcpEditor.getJCPModel();
        }
        return null;
    }

    public DrawingPanel getDrawingPanel() {
        return jcpEditor.getDrawingPanel();
    }
    public JCPComposite getJcpComposite() {
        return (JCPComposite)jcpEditor.getJcpComposite();
    }

    /**
     * get the JCPScrollBar
     *
     * @return The jcpScrollBar
     */
    public JCPScrollBar getJCPScrollBar()
    {
        return jcpScrollBar;
    }
    @Override
    public boolean isDirty() {
        //iterates over embedded editors, if one is dirty, returns true else false
        return super.isDirty();
    }

    class TestOperation implements IUndoableOperation{
        private Vector contexts=new Vector();

        public TestOperation(Shell shell){
        }
        public boolean canUndo(){
            return true;
        }

        public IUndoContext[] getContexts(){
            IUndoContext[] c=new IUndoContext[contexts.size()];
            for(int i=0;i<contexts.size();i++){
                c[i]=(IUndoContext)contexts.get(i);
            }
            return c;
        }

        public void removeContext(IUndoContext con){
            contexts.remove(con);
        }

        public IStatus undo(IProgressMonitor monitor, IAdaptable info){
            return Status.OK_STATUS;
        }

        public boolean canExecute(){
            return true;
        }

        public boolean canRedo(){
            return true;
        }

        public void dispose(){
            clipboard.dispose();
        }

        public IStatus execute(IProgressMonitor monitor, IAdaptable info){
            return Status.OK_STATUS;
        }

        public boolean hasContext(IUndoContext context){
            return contexts.contains(context);
        }
        public IStatus redo(IProgressMonitor monitor, IAdaptable info){
            return Status.OK_STATUS;
        }

        public void addContext(IUndoContext context){
            contexts.add(context);
        }

        public String getLabel(){
            return "test";
        }

    }

    class CutAction extends Action{
        protected Clipboard clipboard;
        protected JCPMultiPageEditor jcpeditor;
        public CutAction(JCPMultiPageEditor jcpeditor, Clipboard clipboard) {
            super("Cut");
            this.jcpeditor=jcpeditor;
            this.clipboard = clipboard;
        }

        public void run() {
            IAtomContainer ac = null;
            try {
                ac = new Molecule((IAtomContainer)jcpeditor.getJcpModel().getRendererModel().getSelectedPart().clone());
            } catch (CloneNotSupportedException e) {
//                FIXME: should at least report to logger, but not available!!!
            }
            clipboard.setContents(new Object[] {ac}, new Transfer[] { AtomContainerTransfer.getInstance()});
            for(int i=0;i<ac.getAtomCount();i++){
                jcpeditor.getJcpModel().getChemModel().getMoleculeSet().getAtomContainer(0).removeAtomAndConnectedElectronContainers(jcpeditor.getJcpModel().getRendererModel().getSelectedPart().getAtom(i));
                ac.getAtom(i).setPoint2d(jcpeditor.getJcpModel().getRendererModel().getRenderingCoordinate(jcpeditor.getJcpModel().getRendererModel().getSelectedPart().getAtom(i)));
                jcpeditor.getJcpModel().getRendererModel().getRenderingCoordinates().remove(jcpeditor.getJcpModel().getRendererModel().getSelectedPart().getAtom(i));
            }
            jcpeditor.getJcpModel().fireChange();
            jcpeditor.getDrawingPanel().repaint();
            //undoredo
            UndoableEdit  edit = new RemoveAtomsAndBondsEdit(jcpeditor.getJcpModel().getChemModel(),ac,"paste");
            UndoableAction.pushToUndoRedoStack(edit,jcpeditor.getJcpModel(),JCPMultiPageEditor.this.getUndoContext(), JCPMultiPageEditor.this.getDrawingPanel());
        }
    }
    class CopyAction extends Action{
        protected Clipboard clipboard;
        protected JCPMultiPageEditor jcpeditor;
        public CopyAction(JCPMultiPageEditor jcpeditor, Clipboard clipboard) {
            super("Copy");
            this.jcpeditor=jcpeditor;
            this.clipboard = clipboard;
        }

        public void run() {
            IAtomContainer ac=new Molecule(jcpeditor.getJcpModel().getRendererModel().getSelectedPart());
            clipboard.setContents(new Object[] {ac}, new Transfer[] { AtomContainerTransfer.getInstance()});
            for(int i=0;i<ac.getAtomCount();i++){
                ac.getAtom(i).setPoint2d(jcpeditor.getJcpModel().getRendererModel().getRenderingCoordinate(jcpeditor.getJcpModel().getRendererModel().getSelectedPart().getAtom(i)));
            }
        }
    }

    class PasteAction extends Action{
        protected Clipboard clipboard;
        protected JCPMultiPageEditor jcpeditor;
        public PasteAction(JCPMultiPageEditor jcpeditor, Clipboard clipboard) {
            super("Paste");
            this.jcpeditor=jcpeditor;
            this.clipboard = clipboard;
        }

        public void run() {
            IAtomContainer ac = (IAtomContainer)clipboard.getContents(AtomContainerTransfer.getInstance());
            if (ac == null)
                return;
            IAtomContainer topaste = null;
            try {
                topaste = (IAtomContainer)ac.clone();
            } catch (CloneNotSupportedException e) {
                logger.error("problem cloning stuff to paste");
            }
            org.openscience.cdk.interfaces.IChemModel chemModel = jcpeditor.getJcpModel().getChemModel();
            //translate the new structure a bit
            GeometryTools.translate2D(topaste, new Vector2d(25,25),jcpeditor.getJcpModel().getRendererModel().getRenderingCoordinates()); //in pixels
            //paste the new structure into the active model
            IMoleculeSet moleculeSet = chemModel.getMoleculeSet();
            if (moleculeSet == null) {
                moleculeSet = new MoleculeSet();
                chemModel.setMoleculeSet(moleculeSet);
            }
            for(int i=0;i<topaste.getAtomCount();i++){
                jcpeditor.getJcpModel().getRendererModel().setRenderingCoordinate(topaste.getAtom(i), new Point2d(topaste.getAtom(i).getPoint2d()));
            }
            moleculeSet.addMolecule(new Molecule(topaste));
            //make the pasted structure selected
            jcpeditor.getJcpModel().getRendererModel().setSelectedPart(topaste);
            jcpeditor.getJcpModel().fireChange();
            jcpeditor.getDrawingPanel().repaint();
            //undoredo
            UndoableEdit  edit = new AddAtomsAndBondsEdit(chemModel,topaste,"paste",jcpeditor.getJcpModel().getControllerModel());
            UndoableAction.pushToUndoRedoStack(edit,jcpeditor.getJcpModel(),JCPMultiPageEditor.this.getUndoContext(), JCPMultiPageEditor.this.getDrawingPanel());
        }
    }

    class SelectAllAction extends Action {
        protected JCPMultiPageEditor jcpeditor;
        public SelectAllAction(JCPMultiPageEditor jcpeditor) {
            super("Select All");
            this.jcpeditor=jcpeditor;
        }

        public void run() {
            IAtomContainer atomcontainer=jcpeditor.getJcpModel().getChemModel().getBuilder().newAtomContainer();
            for(int i=0;i<jcpeditor.getJcpModel().getChemModel().getMoleculeSet().getAtomContainerCount();i++){
                atomcontainer.add(jcpeditor.getJcpModel().getChemModel().getMoleculeSet().getAtomContainer(i));
            }
            jcpeditor.getJcpModel().getRendererModel().setSelectedPart(atomcontainer);
//            textEditor.selectAndReveal(0, ((BioResourceEditorInput)getEditorInput()).getBioResource().getParsedResourceAsString().length());
        }
    }


    class FindAction extends Action {
        protected JCPMultiPageEditor jcpeditor;
        public FindAction(JCPMultiPageEditor jcpeditor) {
            super("Find");
            this.jcpeditor=jcpeditor;
        }

        public void run() {
            //TODO code
        }
    }


    class PrintAction extends Action implements Printable {
        protected JCPMultiPageEditor jcpeditor;
        public PrintAction(JCPMultiPageEditor jcpeditor) {
            super("Print");
            this.jcpeditor=jcpeditor;
        }

        public void run() {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable(this);
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (PrinterException pe) {
                    logger.error("Error printing: " + pe);
                }
            }
        }

        /**
         *  Prints the actual drawingPanel
         *
         * @param  g           Graphics object of drawinPanel
         * @param  pageFormat  Description of the Parameter
         * @param  pageIndex   Description of the Parameter
         * @return             Description of the Return Value
         */
        public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
            //get eventually selected parts
            IAtomContainer beforePrinting = jcpeditor.getJcpModel().getRendererModel().getSelectedPart();
            //disable selection for printing
            jcpeditor.getJcpModel().getRendererModel().setSelectedPart(new org.openscience.cdk.AtomContainer());
            if (pageIndex > 0) {
                //enable selection again
                jcpeditor.getJcpModel().getRendererModel().setSelectedPart(beforePrinting);
                return (NO_SUCH_PAGE);
            }
            else {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                jcpeditor.getDrawingPanel().setDoubleBuffered(false);
                jcpeditor.getDrawingPanel().paint(g2d);
                jcpeditor.getDrawingPanel().setDoubleBuffered(true);
                //enable selection again
                if (beforePrinting != null) jcpeditor.getJcpModel().getRendererModel().setSelectedPart(beforePrinting);
                return (PAGE_EXISTS);
            }
        }
    }

    private IAtomContainer selectedContent = null;

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {

//        logger.debug("listen in MPE");
        if (this.ignoreSelectionChange) {
            this.ignoreSelectionChange = false;
            return;
        }

        if(part==this)
            return;
        if (!(selection instanceof IStructuredSelection)) return;
        if (getJcpModel()==null) return;

        //Check if selection includes atoms/bonds and highlight in that case
        Iterator it=((IStructuredSelection)selection).iterator();

        if (it.hasNext()) {

            if (selectedContent == null) {
                selectedContent = new AtomContainer();
            } else {
                selectedContent.removeAllElements();
            }

            while( it.hasNext()){
                Object obj=it.next();
//                if(obj instanceof CDKChemObject){
//                    obj=((CDKChemObject)obj).getChemobj();
//                    if (obj instanceof IAtom){
//                        selectedContent.addAtom((IAtom)obj);
//                    }
//                    if (obj instanceof IBond){
//                        selectedContent.addBond((IBond)obj);
//                    }
//                }
            }
            getJcpModel().getRendererModel().setExternalSelectedPart(selectedContent);
            this.getDrawingPanel().repaint();
        }

    }

    /**
     * It is required that input has 2D-coordinates
     * @return
     */
    public static boolean isValidInput(IEditorInput input){
//        if (!(input instanceof BioResourceEditorInput)) {
//            BioclipseConsole.writeToConsole("JChemPaint: Not of BioResourceEditor input.");
//            return false;
//        }
//
//        BioResourceEditorInput bioInput = (BioResourceEditorInput) input;
//        BioResource bioRes = (BioResource) bioInput.getBioResource();
//
//        if (bioRes.isParsed()==false){
//            BioclipseConsole.writeToConsole("JChemPaint: Not parsed error.");
//            return false;
//        }
//
//        if (!(bioRes instanceof CDKResource)) {
//            BioclipseConsole.writeToConsole("JChemPaint: Not a CDKResource.");
//            return false;
//        }
//        CDKResource cdkres = (CDKResource) bioRes;
//        logger.debug("Parsed resource: " + cdkres.getParsedResource());
//        IChemModel cmodel = ((IChemFile)cdkres.getParsedResource()).getChemSequence(0).getChemModel(0);
//        IAtomContainer ac = cmodel.getMoleculeSet().getAtomContainer(0);
//        if (ac.getAtomCount() > 0 && cdkres.has2DCoordinates()==false) {
//            BioclipseConsole.writeToConsole("JChemPaint: No 2D-coordinates.");
//            return false;
//        }

        return true;

    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Message",
                message);
    }





    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fListeners.add(listener);

    }

    public ISelection getSelection() {
        ArrayList l=new ArrayList();
        if(jcpEditor != null && jcpEditor.getJCPModel() != null &&
                jcpEditor.getJCPModel().getRendererModel() != null &&
                jcpEditor.getJCPModel().getRendererModel().getSelectedPart()!=null){
            for(int i=0;i<jcpEditor.getJCPModel().getRendererModel().getSelectedPart().getAtomCount();i++){
                l.add(jcpEditor.getJCPModel().getRendererModel().getSelectedPart().getAtom(i));
            }
            for(int i=0;i<jcpEditor.getJCPModel().getRendererModel().getSelectedPart().getBondCount();i++){
                l.add(jcpEditor.getJCPModel().getRendererModel().getSelectedPart().getBond(i));
            }
        }
        StructuredSelection strucsel=new StructuredSelection(l);
        return strucsel;
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        // I do not know why a selection provider needs a setSelection, so I leave it blank
    }


    private void fireSelectionEvent() {
        try{
            ISelection selection= getSelection();
            SelectionChangedEvent event= new SelectionChangedEvent(this, selection);

            Object[] listeners= fListeners.getListeners();
            for (int i= 0; i < listeners.length; i++) {
                ((ISelectionChangedListener) listeners[i]).selectionChanged(event);
            }
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }
    public void stateChanged(EventObject arg0) {
        this.fireSelectionEvent();

    }


    private String getContentsFromEditor(){

        IEditorInput input=getEditorInput();
        if (!(input instanceof IFileEditorInput)) {
            logger.debug("Not FIleEditorInput.");
            //TODO: Close editor?
            return null;
        }
        IFileEditorInput finput = (IFileEditorInput) input;

        underlyingFile=finput.getFile();
        if (!(underlyingFile.exists())){
            logger.debug("File does not exist.");
            //TODO: Close editor?
            return null;
        }

//        return file.getFullPath().toFile();


        InputStream instream;
        try {
            instream = underlyingFile.getContents();
            StringBuilder builder = new StringBuilder();

            // read bytes until eof
            for(int i = instream.read(); i != -1; i = instream.read())
            {
                builder.append((char)i);
            }
            instream.close();

            return builder.toString();

        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


}
