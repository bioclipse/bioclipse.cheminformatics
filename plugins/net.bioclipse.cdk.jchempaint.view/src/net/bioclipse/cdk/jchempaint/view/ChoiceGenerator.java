/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     TODO: Someone's name - what e did
 *     TODO: mail@mail.edu Date
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.generators.IGenerator;

public class ChoiceGenerator implements IGenerator {

    boolean use = false;
    IGenerator generator;

    public ChoiceGenerator(IGenerator generator) {
        this.generator = generator;
    }

    public void setUse(boolean use) {
        this.use = use;
    }
    public IRenderingElement generate( IAtomContainer ac,
                                       RendererModel model ) {
        if(generator == null) return EMPTY_ELEMENT;

        if(use)
            return generator.generate( ac, model );
        else
            return EMPTY_ELEMENT;
    }

    public static final String EP_GENERATOR = "net.bioclipse.cdk.ui.sdf.generator";

    public static ChoiceGenerator getGeneratorsFromExtensionPoint() {

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint generatorExtensionPoint = registry
        .getExtensionPoint(EP_GENERATOR);

        IExtension[] generatorExtensions
                            = generatorExtensionPoint.getExtensions();

        for(IExtension extension : generatorExtensions) {

            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {
                try {
                    final IGenerator generator = (IGenerator) element.createExecutableExtension("class");
                    return new ChoiceGenerator(generator);
                } catch (CoreException e) {
                    LogUtils.debugTrace( Logger.getLogger( ChoiceGenerator.class) ,e);
                }
            }
        }
        return new ChoiceGenerator(null);
    }

    public static IRenderingElement EMPTY_ELEMENT = new IRenderingElement() {

        public void accept( IRenderingVisitor v ) {

        }

    };
}