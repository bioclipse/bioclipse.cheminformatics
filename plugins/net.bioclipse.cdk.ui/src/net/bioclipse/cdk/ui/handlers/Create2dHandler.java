/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;


import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A handler class for a Generate 2D Coordinates menu item
 */
public class Create2dHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		  ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		  if (sel.isEmpty()==false){
		      if (sel instanceof IStructuredSelection) {
		         IStructuredSelection ssel = (IStructuredSelection) sel;
		         ICDKMolecule mol;
				 try {
					mol = Activator.getDefault().getCDKManager().loadMolecule((IFile)ssel.getFirstElement());
					Activator.getDefault().getCDKManager().generate2dCoordinates(mol);
				 } catch (Exception e) {
					throw new ExecutionException(e.getMessage());
				 }
		         MessageBox mb = new MessageBox(new Shell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		         mb.setText("Change file");
		         mb.setMessage("Do you want to write the coordinates into the existing file? If no, a new one will be created.");
		         int val=mb.open();
		         if(val==SWT.YES){
		        	 try {
						Activator.getDefault().getCDKManager().saveMolecule(mol, (IFile)ssel.getFirstElement(), ((IFile)ssel.getFirstElement()).getFileExtension());
					} catch (Exception e) {
						throw new ExecutionException(e.getMessage());
					}
		         }else{
		        	 //TODO
		         }
		      }
		  }		
		  return null;
	}

}
