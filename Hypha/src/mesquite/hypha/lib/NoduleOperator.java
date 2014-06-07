package mesquite.hypha.lib;

import java.awt.*;

import mesquite.lib.*;

public abstract class NoduleOperator extends TreeDisplayDrawnExtra {
	private NoduleCoordinator coordinatorModule; //TODO: remove?
	private Tree tree = treeDisplay.getTree();//TODO: OK?
//	NoduleLegend legend;
	MesquiteNumber result;
	MesquiteString resultString;
	
	/**Constructor method for NoduleOperator; should probably be overridden*/
	public NoduleOperator(NoduleCoordinator ownerModule, TreeDisplay treeDisplay){
		super(ownerModule, treeDisplay);
		coordinatorModule = ownerModule;
	}
	/*..................................................................*/
	/*If NoduleOperator has a legend, it should be instantiated in drawOnTree method (inherited from TreeDisplayDrawnExtra).*/
	public abstract void drawOnTree(Tree tree, int drawnRoot, Graphics g);
	/*..................................................................*/
	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
//TODO: include or delete:
//		treeDisplay.getTreeDrawing().drawHighlight(tree, drawnRoot, g, false);
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
		super.turnOff();
	}
	/*..................................................................*/
	/**Draws on branch <b>node</b>, potentially calling additional methods if multiple drawings
	 * are to be drawn on a single branch.  Should be called by drawOnTree method starting at root.
	 * Will likely involve recursion if entire tree is subject to drawing.*/
	public abstract void drawOnBranch(Tree tree, int node, Graphics g);
	
}
