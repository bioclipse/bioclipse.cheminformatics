package net.bioclipse.jmol.actions;

import java.util.Map;

import net.bioclipse.jmol.editors.JmolEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunScriptHandler extends AbstractHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(RunScriptHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editor=HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof JmolEditor)) {
			logger.error("A jmol command was run but jmol is not active editor");
			return null;
		}
		JmolEditor jmolEditor = (JmolEditor) editor;
		String script=event.getParameter("net.bioclipse.jmol.scriptParameter");
		
		jmolEditor.runScript("select all;" + script + ";select none;");
//		jmolEditor.runScript(script);

		return null;
	}

	
}
