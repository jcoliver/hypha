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
package mesquite.hypha.BranchLengthFromOtherTree;

import mesquite.hypha.lib.NumForNodeAndOtherTree;
import mesquite.lib.*;

/*TODO: Conflict coloring untested.*/

/**A module providing branch length from one tree as a NumberForNode of another tree.  Primarily to be 
 * used to report support values saved as branch lengths (e.g. bootstrap proportions from PAUP) onto 
 * another tree.*/
public class BranchLengthFromOtherTree extends NumForNodeAndOtherTree {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/**returns a MesquiteTree object based on Tree tree, which has been pruned of any
	 * taxa that are not present in Tree otherTree.*/
	private MesquiteTree pruneToMatch(Tree tree, Tree otherTree){
		MesquiteTree mTree = new MesquiteTree(tree.getTaxa());
		mTree = (MesquiteTree)tree.cloneTree();
		int[] mTreeTerms = new int[mTree.numberOfTerminalsInClade(mTree.getRoot())];
		mTreeTerms = mTree.getTerminalTaxa(mTree.getRoot()); //Stores taxon number of terminal nodes
		for(int it = 0; it < mTreeTerms.length; it++){
			if(!otherTree.taxonInTree(mTreeTerms[it]))
				mTree.deleteClade(mTree.nodeOfTaxonNumber(mTreeTerms[it]), false);
		}
		return mTree;
	}
	/**Compares a node in a Tree to a MesquiteTree (in this case a pruned version of Tree) to a Tree, 
	 * and returns true if at least one taxa from each of the daughter nodes is present in the MesquiteTree.
	 * Before calling, should make sure the node passed exists and is internal.*/
	private boolean descendantCheck(Tree tree, MesquiteTree mTree, int node){
		boolean check = true;
		int numDaughters = tree.numberOfDaughtersOfNode(node);
		/*checkD is an array storing booleans for each daughter of node; elements for those daughter lineages 
		 * represented by at least 1 terminal taxon in the MesquiteTree mTree should = true.  If a daughter
		 * lineage is not represented in the mTree, corresponding array element should = false.*/
		boolean[] checkD = new boolean[numDaughters];
		if(tree.nodeIsInternal(node)){
			/*dCount is a counter so the elements of the checkD array can be accessed correctly.*/
			int dCount = 0;
			for(int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				if(tree.nodeIsInternal(d)){
					int numDaughterTerms = tree.numberOfTerminalsInClade(d);
					int[] dTerms = new int[numDaughterTerms];
					dTerms = tree.getTerminalTaxa(d); //Taxon numbers are passed NOT node numbers
					int termCount = 0;
					while(!checkD[dCount] && termCount<numDaughterTerms){
						if(mTree.taxonInTree(dTerms[termCount])){
							checkD[dCount]=true;
						}
						termCount++;
					}
					if(!checkD[dCount]){
						return false;
					}
				}
				else if(mTree.taxonInTree(tree.taxonNumberOfNode(d))){
					checkD[dCount] = true;
					} else return false;
				dCount++;
			}
		}
		int daughterCount = 0;
		while(check && daughterCount<numDaughters){
			if(!checkD[daughterCount]){
				check=false;
			}
			daughterCount++;
		}
		return check;
	}
	/*.................................................................................................................*/
	private int[] getInternalDescendants(int node, Tree tree, int[] nodeList, MesquiteInteger count){
		for(int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			if(tree.nodeIsInternal(d)){
				nodeList = getInternalDescendants(d,tree,nodeList,count);
			}
		}
		if(tree.nodeIsInternal(node) && tree.nodeExists(node)){
			nodeList[count.getValue()] = node;
			count.increment();
		}
		return nodeList;
	}
	/*.................................................................................................................*/
	/**Finds the most recent common ancestor in MesquiteTree otherTree, for terminal descendents of node 
	 * (which is in Tree tree), and if mrca exists, returns the corresponding branch length of the
	 * mrca in otherTree.  mrca determination ignores taxa that are in tree but absent in otherTree 
	 * (because the argument ignoreMissing passed to MesquiteTree.mrcaTaxons is set to true).*/
	private MesquiteNumber getValue(Tree focalTree, Tree otherTree, int node){
		/*Mostly pasted from BranchLabelFromOtherTree, with getNodeLabel replaced with getBranchLength*/		
		MesquiteNumber mNum = new MesquiteNumber();
		mNum.setToUnassigned();
		int[] terminalsInFocalTree = new int[focalTree.numberOfTerminalsInClade(node)];
		terminalsInFocalTree = focalTree.getTerminalTaxa(node); // Returns taxon number, not node number
		int mrca = otherTree.mrcaTaxons(terminalsInFocalTree, true);
		int dCount = 0;
		if(mrca!=0){
			/**All the descendants of mrca in otherTree*/
			int[] terminalsInOtherTree = new int[otherTree.numberOfTerminalsInClade(mrca)];
			terminalsInOtherTree = otherTree.getTerminalTaxa(mrca); // Returns taxon number, not node number
			double largestConflict = 0.0;
			boolean conflict = false;
			//Check all terminals or until the first conflicting taxon (present in both trees, but not a descendant of node in focalTree). 
			while(dCount < terminalsInOtherTree.length && !conflict){
				//Terminal taxon is in the focal tree...
				if(focalTree.taxonInTree(terminalsInOtherTree[dCount])){
					//...but is not included in current clade descendant from node.  Find largest conflicting support value in relevant branches.
					if(!focalTree.descendantOf(focalTree.nodeOfTaxonNumber(terminalsInOtherTree[dCount]), node)){
						conflict = true;
					}
				}
				dCount++;
			}
			if(conflict){
				MesquiteInteger count = new MesquiteInteger(0);
				int[] internalNodesOfMRCA = new int[otherTree.numberOfInternalsInClade(mrca)]; //Array will hold mrca & all descendant internal nodes
				internalNodesOfMRCA = getInternalDescendants(mrca, otherTree, internalNodesOfMRCA,count);
				/*Now we have a list of all internal descendant nodes.  Want to check:
				 * 1. is the branch length higher than largest conflict?  If not, do nothing.
				 * 2. If so, in the set of terminal taxa, is there one from the clade defined by node? If not, do nothing.
				 * 3. If so, in the set of terminal taxa, is there one NOT in the clade defined by node, but still in tree?  If not, do nothing.
				 * 4. If so, the branch is relevant.  Assign new largestConflict.*/
				for(int internal = 0; internal < internalNodesOfMRCA.length; internal++){
					int currentInternalOfOtherTree = internalNodesOfMRCA[internal];
					if(internalNodesOfMRCA[internal] != mrca){ // Don't need to do comparison with mrca
						if(MesquiteDouble.isCombinable(otherTree.getBranchLength(currentInternalOfOtherTree))){ //node has a combinable length
							MesquiteDouble currentConflict = new MesquiteDouble();
							currentConflict.setValue(otherTree.getBranchLength(currentInternalOfOtherTree)); //get the branch length as double
							if(largestConflict < currentConflict.getValue()){
								int[] currentTerminals = new int[otherTree.numberOfTerminalsInClade(currentInternalOfOtherTree)];
								currentTerminals = otherTree.getTerminalTaxa(currentInternalOfOtherTree);
								int termCount = 0;
								boolean atLeastOneIncl = false; //At least one node in clade defined by node in focalTree
								boolean atLeastOneExcl = false; //At least one node NOT in clade defined by node in focalTree (but is somewhere else in focalTree)
								//Don't want to look at terminalsInOtherTree, just want terminal descendants of the current internal node (internalNodesOfMRCA[internal])
								while(termCount < currentTerminals.length && !(atLeastOneIncl && atLeastOneExcl)){
									if(focalTree.descendantOf(focalTree.nodeOfTaxonNumber(currentTerminals[termCount]), node)){
										atLeastOneIncl = true;
									} else {
										if(focalTree.nodeExists(focalTree.nodeOfTaxonNumber(currentTerminals[termCount]))){
											atLeastOneExcl = true;
										}
									}
									termCount++;
								}
								if(atLeastOneIncl && atLeastOneExcl){
									largestConflict = currentConflict.getValue();
								}
							}
							currentConflict = null;
						}
					}
				}
				count = null;
				internalNodesOfMRCA = null;
			}
			
			if(!conflict){// No conflict, just report support value
				double branchLength = otherTree.getBranchLength(mrca);
				if(MesquiteDouble.isCombinable(branchLength))
					mNum.setValue(branchLength);
			}
			else {
				// There's conflict, so the largest conflicting value is returned as a negative value
				if(largestConflict > 0.0){
					mNum.setValue(-1 * largestConflict);
				} else mNum.setValue(0.0); //if largestConflict == 0.0, it shouldn't be counted as conflicting. 
			}
			terminalsInOtherTree = null;
		}
		terminalsInFocalTree = null;
		return mNum;
	}
	/*.................................................................................................................*/
	/* Earlier version of getValue; no conflict coloring:
	private MesquiteNumber getValue(Tree Tree, Tree otherTree, int node){
		MesquiteNumber mNum = new MesquiteNumber();
		mNum.setToUnassigned();
		int numDesTerms = tree.numberOfTerminalsInClade(node); //Number of descendent terminal taxa, for following array:
		int[] desTerms = new int[numDesTerms];
		desTerms = tree.getTerminalTaxa(node);
		int mrca = otherTree.mrcaTaxons(desTerms, true);
		boolean allIncluded=true;
		int dCount = 0;
		if(mrca!=0){
			int[] mrcaDes = new int[otherTree.numberOfTerminalsInClade(mrca)];
			mrcaDes = otherTree.getTerminalTaxa(mrca);

			while(allIncluded && dCount < mrcaDes.length){
				if(tree.taxonInTree(mrcaDes[dCount]) && !tree.descendantOf(tree.nodeOfTaxonNumber(mrcaDes[dCount]), node)){
					allIncluded=false;
				}
				dCount++;
			}
			if(allIncluded){
				mNum.setValue(otherTree.getBranchLength(mrca));
			}
		}
		return mNum;
	 */		
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, Tree otherTree, int node, MesquiteNumber result, MesquiteString resultString) {
	   	clearResultAndLastResult(result);
		if(tree==null || otherTree==null){
			return;
		}
		if(tree.getTaxa()!=otherTree.getTaxa()){//Trees must operate on same taxa
			return;
		}
		if(!otherTree.hasBranchLengths()){
			result.setToUnassigned();
			resultString.setValue("Branch Length for node from other tree could not be calculated for node " + node + ".");
			saveLastResult(result);
			saveLastResultString(resultString);
			return;
		}
		MesquiteTree mTree = pruneToMatch(tree, otherTree);
		mTree.setName("Pruned version of " + tree.getName() + " based on " + otherTree.getName());
		if(tree.nodeExists(node) && tree.nodeIsInternal(node)){
			if(!descendantCheck(tree, mTree, node)){
				result.setToUnassigned();
				resultString.setValue("Branch Length for node from other tree could not be calculated for node " + node + ".");
				saveLastResult(result);
				saveLastResultString(resultString);
				return;
			}
			else {
				result.setValue(getValue(tree, otherTree, node));
				resultString.setValue("Result = " + result.toString());
			}
		}
		mTree.dispose();
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Branch Lengths for Node from Other Tree";
	}
	/*.................................................................................................................*/
	public String getExplanation(){
		return "Supplies a branch length for a node in a tree, where the branch length is based on another tree.  This " +
				"can be used to report support values (such as bootstrap proportions from PAUP) from one tree onto another tree.";
	}
}