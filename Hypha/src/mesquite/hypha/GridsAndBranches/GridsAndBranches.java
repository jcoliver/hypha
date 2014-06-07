package mesquite.hypha.GridsAndBranches;

import java.awt.*;
import java.util.*;
import mesquite.hypha.GridForNode.GridForNode;
import mesquite.hypha.NumForNodeWithThreshold.*;
import mesquite.hypha.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/**Tree display assistant for drawing a series of grid cells on a branch, coloring cells
 * based on values from other trees.  Also colors branches based on those same trees.  Essentially
 * a module combining GridForNode and ThresholdBranchColor, ensuring that branch colors are drawn
 * <i>under</i> the grid cells.*/
public class GridsAndBranches extends NoduleCoordinator {
	MesquiteSubmenuSpec termMenuItem;
	protected Color terminalBranchColor = Color.black;
	protected MesquiteString termColorName;
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
	MesquiteBoolean displayCellColor, displayCellValue, includeMissing, drawOutline;
	MesquiteMenuItemSpec displayCellColorItem, displayCellValueItem, includeMissingItem, drawOutlineItem;
	Font currentFont = null;
	String myFont = null;
	int myFontSize = -1;
	MesquiteString fontSizeName, fontName;
	NoduleOperator GnBOperator;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeDecor = new Vector();
		makeMenu("Grids_N_Branches");
		addMenuItem("Grid Coordinator & Branch Colorer", null);
		addMenuItem("Set Cell Drawing Size...", makeCommand("setCellSize", this));
		aboveTMenuItem = addSubmenu(null, "Above Threshold Branch Color", makeCommand("setAboveT", this));
		aboveTMenuItem.setList(ColorDistribution.standardColorNames);
		atColorName = new MesquiteString(ColorDistribution.getStandardColorName(aboveThreshColor));
		if(atColorName!=null){
			aboveTMenuItem.setSelected(atColorName);
		}
		belowTMenuItem = addSubmenu(null, "Below Threshold Branch Color", makeCommand("setBelowT", this));
		belowTMenuItem.setList(ColorDistribution.standardColorNames);
		btColorName = new MesquiteString(ColorDistribution.getStandardColorName(belowThreshColor));
		if(btColorName!=null){
			belowTMenuItem.setSelected(btColorName);
		}
		inAMenuItem = addSubmenu(null, "Missing or Inapplicable Branch Color", makeCommand("setInApp", this));
		inAMenuItem.setList(ColorDistribution.standardColorNames);
		inAColorName = new MesquiteString(ColorDistribution.getStandardColorName(inAppColor));
		if(inAppColor!=null){
			inAMenuItem.setSelected(inAColorName);
		}
		termMenuItem = addSubmenu(null, "Terminal Branch Color", makeCommand("setTermColor", this));
		termMenuItem.setList(ColorDistribution.standardColorNames);
		termColorName = new MesquiteString(ColorDistribution.getStandardColorName(terminalBranchColor));
		if(terminalBranchColor!=null){
			termMenuItem.setSelected(termColorName);
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
		
		currentFont = MesquiteWindow.defaultFont;
		fontName = new MesquiteString(MesquiteWindow.defaultFont.getName());
		fontSizeName = new MesquiteString(Integer.toString(MesquiteWindow.defaultFont.getSize()));
		MesquiteSubmenuSpec msf = addSubmenu(null, "Font", makeCommand("setFont", this), MesquiteSubmenu.getFontList());
		msf.setList(MesquiteSubmenu.getFontList());
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
		addMenuItem("Close Grids & Branches", makeCommand("close", this));
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
				String helpString = "Enter the cell height and width for the node grid coordinator.";
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
		else if (checker.compare(this.getClass(), "Set color of cells & branches for values equal to or greater than threshold.", "[name of color]", commandName, "setAboveT")) {
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

		else if (checker.compare(this.getClass(), "Set color of cells & branches for values less than threshold.", "[name of color]", commandName, "setBelowT")) {
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
		
	 	else if (checker.compare(this.getClass(), "Set color of cells & branches for missing or inapplicable values.", "[name of color]", commandName, "setInApp")) {
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
	 	else if (checker.compare(this.getClass(), "Set color of terminal branches", "[name of color]", commandName, "setTermColor")){
	 		Color tc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if(tc == null)
	 			return null;
	 		terminalBranchColor = tc;
	 		termColorName.setValue(tc.toString());
	 		MesquiteString tCName = new MesquiteString(ColorDistribution.getStandardColorName(terminalBranchColor));
	 		if(tCName!=null){
	 			termMenuItem.setSelected(tCName);
	 			resetContainingMenuBar();
	 			redraw();
	 		}
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
	 			}
	 			else {
	 				includeMissingItem.setEnabled(false);
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
	 		if(current!=drawOutline.getValue()){
	 			parametersChanged();
	 			redraw();
	 		}
	 	}
	 	else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				Enumeration e = nodeDecor.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof GnBOperator) {
						GnBOperator gnbO = (GnBOperator)obj;
						if (gnbO.legend!=null)
							gnbO.legend.setOffsetX(offset);
					}
				}
			}
		}
	 	else if (checker.compare(this.getClass(), "Sets initial vertical offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
				Enumeration e = nodeDecor.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof GnBOperator) {
						GnBOperator gnbO = (GnBOperator)obj;
						if (gnbO.legend!=null)
							gnbO.legend.setOffsetY(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the font used for the node grid values", "[name of font]", commandName, "setFont")) {
			String t = parser.getFirstToken(arguments);
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
	 	else if (checker.compare(this.getClass(), "Turn off the Grids & Branches Coordinator", null, commandName, "close")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
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
		if(termColorName!=null){
			String tCName = ColorDistribution.getStandardColorName(terminalBranchColor);
			if(tCName!=null)
				temp.addLine("setTermColor " + StringUtil.tokenize(tCName));
		}
		temp.addLine("toggleIncludeMissing " + includeMissing.toOffOnString());
		temp.addLine("toggleDisplayCellColor " + displayCellColor.toOffOnString());
		temp.addLine("toggleDisplayCellValue " + displayCellValue.toOffOnString());
		temp.addLine("toggleDrawOutline " + drawOutline.toOffOnString());
		for(int iR = 0; iR < numRows; iR++){
			for(int iC = 0; iC < numCols; iC++){
				temp.addLine("setNumForNode_" + iR + "_" + iC + " ", numForNodeTask[iR][iC]);
			}
		}
		GnBOperator gnbO = (GnBOperator)nodeDecor.elementAt(0);
		if(gnbO!=null && gnbO.legend!=null){
			temp.addLine("setInitialOffsetX " + gnbO.legend.getOffsetX());
			temp.addLine("setInitialOffsetY " + gnbO.legend.getOffsetY());
		}
		if(myFont != null)
			temp.addLine("setFont " + myFont);
		if(myFontSize > 0)
			temp.addLine("setFontSize " + myFontSize);
		return temp;
	}
	/*..................................................................*/
	/**Creates a TreeDisplayExtra, which will do the actual drawing on the tree.  Called by this
	 * module's employer (usually BasicTreeWindowMaker)*/
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		GnBOperator newGrid = new GnBOperator(this, treeDisplay);
		nodeDecor.addElement(newGrid);
		return newGrid;
	}
	/*..................................................................*/
	/**Closes node operators.*/
	public void closeAllNodeOperators() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof GnBOperator){
				GnBOperator gnbO = (GnBOperator)obj;
				gnbO.turnOff();
			}
		}
	}
	/*..................................................................*/
	/**Redraws decorated tree.  Usually called when parameters or parameters of employee modules have changed.*/
	public void redraw() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof GnBOperator){
				GnBOperator gnbO = (GnBOperator)obj;
				gnbO.getTreeDisplay().repaint();
				if(gnbO.legend!=null){
					gnbO.legend.adjustColors(getColorArray());
				}
			}
		}	
	}
	/*..................................................................*/
	/**Returns array of colors reflecting values which are above the threshold,
	 * below the threshold, missing/inapplicable, or corrsponding to terminal branches.*/
	public Color[] getColorArray(){
		Color[] branchColors = {aboveThreshColor, belowThreshColor, inAppColor, terminalBranchColor};
		return branchColors;
	}
	/*..................................................................*/
	/**Returns array of state names.*/
	public String[] getStateNames(){
		String[] stateNames = {"Above Threshold", "Below Threshold", "Missing or Inapplicable"};
		return stateNames;
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
	/**Returns whether to draw an outline around the entire grid.  If drawOutline==false, only internal borders
	 * between grid cells will be drawn (by GnBOperator).*/
	public MesquiteBoolean getDrawOutline(){
		return drawOutline;
	}
	/*..................................................................*/
	public int getCellWidth() {
		return cellWidth;
	}
	/*..................................................................*/
	public int getCellHeight() {
		return cellHeight;
	}
	/*..................................................................*/
	public Color getTermColor(){
		return terminalBranchColor;
	}
	/*..................................................................*/
	public MesquiteBoolean getDisplayCellColor() {
		return displayCellColor;
	}
	/*..................................................................*/
	public MesquiteBoolean getDisplayCellValue() {
		return displayCellValue;
	}
	/*..................................................................*/
	public MesquiteBoolean getIncludeMissing() {
		return includeMissing;
	}
	/*..................................................................*/
	public Font getCurrentFont() {
		return currentFont;
	}
	/*..................................................................*/
	public void setCurrentFont(Font currentFont) {
		this.currentFont = currentFont;
	}
	/*..................................................................*/
	public int getMyFontSize() {
		return myFontSize;
	}
	/*..................................................................*/
	public void setMyFontSize(int myFontSize) {
		this.myFontSize = myFontSize;
	}
	/*..................................................................*/
	public String getMyFont() {
		return myFont;
	}
	/*..................................................................*/
	public void setMyFont(String myFont) {
		this.myFont = myFont;
	}
	/*..................................................................*/
	public boolean requestPrimaryChoice(){
		return false;
	}
	/*..................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*..................................................................*/
	public String getName() {
		return "Grid Coordinator & Threshold Branch Colorer";
	}	
}
/*..................................................................*/
/* ================================================================ */
class GnBOperator extends NoduleOperator{
	private GridsAndBranches gnbModule;
	private Tree tree = treeDisplay.getTree();
	MesquiteNumber result;
	MesquiteString resultString;
	GnBLegend legend;
	
	/**Constructor method of GnBOperator*/
	public GnBOperator(GridsAndBranches ownerModule, TreeDisplay treeDisplay){
		super(ownerModule, treeDisplay);
		gnbModule = ownerModule;
	}
	/*..................................................................*/
	/**Fetches the NumberForNode value, which should be a measure of clade support, either in the form
	 * of a branch length (e.g. PAUP bootstrap support) or branch label (e.g. MrBayes posterior probability).*/
	private MesquiteNumber doCalculations(int node, int row, int col){
		if(tree.nodeExists(node)){
			if(result==null){
				result = new MesquiteNumber();
				result.setToUnassigned();
			} else result.setToUnassigned();
			if(resultString==null)
				resultString = new MesquiteString("");
			else resultString.setValue("");
			(gnbModule.getNumNodeTask())[row][col].calculateNumber(tree, node, result, resultString);
		} else result.setToUnassigned();
		return result;
	}

	/*..................................................................*/
	/**Recurses through tree, calling appropriate methods to draw grids and color branches appropriately.*/
	public void drawOnBranch(Tree tree, int node, Graphics g){
		if(gnbModule.getNumNodeTask()==null)
			return;
		int gridWidth = gnbModule.getNumCols() * gnbModule.getCellWidth();
		int gridHeight = gnbModule.getNumRows() * gnbModule.getCellHeight();
		int gridX, gridY, startX, startY;
		Color branchColor;
		Color cellColor = Color.WHITE;
		Color textColor = Color.BLACK;
		Color altTextColor = Color.WHITE;
		Color oC = g.getColor();

		/**Array that will hold support value information of <b>node</b> harvested from other trees*/
		MesquiteNumber branchValues[][];
		branchValues = new MesquiteNumber[gnbModule.getNumRows()][gnbModule.getNumCols()];

		//Recurses through tree:
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			//Checks to make sure node isn't terminal:
			if(!tree.nodeIsTerminal(d)){
				drawOnBranch(tree, d, g);
				branchColor = gnbModule.getAboveThreshColor();
				/*These two for loops harvest support information from other trees*/
				for(int iR = 0; iR < gnbModule.getNumRows(); iR++){
					for(int iC = 0; iC < gnbModule.getNumCols(); iC++){
						branchValues[iR][iC] = new MesquiteNumber(MesquiteInteger.unassigned);
						branchValues[iR][iC].setValue(this.doCalculations(d, iR, iC));
						if(branchColor == gnbModule.getAboveThreshColor()){
							if(!branchValues[iR][iC].isCombinable()){
								branchColor = gnbModule.getInAppColor();
							}
							else if(branchValues[iR][iC].isLessThan(gnbModule.getNumNodeTask()[iR][iC].getThreshold())){
								branchColor = gnbModule.getBelowThreshColor();
							}
						}
						else if(branchColor == gnbModule.getBelowThreshColor()){
							if(!branchValues[iR][iC].isCombinable()){
								branchColor = gnbModule.getInAppColor();
							}
						}
					}
				}
				g.setColor(branchColor);
				treeDisplay.getTreeDrawing().fillBranch(tree, d, g);
				g.setColor(oC);
				//Branch has been colored, now grids are drawn over branch:
				MesquiteNumber middleX = new MesquiteNumber();
				MesquiteNumber middleY = new MesquiteNumber();
				MesquiteDouble angle = new MesquiteDouble();
				treeDisplay.getTreeDrawing().getMiddleOfBranch(tree, d, middleX, middleY, angle);
				gridX = (int)(middleX.getIntValue() - gridWidth/2);
				gridY = (int)(middleY.getIntValue() - gridHeight/2);
				startX = gridX;
				startY = gridY;
				int cellIndex = 0;

				for(int iR = 0; iR < gnbModule.getNumRows(); iR++){
					//Checks to see if drawing the first row; maybe unnecessary...
					if(iR==0){
						startY = gridY;
					}
					for(int iC = 0; iC < gnbModule.getNumCols(); iC++){
						//Resets starting X position if starting a new row
						if(iC==0){
							startX = gridX;
						}
						int sigFigs = gnbModule.getNumNodeTask()[iR][iC].getSigFigs();
						if(!MesquiteInteger.isCombinable(sigFigs)){
							sigFigs = 2;
						}
						if(gnbModule.getDisplayCellColor().getValue()){
							if(branchValues[iR][iC].isCombinable()){
								if(branchValues[iR][iC].isLessThan(gnbModule.getNumNodeTask()[iR][iC].getThreshold()))
									cellColor = gnbModule.getBelowThreshColor();
								else cellColor = gnbModule.getAboveThreshColor();
							} else cellColor = gnbModule.getInAppColor();
						}
						else g.setColor(Color.WHITE);
						g.setColor(cellColor);
						g.fillRect(startX, startY, gnbModule.getCellWidth()+1, gnbModule.getCellHeight()+1);
						g.setColor(Color.gray);
						if(gnbModule.getDrawOutline().getValue()){
							g.drawRect(startX, startY, gnbModule.getCellWidth(), gnbModule.getCellHeight());
						}
						else{
							drawInternalBorders(startX, startY, iR, iC, g);
						}

						if(gnbModule.getDisplayCellValue().getValue()){
							if(gnbModule.getCurrentFont()!=null){
								g.setFont(gnbModule.getCurrentFont());
							}
							if(gnbModule.getDisplayCellColor().getValue() && textColor==cellColor) //Makes sure the values displayed in cell don't have font color same as color displayed as cell (currently only black & white)
								g.setColor(altTextColor);
							else g.setColor(textColor);
							if(branchValues[iR][iC].isCombinable()){
								String nodeValString = MesquiteDouble.toStringDigitsSpecified(branchValues[iR][iC].getDoubleValue(), sigFigs);
								g.drawString(nodeValString, StringUtil.getStringCenterPosition(nodeValString, g, startX, gnbModule.getCellWidth(), null), StringUtil.getStringVertPosition(g, startY, gnbModule.getCellHeight(), null));
							}
							else
								if(gnbModule.getIncludeMissing().getValue())
									g.drawString("-", StringUtil.getStringCenterPosition("-", g, startX, gnbModule.getCellWidth(), null), StringUtil.getStringVertPosition(g, startY, gnbModule.getCellHeight(), null));
						}
						g.setColor(oC);
						cellIndex++;
						startX += gnbModule.getCellWidth(); //Increments the x position for drawing the next grid cell in the row
					}
					startY += gnbModule.getCellHeight(); //Increments the y position for drawing the next row
				}
			}
			else 
			{
				g.setColor(gnbModule.getTermColor());
				treeDisplay.getTreeDrawing().fillBranch(tree, d, g);
				g.setColor(oC);
			}
		}
	}

	/*..................................................................*/
	/**Draws only internal borders between grid cells, as opposed to internal & external borders*/
	private void drawInternalBorders(int x, int y, int row, int col, Graphics g){
		if(gnbModule.getNumCols() > 1){
			if(col > 0){
				g.drawLine(x, y, x, y+gnbModule.getCellHeight());
				if(col < gnbModule.getNumCols() - 1)
					g.drawLine(x + gnbModule.getCellWidth(), y, x + gnbModule.getCellWidth(), y + gnbModule.getCellHeight());
			}
			else g.drawLine(x + gnbModule.getCellWidth(), y, x + gnbModule.getCellWidth(), y + gnbModule.getCellHeight());
		}
		if(gnbModule.getNumRows() > 1){
			if(row > 0){
				g.drawLine(x, y, x+gnbModule.getCellWidth(), y);
				if(row < gnbModule.getNumRows() - 1)
					g.drawLine(x, y + gnbModule.getCellHeight(), x + gnbModule.getCellWidth(), y + gnbModule.getCellHeight());
			}
			else g.drawLine(x, y + gnbModule.getCellHeight(), x + gnbModule.getCellWidth(), y + gnbModule.getCellHeight());
		}
	}

	/*..................................................................*/
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g){
		Font origFont = g.getFont();
		drawOnBranch(tree, tree.getRoot(), g);
		if(legend==null){
			legend = new GnBLegend(gnbModule, treeDisplay, "Grid & Branch Coordinator", Color.white);
			addPanelPlease(legend);
			legend.setVisible(true);
		}
		if(legend!=null)
			legend.adjustLocation();
		g.setFont(origFont);
	}
	/*..................................................................*/
	public void turnOff(){
		if(legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}
/*..................................................................*/
/* ================================================================ */
class GnBLegend extends NoduleLegend{
	private LegendHolder gnbModule;
	private Color[] branchColors;
	private int boxWidth = 28;
	private int boxHeight = 12;
	private BoxDimensions[][] legendBoxInfo;
	
	/**Constructor method of GnBLegend.*/
	public GnBLegend(LegendHolder gnbModule, TreeDisplay treeDisplay, String title, Color titleColor){
		super(gnbModule, treeDisplay);
		setVisible(false);
		this.gnbModule = gnbModule;
		this.title = title;
		this.titleColor = titleColor;
		legendBoxInfo = new BoxDimensions[((GridsAndBranches)this.gnbModule).getNumRows()][((GridsAndBranches)this.gnbModule).getNumCols()];
		setOffsetX(this.gnbModule.getInitialOffsetX());
		setOffsetY(this.gnbModule.getInitialOffsetY());
		setLayout(null);
		setSize(legendWidth, legendHeight);
		dropDownTriangle = MesquitePopup.getDropDownTriangle();
		if(gnbModule.showLegend()){
			reviseBounds();
		}
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
			g.setColor(titleColor);
			g.drawString(title, 4, 14);
			g.setColor(Color.white);
			g.fillRect(3, specsYStart, legendWidth-6, specsHeight - 6);
			g.setColor(Color.black);
		}
		/*Draws & fills rectangles with colors for each of the three states.*/
		for (int ibox=0; ibox<((GridsAndBranches)gnbModule).getStateNames().length; ibox++) {
			g.setColor(((GridsAndBranches)gnbModule).getColorArray()[ibox]);
			g.fillRect(4, ibox*16 + specsYStart + 4, boxWidth, boxHeight);
			g.setColor(Color.gray);
			g.drawRect(4, ibox*16 + specsYStart + 4, boxWidth, boxHeight);
			g.setColor(Color.black);
			if (((GridsAndBranches)gnbModule).getStateNames()[ibox]!=null)
				g.drawString(((GridsAndBranches)gnbModule).getStateNames()[ibox], boxWidth + 8, ibox*16 + 14 + specsYStart);
		}
		int startX = 4;
		int startY = (((GridsAndBranches)gnbModule).getStateNames().length + 1)*16 + specsYStart + 12;
		g.setColor(Color.gray);
		if(((GridsAndBranches)gnbModule).getNumNodeTask()!=null){
			g.setColor(Color.black);
			g.drawString("Threshold Values", startX, startY - 10);
			for(int iR = 0; iR < ((GridsAndBranches)gnbModule).getNumRows(); iR++){
				for(int iC = 0; iC < ((GridsAndBranches)gnbModule).getNumCols(); iC++){
					g.setColor(Color.gray);
					g.drawRect(startX, startY, boxWidth, boxHeight);
					g.setColor(Color.black);
					String thresholdString = ((GridsAndBranches)gnbModule).getNumNodeTask()[iR][iC].getThreshold().toString();
					g.drawString(thresholdString, StringUtil.getStringCenterPosition(thresholdString, g, startX, boxWidth, null), StringUtil.getStringVertPosition(g, startY, boxHeight, null));
					dropDownTriangle.translate((startX + boxWidth-6), (startY+1));
					g.setColor(Color.white);
					g.drawPolygon(dropDownTriangle);
					g.setColor(Color.black);
					g.fillPolygon(dropDownTriangle);
					dropDownTriangle.translate(-(startX + boxWidth-6), -(startY+1));
					legendBoxInfo[iR][iC] = new BoxDimensions(startX, startX+boxWidth, startY, startY+boxHeight);
					startX += boxWidth;
				}
				startX = 4;
				startY += boxHeight;
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*..................................................................*/
	/*For menu operations of legend*/
	public void boxTouched(int whichR, int whichC, int whereX, int whereY){
		((GridsAndBranches)gnbModule).getNumNodeTask()[whichR][whichC].showPopUp(this, whereX, whereY);
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
				while(iR < ((GridsAndBranches)gnbModule).getNumRows() && whichRow < 0){
					if(y < legendBoxInfo[iR][iC].getBottom() && y > legendBoxInfo[iR][iC].getTop()){
						whichRow = iR;
					}
					iR++;
				}
				iR = 0;
				while(iC < ((GridsAndBranches)gnbModule).getNumCols() && whichColumn < 0){
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
	/*..................................................................*/
	public void adjustColors(Color[] colorArray){
		if(colorArray.length == 3){
			branchColors = colorArray;
			repaint();//TODO: method does not repaint legend when colors are changed (legend colors are updated when legend is moved).
		}
	}
}

/*======================================================================================*/
/**A class to contain positional information for legend boxes*/
class BoxDimensions{
	private int left, right, top, bottom;
	/*..................................................................*/
	public BoxDimensions(int left, int right, int top, int bottom){
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
}