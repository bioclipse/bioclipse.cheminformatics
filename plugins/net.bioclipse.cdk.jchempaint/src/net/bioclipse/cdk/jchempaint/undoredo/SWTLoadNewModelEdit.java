package net.bioclipse.cdk.jchempaint.undoredo;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openscience.cdk.controller.undoredo.LoadNewModelEdit;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReactionSet;


public class SWTLoadNewModelEdit extends LoadNewModelEdit implements IUndoableOperation{

    private IUndoContext context;

    public SWTLoadNewModelEdit(IChemModel chemModel, IMoleculeSet oldsom,
            IReactionSet oldsor, IMoleculeSet newsom, IReactionSet newsor,
            String type,IUndoContext context) {

        super( chemModel, oldsom, oldsor, newsom, newsor, type );
        this.context = context;
    }

    public void addContext( IUndoContext context ) {

        // TODO Auto-generated method stub

    }

    public boolean canExecute() {

        // TODO Auto-generated method stub
        return false;
    }

    public void dispose() {

        // TODO Auto-generated method stub

    }

    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
                                                                       throws ExecutionException {

        // TODO Auto-generated method stub
        return Status.OK_STATUS;
    }

    public IUndoContext[] getContexts() {

        // TODO Auto-generated method stub
        return null;
    }

    public String getLabel() {

        return super.getPresentationName();
    }

    public boolean hasContext( IUndoContext context ) {

        return context.matches( this.context );
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
                                                                    throws ExecutionException {

        super.redo();
        return Status.OK_STATUS;
    }

    public void removeContext( IUndoContext context ) {

        // TODO Auto-generated method stub

    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
                                                                    throws ExecutionException {

        super.undo();
        return Status.OK_STATUS;
    }

}
