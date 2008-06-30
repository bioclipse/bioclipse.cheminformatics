/*******************************************************************************
 * Copyright (c) 2007 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/

package net.bioclipse.jmol.views;

import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKConformer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.AtomContainerSelection;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.AtomIndexSelection;
import net.bioclipse.core.domain.IChemicalSelection;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.ModelSelection;
import net.bioclipse.core.domain.ScriptSelection;
import net.bioclipse.jmol.adapter.cdk.CdkJmolAdapter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * A view for Jmol embedded in an SWT_AWT frame (requires java5.0+)
 * 
 * @author ola
 */
public class JmolView extends ViewPart implements ISelectionListener, ISelectionProvider, ISelectionChangedListener{

    //Use logging
    private static final Logger logger = Logger.getLogger(JmolView.class);

    public static final String ID = "net.bioclipse.jmol.views.JmolView";

    private JmolPanel jmolPanel;
    private Text text;
    private ArrayList<String> history;

    private Composite composite;

    //Provide selections from Jmol to e.g. outline
    private List<ISelectionChangedListener> selectionListeners;
    private JmolSelection selection;

    private ICDKManager cdk;

    //Keep track if we have contents in view
    private boolean cleared;

    //Store the chemfile we are displaying
    IChemFile chemFile;

    private List lastSelected;
    
    /**
     * The constructor.
     */
    public JmolView() {
        history = new ArrayList<String>(50);
        jmolPanel= new JmolPanel(this, new CdkJmolAdapter());
        cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        setCleared( true );
        lastSelected=new ArrayList();
    }

    public boolean isCleared() {
        return cleared;
    }

    
    public void setCleared( boolean cleared ) {
        this.cleared = cleared;
    }


    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {

        logger.debug("JmolView is initiating...");

        //Set the layout for parent
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace=true;
        parent.setLayoutData(layoutData);

        //Add the Jmol composite to the top
        composite = new Composite(parent, SWT.EMBEDDED);
        layout = new GridLayout();
        composite.setLayout(layout);
        layoutData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(layoutData);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "net.bioclipse.jmol.scriptInputField");    


        java.awt.Frame fileTableFrame = SWT_AWT.new_Frame(composite);
        java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
        fileTableFrame.add(panel);

        JScrollPane scrollPane = new JScrollPane(jmolPanel);
        panel.add(scrollPane);

        jmolPanel.addMouseListener((MouseListener) new JmolCompMouseListener(composite));

        Label label1 = new Label(parent, SWT.NONE);
        label1.setText("Jmol scripting console");

        //Layout the text field below Jmol
        text = new Text(parent, SWT.SINGLE | SWT.BORDER);

        GridData layoutData2 = new GridData();
        layoutData2.grabExcessHorizontalSpace = true;
        layoutData2.horizontalAlignment = GridData.FILL;
        text.setLayoutData(layoutData2);
        text.setTextLimit(60);
        text.setEditable(true);
        text.setEnabled(false);
        text.setText("");

        text.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if (e.stateMask != SWT.CTRL && e.keyCode == SWT.CR){
                    //Execute jmol command
                    String jmolcmd=text.getText();
                    if (jmolcmd!=null && jmolcmd.length()>0)
//                        executeJmolCommand(jmolcmd);
                        if (!history.contains(text.getText()))
                            history.add(0, text.getText());
                    text.setText("");
                }
                /*laszlo: 
                 * Store a history of typed commands in Jmol script console,
                 * toggle UP/DOWN to see previous script.
                 * */

                if (e.keyCode == SWT.ARROW_UP){
                    text.setText(history.get(0));
                    history.add(history.get(0));
                    history.remove(0);
                }

                if (e.keyCode == SWT.ARROW_DOWN){
                    text.setText(history.get(0));
                    history.add(0, history.get(history.size()-1));
                    history.remove(history.size()-1);
                }
            }
        });

        //Register this page as a listener for selections
        //We want to update information based on selection i e g TreeViewer
        getViewSite().getPage().addSelectionListener(this);

    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // TODO Auto-generated method stub

        reactOnSelection(selection);
    }


    /* Below are for setting selections in Bioclipse from Jmol, e.g when 
     clicked on an Atom*/


    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if(!selectionListeners.contains(listener))
        {
            selectionListeners.add(listener);
        }
    }

    public ISelection getSelection() {
        return selection;
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if(selectionListeners.contains(listener))
            selectionListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        this.selection = (JmolSelection)selection;
        java.util.Iterator<ISelectionChangedListener> iter = selectionListeners.iterator();
        while( iter.hasNext() )
        {
            final ISelectionChangedListener listener = iter.next();
            final SelectionChangedEvent e = new SelectionChangedEvent(this, this.selection);
            //Does SWT stuff so this has to be called on SWT's thread
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    listener.selectionChanged(e);
                }
            });

        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        reactOnSelection(event.getSelection());
    }


    /**
     * 
     * @param selection
     */
    private void reactOnSelection(ISelection selection) {

        if (!(selection instanceof IStructuredSelection))
            return;
        IStructuredSelection eclipseSelection = (IStructuredSelection) selection;

        /*
         * Get info from selections directly or via adapter in this order:
         * 
         * 1. Collect (or genrate from IMolecule) ICDKMolecules
         * 2. Collect ChemModels to visualize
         * 3. Collect atoms/bonds to highlight (IChemicalSelection)
         * 4. Collect scripts to run
         */
        
        //Store selected CDKMols in List
        List<ICDKMolecule> collectedCDKMols=new ArrayList<ICDKMolecule>();

        //Store chemical selections in List
        List<IChemicalSelection> chemicalSelections = 
                                        new ArrayList<IChemicalSelection>();

        //See if list is the same as previously selected
        boolean newSelection=false;
        for (Object obj : eclipseSelection.toList()){
            
            if (!(lastSelected.contains(obj))){
                newSelection=true;
            }
        }
        if (newSelection==false){
            System.out.println("Omitting selection!");
            return;
        }

        //We have a new selection
        lastSelected.clear();

        //Extract molecules and chemical selection from the eclipseSelection
        //Store extracted info in lists
        extractFromSelection( eclipseSelection, collectedCDKMols, chemicalSelections );
        
        //We have now collected everything we are interested in.
        //If no fun, return
        if ((collectedCDKMols.size()<=0) && chemicalSelections.size()<=0)
            return;

        /*
         * If extracted molecules: Set up Chemfile and and send it to jmol
         */
        if (collectedCDKMols!=null){

            logger.debug( "Extracted the following molecules from selection:" );
            for (ICDKMolecule cmol : collectedCDKMols){
                logger.debug( "  * " + cmol.getName() );
            }

            processMolecules( collectedCDKMols );
        }

        
        /*
         * Process any chemicalSelections
        * This means to show/filter/add commands to the already shown molecules
        * For example, display one or more models, highlight atoms, run scripts
        */
        if (chemicalSelections.size()>0){
            
            logger.debug( "Extracted the following chemical selections:" );
            for (IChemicalSelection csel : chemicalSelections){
                logger.debug( "  * " + csel.getSelection() );
            }

            processChemicalSelections( chemicalSelections );
        }

    }

    private void processChemicalSelections(
                                            List<IChemicalSelection> chemicalSelections ) {

        logger.debug("######### Jmol Selections ############");

        //Handle highlighting if we have any
        //Start by atoms/bonds/models in a chemicalSelection

            //Store new model index
            ArrayList<Integer> modelSelectionIndices=new ArrayList<Integer>();
            
            //Store collected atom indices
            ArrayList<Integer> atomSelectionIndices=new ArrayList<Integer>();

            //Store collected scripts to run
            ArrayList<String> collectedScripts=new ArrayList<String>();

            //Loop over all stored IChemicalSelections
            for (IChemicalSelection chemicalSelection : chemicalSelections){

                //Process model selections to display one or more models
                if ( chemicalSelection instanceof ModelSelection ) {
                    ModelSelection modelSel = (ModelSelection) chemicalSelection;

                    for (Integer i : modelSel.getSelection()){
                        //Add one, since jmol starts indices on 1, and store in list
                        modelSelectionIndices.add( i +1 );

                        logger.debug("# jmol: Added model to be shown: " + (i+1));
                    }

                }

                //Process atom selections to highlight one or more atoms by index
                else if ( chemicalSelection instanceof AtomIndexSelection ) {
                    AtomIndexSelection isel = (AtomIndexSelection) chemicalSelection;
                    int[] selindices = isel.getSelection();

                    for (int i=0; i<selindices.length;i++){

                        //Add one, since jmol starts indices on 1
                        atomSelectionIndices.add( new Integer(selindices[i]+1) );
                    }
                }

                else if ( chemicalSelection instanceof AtomContainerSelection ) {
                    //TODO
                }

                else if ( chemicalSelection instanceof ScriptSelection ) {
                    ScriptSelection scriptSelection=(ScriptSelection)chemicalSelection;
                    Map<String, String> scripts = scriptSelection.getSelection();
                    for (String script : scripts.keySet()){
                        if (scripts.get( script ).equals( "jmol" )){
                            collectedScripts.add( script );
                        }
                    }
                }
            }
            

            
            /*
             * We have now collected what we want from the Chemical Selection.
             * Now, set up an run the jmol scripts for this.
             */

            //Start with models by index
            if (modelSelectionIndices.size()>0){

                //Collect all Select commands into one string
                String frameScript="Frame all; display ";
                for (Iterator<Integer> it = modelSelectionIndices.iterator();
                                                                it.hasNext();) {
                    Integer sel = it.next();
                    frameScript+="1." +sel+",";
                }
                
                String colorScript="Select all; wireframe 40; spacefill 20%; " +
                		"color bonds cpk; color atoms cpk; ";
                
                if (modelSelectionIndices.size()>1){
                    colorScript="Select all; spacefill off; wireframe 40;";
                    for (Integer i : modelSelectionIndices){
                        colorScript=colorScript + " Select 1." + i + "; color bonds " + getColorEnum(i) +";";
                    }
                }

                //Remove last comma
                frameScript=frameScript.substring(0, 
                                                   frameScript.length()-1);
//                logger.debug("Jmol running collected display string: '" +
//                                                     frameScript + "'");

                runScript(frameScript);
                runScript(colorScript);
            }

            //Continue with atoms by index
            if (atomSelectionIndices.size()>0){
                //Collect atoms, bonds etc and create script to highlight
                String selectionString="selectionHalos on; Select none; SELECT ";
                Collections.sort( atomSelectionIndices );
                for (Integer i : atomSelectionIndices){
                    selectionString=selectionString + "atomno=" + i.intValue()+",";
                }

                //Remove last comma
                selectionString=selectionString.substring(0, selectionString.length()-1);
//                logger.debug("Collected display string: '" + selectionString + "'");

                runScript( selectionString );
            }
            
            //Process all collected scripts
            if (collectedScripts.size()>0){
                logger.debug("Running scripts from selections in jmol: ");
                for (String script : collectedScripts){
//                    logger.debug("  " + script);
                    runScript( script );
                }
                
            }

        }

    private void processMolecules( List<ICDKMolecule> collectedCDKMols ) {

        logger.debug("######### Process jmol molecules ############");

        //Start by ICDKMolecules to set of the ChemFiles, which Jmol expects
        //as input

            //Store ChemModels here, to add them to ChemFile for input in Jmol
            List<ChemModel> collectedModels=new ArrayList<ChemModel>();

            //Loop over all collected molecules, extract ChemModels and add to list
            for (ICDKMolecule cdkMol: collectedCDKMols){

                //The ICDKMolecule could be a CDKConformer
                if ( cdkMol instanceof CDKConformer ) {
                    CDKConformer cdkConf = (CDKConformer) cdkMol;

                    //Extract individual atomContainers
                    //and add multiple models
                    for (int i=0; i< cdkConf.getConformerContainer().size();i++){
                        
                        IAtomContainer cac= cdkConf.getConformerContainer().get( i );
                        
                        //We need to clone, as same AC returned
                        //TODO: Verfy this
                        IAtomContainer caccopy;
                        try {
                            caccopy = (IAtomContainer) cac.clone();
                            addAtomContainer(collectedModels, caccopy);
//                            logger.debug("Added an AC from conformer.");

                        } catch ( CloneNotSupportedException e ) {
                            logger.debug("Could not clone AC in Conformer."+
                            " AC omitted in Jmol.");
                        }
                        
                    }
                    
                    //FIXME: above uses for, below uses generic iterator.
                    //Remove one of them when bug fixed.
                    
/*                    for (IAtomContainer cac: cdkConf.getConformerContainer()){
                        //We need to clone, as same AC returned
                        //TODO: Verfy this
                        IAtomContainer caccopy;
                        try {
                            caccopy = (IAtomContainer) cac.clone();
                            addAtomContainer(collectedModels, caccopy);
//                            logger.debug("Added an AC from conformer.");

                        } catch ( CloneNotSupportedException e ) {
                            logger.debug("Could not clone AC in Conformer."+
                            " AC omitted in Jmol.");
                        }
                    }
                        */
                }

                //Else, we have an ICDKMolecule
                else {
                    IAtomContainer ac=cdkMol.getAtomContainer();

                    //Only add if have 3D coords
                    if (GeometryTools.has3DCoordinates(ac)){
                        addAtomContainer(collectedModels, ac);
                    }
                }
            }
            
            //If we have models collected, add them to a ChemFile and send to Jmol
            if (collectedModels.size()>0){

                logger.debug("# Jmol, we have " + collectedModels.size() + 
                             " ChemModels to send to jmol");
                
                //Check if stored ChemFile's models differs from New ChemModels
                boolean similar=false;

                if (chemFile!=null){
                    int oldModelCount=chemFile.getChemSequence( 0 ).getChemModelCount();
                    int newModelCount=collectedModels.size();

                    similar=true;
                    
                    //Fast way to see if we should continue comparing models
                    if (oldModelCount==newModelCount){
                        
                        //Compare one by one
                        for (int i=0; i<chemFile.getChemSequence( 0 ).getChemModelCount();i++){
                            IChemModel cm1=chemFile.getChemSequence( 0 ).getChemModel( i );
                            IChemModel cm2=collectedModels.get( i );
                            String title1=(String) cm1.getMoleculeSet().getMolecule( 0 ).getProperty( "cdk:Title" );
                            String title2=(String) cm2.getMoleculeSet().getMolecule( 0 ).getProperty( "cdk:Title" );
                            
                            //If all titles are same, we conclude the chemmodels are similar
                            if (!(title1.equals(title2))){
                                similar=false;
                            }
                                
                        }
                        
                    }
                    else{
                        similar=false;
                    }
                    
                }
                
                //See if we already have chemModels stored, for example 
                //clicking on another conformer
                if (similar==false){
                    //Create a chemfile, as jmol expects this
                    ChemFile cf=new ChemFile();
                    ChemSequence seq=new ChemSequence();
                    cf.addChemSequence(seq);

                    //Add all available chemmodels to chemfile
                    for (ChemModel model : collectedModels){
                        seq.addChemModel(model);
                    }

                    //Set the ChemFile as input to jmol
                    setChemFile(cf);

                    //Indicate that we now have content
                    setCleared( false );
                }
            }else if (isCleared()==false){
                clearView();
                setCleared( true );
            }

            
        } //End act upon collected ChemModels

    private void extractFromSelection(
                                       IStructuredSelection ssel,
                                       List<ICDKMolecule> collectedCDKMols,
                                       List<IChemicalSelection> chemicalSelections ) {

        //Loop all selections; if they can provide AC: add to moleculeSet
        for (Object obj : ssel.toList()){
            
            boolean storeSelection=false;

            
            //If we have an ICDKMolecule, just get the AC directly
            if (obj instanceof ICDKMolecule) {
                ICDKMolecule cdkmol = (ICDKMolecule) obj;
                collectedCDKMols.add( cdkmol );
                storeSelection=true;
                
//                if (cdkmol.getAtomContainer()==null){
//                    logger.debug("CDKMolecule but can't get AtomContainer.");
//                }
//                //Only add if have 3D coords
//                IAtomContainer ac=cdkmol.getAtomContainer();
//                if (GeometryTools.has3DCoordinates(ac)){
//                    addAtomContainer( displayedModels, ac );
//                }
            }


            //Else try to get the different adapters
            else if (obj instanceof IAdaptable) {
                IAdaptable ada=(IAdaptable)obj;

                
                //Handle case where Iadaptable can return a molecule
                Object molobj=ada
                .getAdapter( net.bioclipse.core.domain.IMolecule.class );
                if (molobj!=null){
                    //If adaptable returns a cdkmolecule, add it directly 
                    if (molobj instanceof ICDKMolecule) {
                        ICDKMolecule cdkmol = (ICDKMolecule) molobj;
                        collectedCDKMols.add( cdkmol );
                    }

                    //If adaptable at least returns an IMolecule
                    //we can create CDKMolecule from it (this is costly though)
                    else if (molobj instanceof net.bioclipse.core.domain.IMolecule ){
                        net.bioclipse.core.domain.IMolecule bcmol 
                        = (net.bioclipse.core.domain.IMolecule) molobj;
                        try {
                            //Lengthy operation, as via CML or SMILES
                            ICDKMolecule cdkmol=cdk.create( bcmol );
                            collectedCDKMols.add( cdkmol );
                        } catch ( BioclipseException e ) {
                            e.printStackTrace();
                        }
                    }
                    storeSelection=true;
                }

                //Handle case where Iadaptable can return atoms to be highlighted
                Object chemSelectionObj=ada
                .getAdapter( IChemicalSelection.class );
                if (chemSelectionObj!=null){
                    chemicalSelections.add((IChemicalSelection)chemSelectionObj);
                    storeSelection=true;
                }

                //Handle case where Iadaptable can return models to be shown
                Object chemModelSel=ada
                .getAdapter( ModelSelection.class );
                if (chemModelSel!=null){
                    chemicalSelections.add( (IChemicalSelection)chemModelSel);
                    storeSelection=true;
                }

                //Handle case where Iadaptable can return a script
                Object scriptSelection=ada
                .getAdapter( ScriptSelection.class );
                if (scriptSelection != null){
                    chemicalSelections.add((ScriptSelection)scriptSelection);
                    storeSelection=true;
                }

            } //End of adaptable collection

            if (storeSelection==true){
                System.out.println("Storing selection in jmol");
                lastSelected.add( obj);
            }

        } //End of loop over selections
    }

    /**
     * Return a color from list by index
     * @param i
     * @return
     */
    private String getColorEnum( Integer i ) {

        switch (i%7){
            case 1 : return "red";
            case 2 : return "green";
            case 3 : return "yellow";
            case 4 : return "blue";
            case 5 : return "magenta";
            case 6 : return "cyan";
            case 7 : return "amber";
            default : return "brown";
        }
    }

    private void addAtomContainer( List<ChemModel> models, IAtomContainer ac ) {

        //Create a MolSet to hold the molecule
        MoleculeSet ms=new MoleculeSet();
        ms.addAtomContainer( ac );

        //Create a ChemModel to hold the MolSet
        ChemModel model=new ChemModel();
        model.setMoleculeSet(ms);
        
        models.add(model);
        
    }

    /**
     * Clear JmolView by issuing the "zap" command
     */
     private void clearView() {

         runScript( "zap" );
        
    }

     /**
      * Set a chemfile as mol in Jmol with openClientFile("","",ChemFile)
      * @param cf
      */
    private void setChemFile(IChemFile cf) {
        
        //Compare if we have new things, else just return
        
        chemFile=cf;
//        System.out.println("%%%%%%%%%%%");
//        System.out.println(cf);
//        System.out.println("%%%%%%%%%%%");
        
//        try {
//            ByteArrayOutputStream bo=new ByteArrayOutputStream();
//            CMLWriter writer=new CMLWriter(bo);
//            writer.write( cf );
//            System.out.println("%%%%%%%%%%%");
//            System.out.println(bo.toString());
//            System.out.println("%%%%%%%%%%%");
//            
//        } catch ( CDKException e ) {
//            e.printStackTrace();
//        }
//        
//
//

        logger.debug("Opening Jmol via CDK's ChemFile and CdkJmolAdapter...");

        //Maybe fork off a new thread?? TODO!
        jmolPanel.openClientFile("", "", cf);

        String strError = jmolPanel.getOpenFileError();
        if (strError != null){
            logger.error(strError);
            text.setEnabled(false);
        }
    }

    /**
     * Execute a script in Jmol
     * @param script
     */
    public void runScript( String script ) {

        logger.debug("Running jmol script: '" + script + "'");
        String res=jmolPanel.getViewer().evalString(script);
        if (res!=null)
            logger.debug("Jmol said: '" + res + "'");
        
    }

}