package net.bioclipse.cdk.jchempaint.undoredo;

import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openscience.cdk.controller.undoredo.ChangeHydrogenCountEdit;
import org.openscience.cdk.interfaces.IAtom;


public class SWTChangeHydrogenCountEdit extends ChangeHydrogenCountEdit
        implements IUndoableOperation {
    IUndoContext context;
    public SWTChangeHydrogenCountEdit( Map<IAtom, Integer[]> atomHydrogenCountsMap,
                                    String type, IUndoContext context) {
        super(atomHydrogenCountsMap,type);
        this.context = context;
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

public IStatus execute( IProgressMonitor monitor, IAdaptable info )
       throws ExecutionException {

// TODO Auto-generated method stub
return Status.OK_STATUS;
}

public void addContext( IUndoContext context ) {

}

public boolean canExecute() {

// TODO Auto-generated method stub
return false;
}

public void dispose() {

// TODO Auto-generated method stub

}

public IUndoContext[] getContexts() {

return new IUndoContext[] { this.context };
}

public String getLabel() {
return super.getPresentationName();
}

public boolean hasContext( IUndoContext context ) {

// TODO Auto-generated method stub
return context.matches( this.context );
}
}
