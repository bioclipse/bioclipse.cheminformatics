/* *****************************************************************************
 * Copyright (c) 2013 Jonathan Alvarsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jonathan Alvarsson - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.core.util.TimeCalculator;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.ui.business.IUIManager;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.AtomTypeAwareSaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

/**
 * A handler that can convert SDFiles files into SMILES.
 * 
 * @author jonalv
 */
public class ConvertSDFtoSMILES extends AbstractHandler{

	private static final Logger logger =
		Logger.getLogger(ConvertSDFtoSMILES.class);

    private final IUIManager ui = net.bioclipse.ui.business.Activator
                                     .getDefault().getUIManager();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection sel=HandlerUtil.getCurrentSelection(event);
		if (!(sel instanceof IStructuredSelection))
			throw new ExecutionException("Selection is not a file");

		IStructuredSelection ssel=(IStructuredSelection)sel;
		
		//We only operate on a single file currently
		Object obj = ssel.getFirstElement();
		
		if (!(obj instanceof IFile)) 
			throw new ExecutionException("Selection is not a file");

		final IFile input = (IFile) obj;

		final IPath outPath = input.getFullPath().removeFileExtension().addFileExtension("smi");

		final IFile output = ResourcesPlugin.getWorkspace().getRoot().getFile(outPath);
		if ( ui.fileExists( output ) ) {
            if ( !MessageDialog.openConfirm( 
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                          .getShell(),
                "SMILES-file already exists, confirm overwrite",
                "A file with the same name but the .smi file ending " +
                "already exists. Okey to overwrite it?" ) ) {
               return null;
            }
		}
		Activator.getDefault().getJavaCDKManager()
		                      .convertSDFtoSMILESFile( input );
		return null;
	}		
}
