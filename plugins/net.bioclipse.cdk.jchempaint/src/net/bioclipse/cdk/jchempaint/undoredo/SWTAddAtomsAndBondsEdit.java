package net.bioclipse.cdk.jchempaint.undoredo;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.controller.undoredo.AddAtomsAndBondsEdit;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;

public class SWTAddAtomsAndBondsEdit extends AddAtomsAndBondsEdit 
                                     implements IUndoableOperation {
    
    private AddAtomsAndBondsEdit innerEdit;

    public SWTAddAtomsAndBondsEdit(IChemModel chemModel,
                                   IAtomContainer undoRedoContainer, 
                                   String type, 
                                   IControllerModel c2dm) {
        super(chemModel, undoRedoContainer, type, c2dm);
    }


    public IStatus redo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        innerEdit.redo();
        return null;
    }

    public void removeContext(IUndoContext context) {
        // TODO Auto-generated method stub
        
    }

    public IStatus undo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        innerEdit.undo();
        return Status.OK_STATUS;
    }


    public IStatus execute(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        // TODO Auto-generated method stub
        return Status.OK_STATUS;
    }


    public void addContext(IUndoContext context) {
        // TODO Auto-generated method stub
        
    }


    public boolean canExecute() {
        // TODO Auto-generated method stub
        return false;
    }


    public void dispose() {
        // TODO Auto-generated method stub
        
    }


    public IUndoContext[] getContexts() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean hasContext(IUndoContext context) {
        // TODO Auto-generated method stub
        return false;
    }

}
