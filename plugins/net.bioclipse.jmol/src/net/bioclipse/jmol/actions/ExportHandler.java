package net.bioclipse.jmol.actions;

import net.bioclipse.jmol.Activator;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Export the current JMol editor's contents as a PNG.
 * 
 * @author maclean
 *
 */
public class ExportHandler extends AbstractHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(ExportHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String [] {"PNG Files", "All Files (*.*)"});
		dialog.setFilterExtensions(new String [] {"*.png", "*.*"});
		String result = dialog.open();

		Activator.getDefault().getJmolManager().snapshot(result);

		return null;
	}
}
