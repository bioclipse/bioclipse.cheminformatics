/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.exceptions;

import net.bioclipse.core.business.BioclipseException;


/**
 * Bioclipse Exception signalling a time out exception thrown by CDK.
 * 
 * @author jonalv
 *
 */
public class CDKTimedOutException extends BioclipseException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     * @param cause
     */
    public CDKTimedOutException(String message, Throwable cause) {
        super( message, cause );
    }

}
