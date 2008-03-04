package org.openscience.cdk.applications.jchempaint;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.MissingResourceException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.bioclipse.cdk.ui.editors.JCPEditor;
import net.bioclipse.cdk.ui.editors.JCPMultiPageEditorContributor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.openscience.cdk.applications.APIVersionTester;
import org.openscience.cdk.applications.jchempaint.action.JCPAction;
import org.openscience.cdk.applications.jchempaint.action.PluginDisplayAction;
import org.openscience.cdk.applications.jchempaint.action.PluginMenuAction;

public class MenuBarMaker {
	private static ArrayList actionList = new ArrayList();
	private static String guiString = "stable";
	private static JCPMultiPageEditorContributor contributor;
	
	public static ArrayList createMenuBar(JCPMultiPageEditorContributor theContributor, IMenuManager menuManager) {
		try{
		contributor = theContributor;
		String definition = getMenuResourceString("menubar");
		String[] menuKeys = StringHelper.tokenize(definition);
		for (int i = 0; i < menuKeys.length; i++) {
			String key = menuKeys[i];
			IMenuManager menu = createMenu(key);
			menuManager.add(menu);
		}
		menuManager.add(getJcpPluginMenu());
		return actionList;
		}catch(Throwable ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	private static IMenuManager createMenu(String key) {
		MenuManager submenu = new MenuManager(JCPLocalizationHandler.getInstance().getString(key));
		String[] itemKeys = StringHelper.tokenize(getMenuResourceString(key));
		for (int i = 0; i < itemKeys.length; i++) {
			if (itemKeys[i].equals("-")) {
				Object separator = new Separator();
				actionList.add(separator);
			}
			else if (itemKeys[i].startsWith("@")) {
				IMenuManager menu = createMenu(itemKeys[i].substring(1));
				submenu.add(menu);
			}
			else if (itemKeys[i].endsWith("+")) {
				Action action=createAction(itemKeys[i].substring(0,itemKeys[i].length()-1),true);
				submenu.add(action);
				if(itemKeys[i].substring(0, itemKeys[i].length() - 1).equals("addImplHydrogen"))
					action.setChecked(true);
			}
			else {
				submenu.add(createAction(itemKeys[i],false));
			}
		}
		return submenu;
	}

	/**
	 * Creates a menu for the available plugins
	 * 
	 * @return The menu
	 */
	public static MenuManager getJcpPluginMenu() {
		
		//Uncommented to work in Bioclipse2. FIXME
		return null;
/*		
		    MenuManager menu = new MenuManager("JCP Plugins");
			JCPPropertyHandler jcph = JCPPropertyHandler.getInstance();

			CDKPluginManager pluginManager = new CDKPluginManager(jcph.getJChemPaintDir().toString(), (JCPEditor)contributor.getActiveEditorPart() );

			String pluginDirName=new File(jcph.getJChemPaintDir(), "plugins").toString();
//			pluginManager.setParentClassLoader(Thread.currentThread().getContextClassLoader());
			pluginManager.loadPlugins(pluginDirName);
	        Enumeration pluginsEnum = pluginManager.getPlugins();
	        while (pluginsEnum.hasMoreElements()) {

	        	ICDKPlugin plugin = (ICDKPlugin)pluginsEnum.nextElement();
	        	
	            try{
		            MenuManager pluginMenu = new MenuManager(plugin.getName());
		            boolean hasOneOrMoreDefaultMenuItems = false;
		            JPanel pluginPanel = plugin.getPluginPanel();
		            if (pluginPanel != null) {
		                JCPAction pda=new JCPAction().getAction("org.openscience.cdk.applications.jchempaint.action.PluginDisplayAction", false,false);
		                ((PluginDisplayAction)pda).setPlugin(plugin);
		                pda.setText("Plugin Window");
		                pda.setContributor(contributor);
		                hasOneOrMoreDefaultMenuItems = true;
		                pluginMenu.add(pda);
		            }
		            JPanel configPanel = plugin.getPluginConfigPanel();
		            if (configPanel != null) {
		                JCPAction pda=new JCPAction().getAction("org.openscience.cdk.applications.jchempaint.action.PluginMenuAction", false,false);
		                ((PluginMenuAction)pda).setPanel(configPanel);
		                pda.setText("Config Window");
		                pda.setContributor(contributor);
		                hasOneOrMoreDefaultMenuItems = true;
		            }
		            JMenu customPluginMenu = plugin.getMenu();
		            if (customPluginMenu != null) {
		                if (customPluginMenu.getText().length() == 0) {
		                    customPluginMenu.setText("Plugin's menu");
		                }
		                JFrame panel=new JFrame();
		                JMenuBar mb=new JMenuBar();
		                mb.add(customPluginMenu);
		                panel.setJMenuBar(mb);
		                JCPAction pda=new JCPAction().getAction("org.openscience.cdk.applications.jchempaint.action.PluginMenuAction", false,false);
		                ((PluginMenuAction)pda).setFrame(panel);
		                pda.setText(customPluginMenu.getText());
		                pda.setContributor(contributor);
		                hasOneOrMoreDefaultMenuItems = true;
		                pluginMenu.add(pda);
	
		            }
		            MenuManager aboutMenu = new MenuManager("About");
		            JCPAction pda=new JCPAction().getAction("org.openscience.cdk.applications.jchempaint.action.JCPAction", false,false);
		            pda.setText("v. " + plugin.getPluginVersion());
		            pda.setContributor(contributor);
		            aboutMenu.add(pda);
		            if (APIVersionTester.isBiggerOrEqual("1.10", plugin.getAPIVersion())) {
			            JCPAction pda2=new JCPAction().getAction("org.openscience.cdk.applications.jchempaint.action.JCPAction", false,false);
		                pda2.setText("license: " + plugin.getPluginLicense());
		                pda2.setContributor(contributor);
		                aboutMenu.add(pda2);
		            }
		            pluginMenu.add(aboutMenu);
		            menu.add(pluginMenu);
	        	}catch(Exception ex){
	        		//we do nothing if a single plugin fails
	        		BioclipseConsole.writeToConsole("error loading plugin " + plugin.getName());
	        	}
	        }
	        return menu;
	        
	        */
	}

	   

	   
	   
	private static JCPAction createAction(String key, boolean withCheckBox) {
		JCPAction jcpAction = null;
		String astr = JCPPropertyHandler.getInstance().getResourceString(key + JCPAction.actionSuffix);
		if (astr != null) {
			String translation = JCPLocalizationHandler.getInstance().getString(key);
			jcpAction = new JCPAction().getAction(astr, false, withCheckBox);
			jcpAction.setText(translation);
			jcpAction.setContributor(contributor);
			jcpAction.setEnabled(true);
		}
		return jcpAction;
	}

	private static String getMenuResourceString(String key) {
		String str;
		try {
			str = JCPPropertyHandler.getInstance().getGUIDefinition(guiString).getString(key);
		} catch (MissingResourceException mre) {
			str = null;
		}
		return str;
	}
	
	

}
