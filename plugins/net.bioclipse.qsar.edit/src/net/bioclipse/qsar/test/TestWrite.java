package net.bioclipse.qsar.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.bioclipse.qsar.DescriptorType;
import net.bioclipse.qsar.DescriptorimplType;
import net.bioclipse.qsar.DescriptorlistType;
import net.bioclipse.qsar.DocumentRoot;
import net.bioclipse.qsar.MoleculeType;
import net.bioclipse.qsar.MoleculelistType;
import net.bioclipse.qsar.ParameterType;
import net.bioclipse.qsar.QsarFactory;
import net.bioclipse.qsar.QsarPackage;
import net.bioclipse.qsar.QsarType;
import net.bioclipse.qsar.util.QsarAdapterFactory;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

public class TestWrite {
	public static void main(String[] args) {

		System.out.println("start");

		QsarAdapterFactory factory=new QsarAdapterFactory();
		EditingDomain editingDomain=new AdapterFactoryEditingDomain(factory, new BasicCommandStack());

		//Create the super types
		DocumentRoot root=QsarFactory.eINSTANCE.createDocumentRoot();
		
		//Collect commands here
		CompoundCommand cCmd = new CompoundCommand();
		Command cmd;

		//Create editor type to hold everything else
		QsarType qsar=QsarFactory.eINSTANCE.createQsarType();

//		//could not get SetCommand to work for root element, add directly 
		root.setQsar(qsar);

		//Add QSAR root elements
		//======================
		DescriptorimplType cdk=QsarFactory.eINSTANCE.createDescriptorimplType();
		cdk.setId("net.sf.cdk");
		cdk.setName("Chemistry Development Kit");
		cdk.setNamespace("http://cdk.sourceforge.net");
		cdk.setJar("cdk20080808.jar");
		cmd=AddCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__DESCRIPTORIMPL, cdk);
		cCmd.append(cmd);

		MoleculelistType mollist=QsarFactory.eINSTANCE.createMoleculelistType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__MOLECULELIST, mollist);
		cCmd.append(cmd);

		DescriptorlistType desclist=QsarFactory.eINSTANCE.createDescriptorlistType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__DESCRIPTORLIST, desclist);
		cCmd.append(cmd);

		//Add molecules
		//======================
		MoleculeType mol1=QsarFactory.eINSTANCE.createMoleculeType();
		mol1.setId("molecule01");
		mol1.setName("polycarpol");
		mol1.setPath("/path/to/polycarpol.cml");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE, mol1);
		cCmd.append(cmd);

		MoleculeType mol2=QsarFactory.eINSTANCE.createMoleculeType();
		mol2.setId("molecule02");
		mol2.setName("furosemide");
		mol2.setSmiles("NS(=O)(=O)c1cc(C(O)=O)c(NCc2ccco2)cc1Cl");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE, mol2);
		cCmd.append(cmd);

		MoleculeType mol3=QsarFactory.eINSTANCE.createMoleculeType();
		mol3.setId("molecule03");
		mol3.setName("reserpine");
		mol3.setUrl("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=5770&disopt=SaveXML");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE, mol3);
		cCmd.append(cmd);

		//Add descriptors with parameters
		//======================
		//Desc1
		DescriptorType desc1=QsarFactory.eINSTANCE.createDescriptorType();
		desc1.setId("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#xlogP");
		desc1.setName("XlogP");
		desc1.setNamespace("http://www.blueobelisk.org");
		cmd=AddCommand.create(editingDomain, desclist, QsarPackage.Literals.DESCRIPTORLIST_TYPE__DESCRIPTOR, desc1);
		cCmd.append(cmd);
		//Desc1 params
		DescriptorimplType desc1impl=QsarFactory.eINSTANCE.createDescriptorimplType();
		desc1impl.setId(cdk.getId());
		cmd=SetCommand.create(editingDomain, desc1, QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL, desc1impl);
		cCmd.append(cmd);
		ParameterType param1=QsarFactory.eINSTANCE.createParameterType();
		param1.setKey("checkAromaticity");
		param1.setValue("true");
		cmd=AddCommand.create(editingDomain, desc1, QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER, param1);
		cCmd.append(cmd);
		ParameterType param2=QsarFactory.eINSTANCE.createParameterType();
		param2.setKey("salicylFlag");
		param2.setValue("false");
		cmd=AddCommand.create(editingDomain, desc1, QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER, param2);
		cCmd.append(cmd);

		//Desc2
		DescriptorType desc2=QsarFactory.eINSTANCE.createDescriptorType();
		desc2.setId("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#atomCount");
		desc2.setName("Atom Count");
		desc2.setNamespace("http://www.blueobelisk.org");
		cmd=AddCommand.create(editingDomain, desclist, QsarPackage.Literals.DESCRIPTORLIST_TYPE__DESCRIPTOR, desc2);
		cCmd.append(cmd);
		//Desc2 params
		DescriptorimplType desc2impl=QsarFactory.eINSTANCE.createDescriptorimplType();
		desc2impl.setId("http://rguha.ath.cx/~rguha/cicc/desc/descriptors/org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor");
		desc2impl.setUrl("http://rguha.ath.cx/~rguha/cicc/desc/descriptors/org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor");
		cmd=SetCommand.create(editingDomain, desc2, QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL, desc2impl);
		cCmd.append(cmd);
		ParameterType param3=QsarFactory.eINSTANCE.createParameterType();
		param3.setKey("elementName");
		param3.setValue("C");
		cmd=AddCommand.create(editingDomain, desc2, QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER, param3);
		cCmd.append(cmd);

		//Execute the CompoundCommand
		editingDomain.getCommandStack().execute(cCmd); 		

		//Debug out
		serializeToSTDOUT(root);
		
	}

	private static void serializeToSTDOUT(DocumentRoot root) {
		ResourceSet resourceSet=new ResourceSetImpl();
		URI fileURI;
		try {
			fileURI = URI.createFileURI(new File("myQSAR.xml").getAbsolutePath());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());

			Resource resource=resourceSet.createResource(fileURI);
			resource.getContents().add(root);
			resource.save(Collections.EMPTY_MAP);

			//Serialize to byte[] and print to sysout
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			resource.save(os, Collections.EMPTY_MAP);

			System.out.println(new String(os.toByteArray()));

			System.out.println("end");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
