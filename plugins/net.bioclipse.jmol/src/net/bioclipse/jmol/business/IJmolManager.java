/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.jmol.business;

import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.IBioclipseManager;

public interface IJmolManager extends IBioclipseManager{

	/**
	 * Execute a script in Jmol. If editor active, run script there. 
	 * In the future, if JmolView active, there too.
	 * @param script The script command to run
	 */
	@PublishedMethod(methodSummary="Execute a script in Jmol."
		,params="String to execute as Jmol script")
	@Recorded
	void run(String script);

	
	/**
	 * Load jmoo with a file
	 * @param path Path to file, relative workspace
	 */
	@PublishedMethod(methodSummary="Load jmoo with a file"
		, params="Path to file, relative workspace")
	@Recorded
	void load(String path);

}
