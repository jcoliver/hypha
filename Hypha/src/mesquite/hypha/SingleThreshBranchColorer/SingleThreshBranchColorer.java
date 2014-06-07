package mesquite.hypha.SingleThreshBranchColorer;

import java.awt.*;
import java.util.*;
import mesquite.hypha.NumForNodeWithThreshold.*;
import mesquite.hypha.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/**An early incarnation of Threshold Branch Colorer, which only colors branches based on 
 * support values from a single tree.  Will become obsolete eventually, replaced by 
 * ThresholdBranchColorer.*/
public class SingleThreshBranchColorer extends NoduleCoordinator {
	MesquiteSubmenuSpec aboveTMenuItem, belowTMenuItem, inAMenuItem, termMenuItem;
	private NumForNodeWithThreshold numForNodeTask;
	MesquiteInteger pos = new MesquiteInteger(0); //For doCommand navigation
	MesquiteCommand nfntC;
	NoduleOperator STBCOperator;
	protected Color terminalBranchColor = Color.black;
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

		if(!MesquiteThread.isScripting()){//Conditional to prevent querying user on opening of file
			numForNodeTask = (NumForNodeWithThreshold)hireNamedEmployee(NumberForNode.class, "#NumForNodeWithThreshold");
			if (numForNodeTask == null) {
				return sorry(getName() + " cannot start because no appropriate module to calculate values was obtained.");
			}
			nfntC = makeCommand("setNumForNode", this);
			numForNodeTask.setHiringCommand(nfntC);
		}			
		addMenuItem("Close TBC", makeCommand("close",  this));
		resetContainingMenuBar();
		return true;
	}
	/*..................................................................*/
	/**Performs commands based on the String commandName it is passed.  Mostly used for restoring
	 * objects/numbers upon opening a file which employs this module.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if(checker.compare(this.getClass(), "Sets module used to calculate value for node", "[name of module]", commandName, "setNumForNode")){
			NumForNodeWithThreshold temp = (NumForNodeWithThreshold)replaceEmployee(NumForNodeWithThreshold.class, arguments, "Number for Node with threshold", numForNodeTask);
			if(temp!=null){
				numForNodeTask = temp;
				numForNodeTask.setHiringCommand(nfntC);
			}
			return numForNodeTask;
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
					if (obj instanceof STBCOperator) {
						STBCOperator nGO = (STBCOperator)obj;
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
					if (obj instanceof STBCOperator) {
						STBCOperator nGO = (STBCOperator)obj;
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
		temp.addLine("setNumForNode", numForNodeTask);
		STBCOperator tbco = (STBCOperator)nodeDecor.elementAt(0);
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
		STBCOperator newGrid = new STBCOperator(this, treeDisplay);
		nodeDecor.addElement(newGrid);
		return newGrid;
	}
	/*..................................................................*/
	public void closeAllNodeOperators() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof STBCOperator){
				STBCOperator tbcO = (STBCOperator)obj;
				tbcO.turnOff();
			}
		}
	}
	/*..................................................................*/
	public void redraw() {
		Enumeration e = nodeDecor.elements();
		while (e.hasMoreElements()){
			Object obj = e.nextElement();
			if (obj instanceof STBCOperator){
				STBCOperator tbcO = (STBCOperator)obj;
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
	public NumForNodeWithThreshold getNumTask(){
		return numForNodeTask;
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
		return "Single Threshold Branch Colorer";
	}
}
/*..................................................................*/
/* ================================================================ */
class STBCOperator extends NoduleOperator{
	private SingleThreshBranchColorer tbcModule;
	private Tree tree = treeDisplay.getTree();
	STBCLegend legend;
	MesquiteNumber result;
	MesquiteString resultString;

	/**Constructor method for STBCOperator*/
	public STBCOperator(SingleThreshBranchColorer ownerModule, TreeDisplay treeDisplay){
		super(ownerModule, treeDisplay);
		tbcModule = ownerModule;
	}
	/*..................................................................*/
	private MesquiteNumber doCalculations(int node){
		if(tree.nodeExists(node)){
			if(result==null){
				result = new MesquiteNumber();
				result.setToUnassigned();
			} else result.setToUnassigned();
			if(resultString==null)
				resultString = new MesquiteString("");
			else resultString.setValue("");
			tbcModule.getNumTask().calculateNumber(tree, node, result, resultString);
		}
		else {
			result.setToUnassigned();
		}
		return result;
	}
	/*..................................................................*/
	public void drawOnBranch(Tree tree, int node, Graphics g) {
		Color oC = g.getColor();
		Color branchColor = Color.BLACK;
		if(tbcModule!=null){
			if(tbcModule.getNumTask()!=null){//This conditional may be unnecessary
				MesquiteNumber threshold = new MesquiteNumber(tbcModule.getNumTask().getThreshold());
				MesquiteNumber nodeValue = new MesquiteNumber(this.doCalculations(node));
				if(nodeValue.isCombinable()){
					if(nodeValue.isLessThan(threshold))
						branchColor = tbcModule.getBelowThreshColor();
					else branchColor = tbcModule.getAboveThreshColor();
				} else branchColor = tbcModule.getInAppColor();
				//Recurses through tree
				for(int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
					drawOnBranch(tree, d, g);
					if(tree.nodeIsTerminal(d)){
						g.setColor(tbcModule.getTerminalBranchColor());
					}
					else g.setColor(branchColor);
					treeDisplay.getTreeDrawing().fillBranch(tree, d, g);
					g.setColor(oC);
				}
			}
		}
	}
	/*..................................................................*/
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnBranch(tree, tree.getRoot(), g);
		if(legend==null){
			legend = new STBCLegend(tbcModule, treeDisplay, "ThresholdBranchColorer Legend", Color.white, tbcModule.getBranchColors());
			addPanelPlease(legend);
			legend.setVisible(true);
		}
		if(legend!=null)
			legend.adjustLocation();
	}
	/**Turns off the STBCOperator & TBCLegend.*/
	public void turnOff(){
		if (legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}
/*..................................................................*/
/* ================================================================ */
class STBCLegend extends NoduleLegend{
	private LegendHolder tbcModule;
	private Color titleColor;
	private Color[] branchColors;

	/**Constructor method for TBCLegend*/
	public STBCLegend(LegendHolder tbcModule, TreeDisplay treeDisplay, String title, Color titleColor, Color[] branchColors){
		super(tbcModule, treeDisplay);
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.branchColors = branchColors;
		legendWidth=defaultLegendWidth;
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
		String[] stateNames = {"Above Threshold", "Below Threshold", "Missing or Inapplicable"};//TODO: hardcoded?
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