/*******************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.jmol.actions;

import net.bioclipse.jmol.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Export the current JMol editor's contents as a PNG.
 * 
 * @author maclean
 *
 */
public class ExportHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		SaveAsDialog dialog = new SaveAsDialog(shell); 
		int returnValue = dialog.open();

		if (returnValue == SaveAsDialog.OK) {
		    IPath path = dialog.getResult();
		    String strPath = path.toPortableString();
		    if (!strPath.endsWith(".png")) strPath += ".png";
		    Activator.getDefault().getJmolManager().snapshot(strPath);
		}

		return null;
	}
}
