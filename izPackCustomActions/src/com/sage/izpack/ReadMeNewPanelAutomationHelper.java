package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.finish.FinishPanelAutomation;

public class ReadMeNewPanelAutomationHelper  extends FinishPanelAutomation{

	private static Logger logger = Logger.getLogger(ReadMeNewPanelAutomationHelper.class.getName());

		
	private ResourcesHelper _resourceHelper;
	
	public ReadMeNewPanelAutomationHelper(InstallData installData, Resources resources) throws NativeLibException
    {
		super();
		
		logger.log(Level.FINE, "ReadMeNewPanelAutomationHelper instance. Init custom resources");
		
		_resourceHelper = new ResourcesHelper(installData, resources);
		_resourceHelper.mergeCustomMessages();
		
		logger.log(Level.FINE, "ReadMeNewPanelAutomationHelper instance. Custom resources initialized");		
    }		

	
}
