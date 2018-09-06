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
package mesquite.hypha.aHyphaIntro;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class aHyphaIntro extends PackageIntro{

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public Class getDutyClass(){
		return aHyphaIntro.class;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getManualPath(){
		return getPath() +"/manual/index.html";  
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		if (MesquiteTrunk.debugMode){
			return "http://mesquiteproject.org/packages/hypha/noticesDev.xml";
		} else if (isPrerelease()){
			return "http://mesquiteproject.org/packages/hypha/noticesPrerelease.xml";
		} else return "http://mesquiteproject.org/packages/hypha/notices.xml";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Hypha package";
	}
	/*.................................................................................................................*/
	public String getPackageName() {
		return "Hypha package";
	}
	/*.................................................................................................................*/
	public String getExplanation(){
		return "The Hypha package includes modules for displaying support values from multiple trees on a single reference tree.";
	}
	/*.................................................................................................................*/
	public String getPackageCitation(){
		if(isPrerelease()){
		return "Oliver, J.C., Miadlikowska, J., Arnold, A.E., Maddison, D.R., & Lutzoni, F. 2013. Hypha: a Mesquite package for support value integration. Prerelease version " + getPackageVersion() + ".";
		} else return "Oliver, J.C., Miadlikowska, J., Arnold, A.E., Maddison, D.R., & Lutzoni, F. 2013. Hypha: a Mesquite package for support value integration. Version " + getPackageVersion() + ".";
	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
		return true; 
	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules*/
	public String getPackageVersion(){
		return "1.0";
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules as an integer*/
	public int getPackageVersionInt(){
		return 1000;
	}
	/*.................................................................................................................*/
	/** Returns build number for a package of modules as an integer*/
	public int getPackageBuildNumber(){
		return 2;
	}
/* release history:
	 */
	/*.................................................................................................................*/
	public String getPackageURL(){
		return "http://mesquiteproject.org/packages/hypha";
	}
	/*.................................................................................................................*/
	public String getPackageDateReleased(){
		return "31 May 2013";
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}
}