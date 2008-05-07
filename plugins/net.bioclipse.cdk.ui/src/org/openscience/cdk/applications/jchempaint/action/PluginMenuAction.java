package org.openscience.cdk.applications.jchempaint.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PluginMenuAction extends JCPAction {
    
    private static final long serialVersionUID = -2699051675412699258L;
    
    private JFrame frame;
    private JPanel panel;
    
    public void setFrame(JFrame frame) {
        this.frame=frame;
    }
    
    public void setPanel(JPanel panel) {
        this.panel=panel;
    }

    
    public void run() {
        run(null);
    }
    
    public void run(ActionEvent e) {
        if(panel!=null)
            panel.show();
        if(frame!=null)
            frame.show();
    }
}