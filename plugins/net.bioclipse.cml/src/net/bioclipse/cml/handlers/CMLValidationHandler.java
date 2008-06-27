package net.bioclipse.cml.handlers;

import net.bioclipse.cml.managers.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class CMLValidationHandler  extends AbstractHandler{
	  public Object execute(ExecutionEvent arg0) throws ExecutionException {
			Activator.getDefault().getValidateCMLManager();
			return null;
	  }
}
