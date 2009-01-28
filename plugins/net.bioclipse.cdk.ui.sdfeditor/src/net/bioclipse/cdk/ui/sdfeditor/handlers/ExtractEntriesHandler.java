package net.bioclipse.cdk.ui.sdfeditor.handlers;

import net.bioclipse.cdk.ui.wizards.ExtractWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;


public class ExtractEntriesHandler extends AbstractHandler {


    public Object execute( ExecutionEvent event ) throws ExecutionException {
        ISelection sel =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getSelectionService().getSelection();
        if ( !sel.isEmpty() ) {
            if ( sel instanceof IStructuredSelection ) {
                try {
                    ExtractWizard wiz=new ExtractWizard();
                    WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
                    dialog.open();
                } catch ( Exception e ) {
                    e.printStackTrace();
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }
        return null;
    }
}
