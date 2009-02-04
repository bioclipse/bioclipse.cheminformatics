package net.bioclipse.jmol.actions;

import net.bioclipse.jmol.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Export the current JMol editor's contents as a PNG.
 * 
 * @author maclean
 *
 */
public class ExportHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		SaveAsDialog dialog = new SaveAsDialog(shell); 
		int returnValue = dialog.open();

		if (returnValue == SaveAsDialog.OK) {
		    IPath path = dialog.getResult();
		    Activator.getDefault().getJmolManager().snapshot(
		            path.toPortableString());
		}

		return null;
	}
}
