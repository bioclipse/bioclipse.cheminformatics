package net.bioclipse.cml.handlers;

import net.bioclipse.cml.managers.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class CMLValidationHandler extends AbstractHandler{
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		  ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		  if (sel.isEmpty()==false){
		      if (sel instanceof IStructuredSelection) {
		         IStructuredSelection ssel = (IStructuredSelection) sel;
		         String display=Activator.getDefault().getValidateCMLManager().validate(ssel.getFirstElement().toString());
		         MessageBox mb = new MessageBox(new Shell(), SWT.OK);
		         mb.setText("CML checked");
		         mb.setMessage(display);
		         mb.open();
		      }
		  }		
		  return null;
	}
}
