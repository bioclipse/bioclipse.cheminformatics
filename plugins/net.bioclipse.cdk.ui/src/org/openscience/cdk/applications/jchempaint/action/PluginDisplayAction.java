package org.openscience.cdk.applications.jchempaint.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

//import org.openscience.cdk.applications.plugin.ICDKPlugin;

public class PluginDisplayAction extends JCPAction {
    
	private static final long serialVersionUID = -2699051675412699258L;
	
//	private ICDKPlugin plugin;
//    
//    public void setPlugin(ICDKPlugin plugin) {
//        this.plugin = plugin;
//    }
    
	public void run() {
		run(null);
	}
	
	public void run(ActionEvent e) {
//    	  JPanel pluginPanel = plugin.getPluginPanel();
//        plugin.start();
//        if (pluginPanel != null) {
//            JDialog pluginWindow = new JDialog();
//            pluginWindow.setTitle(plugin.getName());
//            pluginWindow.getContentPane().add(pluginPanel);
//            pluginWindow.setJMenuBar(getMenuBar());
//            pluginWindow.pack();
//            pluginWindow.show();
//        }
    }
    
    private JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
/*
        // try to plugin's private menu
        JMenu customPluginMenu = plugin.getMenu();
        if (customPluginMenu != null) {
            if (customPluginMenu.getText().length() == 0) {
                customPluginMenu.setText("Menu");
            }
            menuBar.add(customPluginMenu);
        }
            
        // add menu with plugin info
        JMenu aboutMenu = new JMenu("About");
        JMenuItem versionMenuItem = new JMenuItem("v. " + plugin.getPluginVersion());
        versionMenuItem.setEnabled(false);
        aboutMenu.add(versionMenuItem);
        menuBar.add(aboutMenu);
*/        
        return menuBar;
    }
    
}