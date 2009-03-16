package net.bioclipse.cdk.smartsmatching.views;

import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class SmartsContextProvider implements IContextProvider{

    public IContext getContext( Object target ) {

        System.out.println("context requested for: " + target);
        if ( target instanceof Tree ) {
            Tree tree = (Tree) target;
            TreeItem[] items = tree.getSelection();
            for (int i=0; i<items.length;i++){
                System.out.println("Item "+ i + ": " + items[i].getData());
            }
        }
        System.out.println("--");
        return null;
    }

    public int getContextChangeMask() {

        return SELECTION;
    }

    public String getSearchExpression( Object target ) {

        // TODO Auto-generated method stub
        return null;
    }

}
