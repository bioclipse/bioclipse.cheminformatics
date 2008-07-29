/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cml.managers;

import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.xmlcml.cml.base.CMLElement;

public interface IValidateCMLManager extends IBioclipseManager{


	    /**
	     * example method
	     */
	    @Recorded
	    public String validate(IFile input);
	    
	    /*
	     * After a validation, this tells if the validation was successfull
	     */
	    public boolean getSuceeded();
	    
	    /*
	     * After a validation, this contains the parsed file
	     */
	    public CMLElement getCMLElement();
}
