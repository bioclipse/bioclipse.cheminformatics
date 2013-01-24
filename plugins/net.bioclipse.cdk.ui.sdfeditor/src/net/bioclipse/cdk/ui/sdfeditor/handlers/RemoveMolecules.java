package net.bioclipse.cdk.ui.sdfeditor.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveMolecules extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor( event );
        MoleculesEditor molEditor = (MoleculesEditor)
                                    part.getAdapter( MoleculesEditor.class );

        int[] vals  = molEditor.getMolTableViewer().getSelectedRows();
        List<Integer> indices = new ArrayList<Integer>(vals.length);
        for(int i:vals) indices.add(i);
        Collections.sort(indices,Collections.reverseOrder());
        for(int i:indices) {
        	molEditor.getModel().delete(i);
        }
        
        molEditor.getMolTableViewer().setSelection(StructuredSelection.EMPTY);
        molEditor.setDirty(true);
        molEditor.refresh();
        return null;
	}
}
