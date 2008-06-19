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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculeSelection;
import net.bioclipse.core.business.BioclipseException;

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
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IMoleculeSet;
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
    
    /**
     * The constructor.
     */
    public JmolView() {
        history = new ArrayList<String>(50);
        jmolPanel= new JmolPanel(this);
        cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        setCleared( true );
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

        IStructuredSelection ssel = (IStructuredSelection) selection;

        IMoleculeSet moleculeSet=new MoleculeSet();
        MoleculeSelection molSelection=null;

        //Loop all selections; if they can provide AC: add to moleculeSet
        for (Object obj : ssel.toList()){

            //TODO: Handle multiple selections!

            //If we have an ICDKMolecule, just get the AC
            if (obj instanceof ICDKMolecule) {
                ICDKMolecule mol = (ICDKMolecule) obj;
                if (mol.getAtomContainer()==null){
                    logger.debug("CDKMolecule but can't get AtomContainer.");
                }
                //Only add if have 3D coords
                IAtomContainer ac=mol.getAtomContainer();
                if (GeometryTools.has3DCoordinates(ac)){
                    moleculeSet.addAtomContainer(ac);
                }
            }

            //Try to get an IMolecule via the adapter
            else if (obj instanceof IAdaptable) {
                IAdaptable ada=(IAdaptable)obj;

                //Handle case where Iadaptable can return a molecule
                Object molobj=ada
                .getAdapter( net.bioclipse.core.domain.IMolecule.class );
                if (molobj==null || 
                        (!(molobj instanceof net.bioclipse.core.domain.IMolecule ))){
                    //Nothing to show
//                  clearView();
//                  return;
                }

                if (molobj!=null){

                    net.bioclipse.core.domain.IMolecule bcmol 
                    = (net.bioclipse.core.domain.IMolecule) molobj;

                    //Create cdkmol from IMol, via CML or SMILES if that fails
                    ICDKMolecule cdkMol;
                    try {
                        //Create molecule
                        cdkMol = cdk.create( bcmol );
                        IAtomContainer ac=cdkMol.getAtomContainer();


                        //Only add if have 3D coords
                        if (GeometryTools.has3DCoordinates(ac)){
                            moleculeSet.addAtomContainer(ac);
                        }

                    } catch ( BioclipseException e ) {
                        e.printStackTrace();
                    }
                }
                
                //Handle case where Iadaptable can return a molecule
                Object selobj=ada
                .getAdapter( net.bioclipse.cdk.domain.MoleculeSelection.class );
                if (selobj!=null){
                    molSelection=(MoleculeSelection)selobj;
                    
                }

                
            }


            //Set new mol in Jmol
            if (moleculeSet.getAtomContainerCount()>0){
                ChemFile cf=new ChemFile();
                ChemSequence seq=new ChemSequence();
                cf.addChemSequence(seq);
                ChemModel model=new ChemModel();
                seq.addChemModel(model);

                //Set the molset to the model
                model.setMoleculeSet(moleculeSet);
                
                //Set the ChemFile as input to jmol
                setMolecule(cf);

                //Indicate that we now have content
                setCleared( false );

            }else
                if (isCleared()==false){
                    clearView();
                    setCleared( true );
                }

            //Handle highlighting if we have any
            if (molSelection!=null){
                
                IAtomContainer selAC=molSelection.getSelection();
                List<IAtomContainer> lst=ChemFileManipulator.getAllAtomContainers( chemFile );
                if (lst!=null && lst.size()>0){
                    IAtomContainer ac=lst.get( 0 );
                    System.out.println("** Current AC is:\n");
                    for (int i=0; i<ac.getAtomCount();i++){
                        System.out.println("Atom: " + ac.getAtom( i ));
                    }
                    System.out.println("\n** Should highlight these atoms:\n");
                    for (int i=0; i<selAC.getAtomCount();i++){
                        System.out.println("Atom: " + selAC.getAtom( i ));
                    }
                    
                }

            }
            
        }

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
    private void setMolecule(IChemFile cf) {
        
        chemFile=cf;

        logger.debug("Opening Jmol via CDK's ChemFile and CdkJmolAdapter...");

        //Maybe fork off a new thread?? TODO!
        jmolPanel.openClientFile("", "", cf);

        text.setEnabled(true);
        text.getParent().redraw();

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