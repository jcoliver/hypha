package mesquite.hypha.ThresholdBranchColorer;

import java.awt.*;
import java.util.*;
import mesquite.hypha.NumForNodeWithThreshold.*;
import mesquite.hypha.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class ThresholdBranchColorer extends NoduleCoordinator {
	MesquiteSubmenuSpec termMenuItem;
	private NumForNodeWithThreshold[] numForNodeTask;
	private int numTrees = MesquiteInteger.unassigned;
	static int maxTrees = 9;
	MesquiteInteger pos = new MesquiteInteger(0); //For doCommand navigation
	NoduleOperator TBCOperator;
	private Color terminalBranchColor = Color.black;
	protected MesquiteString termColorName;
	
	public boolean startJob(String arguments, Object condition,	boolean hiredByName) {
		nodeDecor = new Vector();
		makeMenu("TBC");
		addMenuItem("Threshold Branch Colorer", null);
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

		if(!MesquiteThread.isScripting()){ //Conditional to prevent querying user on opening of file
			if(!MesquiteInteger.isCombinable(numTrees)){
				int newTrees = 1;
				String helpString = "Enter the number of trees you would like to use for the Threshold Branch Coordinator (minimum = 1, maximum = 9).  This module will color branches of the currently displayed tree based on values from other trees.";
				newTrees = MesquiteInteger.queryInteger(containerOfModule(), "Threshold Branch Colorer Setup", "Number of Trees", helpString, newTrees, 1, 9);
				if(newTrees < 10 && newTrees > 0){
					numTrees = newTrees;
				}
				else return sorry(getName() + " couldn't start because the number of trees entered was not within the acceptable range.");
			}
			if(MesquiteInteger.isCombinable(numTrees)){
				numForNodeTask = new NumForNodeWithThreshold[numTrees];
				for(int iT = 0; iT < numTrees; iT++){
					numForNodeTask[iT] = (NumForNodeWithThreshold)hireNamedEmployee(NumberForNode.class, "#NumForNodeWithThreshold");
					if(numForNodeTask[iT]==null)
						return sorry(getName() + " couldn't find a suitable tree for tree number " + (iT+1) + ".");
				}
			}
		}

		addMenuItem("Close TBC", makeCommand("close",  this));
		addMenuItem("-", null);
		resetContainingMenuBar();
		return true;
	}
	/*..................................................................*/
	/**Performs commands based on the String commandName it is passed.  Mostly used for restoring
	 * objects/numbers upon opening a file which employs this module.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if(checker.compareStart(this.getClass(), "Sets module used to calculate value for node", "[name of module]", commandName, "setNumForNode")){
			String disposable = ParseUtil.getFirstToken(commandName, pos); //A string used only to move the parser position
			int tempTree = MesquiteInteger.fromString((ParseUtil.getToken(commandName, pos)));
			NumForNodeWithThreshold temp = (NumForNodeWithThreshold)replaceEmployee(NumForNodeWithThreshold.class, arguments, "Number for Node with threshold", numForNodeTask[tempTree]);
			if(temp!=null){
				numForNodeTask[tempTree] = temp;
				return numForNodeTask[tempTree];
			}
		}
		else if(checker.compare(this.getClass(), "Number of Trees", "[number]", commandName, "setNumTrees")){
			MesquiteInteger tempTrees = new MesquiteInteger(MesquiteInteger.fromFirstToken(arguments, pos));
			if(!tempTrees.isCombinable()){
				String helpString = "Enter the number of trees you would like to use for the Threshold Branch Coordinator (minimum = 1, maximum = 9).  This module will color branches of the currently displayed tree based on values from other trees.";
				MesquiteInteger.queryInteger(containerOfModule(), "Threshold Branch Colorer Setup", "Number of Trees", helpString, tempTrees.getValue(), 1, 9, true);
			}
			if(tempTrees.isCombinable())
				numTrees = tempTrees.getValue();
			else numTrees = 1;
			numForNodeTask = new NumForNodeWithThreshold[numTrees];
		}

		else if (checker.compare(this.getClass(), "Set color of branches for values equal to or greater than threshold.", "[name of color]", commandName, "setAboveT")) {
			Color atc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
			if (atc == null)
				return null;
			aboveThreshColor = atc;
			atColorName.setValue(atc.toString());
			MesquiteString aName = new MesquiteString(ColorDistribution.getStandardColorName(aboveThreshColor));
			if(aName!=null){
				aboveTMenuItem.setSelected(aName);
				resetContainingMenuBar();
				redraw();
			}			
		}
		else if (checker.compare(this.getClass(), "Set color of branches for values less than threshold.", "[name of color]", commandName, "setBelowT")) {
	 		Color btc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (btc == null)
	 			return null;
	 		belowThreshColor = btc;
			btColorName.setValue(btc.toString());
			MesquiteString bName = new MesquiteString(ColorDistribution.getStandardColorName(belowThreshColor));
			if(bName!=null){
				belowTMenuItem.setSelected(bName);
				resetContainingMenuBar();
				redraw();
			}
	 	}
	 	else if (checker.compare(this.getClass(), "Set color of branches for missing or inapplicable values.", "[name of color]", commandName, "setInApp")) {
	 		Color iac = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
	 		if (iac == null)
	 			return null;
	 		inAppColor = iac;
	 		inAColorName.setValue(iac.toString());
			MesquiteString inAName = new MesquiteString(ColorDistribution.getStandardColorName(inAppColor));
			if(inAName!=null){
				inAMenuItem.setSelected(inAName);
				resetContainingMenuBar();
				redraw();
			}
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
	 	else if (checker.compare(this.getClass(), "Turn off the Threshold Branch Colorer", null, commandName, "close")) {
			iQuit();
			resetContainingMenuBar();
		}
	 	else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				Enumeration e = nodeDecor.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TBCOperator) {
						TBCOperator nGO = (TBCOperator)obj;
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
				Enumeration e = nodeDecor.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TBCOperator) {
						TBCOperator nGO = (TBCOperator)obj;
						if (nGO.legend!=null)
							nGO.legend.setOffsetY(offset);
					}
				}
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*..................................................................*/
	/**Writes parameters of this module to NEXUS file in MESQUITE block*/
	public Snapshot getSnapshot(MesquiteFile file){
		Snapshot temp = new Snapshot();
		temp.addLine("setNumTrees " + numTrees);
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
		for(int iT = 0; iT < numTrees; iT++){
			temp.addLine("setNumForNode_" + iT, numForNodeTask[iT]);
		}
		TBCOperator tbco = (TBCOperator)nodeDecor.elementAt(0);
		if (tbco!=null && tbco.legend!=null) {
			temp.addLine("setInitialOffsetX " + tbco.legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tbco.legend.getOffsetY());
		}
		return temp;
	}
	/*..................................................................*/
	/**Creates a TreeDisplayExtra, which will do the actual drawing on the tree.  Called by this
	 * module's employer (usually BasicTreeWindowMaker)*/
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TBCOperator newGrid = new TBCOperator(this, treeDisplay);
		nodeDecor.addElement(newGrid);
		return newGrid;
	}
	/*..................................................................*/
	public void closeAllNodeOperators() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof TBCOperator){
				TBCOperator tbcO = (TBCOperator)obj;
				tbcO.turnOff();
			}
		}
	}
	/*..................................................................*/
	public void redraw() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof TBCOperator){
				TBCOperator tbcO = (TBCOperator)obj;
				tbcO.getTreeDisplay().repaint();
				if(tbcO.legend!=null){
					tbcO.legend.adjustColors(getBranchColors());
				}
			}
		}	
	}
	/*..................................................................*/
	/**Returns array of colors corresponding to the colors reflecting values which are above the threshold,
	 * below the threshold, or missing or inapplicable (in that order in the array returned)*/
	public Color[] getBranchColors(){
		Color[] branchColors = {aboveThreshColor, belowThreshColor, inAppColor};
		return branchColors;
	}
	/*..................................................................*/
	/**Returns array of state names.*/
	public String[] getStateNames(){
		String[] stateNames = {"Above Threshold", "Below Threshold", "Missing or Inapplicable"};
		return stateNames;
	}
	/*..................................................................*/
	public NumForNodeWithThreshold[] getNumTask(){
		return numForNodeTask;
	}
	public int getNumTrees(){
		return numTrees;
	}
	/*..................................................................*/
	public Color getTerminalBranchColor(){
		return terminalBranchColor;
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
		return "Threshold Branch Colorer";
	}
}
/*..................................................................*/
/* ================================================================ */
class TBCOperator extends NoduleOperator{
	private ThresholdBranchColorer tbcModule;
	private Tree tree = treeDisplay.getTree();
	private int numTrees;
	private MesquiteNumber[] thresholds;
	TBCLegend legend;
	MesquiteNumber result;
	MesquiteString resultString;

	/**Constructor method for TBCOperator*/
	public TBCOperator(ThresholdBranchColorer ownerModule, TreeDisplay treeDisplay){
		super(ownerModule, treeDisplay);
		tbcModule = ownerModule;
	}
	/*..................................................................*/
	/**Calls the NumberForNode calculateNumber method for a particular node of tree, using the node value from otherTree*/
	private MesquiteNumber doCalculations(int node, int otherTree){
		if(tree.nodeExists(node)){
			if(result==null){
				result = new MesquiteNumber();
				result.setToUnassigned();
			} else result.setToUnassigned();
			if(resultString==null)
				resultString = new MesquiteString("");
			else resultString.setValue("");
			tbcModule.getNumTask()[otherTree].calculateNumber(tree, node, result, resultString);
		}
		else {
			result.setToUnassigned();
		}
		return result;
	}
	/*..................................................................*/
	public void drawOnBranch(Tree tree, int node, Graphics g) {
		Color inApp = tbcModule.getInAppColor();
		Color belowThresh = tbcModule.getBelowThreshColor();
		Color aboveThresh = tbcModule.getAboveThreshColor();
		Color terminalColor = tbcModule.getTerminalBranchColor();
		Color oC = g.getColor();
		Color branchColor = Color.BLACK;
		if(tbcModule!=null){
			if(tbcModule.getNumTask()!=null){//This conditional may be unnecessary
				//Recurses through tree
				for(int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
					if(tree.nodeIsTerminal(d)){
						g.setColor(terminalColor);
						treeDisplay.getTreeDrawing().fillBranch(tree, d, g);
						g.setColor(oC);
					}
					else {
						drawOnBranch(tree, d, g);
						int iT = 0;
						branchColor = aboveThresh;
						//Searches through trees for node and assigns branch color
						while(iT < numTrees && branchColor != inApp){
							MesquiteNumber nodeValue = new MesquiteNumber(this.doCalculations(d, iT));
							if(!nodeValue.isCombinable()){
								branchColor = inApp;
							}
							else if(branchColor != belowThresh){
								if(nodeValue.isLessThan(thresholds[iT]))
									branchColor = belowThresh;
							}
							iT++;
						}
						g.setColor(branchColor);
						treeDisplay.getTreeDrawing().fillBranch(tree, d, g);
						g.setColor(oC);
					}
				}
			}
		}
	}
	/*..................................................................*/
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if(tbcModule!=null){
			numTrees = tbcModule.getNumTrees();
			thresholds = new MesquiteNumber[numTrees];
			for(int iT = 0; iT < numTrees; iT++){
				thresholds[iT] = tbcModule.getNumTask()[iT].getThreshold();
			}
			drawOnBranch(tree, tree.getRoot(), g);
			if(legend==null){
				legend = new TBCLegend(tbcModule, treeDisplay, "Threshold Branch Colorer", Color.white, tbcModule.getBranchColors());
				addPanelPlease(legend);
				legend.setVisible(true);
			}
			if(legend!=null)
				legend.adjustLocation();
		}
	}
	/*..................................................................*/
	/**Turns off the TBCOperator & TBCLegend.*/
	public void turnOff(){
		if (legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}
/*..................................................................*/
/* ================================================================ */
class TBCLegend extends NoduleLegend{
	private LegendHolder tbcModule;
	private Color titleColor;
	private Color[] branchColors;

	/**Constructor method for TBCLegend*/
	public TBCLegend(LegendHolder tbcModule, TreeDisplay treeDisplay, String title, Color titleColor, Color[] branchColors){
		super(tbcModule, treeDisplay);
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.branchColors = branchColors;
		legendWidth=defaultLegendHeight;
		legendHeight=defaultLegendHeight;
	}
	/*..................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		/*Draws Legend box:*/
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
		/*Fills legend box with color info*/
		String[] stateNames = {"Above Threshold", "Below Threshold", "Missing or Inapplicable"};
		for (int ibox=0; ibox<stateNames.length; ibox++) {
			g.setColor(branchColors[ibox]);
			g.fillRect(4, ibox*16 + 26, 22, 4);
			g.setColor(Color.black);
			g.drawRect(4, ibox*16 + 26, 22, 4);
			if (stateNames[ibox]!=null)
				g.drawString(stateNames[ibox], 28, ibox*16 + 32);//TODO: dimensions questionable
		}
	}
	/*..................................................................*/
	public void adjustColors(Color[] branchC){
		if(branchC.length == 3){
			branchColors = branchC;
			repaint();
		}
	}
}