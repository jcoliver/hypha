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
package mesquite.hypha.lib;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/**Provides a superclass for NumberForNode modules which require information from trees other than 
 * the one that the node belongs to.  Modeled after NForTaxonWithTrees. */
public abstract class NumForNodeAndOtherTree extends NumberForNode {
	TreeSource otherTreeTask;
	MesquiteString treeSourceName;
	MesquiteCommand cstC;
	MesquiteNumber result;
	MesquiteString resultString;
	protected boolean needsRecalculation;
	protected Taxa currentTaxa;
	int currentTree = 0;
	MesquiteInteger doCommandPos = new MesquiteInteger(0); //For doCommand navigation

	/*.................................................................................................................*/
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a tree source.",
		"The tree source can be selected initially or in the Tree Source submenu");
	}
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		otherTreeTask = (TreeSource)hireEmployee(TreeSource.class, "Tree source for " + getName());
		if(otherTreeTask==null)
			return sorry(getName() + " couldn't start because no source of tree obtained");
		treeSourceName = new MesquiteString(otherTreeTask.getName());
		cstC =  makeCommand("setTreeSource",  this);
		otherTreeTask.setHiringCommand(cstC);
		if (numModulesAvailable(TreeSource.class)>1){ 
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source (for " + getName() + ")", cstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
		
		if (getHiredAs() == NumberForNode.class){ //not hired as incrementable; offer menu item to change
			addMenuItem("Choose tree...", makeCommand("chooseTree",  this));
		}
		
		result = new MesquiteNumber();
		resultString = new MesquiteString();
		needsRecalculation = MesquiteThread.isScripting();
		return true;  
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m!=otherTreeTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee != otherTreeTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
			needsRecalculation = true;
			if (!MesquiteThread.isScripting())
				super.employeeParametersChanged(employee, source, notification);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeSource " , otherTreeTask);
		if(getHiredAs() == NumberForNode.class)
			temp.addLine("setTreeNumber " + currentTree);
		temp.addLine("doCalc");//TODO delete?
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the source of trees for comparison", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp =  (TreeSource)replaceEmployee(TreeSource.class, arguments, "Tree source for " + getName(), otherTreeTask);
				if (temp!=null) {
					otherTreeTask = temp;
					otherTreeTask.setHiringCommand(cstC);
					treeSourceName.setValue(otherTreeTask.getName());
					otherTreeTask.initialize(currentTaxa);
					if (!MesquiteThread.isScripting()) {
						needsRecalculation = true;
						parametersChanged();
					}
				}
				return temp;
		}
		else if (checker.compare(this.getClass(), "Present a dialog box to choose a tree from the current tree source", null, commandName, "chooseTree")) {
			int ic=otherTreeTask.queryUserChoose(currentTaxa, "for " + getName());
			if (MesquiteInteger.isCombinable(ic)) {
				currentTree = ic;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the tree to be the i'th one from the current tree source", "[number of tree to be used]", commandName, "setTreeNumber")) {
			int ic = MesquiteInteger.fromFirstToken(arguments, doCommandPos);
			if (MesquiteInteger.isCombinable(ic)) {
				currentTree = ic;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Requests calculations", null, commandName, "doCalc")) {
			needsRecalculation = true;
			parametersChanged();
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public abstract void calculateNumber(Tree tree, Tree otherTree, int node, MesquiteNumber result, MesquiteString resultString);
	
	/*.................................................................................................................*/
	/**Called by employer of NumberForNode; in turn calls the abstract calculateNumber method above, passing two trees.*/
	public void calculateNumber(Tree tree, int node, MesquiteNumber result,	MesquiteString resultString) {
		if(tree==null || result==null || otherTreeTask == null)
			return;
		currentTaxa = tree.getTaxa();
		calculateNumber(tree, otherTreeTask.getTree(currentTaxa ,currentTree), node, result, resultString);
	}
	/*.................................................................................................................*/
	/**Returns the "other" tree used for calculations
	 * */
	public Tree getOtherTree() {
		if (otherTreeTask == null || currentTaxa == null || !MesquiteInteger.isCombinable(currentTree)) {
			return null;
		} else {
			return otherTreeTask.getTree(currentTaxa, currentTree);
		}
	}
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 *  happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree) {
		currentTaxa = tree.getTaxa();
		needsRecalculation = true;
		otherTreeTask.initialize(currentTaxa);
	}
}