package org.openscience.cdk.renderer.selection;

import java.awt.Color;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * 
 * @author maclean
 * @cdk.module render
 */
public class LogicalSelection implements ISelection {
    
    public enum Type { ALL, NONE };
    
    private Type type;
    
    private IChemModel chemModel;
    
    public LogicalSelection(LogicalSelection.Type type) {
        this.type = type;
    }

    public void clear() {
        this.type = Type.NONE;
        this.chemModel = null;
    }

    public IRenderingElement generate(Color color) {
        return null;
    }

    public IAtomContainer getConnectedAtomContainer() {
        if (this.chemModel != null) {
            IAtomContainer ac = this.chemModel.getBuilder().newAtomContainer();
            for (IAtomContainer other : 
                ChemModelManipulator.getAllAtomContainers(chemModel)) {
                ac.add(other);
            }
            return ac;
        }
        return null;
    }

    public boolean isFilled() {
        return this.chemModel != null;
    }

    public boolean isFinished() {
        return true;
    }

    public void select(IChemModel chemModel) {
        if (this.type == Type.ALL) { 
            this.chemModel = chemModel;
        }
    }
    
    public void select(IAtomContainer atomContainer) {
        this.chemModel = atomContainer.getBuilder().newChemModel();
        IMoleculeSet molSet = atomContainer.getBuilder().newMoleculeSet();
        molSet.addAtomContainer(atomContainer);
        this.chemModel.setMoleculeSet(molSet);
    }

}
