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
import java.util.List;

import net.bioclipse.core.domain.IMolecule;
import uk.ac.manchester.libchebi.Comment;
import uk.ac.manchester.libchebi.DatabaseAccession;
import uk.ac.manchester.libchebi.Name;
import uk.ac.manchester.libchebi.Reference;
import uk.ac.manchester.libchebi.Relation;

public interface IChEBIMolecule extends IMolecule {
	
	public String getId();

	public String getParentId() throws IOException, ParseException;

	public String getName() throws IOException, ParseException;

	public List<Name> getNames() throws IOException, ParseException;

	public String getInchi() throws IOException, ParseException;

	public String getInchiKey() throws IOException, ParseException;

	public String getDefinition() throws IOException, ParseException;

	public List<Comment> getComments() throws IOException, ParseException;

	public short getStar() throws IOException, ParseException;

	public int getCharge() throws IOException, ParseException;

	public List<DatabaseAccession> getDatabaseAccessions() throws IOException, ParseException;
	
	public List<Reference> getReferences() throws IOException, ParseException;

	public List<Relation> getOutgoings() throws IOException, ParseException;

	public List<Relation> getIncomings() throws IOException, ParseException;
}
