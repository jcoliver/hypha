/* Mesquite source code.  Copyright 1997-2013 W. Maddison and D. Maddison.  Module by J.C. Oliver. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.hypha.GridForNode;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.hypha.NumForNodeWithThreshold.NumForNodeWithThreshold;

/**Tree display assistant for drawing a series of grid cells on a branch.  Was originally
 * a subclass of TreeDisplayAssistantAO. */

public class GridForNode extends TreeDisplayAssistantA implements LegendHolder{
	private Vector<NodeGridOperator> grids;  //
	MesquiteSubmenuSpec aboveTMenuItem, belowTMenuItem, inAMenuItem, lowCMenuItem, highCMenuItem;
	private NumForNodeWithThreshold[][] numForNodeTask;
	private int numCols = MesquiteInteger.unassigned;
	private int numRows = MesquiteInteger.unassigned;
	private int numCells = MesquiteInteger.unassigned;
	static int maxCells = 9;
	static int defaultHeight = 10;
	static int defaultWidth = 20;
	private int cellHeight = defaultHeight;
	private int cellWidth = defaultWidth;
	MesquiteInteger pos = new MesquiteInteger(0); //For doCommand navigation
	public Color inAppColor = Color.gray;
	public Color belowThreshColor = Color.white;
	public Color aboveThreshColor = Color.black;
	public Color lowConflictColor = ColorDistribution.lightBlue;
	public Color highConflictColor = Color.red;
	
	MesquiteBoolean displayCellColor, displayCellValue, includeMissing, drawOutline, annotateNodes;
	MesquiteMenuItemSpec displayCellColorItem, displayCellValueItem, includeMissingItem, drawOutlineItem, formatForPDFItem, annotateNodesMenuItem;//suppressRedrawItem, sigFigsItem;
	MesquiteString atColorName, btColorName, inAColorName, lcColorName, hcColorName;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	Font currentFont = null;
	String myFont = null;
	int myFontSize = -1;
	MesquiteString fontSizeName, fontName;
	MesquiteBoolean formatForPDF;
//	MesquiteBoolean suppressRedraw;
	
	public String getName() {
		return "Grids for nodes";
	}
	/**Establishes grid size (number of rows & colums), value to be used for coloring grid cells (NumberForNodes)
	 * for each grid cell, and the threshold to be used when deciding what color to fill in for each cell.*/
	public boolean startJob(String arguments, Object condition,	boolean hiredByName) {
		grids = new Vector<NodeGridOperator>();
		makeMenu("Grids");
		addMenuItem("Set Cell Drawing Size...", makeCommand("setCellSize", this));
		aboveTMenuItem = addSubmenu(null, "Above Threshold Cell Color", makeCommand("setAboveT", this));
		aboveTMenuItem.setList(ColorDistribution.standardColorNames);
		atColorName = new MesquiteString(ColorDistribution.getStandardColorName(aboveThreshColor));
		if(atColorName!=null){
			aboveTMenuItem.setSelected(atColorName);
		}
		belowTMenuItem = addSubmenu(null, "Below Threshold Cell Color", makeCommand("setBelowT", this));
		belowTMenuItem.setList(ColorDistribution.standardColorNames);
		btColorName = new MesquiteString(ColorDistribution.getStandardColorName(belowThreshColor));
		if(btColorName!=null){
			belowTMenuItem.setSelected(btColorName);
		}
		inAMenuItem = addSubmenu(null, "Missing or Inapplicable Cell Color", makeCommand("setInApp", this));
		inAMenuItem.setList(ColorDistribution.standardColorNames);
		inAColorName = new MesquiteString(ColorDistribution.getStandardColorName(inAppColor));
		if(inAppColor!=null){
			inAMenuItem.setSelected(inAColorName);
		}
		lowCMenuItem = addSubmenu(null, "Low Conflict Cell Color", makeCommand("setLowCon", this));
		lowCMenuItem.setList(ColorDistribution.standardColorNames);
		lcColorName = new MesquiteString(ColorDistribution.getStandardColorName(lowConflictColor));
		if(lowConflictColor!=null){
			lowCMenuItem.setSelected(lcColorName);
		}
		highCMenuItem = addSubmenu(null, "High Conflict Cell Color", makeCommand("setHighCon", this));
		highCMenuItem.setList(ColorDistribution.standardColorNames);
		hcColorName = new MesquiteString(ColorDistribution.getStandardColorName(highConflictColor));
		if(highConflictColor!=null){
			highCMenuItem.setSelected(hcColorName);
		}
		MesquiteSubmenuSpec cellDisplayMenu = addSubmenu(null, "Cell Display");
		displayCellColor = new MesquiteBoolean(true);
		displayCellColorItem = addCheckMenuItemToSubmenu(null, cellDisplayMenu, "Display Cell Color", makeCommand("toggleDisplayCellColor", this), displayCellColor);
		displayCellColorItem.setEnabled(true);
		displayCellValue = new MesquiteBoolean(false);
		displayCellValueItem = addCheckMenuItemToSubmenu(null, cellDisplayMenu, "Display Cell Value", makeCommand("toggleDisplayCellValue", this), displayCellValue);
		displayCellValueItem.setEnabled(true);

		includeMissing = new MesquiteBoolean(false);
		includeMissingItem = addCheckMenuItem(null, "Include Missing Values", makeCommand("toggleIncludeMissing", this), includeMissing);
		includeMissingItem.setEnabled(true);

		drawOutline = new MesquiteBoolean(true);
		drawOutlineItem = addCheckMenuItem(null, "Draw External Grid Border", makeCommand("toggleDrawOutline", this), drawOutline);
		drawOutlineItem.setEnabled(true);

		formatForPDF = new MesquiteBoolean(true);
		formatForPDFItem = addCheckMenuItem(null, "Format Grids for PDF Printing", makeCommand("toggleFormatPDF", this), formatForPDF);
		formatForPDFItem.setEnabled(true);

		annotateNodes = new MesquiteBoolean(false);
		annotateNodesMenuItem = addCheckMenuItem(null, "Annotate Nodes with Grid Values", makeCommand("toggleAnnotateNodes", this), annotateNodes);
		annotateNodesMenuItem.setEnabled(true);

//		suppressRedraw = new MesquiteBoolean(false);
//		suppressRedrawItem = addCheckMenuItem(null, "Suppress Redrawing", makeCommand("toggleSuppressRedraw", this), suppressRedraw);
//		suppressRedrawItem.setEnabled(true);
		
		currentFont = MesquiteWindow.defaultFont;
		fontName = new MesquiteString(MesquiteWindow.defaultFont.getName());
		fontSizeName = new MesquiteString(Integer.toString(MesquiteWindow.defaultFont.getSize()));
		
		MesquiteSubmenuSpec msf = FontUtil.getFontSubmenuSpec(this,this);
		msf.setDocumentItems(false);
		
		
		msf.setSelected(fontName);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Font Size", makeCommand("setFontSize", this), MesquiteSubmenu.getFontSizeList());
		mss.setList(MesquiteSubmenu.getFontSizeList());
		mss.setDocumentItems(false);
		mss.setSelected(fontSizeName);
		
		if(!MesquiteThread.isScripting()){ //Conditional to prevent querying user on opening of file
			if(!MesquiteInteger.isCombinable(numCols) || !MesquiteInteger.isCombinable(numRows)){
				MesquiteInteger newCols = new MesquiteInteger(1);
				MesquiteInteger newRows = new MesquiteInteger(1);
				String helpString = "Enter the number of columns and rows to use for the node grid coordinator.  This module will draw a grid on a branch, with cells shaded to reflect some property of the tree.";
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Grid Coordinator Setup", "Number of rows", "Number of columns", answer, newRows, newCols, 1, 3, 1, 3, helpString);
				if(newCols.isCombinable() && newRows.isCombinable()){
					numCols = newCols.getValue();
					numRows = newRows.getValue();
					numCells = numCols * numRows;
				}
				else return sorry(getName() + " couldn't start because the number of columns or rows entered was not within the acceptable range.");
			}
			if(MesquiteInteger.isCombinable(numCols) && MesquiteInteger.isCombinable(numRows)){
				numForNodeTask = new NumForNodeWithThreshold[numRows][numCols];
				for(int iR = 0; iR < numRows; iR++){
					for(int iC = 0; iC < numCols; iC++){
						numForNodeTask[iR][iC] = (NumForNodeWithThreshold)hireNamedEmployee(NumberForNode.class, "#NumForNodeWithThreshold");
						if(numForNodeTask[iR][iC]==null)
							return sorry(getName() + " couldn't find a suitable grid cell element for cell " + (iR+1) + ", " + (iC +1) + ".");
						else numForNodeTask[iR][iC].setUseMenubar(false);
					}
				}
			}		
		}
		addMenuItem("Close Grids", makeCommand("close",  this));
		resetContainingMenuBar();
		return true;
	}
	/*..................................................................*/
	/**Performs commands based on the String commandName it is passed.  Mostly used for restoring
	 * objects/numbers upon opening a file which employs this module.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if(checker.compareStart(this.getClass(), "Sets module used to calculate value for node", "[name of module]", commandName, "setNumForNode")){
			String disposable = ParseUtil.getFirstToken(commandName, pos); //A string used only to move the parser position.
			int tempRow = MesquiteInteger.fromString((ParseUtil.getToken(commandName, pos)));
			int tempCol = MesquiteInteger.fromString((ParseUtil.getToken(commandName, pos)));
			NumForNodeWithThreshold temp = (NumForNodeWithThreshold)replaceEmployee(NumForNodeWithThreshold.class, arguments, "Number for Node with threshold", numForNodeTask[tempRow][tempCol]);
			if(temp!=null){
				numForNodeTask[tempRow][tempCol] = temp;
				numForNodeTask[tempRow][tempCol].setUseMenubar(false);
				return numForNodeTask[tempRow][tempCol];
			}
		}

		else if(checker.compare(this.getClass(), "Sets the drawing size of cells", "[number] [number]", commandName, "setCellSize")){
			MesquiteInteger tempHeight = new MesquiteInteger(MesquiteInteger.fromFirstToken(arguments, pos));
			MesquiteInteger tempWidth = new MesquiteInteger(MesquiteInteger.fromString(arguments, pos));
			if(!tempHeight.isCombinable() || ! tempWidth.isCombinable()){
				MesquiteBoolean answer = new MesquiteBoolean(false);
				String helpString = "Enter the cell height and width for the node grid coordinator.  Cells can be 4 to 20 pixels wide and 8 to 40 pixels high.";
				if(MesquiteInteger.isCombinable(cellHeight))
					tempHeight.setValue(cellHeight);
				else tempHeight.setValue(defaultHeight);
				if(MesquiteInteger.isCombinable(cellWidth))
					tempWidth.setValue(cellWidth);
				else tempWidth.setValue(defaultWidth);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Cell size setup for Grid Coordinator", "Cell height", "Cell width", answer, tempHeight, tempWidth, 4, 20, 8, 40, helpString);
			}
			if(tempHeight.isCombinable() && tempWidth.isCombinable()){
				cellHeight = tempHeight.getValue();
				cellWidth = tempWidth.getValue();
				
			}
			else{
				if(tempHeight.isCombinable())
					cellHeight = tempHeight.getValue();
				else cellHeight = defaultHeight;
				if(tempWidth.isCombinable())
					cellWidth = tempWidth.getValue();
				else cellWidth = defaultWidth;
			}
			redraw();
		}

		else if(checker.compare(this.getClass(), "Grid dimensions (Rows, Columns)", "[number] [number]", commandName, "setGridDimensions")){
			MesquiteInteger tempRows = new MesquiteInteger(MesquiteInteger.fromFirstToken(arguments, pos));
			MesquiteInteger tempCols = new MesquiteInteger(MesquiteInteger.fromString(arguments, pos));
			if(!tempRows.isCombinable() || !tempCols.isCombinable()){
				MesquiteBoolean answer = new MesquiteBoolean(false);
				String helpString = "Enter the number of columns and rows to use for the node grid coordinator.  This module will draw a grid on a branch, with cells shaded to reflect some property of the tree.";
				tempRows.setValue(1);
				tempCols.setValue(1);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Grid Coordinator Setup", "Number of rows", "Number of columns", answer, tempRows, tempCols, 1, 3, 1, 3, helpString);
				}
			if(tempRows.isCombinable() && tempCols.isCombinable()){
				numRows = tempRows.getValue();
				numCols = tempCols.getValue();
				numCells = numCols * numRows;
			}
			else{
				numRows = 1;
				numCols = 1;
				numCells = numRows * numCols;
			}
			numForNodeTask = new NumForNodeWithThreshold[numRows][numCols];
		}

		else if (checker.compare(this.getClass(), "Set color of cells for values equal to or greater than threshold.", "[name of color]", commandName, "setAboveT")) {
			Color atc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
			if (atc == null)
				return null;
			aboveThreshColor = atc;
			atColorName.setValue(atc.toString());
			MesquiteString aName = new MesquiteString(ColorDistribution.getStandardColorName(aboveThreshColor));
			if(aName!=null)
				aboveTMenuItem.setSelected(aName);
				resetContainingMenuBar();
				redraw();
		}

		else if (checker.compare(this.getClass(), "Set color of cells for values less than threshold.", "[name of color]", commandName, "setBelowT")) {
	 		Color btc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (btc == null)
	 			return null;
	 		belowThreshColor = btc;
			btColorName.setValue(btc.toString());
			MesquiteString bName = new MesquiteString(ColorDistribution.getStandardColorName(belowThreshColor));
			if(bName!=null)
				belowTMenuItem.setSelected(bName);
				resetContainingMenuBar();
				redraw();
	 	}
		
	 	else if (checker.compare(this.getClass(), "Set color of cells for missing or inapplicable values.", "[name of color]", commandName, "setInApp")) {
	 		Color iac = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (iac == null)
	 			return null;
	 		inAppColor = iac;
	 		inAColorName.setValue(iac.toString());
			MesquiteString inAName = new MesquiteString(ColorDistribution.getStandardColorName(inAppColor));
			if(inAName!=null)
				inAMenuItem.setSelected(inAName);
				resetContainingMenuBar();
				redraw();
	 	}
	 	else if (checker.compare(this.getClass(), "Set color of cells for low conflict values.", "[name of color]", commandName, "setLowCon")) {
	 		Color lcc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (lcc == null)
	 			return null;
	 		lowConflictColor = lcc;
	 		lcColorName.setValue(lcc.toString());
			MesquiteString lcName = new MesquiteString(ColorDistribution.getStandardColorName(lowConflictColor));
			if(lcName!=null)
				lowCMenuItem.setSelected(lcName);
				resetContainingMenuBar();
				redraw();
	 	}
	 	else if (checker.compare(this.getClass(), "Set color of cells for high conflict values.", "[name of color]", commandName, "setHighCon")) {
	 		Color hcc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (hcc == null)
	 			return null;
	 		highConflictColor = hcc;
	 		hcColorName.setValue(hcc.toString());
			MesquiteString hcName = new MesquiteString(ColorDistribution.getStandardColorName(highConflictColor));
			if(hcName!=null)
				highCMenuItem.setSelected(hcName);
				resetContainingMenuBar();
				redraw();
	 	}
	 	else if (checker.compare(this.getClass(), "Toggles whether cells are colored according to value.", "[on or off]", commandName, "toggleDisplayCellColor")){
	 		boolean current = displayCellColor.getValue();
	 		displayCellColor.toggleValue(parser.getFirstToken(arguments));
	 		if(current!=displayCellColor.getValue()){
	 			parametersChanged();
	 			redraw();
	 		}
	 	}

	 	else if (checker.compare(this.getClass(), "Toggles whether cell displays corrsponding value.", "[on or off]", commandName, "toggleDisplayCellValue")){
	 		boolean current = displayCellValue.getValue();
	 		displayCellValue.toggleValue(parser.getFirstToken(arguments));
	 		if(current!=displayCellValue.getValue()){
	 			if(displayCellValue.getValue()){
	 				includeMissingItem.setEnabled(true);
//	 				sigFigsItem.setEnabled(true);
	 			}
	 			else {
	 				includeMissingItem.setEnabled(false);
//	 				sigFigsItem.setEnabled(false);
	 			}
	 			parametersChanged();
	 			resetContainingMenuBar();
	 			redraw();
	 		}
	 	}
	 	else if (checker.compare(this.getClass(), "Toggles whether cells with missing values display '-' as value.", "[on or off]", commandName, "toggleIncludeMissing")){
	 		boolean current = includeMissing.getValue();
	 		includeMissing.toggleValue(parser.getFirstToken(arguments));
	 		if(current!=includeMissing.getValue() && displayCellValue != null){
	 			if(displayCellValue.getValue()){
	 				parametersChanged();
	 				redraw();
	 			}
	 		}
	 	}
	 	else if (checker.compare(this.getClass(), "Toggles whether to display external grid outline.", "[on or off]", commandName, "toggleDrawOutline")){
	 		boolean current = drawOutline.getValue();
	 		drawOutline.toggleValue(parser.getFirstToken(arguments));
	 		if(current!=drawOutline.getValue()){//TODO: may need conditional to make sure either values or colors (or both) are being displayed?
	 			parametersChanged();
	 			redraw();
	 		}
	 	}
//	 	else if (checker.compare(this.getClass(), "Toggles whether to suppress re-drawing of grids.", "[on or off]", commandName, "toggleSuppressRedraw")){
//	 		suppressRedraw.toggleValue(parser.getFirstToken(arguments));
//	 	}
	 	else if (checker.compare(this.getClass(), "Toggles whether to adjust drawing for PDF printing.", "[on or off]", commandName, "toggleFormatPDF")){
	 		formatForPDF.toggleValue();
	 		redraw();
	 	}

	 	else if (checker.compare(this.getClass(), "Toggles whether to add node grid values as node annotations to tree", "[on or off]", commandName, "toggleAnnotateNodes")) {
	 		annotateNodes.toggleValue(parser.getFirstToken(arguments));
	 		if(annotateNodes.getValue()){
 				redraw();
 		 		// After the annotation happens, need to notify listeners that the actual tree has changed
 		 		Enumeration<NodeGridOperator> e = grids.elements();
 		 		while(e.hasMoreElements()){
 		 			Object obj = e.nextElement();
 		 			if(obj instanceof NodeGridOperator){
 		 				NodeGridOperator nGO = (NodeGridOperator)obj;
 		 				((Associable)nGO.getTree()).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
 		 			}
 		 		}
	 		}
	 	}

	 	else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				Enumeration<NodeGridOperator> e = grids.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof NodeGridOperator) {
						NodeGridOperator nGO = (NodeGridOperator)obj;
						if (nGO.legend!=null)
							nGO.legend.setOffsetX(offset);
					}
				}
			}
		}

	 	else if (checker.compare(this.getClass(), "Sets initial vertical offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
				Enumeration<NodeGridOperator> e = grids.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof NodeGridOperator) {
						NodeGridOperator nGO = (NodeGridOperator)obj;
						if (nGO.legend!=null)
							nGO.legend.setOffsetY(offset);
					}
				}
			}
		}

		else if (checker.compare(this.getClass(), "Sets the font used for the node grid values", "[name of font]", commandName, "setFont")) {
			String t = ParseUtil.getFirstToken(arguments, pos);
			if (currentFont==null){
				myFont = t;
				fontName.setValue(t);
			}
			else {
				Font fontToSet = new Font (t, currentFont.getStyle(), currentFont.getSize());
				if (fontToSet!= null) {
					myFont = t;
					fontName.setValue(t);
					currentFont = fontToSet;
				}
			}
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the font used for the node grid values", "[name of font]", commandName, FontUtil.setFontOther)) {
			String t=FontUtil.getFontNameFromDialog(containerOfModule());
			if (t!=null) {
				if (currentFont==null){
					myFont = t;
					fontName.setValue(t);
				}
				else {
					Font fontToSet = new Font (t, currentFont.getStyle(), currentFont.getSize());
					if (fontToSet!= null) {
						myFont = t;
						fontName.setValue(t);
						currentFont = fontToSet;
					}
				}
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the font size used for the node grid values", "[size of font]", commandName, "setFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments);
			if (currentFont==null){
				if (!MesquiteThread.isScripting() && !MesquiteInteger.isPositive(fontSize))
					fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font Size", "Font Size", 12);
				if (MesquiteInteger.isPositive(fontSize)) {
					myFontSize = fontSize;
					fontSizeName.setValue(Integer.toString(fontSize));
				}
			}
			else {
				if (!MesquiteThread.isScripting() && !MesquiteInteger.isPositive(fontSize))
					fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font Size", "Font Size", currentFont.getSize());
				if (MesquiteInteger.isPositive(fontSize)) {
					myFontSize = fontSize;
					Font fontToSet = new Font (currentFont.getName(), currentFont.getStyle(), fontSize);
					if (fontToSet!= null) {
						currentFont = fontToSet;
						fontSizeName.setValue(Integer.toString(fontSize));
					}
				}
			}
			parametersChanged();
		}

	 	else if (checker.compare(this.getClass(), "Turn off the grid coordinator", null, commandName, "close")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*..................................................................*/
	/**Creates a TreeDisplayExtra, which will do the actual drawing on the tree.  Called by this
	 * modules employer (usually BasicTreeWindowMaker)*/
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeGridOperator newGrid = new NodeGridOperator(this, treeDisplay, numForNodeTask);
		grids.addElement(newGrid);
		return newGrid;
	}
	/*..................................................................*/
	/**Writes a Snapshot to MesquiteFile file, containing information about the number of rows, columns, and cells.
	 * also records NumberForNode hired for each cell, as well as threshold value used for coloring cells.*/
	public Snapshot getSnapshot(MesquiteFile file){
		Snapshot temp = new Snapshot();
		temp.addLine("setGridDimensions " + numRows + " " + numCols);
		temp.addLine("setCellSize " + cellHeight + " " +  cellWidth);
		if(atColorName!=null){
			String aName = ColorDistribution.getStandardColorName(aboveThreshColor);
			if (aName!=null)
				temp.addLine("setAboveT " + StringUtil.tokenize(aName));
		}
		if(btColorName!=null){
			String bName = ColorDistribution.getStandardColorName(belowThreshColor);
			if (bName!=null)
				temp.addLine("setBelowT " + StringUtil.tokenize(bName));
		}
		if(inAColorName!=null){
			String inAName = ColorDistribution.getStandardColorName(inAppColor);
			if (inAName!=null)
				temp.addLine("setInApp " + StringUtil.tokenize(inAName));
		}
		if(lcColorName!=null){
			String lcName = ColorDistribution.getStandardColorName(lowConflictColor);
			if (lcName!=null)
				temp.addLine("setLowCon " + StringUtil.tokenize(lcName));
		}
		if(hcColorName!=null){
			String hcName = ColorDistribution.getStandardColorName(highConflictColor);
			if (hcName!=null)
				temp.addLine("setHighCon " + StringUtil.tokenize(hcName));
		}
		temp.addLine("toggleIncludeMissing " + includeMissing.toOffOnString());
		temp.addLine("toggleDisplayCellColor " + displayCellColor.toOffOnString());
		temp.addLine("toggleDisplayCellValue " + displayCellValue.toOffOnString());
		temp.addLine("toggleDrawOutline " + drawOutline.toOffOnString());
		temp.addLine("toggleAnnotateNodes " + annotateNodes.toOffOnString());
		for(int iR = 0; iR < numRows; iR++){
			for(int iC = 0; iC < numCols; iC++){
				temp.addLine("setNumForNode_" + iR + "_" + iC + " ", numForNodeTask[iR][iC]);
			}
		}
		NodeGridOperator ngo = (NodeGridOperator)grids.elementAt(0);
		if (ngo!=null && ngo.legend!=null) {
			temp.addLine("setInitialOffsetX " + ngo.legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + ngo.legend.getOffsetY());
		}
		if(myFont != null)
			temp.addLine("setFont " + StringUtil.tokenize(myFont));
		if(myFontSize > 0)
			temp.addLine("setFontSize " + myFontSize);
		return temp;
	}
	/*..................................................................*/
	/**Returns the array of NumberForNode objects used to color grid cells*/
	public NumForNodeWithThreshold[][] getNumNodeTask(){
		return numForNodeTask;
	}
	/*..................................................................*/
	/**Returns the number of rows in the grid.*/
	public int getNumRows(){
		return numRows;
	}
	/*..................................................................*/
	/**Returns the number of columns in the grid.*/
	public int getNumCols(){
		return numCols;
	}
	/*..................................................................*/
	/**Returns the number of cells in the grid.*/
	public int getNumCells(){
		return numCells;
	}
	/*..................................................................*/
	public boolean requestPrimaryChoice(){
		return false;
	}
	/*..................................................................*/
	public void closeAllNodeOperators(){
		Enumeration<NodeGridOperator> e = grids.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof NodeGridOperator){
				NodeGridOperator nGO = (NodeGridOperator)obj;
				nGO.turnOff();
			}
		}
	}
	/*..................................................................*/
	//TODO: this method should probably be more flushed out...
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification){
		redraw();
	}
	/*..................................................................*/
	public void redraw(){
		Enumeration<NodeGridOperator> e = grids.elements();
		while(e.hasMoreElements()){
			Object obj = e.nextElement();
			if(obj instanceof NodeGridOperator){
				NodeGridOperator nGO = (NodeGridOperator)obj;
				nGO.getTreeDisplay().repaint();
				if(nGO.legend!=null){
					nGO.legend.adjustColors(getCellColors());
				}
			}
		}
	}
	/*..................................................................*/
	public void endJob(){
		closeAllNodeOperators();
		super.endJob();
	}
	/*..................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*..................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*..................................................................*/
	public int getCellWidth() {
		return cellWidth;
	}
	/*..................................................................*/
	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}
	/*..................................................................*/
	public int getCellHeight() {
		return cellHeight;
	}
	/*..................................................................*/
	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}
	/*..................................................................*/
	/**Returns array of colors corresponding to the colors reflecting values which are above the threshold,
	 * below the threshold, or missing or inapplicable (in that order in the array returned)*/
	public Color[] getCellColors(){
		Color[] cellColors = {aboveThreshColor, belowThreshColor, inAppColor, lowConflictColor, highConflictColor};
		return cellColors;
	}
	/*..................................................................*/
	public String[] getStateNames(){
		String[] stateNames = {"Above Threshold", "Below Threshold", "Missing or Inapplicable", "Low Conflict", "High Conflict"};
		return stateNames;
	}
	/*..................................................................*/
	public int getInitialOffsetX() {
		return initialOffsetX;
	}
	/*..................................................................*/
	public int getInitialOffsetY() {
		return initialOffsetY;
	}
	/*..................................................................*/
	public boolean showLegend() {
		return true;
	}
	public MesquiteBoolean getIncludeMissing(){
		return includeMissing;
	}
	public MesquiteBoolean getDisplayCellColor() {
		return displayCellColor;
	}
	public MesquiteBoolean getDisplayCellValue() {
		return displayCellValue;
	}
	public void setDisplayCellColor(MesquiteBoolean displayCellColor) {
		this.displayCellColor = displayCellColor;
	}
	public void setDisplayCellValue(MesquiteBoolean displayCellValue) {
		this.displayCellValue = displayCellValue;
	}
	public MesquiteBoolean getDrawOutline(){
		return drawOutline;
	}
	public Font getCurrentFont() {
		return currentFont;
	}
	public void setCurrentFont(Font currentFont) {
		this.currentFont = currentFont;
	}
	public int getMyFontSize() {
		return myFontSize;
	}
	public void setMyFontSize(int myFontSize) {
		this.myFontSize = myFontSize;
	}
	public String getMyFont() {
		return myFont;
	}
	public void setMyFont(String myFont) {
		this.myFont = myFont;
	}
}

/*======================================================================================*/
class NodeGridOperator extends TreeDisplayDrawnExtra{
	private GridForNode gridModule;
	private Tree tree = treeDisplay.getTree();
	GridLegend legend;
	MesquiteNumber result;
	MesquiteString resultString;
	NumForNodeWithThreshold[][] numForNodeCells;
//	boolean togglePrint = true;
	private boolean initialSetup = true;

	/**Constructor method for NodeGridOperator*/
	public NodeGridOperator(GridForNode ownerModule, TreeDisplay treeDisplay, NumForNodeWithThreshold[][] numForNodeArray){
		super(ownerModule, treeDisplay);
		gridModule = ownerModule;
	}
	/*..................................................................*/
	/**Calls the NumberForNode calculate number method for a particular node and cell element*/
	private MesquiteNumber doCalculations(int node, int row, int col){
		if(tree.nodeExists(node)){
			if(result==null){
				result = new MesquiteNumber();
				result.setToUnassigned();
			} else result.setToUnassigned();
			if(resultString==null)
				resultString = new MesquiteString("");
			else resultString.setValue("");
			numForNodeCells[row][col].calculateNumber(tree, node, result, resultString);
		}
		else {
			result.setToUnassigned();
		}
		return result;
	}
	/*..................................................................*/
	/**Operates on passed Graphics object; fills row by row, calling drawGridCell for each cell*/
	private void drawGridOnBranch(Tree tree, int node, Graphics g){
		numForNodeCells = gridModule.getNumNodeTask();
		boolean annotateNodes = gridModule.annotateNodes.getValue();
		if(numForNodeCells!=null){
			int gridWidth = gridModule.getNumCols() * gridModule.getCellWidth();
			int gridHeight = gridModule.getNumRows() * gridModule.getCellHeight();
			int gridX, gridY, startX, startY;
			//Recurses through tree
			for(int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				//Checks to make sure node isn't terminal (don't want these grids on terminal branches):
				if(!tree.nodeIsTerminal(d)){
					drawGridOnBranch(tree, d, g);
					MesquiteNumber middleX = new MesquiteNumber();
					MesquiteNumber middleY = new MesquiteNumber();
					MesquiteDouble angle = new MesquiteDouble();
					treeDisplay.getTreeDrawing().getMiddleOfBranch(tree, d, middleX, middleY, angle);
					gridX = (int)(middleX.getIntValue() - gridWidth/2);
					gridY = (int)(middleY.getIntValue() - gridHeight/2);
					startX = gridX;
					startY = gridY;
					// An array we'll use (if annotateNodes is true) to store node annotations
					String[] annotations = new String[gridModule.getNumRows() * gridModule.getNumCols()];
					int annotationIndex = 0;
					MesquiteNumber cellValue = new MesquiteNumber(0);
					//Draws cells row by row
					for(int iR = 0; iR < gridModule.getNumRows(); iR++){
						//Checks to see if drawing the first row; maybe unnecessary...
						if(iR==0){
							startY = gridY;
						}
						for(int iC = 0; iC < gridModule.getNumCols(); iC++){
							//Resets starting X position if starting a new row
							if(iC==0){
								startX = gridX;
							}
							cellValue.setToUnassigned();;
							drawGridCell(tree, d, iR, iC, g, startX, startY, cellValue);
							// Store the annotation for this cell in the String array
							annotations[annotationIndex] = "NGV" + (iR + 1) + "." + (iC + 1) + "=" + cellValue.toString();							
							annotationIndex++;
							startX += gridModule.getCellWidth(); //Increments the x position for drawing the next grid cell in the row
						}
						startY += gridModule.getCellHeight(); //Increments the y position for drawing the next row
					}
					// Write those annotations to the tree (if appropriate)
					if (annotateNodes) {
						if (tree instanceof Associable) {
							String annotationString = String.join(":", annotations);
							((Associable)tree).setAssociatedObject(NameReference.getNameReference("NodeGridValues"), d, annotationString);
						}
					}
				}
			}
		}

	}
	/*..................................................................*/
	/**Draws a grid cell; color is based on weather NumberForNode value is
	 * (1) applicable and (2) above or below a user-defined threshold value.*/
	private void drawGridCell(Tree tree, int node, int row, int col, Graphics g, int x, int y, MesquiteNumber cellValue){
		Color oC = g.getColor();
		Color inApp = gridModule.inAppColor;
		Color belowThresh = gridModule.belowThreshColor;
		Color aboveThresh = gridModule.aboveThreshColor;
		Color lowConflict = gridModule.lowConflictColor;
		Color highConflict = gridModule.highConflictColor;
		Color boxColor = Color.WHITE;
		Color textColor = Color.BLACK;
		Color altTextColor = Color.WHITE;
		if(numForNodeCells!=null){
			MesquiteNumber threshold = new MesquiteNumber(numForNodeCells[row][col].getThreshold());//TODO: may want to put conditional here, to make sure thresholdArray!=null
			MesquiteNumber nodeValue = new MesquiteNumber(this.doCalculations(node, row, col));
			cellValue.setValue(nodeValue);
			int sigFigs = numForNodeCells[row][col].getSigFigs();
			if(!MesquiteInteger.isCombinable(sigFigs)){
				sigFigs = 2;
			}
		if(gridModule.getDisplayCellColor().getValue()){
			if(nodeValue.isCombinable() && !nodeValue.isNegative()){
				if(nodeValue.isLessThan(threshold))
					boxColor = belowThresh;
				else boxColor = aboveThresh;
			} else if(nodeValue.isCombinable()){
				MesquiteNumber negativeThresh = new MesquiteNumber(threshold);
				negativeThresh.multiplyBy(-1.0);
				if(nodeValue.isMoreThan(negativeThresh)){
					boxColor = lowConflict; //Low conflict
				} else boxColor = highConflict; //Big conflict
			} else boxColor = inApp;
		}
		else g.setColor(Color.WHITE);

/*	To turn off conflict coloring, comment out the above conditionals, and instead use:
			if(gridModule.getDisplayCellColor().getValue()){
				if(nodeValue.isCombinable()){
					if(nodeValue.isLessThan(threshold))
						boxColor = belowThresh;
					else boxColor = aboveThresh;
				} else boxColor = inApp;
			}
			else g.setColor(Color.WHITE);
*/
			g.setColor(boxColor);
			if(!gridModule.formatForPDF.getValue()){
						g.fillRect(x, y, gridModule.getCellWidth()+1, gridModule.getCellHeight()+1);
			} else g.fillRect(x, y, gridModule.getCellWidth(), gridModule.getCellHeight());
			g.setColor(Color.gray);
			if(gridModule.getDrawOutline().getValue()){
				g.drawRect(x, y, gridModule.getCellWidth(), gridModule.getCellHeight());
			}
			else{
				drawInternalBorders(x, y, row, col, g);
			}

			if(gridModule.getDisplayCellValue().getValue()){
				if(gridModule.getCurrentFont()!=null){
					g.setFont(gridModule.getCurrentFont());
				}
				if(gridModule.getDisplayCellColor().getValue() && textColor==boxColor) //Makes sure the values displayed in cell don't have font color same as color displayed as cell (currently only black & white)
					g.setColor(altTextColor);
				else g.setColor(textColor);
				if(nodeValue.isCombinable()){
					String nodeValString = MesquiteDouble.toStringDigitsSpecified(nodeValue.getDoubleValue(), sigFigs);
					g.drawString(nodeValString, StringUtil.getStringCenterPosition(nodeValString, g, x, gridModule.getCellWidth(), null), StringUtil.getStringVertPosition(g, y, gridModule.getCellHeight(), null));
				}
				else
					if(gridModule.getIncludeMissing().getValue())
						g.drawString("-", StringUtil.getStringCenterPosition("-", g, x, gridModule.getCellWidth(), null), StringUtil.getStringVertPosition(g, y, gridModule.getCellHeight(), null));
			}
			g.setColor(oC);
		}
	}
	/*..................................................................*/
	/**Draws only internal borders between grid cells, as opposed to internal & external borders*/
	private void drawInternalBorders(int x, int y, int row, int col, Graphics g){
		if(gridModule.getNumCols() > 1){
			if(col > 0){
				g.drawLine(x, y, x, y+gridModule.getCellHeight());
				if(col < gridModule.getNumCols() - 1)
					g.drawLine(x + gridModule.getCellWidth(), y, x + gridModule.getCellWidth(), y + gridModule.getCellHeight());
			}
			else g.drawLine(x + gridModule.getCellWidth(), y, x + gridModule.getCellWidth(), y + gridModule.getCellHeight());
		}
		if(gridModule.getNumRows() > 1){
			if(row > 0){
				g.drawLine(x, y, x+gridModule.getCellWidth(), y);
				if(row < gridModule.getNumRows() - 1)
					g.drawLine(x, y + gridModule.getCellHeight(), x + gridModule.getCellWidth(), y + gridModule.getCellHeight());
			}
			else g.drawLine(x, y + gridModule.getCellHeight(), x + gridModule.getCellWidth(), y + gridModule.getCellHeight());
		}
	}
	/*..................................................................*/
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {//Called by TreeDisplay, potentially twice
		Font origFont = g.getFont();
		// Remove grid annotations if they're supposed to be gone
		if (!gridModule.annotateNodes.getValue()) {
			if (((Associable)tree).anyAssociatedObject(NameReference.getNameReference("NodeGridValues"))) {
				((Associable)tree).removeAssociatedObjects(NameReference.getNameReference("NodeGridValues"));
			}
		}
		drawGridOnBranch(tree, tree.getRoot(), g);
		if(legend==null){
			legend = new GridLegend(gridModule, treeDisplay, "Grid Coordinator Legend", Color.white, gridModule.getCellColors(), gridModule.getNumNodeTask(), gridModule.getNumRows(), gridModule.getNumCols());
			addPanelPlease(legend);
			legend.setVisible(true);
		}
		if(legend!=null)
			legend.adjustLocation();
		if(initialSetup){ //When grids are first set up (or file is first opened), some boxes are not colored correctly.  This second call for drawing should fix the problem. Oliver. Nov.16.2012.
			drawGridOnBranch(tree,tree.getRoot(),g);
			initialSetup = false;
		}
		g.setFont(origFont);
	}
	/*..................................................................*/
	// Only included as required by superclass
	public void printOnTree(Tree tree, int drawnRoot, Graphics g) { //Called by TreeDisplay, potentially twice
		//drawOnTree(tree, drawnRoot, g); 
	}
	/*..................................................................*/
	public void setTree(Tree tree) {
		this.tree = tree;
	}
	/*..................................................................*/
	public Tree getTree(){
		return tree;
	}
	/*..................................................................*/
	public void turnOff(){
		if (numForNodeCells!=null)
			numForNodeCells=null;
		if (legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}

/*======================================================================================*/
class GridLegend extends TreeDisplayLegend{
	private LegendHolder gridModule;
	private static final int defaultLegendWidth=160;
	private static final int defaultLegendHeight=120;
	private boolean holding = false;
	final int defaultSpecsHeight = (114 + MesquiteModule.textEdgeCompensationHeight) * 1;
	private int specsHeight = defaultSpecsHeight;
	private int e = 22;
	private int scrollAreaHeight = 41;
	private String title;
	private Color titleColor;
	private Color[] cellColors;
	private String[] stateNames;
	private int topEdge = 6; //Used for popup menu operation, but perhaps incorrectly...
	private int nRows;
	private int nCols;
	private int boxWidth = 28;
	private int boxHeight = 12;
	Polygon dropDownTriangle;
	private NumForNodeWithThreshold[][] numNodeTask;
	private BoxDimensions[][] legendBoxInfo;

	/**Constructor method for GridLegend*/
	public GridLegend(LegendHolder gridModule, TreeDisplay treeDisplay, String title, Color titleColor, Color[] cellColors, NumForNodeWithThreshold[][] numNodeTask, int nRows, int nCols){
		super(treeDisplay, defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.numNodeTask = numNodeTask;
		this.nRows = nRows;
		this.nCols = nCols;
		legendBoxInfo = new BoxDimensions[this.nRows][this.nCols];
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		setOffsetX(gridModule.getInitialOffsetX());
		setOffsetY(gridModule.getInitialOffsetY());
		this.gridModule = gridModule;
		setBackground(ColorDistribution.darkGreen);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		dropDownTriangle = MesquitePopup.getDropDownTriangle();

		if (gridModule.showLegend()){
			reviseBounds();
		}
	}
	/*..................................................................*/
	public void setTitle(String title) {
		this.title = title;
	}
	/*..................................................................*/
	public void setVisible(boolean b) {
		super.setVisible(b);
	}
	/*..................................................................*/
	public void refreshSpecsBox(){
	}
	/*..................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			g.setColor(Color.black);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			g.fillRect(legendWidth-6, legendHeight-6, 6, 6);
			g.setColor(Color.black);
			g.drawLine(0, 0, legendWidth-1, 0);
			g.setColor(titleColor);
			g.drawString(title, 4, 14);
			g.setColor(Color.white);
			g.fillRect(3, e, legendWidth-6, specsHeight-6);
			g.setColor(Color.black);
		}
		/*Draws & fills rectangles with colors for each of the three states.*/
		if(this.gridModule instanceof GridForNode){
			stateNames = ((GridForNode)this.gridModule).getStateNames();
			cellColors = ((GridForNode)this.gridModule).getCellColors();
		}
		for (int ibox=0; ibox<stateNames.length; ibox++) {
			g.setColor(cellColors[ibox]);
			g.fillRect(4, ibox*16 + e + 2 , boxWidth, boxHeight);
			g.setColor(Color.gray);
			g.drawRect(4, ibox*16 + e + 2 , boxWidth, boxHeight);
			g.setColor(Color.black);
			if (stateNames[ibox]!=null)
				g.drawString(stateNames[ibox], boxWidth + 8, ibox*16 + 12 + e + 2 );
		}
		int startX = 4;
		int startY = (stateNames.length + 1)*16 + e + 12;
		g.setColor(Color.gray);
		if(numNodeTask!=null){
			g.setColor(Color.black);
			g.drawString("Threshold Values", startX, startY - 10);
			for(int iR = 0; iR < nRows; iR++){
				for(int iC = 0; iC < nCols; iC++){
					g.setColor(Color.gray);
					g.drawRect(startX, startY, boxWidth, boxHeight);
					g.setColor(Color.black);
					g.drawString(numNodeTask[iR][iC].getThreshold().toString(), StringUtil.getStringCenterPosition(numNodeTask[iR][iC].getThreshold().toString(), g, startX, boxWidth, null), StringUtil.getStringVertPosition(g, startY, boxHeight, null));
					dropDownTriangle.translate((startX + boxWidth-6), (startY+1));
					g.setColor(Color.white);
					g.drawPolygon(dropDownTriangle);
					g.setColor(Color.black);
					g.fillPolygon(dropDownTriangle);
					dropDownTriangle.translate(-(startX + boxWidth-6), -(startY+1));
					legendBoxInfo[iR][iC] = new BoxDimensions(startX, startX+boxWidth, startY, startY+boxHeight, iR, iC);
					startX += boxWidth;
				}
				startX = 4;
				startY += boxHeight;
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*..................................................................*/
	public void printAll(Graphics g) {
		g.setColor(Color.black);
		g.drawString(title, 4, 14);
	}
	/*..................................................................*/
	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight  = defaultSpecsHeight;
		checkComponentSizes();
	}
	/*..................................................................*/
	public void reviseBounds(){
		checkComponentSizes();
		Point where = getLocation();
		Rectangle bounds = getBounds();
		if (bounds.width!=legendWidth || bounds.height!=legendHeight) //make sure a change is really needed
			setBounds(where.x,where.y,legendWidth, legendHeight);
	}
	/*..................................................................*/
	public void adjustColors(Color[] cellC){
		if(cellC.length == 3){
			cellColors = cellC;
			repaint();
		}
	}
	/*..................................................................*/
	public void checkComponentSizes(){
		legendHeight=specsHeight + e + 4;
	}
	/*..................................................................*/
	public void onHold() {
		holding = true;
	}
	/*..................................................................*/
	public void offHold() {
		holding = false;
	}
	/*..................................................................*/
	/*For menu operations of legend*/
	public void boxTouched(int whichR, int whichC, int whereX, int whereY){
		numNodeTask[whichR][whichC].showPopUp(this, whereX, whereY);
	}
	/*..................................................................*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (y<=topEdge) {
			super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
		else	 {
			if(legendBoxInfo!=null){
				int whichRow = -1;
				int whichColumn = -1;
				int iR = 0;
				int iC = 0;
				while(iR < nRows && whichRow < 0){
					if(y < legendBoxInfo[iR][iC].getBottom() && y > legendBoxInfo[iR][iC].getTop()){
						whichRow = iR;
					}
					iR++;
				}
				iR = 0;
				while(iC < nCols && whichColumn < 0){
					if (x > legendBoxInfo[iR][iC].getLeft() && x < legendBoxInfo[iR][iC].getRight()){
						whichColumn = iC;
					}
					iC++;
				}
				if (whichRow > -1 && whichColumn > -1){
					boxTouched(whichRow, whichColumn, x, y);
				}
				else super.mouseDown(modifiers, clickCount, when, x, y, tool);
			}
			else
				super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
	}
}
/*======================================================================================*/
/**A class to contain positional information for legend boxes*/
class BoxDimensions{
	private int left, right, top, bottom, row, col;
	/*..................................................................*/
	public BoxDimensions(int left, int right, int top, int bottom, int row, int col){
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	/*..................................................................*/
	public int getBottom() {
		return bottom;
	}
	/*..................................................................*/
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	/*..................................................................*/
	public int getLeft() {
		return left;
	}
	/*..................................................................*/
	public void setLeft(int left) {
		this.left = left;
	}
	/*..................................................................*/
	public int getRight() {
		return right;
	}
	/*..................................................................*/
	public void setRight(int right) {
		this.right = right;
	}
	/*..................................................................*/
	public int getTop() {
		return top;
	}
	/*..................................................................*/
	public void setTop(int top) {
		this.top = top;
	}
	/*..................................................................*/
	public int getCol() {
		return col;
	}
	/*..................................................................*/
	public void setCol(int col) {
		this.col = col;
	}
	/*..................................................................*/
	public int getRow() {
		return row;
	}
	/*..................................................................*/
	public void setRow(int row) {
		this.row = row;
	}
	/**Prints dimensions of box at (row, col) to the log.  Used for debugging purposes.*/
	public void printDimensions(int row, int col){
		MesquiteTrunk.mesquiteTrunk.logln("Bounds of box " + row + ", " + col + ":");
		MesquiteTrunk.mesquiteTrunk.logln("\tLeft:" + left + "\tRight:" + right);
		MesquiteTrunk.mesquiteTrunk.logln("\tTop:" + top + "\tBottom:" + bottom);
	}
}