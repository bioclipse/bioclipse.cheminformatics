/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cdk.ui.actions;

import net.bioclipse.cdk.domain.CDKMolecule;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionDelegate;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

public class CreateSmilesAction extends ActionDelegate{


    private IStructuredSelection selection = StructuredSelection.EMPTY;

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection sel) {
        if(sel instanceof IStructuredSelection)
            selection = (IStructuredSelection) sel;
        else 
            selection = StructuredSelection.EMPTY;
    }

    @Override
    public void run(IAction action) {

        Object firstElement = selection.getFirstElement();
        if(!(firstElement instanceof CDKMolecule)) {
            System.out.println("Selection is not Molecule");
            return;
        }

        CDKMolecule mol = (CDKMolecule) firstElement;
        IAtomContainer ac=mol.getAtomContainer();

        if (ac==null){
            System.out.println("No AtomContainer loaded.");
            return;
        }
        else if (!(ac instanceof IMolecule)) {
            System.out.println("AC is not IMolecule.");
            return;
        }
        IMolecule imol = (IMolecule) ac;
        SmilesGenerator gen=new SmilesGenerator();
        try{
            String smiles=gen.createSMILES(imol);
            System.out.println("Smiles is: " + smiles);
        }catch (Exception e){
            System.out.println("General exception when generate smiles, originating in CDK. ");
            return;
        }


    }

}
