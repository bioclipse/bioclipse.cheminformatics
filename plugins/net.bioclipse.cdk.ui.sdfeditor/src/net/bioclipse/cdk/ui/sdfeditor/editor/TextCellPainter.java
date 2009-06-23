/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;


import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.model.INatTableModel;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.renderer.ICellRenderer;
import net.sourceforge.nattable.typeconfig.style.DisplayModeEnum;
import net.sourceforge.nattable.typeconfig.style.IStyleConfig;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Author : Andy Tsoi<br>
 * Created Date : 2007¦~9¤ë23¤é<br>
 */
public class TextCellPainter implements ICellPainter {

  int style = SWT.LEFT;

  public TextCellPainter() {

  }

  public TextCellPainter(int style) {
    this.style = style;
  }

  /**
   * @return the style
   */
  public int getStyle() {
    return style;
  }

  /**
   * @param style
   *            the style to set
   */
  public void setStyle(int style) {
    this.style = style;
  }

  public void drawCell(GC gc, Rectangle rectangle, NatTable natTable, ICellRenderer natCellRenderer, int row,
      int col, boolean selected) {
    Color orgFG = gc.getForeground();
    Color orgBG = gc.getBackground();
    Font orgFont = gc.getFont();

    // Selection Color
    IStyleConfig normalStyleConfig = natCellRenderer.getStyleConfig(DisplayModeEnum.NORMAL.toString(), row, col);
    IStyleConfig selectionStyleConfig = natCellRenderer.getStyleConfig(DisplayModeEnum.SELECT.toString(), row, col);
    
    Color fg = selected ? selectionStyleConfig.getForegroundColor(row, col)
        : normalStyleConfig.getForegroundColor(row, col);
    Color bg = selected ? selectionStyleConfig.getBackgroundColor(row, col)
        : normalStyleConfig.getBackgroundColor(row, col);
    Font font = normalStyleConfig.getFont(row, col);
    
    Object o = natCellRenderer.getValue( row, col );
    if(o instanceof IColorProvider) {
        Color c=((IColorProvider)o).getForeground( o );
        fg = c!=null?c:fg;
        c= ((IColorProvider)o).getBackground( o );
        bg = c!=null?c:bg;
    }
    String text = natCellRenderer.getDisplayText(row, col);
    text = text == null ? "" : text;

    Image icon = getImage(natCellRenderer, row, col);

    gc.setFont(font);
    gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
    gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);

    INatTableModel tableModel = natTable.getNatTableModel();
    // Allow display grid
    if (tableModel.isGridLineEnabled()) {
      rectangle.x = rectangle.x + 1;
      rectangle.width = rectangle.width - 1;
      rectangle.y = rectangle.y + 1;
      rectangle.height = rectangle.height - 1;
    }

    drawBackground(gc, rectangle);

    // Draw Single Cell Selection but will not called for column header
    if (selected && tableModel.isSingleCellSelection()) {
      drawSingleCellSelection(gc, natTable, natCellRenderer, row, col, rectangle, fg, bg);
    }

    int imageWidth = icon != null ? icon.getBounds().width : 0;
    // Support Multiple Line
    int multiple = ((gc.getFontMetrics().getHeight() * GUIHelper.getNumberOfNewLine(text) + SPACE));

    int topAlign = rectangle.y + rectangle.height / 2
        - ((multiple > rectangle.height ? rectangle.height / 2 : multiple / 2));

    if (imageWidth > 0) {
      gc.drawImage(icon, rectangle.x + SPACE, topAlign);
      imageWidth = imageWidth + SPACE;
    }

    // Draw Text

    drawText(gc, rectangle, text, imageWidth, topAlign);

    gc.setForeground(orgFG);
    gc.setBackground(orgBG);
    gc.setFont(orgFont);

  }

  protected Image getImage(ICellRenderer cellRenderer, int row, int col) {
    Image icon = cellRenderer.getStyleConfig(DisplayModeEnum.NORMAL.name(), row, col).getImage(row, col);
    return icon;
  }

  protected void drawBackground(GC gc, Rectangle rectangle) {
    gc.fillRectangle(rectangle);
  }

  protected void drawSingleCellSelection(GC gc, NatTable natTable, ICellRenderer natCellRenderer, int row,
      int col, Rectangle rectangle, Color fg, Color bg) {
    Color origFg = gc.getForeground();
    Color origBg = gc.getBackground();
    
    INatTableModel tableModel = natTable.getNatTableModel();

    Color selectedfg = natCellRenderer.getStyleConfig(DisplayModeEnum.SELECT.toString(), row, col).getForegroundColor(row, col);
    Color selectedbg = natCellRenderer.getStyleConfig(DisplayModeEnum.SELECT.toString(), row, col).getBackgroundColor(row, col);
    gc.setForeground(selectedfg != null ? selectedfg : GUIHelper.COLOR_LIST_FOREGROUND);
    gc.setBackground(selectedbg != null ? selectedbg : GUIHelper.COLOR_LIST_BACKGROUND);

    Rectangle cellRect = natTable.getModelBodyCellBound(row, col);
    if (tableModel.isGridLineEnabled()) {
      cellRect.x = cellRect.x + 1;
      cellRect.y = cellRect.y + 1;
      cellRect.width = cellRect.width - 1;
      cellRect.height = cellRect.height - 1;
    }
    drawBackground(gc, cellRect);
    
    gc.setForeground(GUIHelper.COLOR_BLACK);
    gc.drawRectangle(rectangle.x, rectangle.y, rectangle.width - 1, rectangle.height - 1);

    gc.setForeground(origFg);
    gc.setBackground(origBg);
  }

  protected void drawText(GC gc, Rectangle rectangle, String text, int imageWidth, int topAlign) {
    gc.setClipping(rectangle);

    String originalText = text;
    text = GUIHelper.getAvailableTextToDisplay(gc, rectangle, text);

    if ((style & SWT.LEFT) == SWT.LEFT) {
      gc.drawText(text, imageWidth + rectangle.x + SPACE, topAlign, true);
    } else if ((style & SWT.RIGHT) == SWT.RIGHT) {
      Point point = gc.textExtent(text);
      gc.drawText(text, rectangle.x + (rectangle.width - point.x - SPACE), topAlign, true);
    } else if ((style & SWT.CENTER) == SWT.CENTER) {
      Point point = gc.textExtent(originalText);
      int rightAlign = rectangle.x + rectangle.width / 2 - point.x / 2;

      if (rectangle.width < (imageWidth + point.x + SPACE)) {
        gc.drawText(text, imageWidth + rectangle.x + SPACE, topAlign, true);
      } else {
        gc.drawText(text, rightAlign < (rectangle.x + imageWidth) ? (rectangle.x + imageWidth) : rightAlign,
            topAlign, true);
      }
    }
    
    gc.setClipping((Rectangle) null);
  }
}
