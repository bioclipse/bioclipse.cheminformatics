/*******************************************************************************
 * Copyright (c) 2008  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: Bioclipse Project <http://www.bioclipse.net>
 ******************************************************************************/
package net.bioclipse.cdk.debug.test;
import net.bioclipse.cdkdebug.business.CDKDebugManager;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.coverage.AbstractCoverageTest;
/**
 * JUnit tests for checking if the tested Manager is properly tested.
 * 
 * @author egonw
 */
public class CoverageTest extends AbstractCoverageTest {
    private static CDKDebugManager manager = new CDKDebugManager();
    @Override
    public IBioclipseManager getManager() {
        return manager;
    }
}