package org.openscience.cdk.applications.jchempaint;

import javax.swing.undo.UndoableEdit;

import org.eclipse.core.commands.operations.IUndoContext;
import org.openscience.cdk.applications.jchempaint.action.UndoableAction;
import org.openscience.cdk.applications.undoredo.IUndoRedoHandler;

public class JCPBioclipseUndoRedoHandler implements IUndoRedoHandler {
    JChemPaintModel jcpm=null;
    IUndoContext undoContext=null;
    DrawingPanel drawingPanel=null;

    public void postEdit(UndoableEdit edit) {
        UndoableAction.pushToUndoRedoStack(edit,jcpm,undoContext, drawingPanel);
    }

    public void setDrawingPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }

    public void setJcpm(JChemPaintModel jcpm) {
        this.jcpm = jcpm;
    }

    public void setUndoContext(IUndoContext undoContext) {
        this.undoContext = undoContext;
    }


}