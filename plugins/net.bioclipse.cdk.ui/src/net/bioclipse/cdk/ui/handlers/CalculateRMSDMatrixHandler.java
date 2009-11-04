/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ui.business.IUIManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * A handler for calculating a RMSD distance matrix, using the error of the
 * Kabsch alignment of the maximal common suctructures.
 */
public class CalculateRMSDMatrixHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow().
            getSelectionService().getSelection();
        if (!sel.isEmpty() && sel instanceof IStructuredSelection ) {
            IStructuredSelection ssel = (IStructuredSelection)sel;
            ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
            IUIManager ui = net.bioclipse.ui.business.Activator.getDefault().
                getUIManager();
            List<IMolecule> molecules = new ArrayList<IMolecule>(ssel.size());
            for (Object file : ssel.toList())
                molecules.add(cdk.loadMolecule((IFile)file));
            String path = "/Virtual/rmsd.csv";
            while (ui.fileExists(path))
                path = "/Virtual/rmsd" + UUID.randomUUID() + ".csv";
            try {
                String matrix = cdk.calculateRMSD(molecules, path);
                ui.open(matrix);
            } catch (BioclipseException cause) {
                throw new ExecutionException(
                    "Error while calculating RMSD matrix...",
                    cause
                );
            }
        }
        return null;
    }
}
