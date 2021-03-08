package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanelAutomationHelper;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;

public class CheckedHelloNewPanelAutomationHelper extends CheckedHelloPanelAutomationHelper {

	private static Logger logger = Logger.getLogger(CheckedHelloNewPanelAutomationHelper.class.getName());

	private final RegistryHelper registryHelper;
    private final boolean registered;
    private InstallData installData;
    
	public CheckedHelloNewPanelAutomationHelper(RegistryDefaultHandler handler, InstallData installData) throws NativeLibException
    {
        super(handler, installData);
        this.registryHelper = new RegistryHelper(handler, installData);
        this.registered = registryHelper.isRegistered();
        this.installData = installData;
        
        String path = registryHelper.getInstallationPath();
		if (path != null) {
			installData.setVariable("TargetPanel.dir.windows", path);
			logger.log(Level.FINE, "Set TargetPanel.dir.windows: " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, "Set INSTALL_PATH", path);
		}
    }
	

    /**
     * {@inheritDoc}
     */
    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot)
    {
		// We had to override this method to remove APP_VER
		// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
    	Variables variables = this.installData.getVariables();		
        installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
    }

}
