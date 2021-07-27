package com.sage.izpack;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.target.TargetPanel;

public class TargetNewPanel  extends  TargetPanel {

	private static Logger logger = Logger.getLogger(TargetNewPanel.class.getName());

	
	public TargetNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, resources, log);
	}

	
    @Override
    public void saveData()
    {
    	super.saveData();
        // String path = pathSelectionPanel.getPath();
        // installData.setInstallPath(path);
    }

    
}
