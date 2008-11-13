package net.bioclipse.cdk.jchempaint.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;



public class CenterMoleculeHandler extends AbstractHandler implements IHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        // TODO Auto-generated method stub
        return null;
    }

    public JChemPaintView getView(ExecutionEvent event) {
        IWorkbenchPart part = HandlerUtil.getActivePart( event );
        return (JChemPaintView) part;
    }
}
