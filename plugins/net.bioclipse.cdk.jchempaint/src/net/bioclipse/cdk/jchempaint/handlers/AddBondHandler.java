package net.bioclipse.cdk.jchempaint.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openscience.cdk.controller.AddBondModule;
import org.openscience.cdk.controller.ControllerHub;

public class AddBondHandler extends AbstractJChemPaintHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ControllerHub hub = getControllerHub(event);
        
        hub.setActiveDrawModule(new AddBondModule(hub));
        return null;
    }

}
