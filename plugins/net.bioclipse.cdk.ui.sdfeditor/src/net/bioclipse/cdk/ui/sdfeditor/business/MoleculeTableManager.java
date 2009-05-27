/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import org.apache.log4j.Logger;

import net.bioclipse.managers.business.IBioclipseManager;


public class MoleculeTableManager implements IBioclipseManager {

    Logger logger = Logger.getLogger( MoleculeTableManager.class );

    public String getNamespace() {
        return "molTable";
    }

    public void dummy() {
        logger.info( "Dummy on molTable manager has been called" );
    }

}
