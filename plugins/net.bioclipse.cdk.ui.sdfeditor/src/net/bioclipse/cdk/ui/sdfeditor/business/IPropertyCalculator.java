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

import net.bioclipse.cdk.domain.ICDKMolecule;


/**
 * @author arvid
 *
 */
public interface IPropertyCalculator<T extends Object> {

    public T calculate(ICDKMolecule molecule);

    public String getPropertyName();

    public T parse( String value );

    public String toString( Object value);

}
