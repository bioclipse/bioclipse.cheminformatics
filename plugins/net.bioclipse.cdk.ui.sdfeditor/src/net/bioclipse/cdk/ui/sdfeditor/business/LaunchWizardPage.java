package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
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
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.SDFWriter;


public class LaunchWizardPage extends AbstractHandler implements IHandler{

    private ISelection selection;
    private IStructuredSelection ssel;
    
    public LaunchWizardPage() {
    }
    
    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
    
        selection = HandlerUtil.getActiveMenuSelection( event );
        if ( selection instanceof IStructuredSelection ) {
            ssel = (IStructuredSelection) selection;
//            System.out.println("Selected: " + ssel.getFirstElement());
        }

        SDFPropertiesImportWizard wizard = new SDFPropertiesImportWizard(ssel);
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
        return null;
    }

    public void selectionChanged(ExecutionEvent event) {
        this.selection = HandlerUtil.getCurrentSelection( event );
    }

}
