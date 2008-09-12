package net.bioclipse.qsar.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.qsar.DescriptorType;
import net.bioclipse.qsar.DescriptorimplType;
import net.bioclipse.qsar.DescriptorlistType;
import net.bioclipse.qsar.DocumentRoot;
import net.bioclipse.qsar.MoleculeResourceType;
import net.bioclipse.qsar.MoleculelistType;
import net.bioclipse.qsar.ParameterType;
import net.bioclipse.qsar.PreprocessingStepType;
import net.bioclipse.qsar.PreprocessingType;
import net.bioclipse.qsar.QsarFactory;
import net.bioclipse.qsar.QsarPackage;
import net.bioclipse.qsar.QsarType;
import net.bioclipse.qsar.ResponseType;
import net.bioclipse.qsar.ResponsesListType;
import net.bioclipse.qsar.util.QsarAdapterFactory;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
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
		cdk.setId("cdk");
		cdk.setNamespace("http://cdk.sourceforge.net");
		cdk.setVendor("Chemistry Development Kit");
		cdk.setName("Chemistry Development Kit");
		cdk.setVersion("1.1.0.20080808");
//		cdk.setType("jar");
//		cdk.setJar("cdk.jar");
		cmd=AddCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__DESCRIPTORIMPL, cdk);
		cCmd.append(cmd);

		DescriptorimplType dragon=QsarFactory.eINSTANCE.createDescriptorimplType();
		dragon.setId("dragon");
		dragon.setNamespace("http://dragon.com");
		dragon.setVendor("MOLMOLMOL");
		dragon.setName("Dragon descriptor");
		dragon.setVersion("5.14");
//		dragon.setType("application");
//		dragon.setPath("/opt/dragon/bin/dragon");
		cmd=AddCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__DESCRIPTORIMPL, dragon);
		cCmd.append(cmd);

		MoleculelistType mollist=QsarFactory.eINSTANCE.createMoleculelistType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__MOLECULELIST, mollist);
		cCmd.append(cmd);

		DescriptorlistType desclist=QsarFactory.eINSTANCE.createDescriptorlistType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__DESCRIPTORLIST, desclist);
		cCmd.append(cmd);

		PreprocessingType prelist=QsarFactory.eINSTANCE.createPreprocessingType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__PREPROCESSING, prelist);
		cCmd.append(cmd);
		
		ResponsesListType reslist=QsarFactory.eINSTANCE.createResponsesListType();
		cmd=SetCommand.create(editingDomain, qsar, QsarPackage.Literals.QSAR_TYPE__RESPONSELIST, reslist);
		cCmd.append(cmd);


		//Add molecules
		//======================
		MoleculeResourceType mol1=QsarFactory.eINSTANCE.createMoleculeResourceType();
		mol1.setId("molecule01");
		mol1.setName("polycarpol");
		mol1.setFile("polycarpol.mol");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE_RESOURCE, mol1);
		cCmd.append(cmd);

		MoleculeResourceType mol2=QsarFactory.eINSTANCE.createMoleculeResourceType();
		mol2.setId("molecule02");
		mol2.setName("smallCollection");
		mol1.setFile("smallCollection.sdf");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE_RESOURCE, mol2);
		cCmd.append(cmd);

		MoleculeResourceType mol3=QsarFactory.eINSTANCE.createMoleculeResourceType();
		mol3.setId("molecule03");
		mol3.setName("reserpine");
		mol3.setUrl("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=5770&disopt=SaveXML");
		cmd=AddCommand.create(editingDomain, mollist, QsarPackage.Literals.MOLECULELIST_TYPE__MOLECULE_RESOURCE, mol3);
		cCmd.append(cmd);


		//Add descriptors with parameters
		//======================
		//Desc1
		DescriptorType desc1=QsarFactory.eINSTANCE.createDescriptorType();
		desc1.setId("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#xlogP");
		desc1.setNamespace("http://www.blueobelisk.org");
		cmd=AddCommand.create(editingDomain, desclist, QsarPackage.Literals.DESCRIPTORLIST_TYPE__DESCRIPTOR, desc1);
		cCmd.append(cmd);
		//Desc1 params
		DescriptorimplType impl1=QsarFactory.eINSTANCE.createDescriptorimplType();
		impl1.setId(cdk.getId());
		cmd=SetCommand.create(editingDomain, desc1, QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL, impl1);
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
		desc2.setNamespace("http://www.blueobelisk.org");
		cmd=AddCommand.create(editingDomain, desclist, QsarPackage.Literals.DESCRIPTORLIST_TYPE__DESCRIPTOR, desc2);
		cCmd.append(cmd);
		//Desc2 params
		DescriptorimplType desc2impl=QsarFactory.eINSTANCE.createDescriptorimplType();
		desc2impl.setId("org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor");
//		desc2impl.setType("soap");
		desc2impl.setNamespace("http://rguha.ath.cx/~rguha/cicc/desc/descriptors/");
		cmd=SetCommand.create(editingDomain, desc2, QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL, desc2impl);
		cCmd.append(cmd);
		ParameterType param3=QsarFactory.eINSTANCE.createParameterType();
		param3.setKey("elementName");
		param3.setValue("C");
		cmd=AddCommand.create(editingDomain, desc2, QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER, param3);
		cCmd.append(cmd);

		//Desc2
		DescriptorType desc3=QsarFactory.eINSTANCE.createDescriptorType();
		desc3.setId("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#atomCount");
		desc3.setNamespace("http://www.blueobelisk.org");
		cmd=AddCommand.create(editingDomain, desclist, QsarPackage.Literals.DESCRIPTORLIST_TYPE__DESCRIPTOR, desc3);
		cCmd.append(cmd);
		//Desc2 params
		DescriptorimplType desc3impl=QsarFactory.eINSTANCE.createDescriptorimplType();
		desc3impl.setId(dragon.getId());
		cmd=SetCommand.create(editingDomain, desc3, QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL, desc3impl);
		cCmd.append(cmd);
		
		//Add preprocessing steps
		//=======================
		PreprocessingStepType calc3D=QsarFactory.eINSTANCE.createPreprocessingStepType();
		calc3D.setId("Smi23d");
		calc3D.setName("Generate 3D coordinates with smi23d");
		calc3D.setOrder("1");
		calc3D.setNamespace("http://www.chembiogrid.org/cheminfo/smi23d/");
		cmd=AddCommand.create(editingDomain, prelist, QsarPackage.Literals.PREPROCESSING_TYPE__PREPROCESSING_STEP, calc3D);
		cCmd.append(cmd);

		PreprocessingStepType att=QsarFactory.eINSTANCE.createPreprocessingStepType();
		att.setId("org.openscience.cdk.atomtype.sybyl");
		att.setName("Sybyl Atom Types");
		att.setOrder("2");
		att.setNamespace("http://cdk.sf.net");
		cmd=AddCommand.create(editingDomain, prelist, QsarPackage.Literals.PREPROCESSING_TYPE__PREPROCESSING_STEP, att);
		cCmd.append(cmd);

		//Add responses
		//=======================
		ResponseType response1=QsarFactory.eINSTANCE.createResponseType();
		response1.setMoleculeResource(mol1.getId());
		response1.setResourceIndex(0);
		response1.setValue((float)11.45);
		cmd=AddCommand.create(editingDomain, reslist, QsarPackage.Literals.RESPONSES_LIST_TYPE__RESPONSE, response1);
		cCmd.append(cmd);

		ResponseType response2=QsarFactory.eINSTANCE.createResponseType();
		response2.setMoleculeResource(mol1.getId());
		response2.setResourceIndex(1);
		response2.setValue((float)15.45);
		cmd=AddCommand.create(editingDomain, reslist, QsarPackage.Literals.RESPONSES_LIST_TYPE__RESPONSE, response2);
		cCmd.append(cmd);

		ResponseType response3=QsarFactory.eINSTANCE.createResponseType();
		response3.setMoleculeResource(mol2.getId());
		response3.setArrayValues("12.56,23.45,34.56");
		cmd=AddCommand.create(editingDomain, reslist, QsarPackage.Literals.RESPONSES_LIST_TYPE__RESPONSE, response3);
		cCmd.append(cmd);


		//Execute the CompoundCommand
		editingDomain.getCommandStack().execute(cCmd); 		

//		for (MoleculeResourceType mol : qsar.getMoleculelist().getMoleculeResource()){
//			System.out.println("mol: " + mol.getName());
//		}
		
		
		//Debug out
		serializeToSTDOUT(root);
		
	}

	private static void serializeToSTDOUT(DocumentRoot root) {
		ResourceSet resourceSet=new ResourceSetImpl();
		URI fileURI;
		try {
			fileURI = URI.createFileURI(new File("myQSAR2.xml").getAbsolutePath());
//			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xml", new EcoreResourceFactoryImpl());

			Resource resource=resourceSet.createResource(fileURI);
			resource.getContents().add(root);

			Map opts=new HashMap();
			opts.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
			
			//Save to file
			resource.save(opts);

			//Serialize to byte[] and print to sysout
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			resource.save(os, opts);
			System.out.println(new String(os.toByteArray()));

			System.out.println("end");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
