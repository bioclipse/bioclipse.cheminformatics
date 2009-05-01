package net.bioclipse.cdk.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class DotViewerFilter extends ViewerFilter {

    @Override
    public boolean select( Viewer viewer, Object parentElement, Object element ) {
        if(((IResource)element).getName().charAt( 0 )=='.')
            return false;
        else
            return true;
    }

}
