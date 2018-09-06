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
package mesquite.hypha.NumForNodeWithThreshold;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/**Hires a Number For Node module and calls the corresponding calculateNumber method; provided
 * as a means to hold a number for node with some associated (threshold) value.  Originally 
 * developed for GridForNode module.*/
public class NumForNodeWithThreshold extends NumberForNode {
	NumberForNode numNodeTask;
	private MesquiteNumber threshold;
	private int sigFigs = 2;

/*.................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if(!MesquiteThread.isScripting()){
			numNodeTask = (NumberForNode)hireEmployee(NumberForNode.class, "Number for Node of Tree");
			if(numNodeTask==null)
				return sorry(getName() + " couldn't find a suitable Number for Node Module.");
			else{
				MesquiteNumber newThresh = new MesquiteNumber(1.0);
				threshold = new MesquiteNumber();
				newThresh = MesquiteNumber.queryNumber(containerOfModule(), "Threshold Value", "Enter new threshold value:", newThresh);
				if(newThresh.isCombinable()){
					threshold=newThresh;
				}
			}
		}
		addMenuItem("Set Threshold...", makeCommand("setThresh", this));
		addMenuItem("Set Significant Digits...", makeCommand("setSigFigs", this));
		addMenuItem("Set Number For Node...", makeCommand("setNumForNode", this));
		return true;
	}
	/*.................................................................*/
	public void calculateNumber(Tree tree, int node, MesquiteNumber result,	MesquiteString resultString) {
	   	clearResultAndLastResult(result);
		if(numNodeTask!=null){
			numNodeTask.calculateNumber(tree, node, result, resultString);
			resultString.setValue("Result = " + result.toString());
		}
		else {
			result.setToUnassigned();
			resultString.setValue("Could not calculate number for " + getName());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................*/
	public void initialize(Tree tree) {}
	/*.................................................................*/
	public Snapshot getSnapshot(MesquiteFile file){
		Snapshot temp = new Snapshot();
		temp.addLine("setNumForNode", numNodeTask);
		temp.addLine("setThresh " + threshold);
		temp.addLine("setSigFigs " + sigFigs);
		return temp;
	}
	/*.................................................................*/
	/**Performs commands based on the String commandName it is passed.  Mostly used for restoring
	 * objects/numbers upon opening a file which employs this module.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if(checker.compare(this.getClass(), "Sets module used to calculate value for node", "[name of module]", commandName, "setNumForNode")){
			NumberForNode temp = (NumberForNode)replaceEmployee(NumberForNode.class, arguments, "Number for Node of Tree", numNodeTask);
			if(temp!=null){
				if(numNodeTask!=null){
					if(!numNodeTask.equals(temp)){
						numNodeTask = temp;
						parametersChanged();
					}
				}
				else numNodeTask = temp;
//				numNodeTask.setUseMenubar(false);
				return numNodeTask;
			}
		}
		else if(checker.compare(this.getClass(), "Sets threshold value for " + getName(), "[number]", commandName, "setThresh")){
			MesquiteNumber tempThresh = new MesquiteNumber();
			tempThresh.setValue(arguments);
			if(!tempThresh.isCombinable()){
				tempThresh = MesquiteNumber.queryNumber(containerOfModule(), "Threshold Value", "Enter new threshold value:", threshold);
			}
			if(tempThresh.isCombinable()){
				if(threshold!=null){
					if(!threshold.equals(tempThresh)){
						threshold=tempThresh;
						parametersChanged();
					}
				}
				else threshold=tempThresh;
			}
		}
		else if(checker.compare(this.getClass(), "Sets significant digits to display for " + getName(), "[number]", commandName, "setSigFigs")){
			MesquiteInteger pos = new MesquiteInteger();
			int newSigFigs = MesquiteInteger.fromFirstToken(arguments, pos);
			if(!MesquiteInteger.isCombinable(newSigFigs)){
				newSigFigs = MesquiteInteger.queryInteger(containerOfModule(), "Significant Digits", "Enter number of significant digits to display:", sigFigs, 0, 5);
			}
			if(MesquiteInteger.isCombinable(newSigFigs) && newSigFigs!=sigFigs){
				sigFigs = newSigFigs;
				parametersChanged();
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................*/
	public String getName() {
		return "Number for Node with Threshold";
	}
	/*.................................................................*/
	public String getExplanation(){
		return "A module providing a number for node and an associated threshold value.";
	}
	/*.................................................................*/
	public boolean requestPrimaryChoice(){
		return false;
	}
	/*.................................................................*/
	public MesquiteNumber getThreshold() {
		return threshold;
	}
	/*.................................................................*/
	public void setThreshold(MesquiteNumber threshold) {
		this.threshold = threshold;
	}
	/*.................................................................*/
	public int getSigFigs(){
		return sigFigs;
	}

}
