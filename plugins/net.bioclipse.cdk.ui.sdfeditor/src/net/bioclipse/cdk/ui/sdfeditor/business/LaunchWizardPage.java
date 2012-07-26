/* *****************************************************************************
 * Copyright (c) 2007-2012 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *    
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This class lunches a wizard that let the user import properties from a 
 * txt-file to a SDF-file. The result is saved in a new sd-file.
 * 
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class LaunchWizardPage extends AbstractHandler implements IHandler{

    private ISelection selection;
    private IStructuredSelection ssel;
    
    public LaunchWizardPage() {  }
    
    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
    
        selection = HandlerUtil.getActiveMenuSelection( event );
        if ( selection instanceof IStructuredSelection ) {
            ssel = (IStructuredSelection) selection;
        }

        SDFPropertiesImportWizard wizard = new SDFPropertiesImportWizard(ssel);
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
                                               .getActiveWorkbenchWindow()
                                               .getShell(), wizard);
        dialog.open();
        return null;
    }

    public void selectionChanged(ExecutionEvent event) {
        this.selection = HandlerUtil.getCurrentSelection( event );
    }

}
