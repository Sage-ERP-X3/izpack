package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.finish.FinishPanelAutomation;

public class ReadMeNewPanelAutomationHelper implements com.izforge.izpack.installer.automation.PanelAutomation{

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

	@Override
	public void createInstallationRecord(InstallData arg0, IXMLElement arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processOptions(InstallData arg0, Overrides arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runAutomated(InstallData arg0, IXMLElement arg1) throws InstallerException {
		// TODO Auto-generated method stub
		
	}		

	
}
