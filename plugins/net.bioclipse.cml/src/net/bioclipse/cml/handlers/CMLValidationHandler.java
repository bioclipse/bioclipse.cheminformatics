/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cml.handlers;
import java.io.IOException;
import net.bioclipse.cml.managers.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
public class CMLValidationHandler extends ActionDelegate{
        public void run(IAction action){
                  ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
                  if (sel.isEmpty()==false){
                      if (sel instanceof IStructuredSelection) {
                         IStructuredSelection ssel = (IStructuredSelection) sel;
                         String display;
                                 try {
                                        Activator.getDefault().getValidateCMLManager().validate(((IFile)ssel.getFirstElement()));
                                 } catch (IOException e) {
                                        throw new RuntimeException(e);
                                 }
                      }
                  }		
        }
}
