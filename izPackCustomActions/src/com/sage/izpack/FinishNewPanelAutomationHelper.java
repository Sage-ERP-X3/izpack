package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.finish.FinishPanelAutomation;

public class FinishNewPanelAutomationHelper extends FinishPanelAutomation {

	private static Logger logger = Logger.getLogger(FinishNewPanelAutomationHelper.class.getName());

	private ResourcesHelper _resourceHelper;
	
	public FinishNewPanelAutomationHelper(InstallData installData, Resources resources) throws NativeLibException
    {
		super();
		
		logger.log(Level.FINE, "FinishNewPanelAutomationHelper instance. Init custom resources");
		
		_resourceHelper = new ResourcesHelper(installData, resources);
		_resourceHelper.mergeCustomMessages();
		
		logger.log(Level.FINE, "FinishNewPanelAutomationHelper instance. Custom resources initialized");		
    }		
}
