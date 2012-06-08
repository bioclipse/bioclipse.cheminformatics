package net.bioclipse.cdk.ui.sdfeditor.business;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


public class LaunchWizardPage extends AbstractHandler {

    private ISelection selection;
    
    
    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
    
    
        SDFPropertiesImportWizard wizard = new SDFPropertiesImportWizard();
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
        return null;
    }

    public void selectionChanged(ExecutionEvent event) {
        this.selection = HandlerUtil.getCurrentSelection( event );
    }

}
