package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.target.TargetPanel;

/*
 *  @author Franck DEPOORTERE
 */
public class TargetNewPanel extends TargetPanel {

	private static Logger logger = Logger.getLogger(TargetNewPanel.class.getName());

	public TargetNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, resources, log);
	}

	

	// TODO: FRDEPO => check if necessary
    // public static String loadDefaultDirFromVariables(Properties vars)
    //{
    	// Version 5.2: Seems Equivalent to 
    	// String path = InstallPathHelper.getPath(installData);
    	
		// Version 4.3:
        //String os = System.getProperty("os.name").replace(' ', '_').toLowerCase();        
        // String path = vars.getProperty("TargetPanel.dir.".concat(os));
        
        // if (path == null) {
        //    path = vars.getProperty("TargetPanel.dir." + (OsVersion.IS_WINDOWS ? "windows" : (OsVersion.IS_OSX ? "macosx" : "unix")));
        //    if (path == null) {
        //        path = vars.getProperty("TargetPanel.dir");
        //    }
        //}
        // if (path != null) {
        //    path = new VariableSubstitutor(vars).substitute(path, null);
        //}
        
       // return path;
    //}
    
	/**
	 * Called when the panel becomes active.
	 */
	@Override
	public void panelActivate() {

		logger.log(Level.FINE, "TargetNewPanel.panelActivate");
		super.panelActivate();
		logger.log(Level.FINE, "TargetNewPanel.panelActivate  Path: " + pathSelectionPanel.getPath());
	}


	@Override
	public void saveData() {

		logger.log(Level.FINE, "TargetNewPanel.saveData");

		// <ComponentName>c:\Sage\SafeX3\${component.node.name}</ComponentName>
		String platformSep = "[\\\\\\/]";// "\\\\"; 
		int positionFromEnd = 0;
		String variableNameRaw = getMetadata().getConfigurationOptionValue("SetVariableName", installData.getRules());
		String variableName = null;
		logger.log(Level.FINE, "TargetNewPanel.saveData  variableNameRaw: " + variableNameRaw);

		if (variableNameRaw != null && variableNameRaw.indexOf("${") >= 0 && variableNameRaw.indexOf("}") >= 0) {

			variableName = variableNameRaw.substring(variableNameRaw.indexOf("${") + 2, variableNameRaw.indexOf("}"));

			String[] pathElements = variableNameRaw.split(platformSep);
			for (int i = 0; i < pathElements.length; i++) {
				String part = pathElements[i];
				if (part.equals(variableName)) {
					positionFromEnd = pathElements.length - 1 - i;
				}
			}
			logger.log(Level.FINE, "TargetNewPanel.saveData  variableName: '" + variableName + "'  positionFromEnd: "
					+ positionFromEnd);
		}

		super.saveData();
		String path = pathSelectionPanel.getPath();

		logger.log(Level.FINE, "TargetNewPanel.saveData  Path: " + path);
		if (variableName != null) {

			String[] pathElements = path.split(platformSep);
			String variableValue = pathElements[(pathElements.length - 1 - positionFromEnd)];
			installData.setVariable(variableName, variableValue);
			logger.log(Level.FINE, "TargetNewPanel.saveData path: " + path + "  setVariable  '" + variableName + "': "
					+ variableValue);		
			}				
		
	}

}
