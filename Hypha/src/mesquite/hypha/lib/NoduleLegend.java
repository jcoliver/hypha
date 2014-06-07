package mesquite.hypha.lib;

import java.awt.*;

import mesquite.lib.*;

public abstract class NoduleLegend extends TreeDisplayLegend {
	private LegendHolder coordinatorModule; //TODO: delete?
	protected static final int defaultLegendWidth=160;
	protected static final int defaultLegendHeight=180;
	protected boolean holding = false;
	protected final int defaultSpecsHeight = (120 + MesquiteModule.textEdgeCompensationHeight) * 1;
	protected String title = "Node Decor";
	protected Color titleColor = Color.BLACK;
	protected int specsHeight = defaultSpecsHeight;
	/**Extra bit of vertical space necessary for legend drawing*/
	protected int e = 4;
	protected int specsYStart = 14 + e;
	private int scrollAreaHeight = 41;//TODO: There is no scroll area...
	protected int topEdge = 6; //TODO: Used for popup menu operation, but perhaps incorrectly...
	/**Used to indicate popup menu available*/
	protected Polygon dropDownTriangle;

	/**Constructor method for NoduleLegend*/
	public NoduleLegend(LegendHolder coordinatorModule, TreeDisplay treeDisplay){
		super(treeDisplay, defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		setOffsetX(coordinatorModule.getInitialOffsetX());
		setOffsetY(coordinatorModule.getInitialOffsetY());
		this.coordinatorModule = coordinatorModule;
		setBackground(ColorDistribution.darkGreen);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		dropDownTriangle = MesquitePopup.getDropDownTriangle();
		if (coordinatorModule.showLegend()){
			reviseBounds();
		}
	}
	/*..................................................................*/
	/**Draws the legend*/
	abstract public void paint(Graphics g);
	/*..................................................................*/
	public void setVisible(boolean b) {
		super.setVisible(b);
	}
	/*..................................................................*/
	public void refreshSpecsBox(){
	}
	/*..................................................................*/
//	abstract public void printAll(Graphics g);
	/*..................................................................*/
	/**Adds the title to legend*/
	public void printAll(Graphics g){
		g.setColor(Color.black);
		g.drawString(title, 4, 14);
	}
	/*..................................................................*/
	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight = defaultSpecsHeight;
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
	public void checkComponentSizes(){
		legendHeight=specsHeight + specsYStart;
	}
	/*..................................................................*/
	public void onHold() {
		holding = true;
	}
	/*..................................................................*/
	public void offHold() {
		holding = false;
	}
}
