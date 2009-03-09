package net.bioclipse.jmol.actions;

import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.business.IJmolManager;
import net.bioclipse.jmol.editors.script.JmolScriptEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;


public class RunEditorScriptHandler extends AbstractHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        
        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        
        if (!( part instanceof JmolScriptEditor )){
            System.out.println("Run script caleld but no script editor active. " +
            		"Should not happen.");
            return null;
        }

        JmolScriptEditor jme = (JmolScriptEditor) part;
        if (jme.isDirty()){

            boolean result = MessageDialog.openQuestion(jme.getSite().getShell(), 
                   "Save Jmol Script", 
                   "Jmol Script Editor needs to be saved before this operation. " +
                   "Save and Continue?");
            if (result) {
                jme.doSave(null);
            }
            else{
                return null;
            }
        }
        
        IJmolManager jmol = Activator.getDefault().getJmolManager();

        IDocument doc = jme.getDocumentProvider().getDocument( jme.getEditorInput() );
        String text=doc.get();
 
        jmol.run( text );
        
        return null;
    }
}
