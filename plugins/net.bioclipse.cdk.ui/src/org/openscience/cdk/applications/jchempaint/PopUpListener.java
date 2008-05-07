package org.openscience.cdk.applications.jchempaint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.bioclipse.cdk.ui.editors.JCPMultiPageEditor;
import net.bioclipse.cdk.ui.editors.JCPMultiPageEditorContributor;

import org.openscience.cdk.applications.jchempaint.action.JCPAction;

public class PopUpListener implements ActionListener {

    private JCPAction action;
    private JCPMultiPageEditorContributor contributor;

    public PopUpListener(JCPMultiPageEditorContributor contributor, JCPAction a) {
        this.contributor = contributor;
        this.action = a;
    }

    public void actionPerformed(ActionEvent e) {
        action.run(e);
        DrawingPanel drawingPanel = ((JCPMultiPageEditor)contributor.getActiveEditorPart()).getDrawingPanel();
        drawingPanel.repaint();
    }


}
