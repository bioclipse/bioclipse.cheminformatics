package net.bioclipse.cdk.jchempaint.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class UndoHandler extends AbstractJChemPaintHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        getEditor(event).undo();
        getEditor(event).update();
        return null;
    }

}
