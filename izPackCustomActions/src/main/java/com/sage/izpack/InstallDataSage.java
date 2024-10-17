package com.sage.izpack;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.util.Platform;

/*
 * Warning: Cannot use com.izforge.izpack.installer.data.InstallData within Uninstaller package: error NoClassDefFoundError
 * So, need to create our own class
 */
public class InstallDataSage extends AutomatedInstallData {

	public InstallDataSage(Variables variables, Platform platform) {
		super(variables, platform);
		
	}

}
