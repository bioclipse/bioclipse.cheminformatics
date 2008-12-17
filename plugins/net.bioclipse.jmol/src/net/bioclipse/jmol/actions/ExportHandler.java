package net.bioclipse.jmol.actions;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.bioclipse.jmol.views.JmolPanel;
import net.bioclipse.jmol.editors.JmolEditor;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
public class ExportHandler extends AbstractHandler implements IHandler {
    private static final Logger logger = Logger.getLogger(ExportHandler.class);
        public Object execute(ExecutionEvent event) throws ExecutionException {
                IEditorPart editor = HandlerUtil.getActiveEditor(event);
                if (!(editor instanceof JmolEditor)) {
                        logger.error("A jmol command was run but jmol is not the active editor");
                        return null;
                }
                JmolEditor jmolEditor = (JmolEditor) editor;
                JmolPanel panel = jmolEditor.getJmolPanel();
                Image image = new BufferedImage(
                                panel.getWidth(),
                                panel.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                Graphics g = image.getGraphics();
                panel.paint(g);
                FileDialog dialog = new FileDialog(HandlerUtil.getActiveShell(event), SWT.SAVE);
                dialog.setFilterNames (new String [] {"PNG Files", "All Files (*.*)"});
                dialog.setFilterExtensions (new String [] {"*.png", "*.*"});
                String result = dialog.open();
                if (result != null) {
                        try {
                                ImageIO.write((RenderedImage) image, "PNG", new File(result));
                        } catch (IOException ioe) {
                                logger.error("Problem with the path " + result);
                        }
                }
                return null;
        }
}
