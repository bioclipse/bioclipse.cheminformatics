package net.bioclipse.cdk.jchempaint.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class RedoHandler extends AbstractJChemPaintHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        getEditor(event).redo();
        getEditor(event).update();
        return null;
    }

}
