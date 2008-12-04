package org.openscience.cdk.renderer;

import java.awt.Color;
import java.awt.Font;

/**
 * @cdk.module render
 */
public class RenderingParameters {

    private int atomRadius = 8;

    private Color backColor = Color.white;

    private double bondDistance = 6.0;

    private double bondWidth = 2.0;

    /** Determines whether atoms are colored by type. */
    private boolean colorAtomsByType = true;
    
    private boolean compact = false;
    
    private Font customFont = null;
    
    private Color externalHighlightColor = Color.orange;
    
    private Color foreColor = Color.black;

    private Color hoverOverColor = Color.lightGray;

    /**
     * Determines whether structures should be drawn as Kekule structures, thus
     * giving each carbon element explicitly, instead of not displaying the
     * element symbol. Example C-C-C instead of /\.
     */
    private boolean kekuleStructure = false;
    
    private double highlightRadiusModel = 0.7;

    private Color mappingColor = Color.gray;

    /**
     * Area on each of the four margins to keep white.
     */
    private double margin = 0.05;

    private Color selectedPartColor = Color.lightGray;
    
    public enum SelectionShape { OVAL, SQUARE };
    
    /**
     * The shape to display over selected atoms
     */
    private SelectionShape selectionShape = SelectionShape.SQUARE;

    /**
     * Determines whether rings should be drawn with a circle if they are
     * aromatic.
     */
    private boolean showAromaticity = false;

    private boolean showAromaticityInCDKStyle = false;

    private boolean showAtomAtomMapping = true;

    private boolean showAtomTypeNames = false;

    /**
     * Determines whether methyl carbons' symbols should be drawn explicit for
     * methyl carbons. Example C/\C instead of /\.
     */
    private boolean showEndCarbons = false;

    /** Determines whether explicit hydrogens should be drawn. */
    private boolean showExplicitHydrogens = true;

    /** Determines whether implicit hydrogens should be drawn. */
    private boolean showImplicitHydrogens = true;

    private boolean showMoleculeTitle = false;

    private boolean showReactionBoxes = true;

    private boolean showTooltip = false;

    private boolean useAntiAliasing = true;

    private boolean willDrawNumbers = false;
    
    public SelectionShape getSelectionShape() {
        return this.selectionShape;
    }
    
    public void setSelectionShape(SelectionShape selectionShape) {
        this.selectionShape = selectionShape;
    }

	public Font getCustomFont() {
        return customFont;
    }

    public void setCustomFont(Font customFont) {
        this.customFont = customFont;
    }

    public double getHighlightRadiusModel() {
        return highlightRadiusModel;
    }

    public void setHighlightRadiusModel(double highlightRadiusModel) {
        this.highlightRadiusModel = highlightRadiusModel;
    }

    public int getAtomRadius() {
        return atomRadius;
    }

    public Color getBackColor() {
        return backColor;
    }

    public double getBondDistance() {
        return bondDistance;
    }

    public double getBondWidth() {
        return bondWidth;
    }

    public Color getExternalHighlightColor() {
        return externalHighlightColor;
    }

    public Color getForeColor() {
        return foreColor;
    }

    public Color getHoverOverColor() {
        return hoverOverColor;
    }

    public Color getMappingColor() {
        return mappingColor;
    }

    public double getMargin() {
        return margin;
    }

    public Color getSelectedPartColor() {
        return selectedPartColor;
    }

    public boolean isColorAtomsByType() {
        return colorAtomsByType;
    }

    public boolean isCompact() {
        return compact;
    }

    public boolean isKekuleStructure() {
        return kekuleStructure;
    }

    public boolean isShowAromaticity() {
        return showAromaticity;
    }

    public boolean isShowAromaticityInCDKStyle() {
        return showAromaticityInCDKStyle;
    }

    public boolean isShowAtomAtomMapping() {
        return showAtomAtomMapping;
    }

    public boolean isShowAtomTypeNames() {
        return showAtomTypeNames;
    }

    public boolean isShowEndCarbons() {
        return showEndCarbons;
    }

    public boolean isShowExplicitHydrogens() {
        return showExplicitHydrogens;
    }

    public boolean isShowImplicitHydrogens() {
        return showImplicitHydrogens;
    }

    public boolean isShowMoleculeTitle() {
        return showMoleculeTitle;
    }

    public boolean isShowReactionBoxes() {
        return showReactionBoxes;
    }

    public boolean isShowTooltip() {
        return showTooltip;
    }

    public boolean isUseAntiAliasing() {
        return useAntiAliasing;
    }

    public boolean isWillDrawNumbers() {
        return willDrawNumbers;
    }

    public void setAtomRadius(int atomRadius) {
        this.atomRadius = atomRadius;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public void setBondDistance(double bondDistance) {
        this.bondDistance = bondDistance;
    }

    public void setBondWidth(double bondWidth) {
        this.bondWidth = bondWidth;
    }

    public void setColorAtomsByType(boolean colorAtomsByType) {
        this.colorAtomsByType = colorAtomsByType;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public void setExternalHighlightColor(Color externalHighlightColor) {
        this.externalHighlightColor = externalHighlightColor;
    }

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
    }

    public void setHoverOverColor(Color hoverOverColor) {
        this.hoverOverColor = hoverOverColor;
    }

    public void setKekuleStructure(boolean kekuleStructure) {
        this.kekuleStructure = kekuleStructure;
    }

    public void setMappingColor(Color mappingColor) {
        this.mappingColor = mappingColor;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public void setSelectedPartColor(Color selectedPartColor) {
        this.selectedPartColor = selectedPartColor;
    }

    public void setShowAromaticity(boolean showAromaticity) {
        this.showAromaticity = showAromaticity;
    }

    public void setShowAromaticityInCDKStyle(boolean showAromaticityInCDKStyle) {
        this.showAromaticityInCDKStyle = showAromaticityInCDKStyle;
    }

    public void setShowAtomAtomMapping(boolean showAtomAtomMapping) {
        this.showAtomAtomMapping = showAtomAtomMapping;
    }

    public void setShowAtomTypeNames(boolean showAtomTypeNames) {
        this.showAtomTypeNames = showAtomTypeNames;
    }

    public void setShowEndCarbons(boolean showEndCarbons) {
        this.showEndCarbons = showEndCarbons;
    }

    public void setShowExplicitHydrogens(boolean showExplicitHydrogens) {
        this.showExplicitHydrogens = showExplicitHydrogens;
    }

    public void setShowImplicitHydrogens(boolean showImplicitHydrogens) {
        this.showImplicitHydrogens = showImplicitHydrogens;
    }

    public void setShowMoleculeTitle(boolean showMoleculeTitle) {
        this.showMoleculeTitle = showMoleculeTitle;
    }

    public void setShowReactionBoxes(boolean showReactionBoxes) {
        this.showReactionBoxes = showReactionBoxes;
    }

    public void setShowTooltip(boolean showTooltip) {
        this.showTooltip = showTooltip;
    }

    public void setUseAntiAliasing(boolean useAntiAliasing) {
        this.useAntiAliasing = useAntiAliasing;
    }

    public void setWillDrawNumbers(boolean willDrawNumbers) {
        this.willDrawNumbers = willDrawNumbers;
    }

}
