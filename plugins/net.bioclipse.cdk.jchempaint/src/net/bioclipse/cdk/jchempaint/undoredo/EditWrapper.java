/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *      Arvid Berg <goglepox@users.sf.net>
 *
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.undoredo;

import net.bioclipse.cdk.jchempaint.Activator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;

public class EditWrapper extends  AbstractOperation implements IUndoRedoable {

    IEdit edit;
    String name;

    public EditWrapper(IEdit edit) {
        super( edit.getClass().getSimpleName());
        this.edit = edit;
        this.name = edit.getClass().getSimpleName();
    }

    @Override
    public IStatus execute(
                           IProgressMonitor monitor,
                           IAdaptable info )
    throws ExecutionException {

        edit.redo();
        return new Status( IStatus.OK, Activator.PLUGIN_ID,
                           "Executed "+ name);
    }

    @Override
    public IStatus redo(
                        IProgressMonitor monitor,
                        IAdaptable info )
    throws ExecutionException {

        edit.redo();
        return new Status( IStatus.OK, Activator.PLUGIN_ID,
                           "Redid "+ name);
    }

    @Override
    public IStatus undo(
                        IProgressMonitor monitor,
                        IAdaptable info )
    throws ExecutionException {

        edit.undo();
        return new Status( IStatus.OK, Activator.PLUGIN_ID,
                           "Undid" + name);
    }

    public void redo() {
        edit.redo();
    }

    public void undo() {
        edit.undo();
    }
}
