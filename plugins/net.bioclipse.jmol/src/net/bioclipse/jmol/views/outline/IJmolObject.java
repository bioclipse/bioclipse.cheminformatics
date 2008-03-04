/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A base interface for objects in the JmolContentOutline
 * @author ola
 */
public interface IJmolObject extends IAdaptable{


	public String getName();

	public void setName(String name);

	public Object getObject();

	public void setObject(Object object);

	public List<IJmolObject> getChildren();

	public void setChildren(List<IJmolObject> children);

	public IJmolObject getParent();

	public void setParent(IJmolObject parent);

	public String getSelectString();

}
