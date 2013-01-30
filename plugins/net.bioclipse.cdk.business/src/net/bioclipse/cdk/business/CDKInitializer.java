/* Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.cdk.business;

import java.io.IOException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.periodictable.PeriodicTable;

/**
 * Initializes some CDK code, to make the CDK manager more responsive later
 * on. It loads data for the two common {@link IChemObjectBuilder}s.
 *
 * @author egonw
 */
public class CDKInitializer implements Runnable {

	public void run() {
		// set up IsotopeFactories
		try {
			IsotopeFactory.getInstance(
				SilentChemObjectBuilder.getInstance()
			);
		} catch (IOException e) {
			// do not care about the exception right now
		}
		try {
			IsotopeFactory.getInstance(
				DefaultChemObjectBuilder.getInstance()
			);
		} catch (IOException e) {
			// do not care about the exception right now
		}

		// Element factory
		PeriodicTable.getElementCount();

		// Load the CDK atom type lists
		AtomTypeFactory.getInstance(
			"org/openscience/cdk/dict/data/cdk-atom-types.owl",
			SilentChemObjectBuilder.getInstance()
		);
		AtomTypeFactory.getInstance(
			"org/openscience/cdk/dict/data/cdk-atom-types.owl",
			DefaultChemObjectBuilder.getInstance()
		);
	}

}
