package mesquite.hypha.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

//public abstract class NoduleCoordinator extends TreeDisplayAssistantAO implements LegendHolder{
public abstract class NoduleCoordinator extends TreeDisplayAssistantA implements LegendHolder{
	//TODO:Are all these color objects necessary?
	public MesquiteSubmenuSpec aboveTMenuItem, belowTMenuItem, inAMenuItem;
	protected Color inAppColor = Color.red;
	protected Color belowThreshColor = Color.white;
	protected Color aboveThreshColor = Color.black;
	public MesquiteString atColorName, btColorName, inAColorName;
	public Vector nodeDecor;
	public int initialOffsetX=MesquiteInteger.unassigned;
	public int initialOffsetY= MesquiteInteger.unassigned;
	String title;//TODO??

	/*..................................................................*/
	public abstract TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay);
	/*..................................................................*/
	public abstract void closeAllNodeOperators();
	/*..................................................................*/
	public abstract void redraw();
	/*..................................................................*/
	//TODO: this method should probably be more flushed out...
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification){
		redraw();
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
	public int getInitialOffsetX() {
		return initialOffsetX;
	}
	/*..................................................................*/
	public int getInitialOffsetY() {
		return initialOffsetY;
	}
	/*..................................................................*/
	public boolean showLegend(){
		return true;
	}
	public Color getInAppColor() {
		return inAppColor;
	}
	public Color getAboveThreshColor() {
		return aboveThreshColor;
	}
	public Color getBelowThreshColor() {
		return belowThreshColor;
	}
}
