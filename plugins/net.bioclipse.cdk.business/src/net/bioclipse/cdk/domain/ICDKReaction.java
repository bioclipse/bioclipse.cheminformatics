/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn, Miguel Rojas
 *     
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import org.openscience.cdk.interfaces.IReaction;


/**
 * An intertace for CDKReaction
 *
 */
public interface ICDKReaction extends net.bioclipse.core.domain.IReaction{

    public IReaction getReaction();
}
