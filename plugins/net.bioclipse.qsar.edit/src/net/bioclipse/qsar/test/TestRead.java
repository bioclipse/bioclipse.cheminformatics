package net.bioclipse.qsar.test;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import net.bioclipse.qsar.DescriptorType;
import net.bioclipse.qsar.DescriptorimplType;
import net.bioclipse.qsar.DescriptorlistType;
import net.bioclipse.qsar.DocumentRoot;
import net.bioclipse.qsar.MoleculeResourceType;
import net.bioclipse.qsar.MoleculelistType;
import net.bioclipse.qsar.QsarPackage;
import net.bioclipse.qsar.QsarType;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
public class TestRead {
        public static void main(String[] args) {
                   // Create a resource set.
                   ResourceSet resourceSet = new ResourceSetImpl();
                  // Register the default resource factory -- only needed for stand-alone!
                  resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                    Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
                  // Register the package -- only needed for stand-alone!
                  QsarPackage qsarPackage=QsarPackage.eINSTANCE;
                   // Get the URI of the model file.
                   URI fileURI = URI.createFileURI(new File("myQSAR2.xml").getAbsolutePath());
                   // Demand load the resource for this file.
                   Resource resource = resourceSet.getResource(fileURI, true);
                   DocumentRoot root=(DocumentRoot) resource.getContents().get(0);
                   QsarType qsar=root.getQsar();
                   // Print the contents of the resource to System.out.
                   try
                   {
                     resource.save(System.out, Collections.EMPTY_MAP);
                   }
                   catch (IOException e) {}
                   //Molecules
                   //=========
                   if (qsar.getMoleculelist()==null){
                           System.out.println("No moleculesList.");
                           return;
                   }
                   MoleculelistType mollist = qsar.getMoleculelist();
                   if (mollist.getMoleculeResource()==null || mollist.getMoleculeResource().size()<=0){
                           System.out.println("No molecules in MoleculesList.");
                           return;
                   }
                   for (MoleculeResourceType mol : mollist.getMoleculeResource()){
                           System.out.println(" ++ Mol: " + mol.getId() + " [" + mol.getFile()+"]");
                   }
                   //Descriptors
                   //===========
                   if (qsar.getDescriptorlist()==null){
                           System.out.println("No descriptorList.");
                           return;
                   }
                   DescriptorlistType desclist = qsar.getDescriptorlist();
                   if (desclist.getDescriptor()==null || desclist.getDescriptor().size()<=0){
                           System.out.println("No descriptors in descriptorList.");
                           return;
                   }
                   for (DescriptorType desc : desclist.getDescriptor()){
                           DescriptorimplType impl = desc.getDescriptorimpl();
                           System.out.println(" ** desc: " + desc.getId() + " [" + impl.getId() + "]");
                   }
        }
}
