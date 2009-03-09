/*******************************************************************************
 * Copyright (c) 2006-2009 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/

package net.bioclipse.jmol.editors.script;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;


/**
 * Jmol Script editor with syntax completion and coloring
 * 
 * @author ola
 * 
 */
public class JmolScriptEditor extends TextEditor {
	
	@Override
	public void setFocus() {
		super.setFocus();
	}

	public static final String ID = "net.bioclipse.jmol.editors.script.JmolScriptEditor";

	
	IAction runScriptAction;

	protected static String[] subStrings;
	
	protected static ArrayList<String> startList;
	protected static ArrayList<String> fullList;
	
	
	/**
	 * Constructor
	 *
	 */
	public JmolScriptEditor()
	{
		super();
		computeSubStrings();
		setSourceViewerConfiguration(new JmolSourceViewerConfig());
	}

	/**
	 * Add action bars by overriding
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "net.bioclipse.jmol.editors.script.JmolScriptEditor");

	}
	public static String[] getSubStrings() {
		return subStrings;
	}

	/**
	 * Compute all substrings of the proposals
	 */
	@SuppressWarnings("unchecked")
	private void computeSubStrings() {
		
		startList=new ArrayList();
		fullList=new ArrayList();
		
		//Concatenate JmolKeywords to proposals
		
		String[] jmolProposals=JmolKeywords.getAllKeywords();
		
		//All words
		for (int i=0;i<jmolProposals.length;i++){
			
			String name=jmolProposals[i];
			
			//This word
			for (int j=1;j<name.length();j++){
				String startAdd=name.substring(0,j);
//				String endAdd=name.substring(j,name.length());
				startList.add(startAdd);
				fullList.add(name);
			}
		}
		
		subStrings=new String[startList.size()];
		for (int i=0; i< fullList.size();i++){
			subStrings[i]=(String)startList.get(i);
		}
				
	}
	
	@SuppressWarnings("unchecked")
	public static String[] lookUpNames(String start){
		
		if (startList==null) return null;
		if (startList.size()<=0) return null;
		
		//Temp list
		ArrayList lst=new ArrayList();
		
		//Look through all in startlist and add matching names to ret
		for (int i=0; i< startList.size();i++){
			String thisStart = (String)startList.get(i).toString().toLowerCase();
			if (thisStart.startsWith(start.toLowerCase())){
				if (!(lst.contains(fullList.get(i).toString().toLowerCase())))
					lst.add(fullList.get(i).toString().toLowerCase());
			}
		}
		
		Collections.sort(lst);
		
		//Convert list to array of strings
		String[] ret=new String[lst.size()];
		for (int i=0; i< lst.size();i++){
			ret[i]=(String)lst.get(i);
		}
		
		return ret;
	}
	
	
	
	
	
}

