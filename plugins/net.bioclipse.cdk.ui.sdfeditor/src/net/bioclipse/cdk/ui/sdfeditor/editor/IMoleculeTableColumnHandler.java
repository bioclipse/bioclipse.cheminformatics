package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.Collection;
import java.util.List;


public interface IMoleculeTableColumnHandler {

    public List<Object> getProperties();

    public void setVisibleProperties( List<Object> visibleProperties );

    public Collection<Object> getAvailableProperties();

}
