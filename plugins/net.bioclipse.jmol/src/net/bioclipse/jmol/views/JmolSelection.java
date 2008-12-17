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
public abstract class JmolSelection implements IStructuredSelection{
    Set<String> selectionSet;
    public Object getFirstElement() {
        return selectionSet.toArray()[0];
    }
    @SuppressWarnings("unchecked")
        public Iterator iterator() {
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
}
