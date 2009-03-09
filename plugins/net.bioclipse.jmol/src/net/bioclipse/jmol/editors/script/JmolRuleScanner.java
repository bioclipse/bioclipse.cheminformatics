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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * RuleScanner for the JmolEditor.
 * 
 * @author ola
 *
 */
public class JmolRuleScanner extends RuleBasedScanner {
	protected static Color DEFAULT_COLOR= new Color(Display.getCurrent(), new RGB(0, 0, 0));
	protected static Color COMMENT_COLOR= new Color(Display.getCurrent(), new RGB(0, 200, 0));

	protected static Color KEYWORD_COLOR= new Color(Display.getCurrent(), new RGB(100, 0, 100));
	protected static Color COLOR_COLOR= new Color(Display.getCurrent(), new RGB(0, 150, 150));
	protected static Color SET_COLOR= new Color(Display.getCurrent(), new RGB(200, 0, 0));
//	private static Color STRING_COLOR= new Color(Display.getCurrent(), new RGB(0, 0, 200));

	public JmolRuleScanner() {

		IToken commentToken= new Token(new TextAttribute(COMMENT_COLOR));
		IToken defaultToken = new Token(new TextAttribute(DEFAULT_COLOR));

		IToken keyToken = new Token(new TextAttribute(KEYWORD_COLOR));
		IToken colorToken= new Token(new TextAttribute(COLOR_COLOR));
		IToken setToken= new Token(new TextAttribute(SET_COLOR, null, SWT.ITALIC));

		IToken tagToken= new Token(new TextAttribute(SET_COLOR, null, SWT.BOLD));

		WordRule keywordRule = new WordRule(new JmolWordDetector(),defaultToken);

		//Add all keywords to rule
		for (int i = 0; i < JmolKeywords.jmolKeywords.length; i++) {
			keywordRule.addWord(JmolKeywords.jmolKeywords[i].toUpperCase(),keyToken);
			keywordRule.addWord(JmolKeywords.jmolKeywords[i].toLowerCase(),keyToken);
		}

		//Add all colors to rule
		for (int i = 0; i < JmolKeywords.jmolColors.length; i++) {
			keywordRule.addWord(JmolKeywords.jmolColors[i].toUpperCase(),colorToken);
			keywordRule.addWord(JmolKeywords.jmolColors[i].toLowerCase(),colorToken);
		}

		//Add all sets to rule
		for (int i = 0; i < JmolKeywords.jmolSets.length; i++) {
			keywordRule.addWord(JmolKeywords.jmolSets[i].toUpperCase(),setToken);
			keywordRule.addWord(JmolKeywords.jmolSets[i].toLowerCase(),setToken);
		}

		IRule[] rules = new IRule[4];
		rules[0] = (new EndOfLineRule("#", commentToken));
		rules[1] = keywordRule;
		rules[2] = new WhitespaceRule(new JmolWhitespaceDetector());
		rules[3] = new TagRule(tagToken);

		
		
		setRules(rules);
	}
}
