package net.bioclipse.jmol.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Abstract class for selections in Jmol to be propagated to workbench
 * @author ola
 *
 */
public abstract class JmolSelection implements IStructuredSelection,
                                               Iterable<String> {

    Set<String> selectionSet;
    
    private boolean updateJmolSelection;
	
    public JmolSelection() {
        updateJmolSelection = false;
    }
    
    public JmolSelection(boolean updateJmolSelection) {
        this.updateJmolSelection = updateJmolSelection;
    }

    public Object getFirstElement() {
        return selectionSet.toArray()[0];
    }

	  public Iterator<String> iterator() {
        return selectionSet.iterator();
    }

    public int size() {
        return selectionSet.size();
    }

    public Object[] toArray() {
        return selectionSet.toArray();
    }

    @SuppressWarnings("unchecked")
	public List toList() {
        List lst=new ArrayList<String>();
        lst.addAll(selectionSet);
        return lst;
    }

    public boolean isEmpty() {
        if (selectionSet==null) return true;
        if (selectionSet.size()<=0) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return selectionSet.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {

        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JmolSelection other = (JmolSelection) obj;
        if ( selectionSet == null ) {
            if ( other.selectionSet != null )
                return false;
        }
        else if ( !( selectionSet.equals( other.selectionSet ) ) )
            return false;
        return true;
    }

    public boolean updateJmolSelection() {

        return updateJmolSelection;
    }
}
