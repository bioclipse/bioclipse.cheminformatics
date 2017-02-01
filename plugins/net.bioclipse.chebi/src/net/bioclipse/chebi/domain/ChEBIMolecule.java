/* Copyright (c) 2016  Egon Willighagen <egonw@user.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.chebi.domain;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IMolecule;
import uk.ac.manchester.libchebi.ChebiEntity;
import uk.ac.manchester.libchebi.ChebiException;
import uk.ac.manchester.libchebi.Comment;
import uk.ac.manchester.libchebi.DatabaseAccession;
import uk.ac.manchester.libchebi.Name;
import uk.ac.manchester.libchebi.Reference;
import uk.ac.manchester.libchebi.Relation;

public class ChEBIMolecule extends BioObject implements IChEBIMolecule {

	private ChebiEntity entity;
	private ICDKMolecule cdkMol;

	/*
     * Needed by Spring
     */
	ChEBIMolecule() {
        super();
    }

	public ChEBIMolecule(String identifier) throws IOException, ParseException, ChebiException {
		this.entity = new ChebiEntity(identifier);
	}
	
	@Override
	public List<IMolecule> getConformers() {
		return Collections.emptyList();
	}

	@Override
	public String toSMILES() throws BioclipseException {
		if (this.entity == null) return "";
		try {
			return this.entity.getSmiles();
		} catch (IOException | ParseException e) {
			throw new BioclipseException(
				"Cannot generate SMILES for ChEBI ID " + this.entity.getId() + ": " + e.getMessage(), e
			);
		}
	}

	@Override
	public String toCML() throws BioclipseException {
		return asCDKMolecule().toCML();
	}

	@Override
	public IResource getResource() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDKMolecule asCDKMolecule() throws BioclipseException {
		if (cdkMol == null) {
			cdkMol = Activator.getDefault().getJavaCDKManager().fromSMILES(this.toSMILES());
		}
		return cdkMol;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ICDKMolecule.class){
			try {
				return asCDKMolecule();
			} catch (BioclipseException e) {
				// could not create a CDK molecule
			}
        }
		if (adapter == IChEBIMolecule.class){
			return this;
		}

		return super.getAdapter(adapter);
	}

	@Override
	public String getId() {
		if (this.entity == null) return "";
		return this.entity.getId();
	}

	@Override
	public String getParentId() throws IOException, ParseException {
		if (this.entity == null) return "";
		return "CHEBI:" + this.entity.getParentId();
	}

	@Override
	public String getName() throws IOException, ParseException {
		return this.entity.getName();
	}

	@Override
	public List<Name> getNames() throws IOException, ParseException {
		return this.entity.getNames();
	}

	@Override
	public String getDefinition() throws IOException, ParseException {
		return this.entity.getDefinition();
	}

	@Override
	public List<Comment> getComments() throws IOException, ParseException {
		return this.entity.getComments();
	}

	@Override
	public short getStar() throws IOException, ParseException {
		return this.entity.getStar();
	}

	@Override
	public int getCharge() throws IOException, ParseException {
		return this.entity.getCharge();
	}

	@Override
	public List<DatabaseAccession> getDatabaseAccessions() throws IOException, ParseException {
		return this.entity.getDatabaseAccessions();
	}

	@Override
	public List<Reference> getReferences() throws IOException, ParseException {
		return this.entity.getReferences();
	}

	@Override
	public List<Relation> getOutgoings() throws IOException, ParseException {
		return this.entity.getOutgoings();
	}

	@Override
	public List<Relation> getIncomings() throws IOException, ParseException {
		return this.entity.getIncomings();
	}
	
}
