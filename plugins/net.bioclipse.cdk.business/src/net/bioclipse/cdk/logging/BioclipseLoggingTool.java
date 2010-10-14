/* Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at                 |
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.cdk.logging;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.preferences.PreferenceConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.openscience.cdk.tools.ILoggingTool;

public class BioclipseLoggingTool implements ILoggingTool {

	private final Logger logger = Logger.getLogger(BioclipseLoggingTool.class);
	private String className;
	
	public static boolean useBioclipseLogging 
	    = new InstanceScope().getNode( PreferenceConstants.NODEQUALIFIER )
                  .getBoolean( PreferenceConstants.BIOCLIPSE_LOGGING, 
                               false );
	
    /**
     * Constructs a LoggingTool which produces log lines without any special
     * indication which class the message originates from.
     */
    public BioclipseLoggingTool(Class<?> clazz) {
		this.className = clazz.getName();
    }

    public static ILoggingTool create(Class<?> sourceClass) {
        return new BioclipseLoggingTool(sourceClass);
    }

	public void debug(Object object) {
		if (!isDebugEnabled()) return;
		logger.debug(className + ": " + object);
	}

	public void debug(Object object, Object... objects) {
		if (!isDebugEnabled()) return;
		StringBuilder result = new StringBuilder();
		result.append(className).append(": ");
		result.append(object.toString());
		for (Object obj : objects) {
			result.append(obj.toString());
		}
		logger.debug(result.toString());
	}

	public void dumpClasspath() {}

	public void dumpSystemProperties() {}

	public void error(Object object) {
		if (!isDebugEnabled()) return;
		logger.error(className + ": " + object);
	}

	public void error(Object object, Object... objects) {
		if (!isDebugEnabled()) return;
		StringBuilder result = new StringBuilder();
		result.append(className).append(": ");
		result.append(object.toString());
		for (Object obj : objects) {
			result.append(obj.toString());
		}
		logger.error(result.toString());
	}

	public void fatal(Object object) {
		if (!isDebugEnabled()) return;
		logger.fatal(className + ": " + object.toString());
	}

	public void info(Object object) {
		if (!isDebugEnabled()) return;
		logger.info(className + ": " + object);
	}

	public void info(Object object, Object... objects) {
		if (!isDebugEnabled()) return;
		StringBuilder result = new StringBuilder();
		result.append(className).append(": ");
		result.append(object.toString());
		for (Object obj : objects) {
			result.append(obj.toString());
		}
		logger.info(result.toString());
	}

	public boolean isDebugEnabled() {
		return useBioclipseLogging;
	}

	public void setStackLength(int length) {
		// ignored
	}

	public void warn(Object object) {
		if (!isDebugEnabled()) return;
		logger.warn(className + ": " + object);
	}

	public void warn(Object object, Object... objects) {
		if (!isDebugEnabled()) return;
		StringBuilder result = new StringBuilder();
		result.append(className).append(": ");
		result.append(object.toString());
		for (Object obj : objects) {
			result.append(obj.toString());
		}
		logger.warn(result.toString());
	}

}
