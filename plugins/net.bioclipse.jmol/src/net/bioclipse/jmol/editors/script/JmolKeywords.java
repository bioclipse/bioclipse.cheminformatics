/*******************************************************************************
 * Copyright (c) 2006 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/

package net.bioclipse.jmol.editors.script;

/**
 * Keywords for Jmol.
 * 
 * @author ola
 */
public class JmolKeywords {
	
	public static final String[] jmolKeywords = { 
		"Backbone",
		"Background",
		"Cartoon",
		"Centre",
		"Clipboard",
		"Color",
		"Colour",
		"Connect",
		"CPK",
		"Dots",
		"Define",
		"Echo",
		"Exit",
		"HBonds",
		"Help",
		"Label",
		"Load",
		"Monitor",
		"Pause",
		"Print",
		"Quit",
		"Renumber",
		"Refresh",
		"Reset",
		"Restrict",
		"Ribbons",
		"Rotate",
		"Save",
		"Script",
		"Select",
		"Set",
		"Show",
		"Slab",
		"Source",
		"Spacefill",
		"SSBonds",
		"Stereo",
		"Strands",
		"Structure",
		"Trace",
		"Translate",
		"Wireframe",
		"Write",
		"Zap",
		"Zoom"
	};
	
	public static final String[] jmolColors = { 
		"cpk",
		"amino",
		"chain",
		"group",
		"shapely",
		"structure",
		"temperature",
		"charge"
	};

	public static final String[] jmolSets = { 
	"AT",
	"Acidic",
	"Acyclic",
	"Aliphatic",
	"Alpha",
	"Amino",
	"Aromatic",
	"Backbone",
	"Basic",
	"Bonded",
	"Buried",
	"CG",
	"Charged",
	"Cyclic",
	"Cystine",
	"Helix",
	"Hetero",
	"Hydrogen",
	"Hydrophobic",
	"Ions",
	"Large",
	"Ligand",
	"Medium",
	"Neutral",
	"Nucleic",
	"Polar",
	"Protein",
	"Purine",
	"Pyrimidine",
	"Selected",
	"Sheet",
	"Sidechain",
	"Small",
	"Solvent",
	"Surface",
	"Turn",
	"Water" 	
	};

	//Concatenate all String[] above into one array
	public static String[] getAllKeywords(){

		int newSize=jmolSets.length + jmolColors.length + jmolKeywords.length;
		String[] allKeywords=new String[newSize];

		int globalcnt = 0;

		int cnt = 0;
		while(cnt<jmolSets.length){
			allKeywords[globalcnt]=jmolSets[cnt];
			cnt++;
			globalcnt++;
		}

		cnt = 0;
		while(cnt<jmolColors.length){
			allKeywords[globalcnt]=jmolColors[cnt];
			cnt++;
			globalcnt++;
		}

		cnt = 0;
		while(cnt<jmolKeywords.length){
			allKeywords[globalcnt]=jmolKeywords[cnt];
			cnt++;
			globalcnt++;
		}
		
		return allKeywords;
	}

}
