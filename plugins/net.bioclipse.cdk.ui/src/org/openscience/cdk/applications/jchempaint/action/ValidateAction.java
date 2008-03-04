/*
 *  $RCSfile$
 *  $Author: shk3 $
 *  $Date: 2007-05-21 22:40:02 +0200 (Mon, 21 May 2007) $
 *  $Revision: 3111 $
 *
 *  Copyright (C) 2003-2005  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sf.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.applications.jchempaint.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;

import net.bioclipse.cdk.ui.editors.JCPMultiPageEditor;

import org.openscience.cdk.applications.jchempaint.DrawingPanel;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.applications.jchempaint.dialogs.ValidateFrame;
import org.openscience.cdk.dict.DictionaryDatabase;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.validate.BasicValidator;
import org.openscience.cdk.validate.CDKValidator;
import org.openscience.cdk.validate.DictionaryValidator;
import org.openscience.cdk.validate.PDBValidator;
import org.openscience.cdk.validate.ProblemMarker;
import org.openscience.cdk.validate.ValencyValidator;
import org.openscience.cdk.validate.ValidatorEngine;


/**
 * An action opening a validation frame
 * 
 * @cdk.module jchempaint
 * @author     E.L. Willighagen <elw38@cam.ac.uk>
 */
public class ValidateAction extends JCPAction
{

	ValidateFrame frame = null;
	private JChemPaintModel jcpmodel;
	private static DictionaryDatabase dictdb = null;
	private static ValidatorEngine engine = null;


	public void run() {
		run(null);
	}
	/**
	 *  Description of the Method
	 *
	 *@param  event  Description of the Parameter
	 */
	public void run(ActionEvent event)
	{
		logger.debug("detected validate action: " + type);
		jcpmodel = ((JCPMultiPageEditor)this.getContributor().getActiveEditorPart()).getJcpModel();
		if (type.equals("run"))
		{
//			IChemObject object = getSource(event);
//			jcpmodel = ((JCPMultiPageEditor)this.getContributor().getActiveEditorPart()).getJcpModel();
			IChemObject object = null;
			if (event == null) {
				object = jcpmodel.getRendererModel().getSelectedPart();
			}
			else {
				object = getSource(event);
			}
			if (object == null)
			{
				// called from main menu
				IChemModel model = jcpmodel.getChemModel();
				if (model != null)
				{
					runValidate(model);
				} else
				{
					logger.debug("Empty model");
				}
			} else
			{
				// calleb from popup menu
				logger.debug("Validate called from popup menu!");
				runValidate(object);
			}
		} else if (type.equals("clear"))
		{
			clearValidate();
		} else if (type.startsWith("toggle") && type.length() > 6)
		{
			String toggle = type.substring(6);
			try
			{
				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
				boolean newChecked = !menuItem.isSelected();
				menuItem.setSelected(newChecked);
				if (toggle.equals("Basic"))
				{
					if (newChecked)
					{
						logger.info("Turned on " + toggle);
						getValidatorEngine().addValidator(new BasicValidator());
					} else
					{
						logger.info("Turned off " + toggle);
						getValidatorEngine().removeValidator(new BasicValidator());
					}
				} else if (toggle.equals("CDK"))
				{
					if (newChecked)
					{
						logger.info("Turned on " + toggle);
						getValidatorEngine().addValidator(new CDKValidator());
					} else
					{
						logger.info("Turned off " + toggle);
						getValidatorEngine().removeValidator(new CDKValidator());
					}
				} else
				{
					logger.error("Don't know what to toggle: " + toggle);
				}
			} catch (ClassCastException exception)
			{
				logger.error("Cannot toggle a non JCheckBoxMenuItem!");
			}
		} else
		{
			logger.error("Unknown command: " + type);
		}
	}

	public static ValidatorEngine getValidatorEngine()
	{
		if (engine == null)
		{
			engine = new ValidatorEngine();
			// default validators
			engine.addValidator(new BasicValidator());
			engine.addValidator(new ValencyValidator());
			engine.addValidator(new CDKValidator());
			engine.addValidator(new DictionaryValidator(dictdb));
			engine.addValidator(new PDBValidator());
		}
		return engine;
	}
	

	/**
	 *  Description of the Method
	 */
	private void clearValidate()
	{
		IChemModel model = jcpmodel.getChemModel();
		IAtomContainer ac = model.getMoleculeSet().getAtomContainer(0);
		Iterator atomsI = ac.atoms();
		logger.info("Clearing errors on atoms: " + ac.getAtomCount());
		while(atomsI.hasNext()){
			ProblemMarker.unmark((IAtom)atomsI.next());
		}
		jcpmodel.fireChange();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  object  Description of the Parameter
	 */
	private void runValidate(IChemObject object)
	{
		logger.info("Running validation");
		clearValidate();
		DrawingPanel drawingPanel = ((JCPMultiPageEditor)this.getContributor().getActiveEditorPart()).getDrawingPanel();
		if (this.jcpmodel != null)
		{
			frame = new ValidateFrame(this);
			frame.validate(object);
			frame.pack();
			frame.show();
		}
	}

	public JChemPaintModel getJcpmodel() {
		return jcpmodel;
	}

}

