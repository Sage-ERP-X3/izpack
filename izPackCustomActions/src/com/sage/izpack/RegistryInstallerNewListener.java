package com.sage.izpack;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Housekeeper;

/*
* @author Franck DEPOORTERE
*/
public class RegistryInstallerNewListener extends com.izforge.izpack.event.RegistryInstallerListener {

	public RegistryInstallerNewListener(IUnpacker unpacker, VariableSubstitutor substitutor, InstallData installData,
			UninstallData uninstallData, Resources resources, RulesEngine rules, Housekeeper housekeeper,
			RegistryDefaultHandler handler) {
		super(unpacker, substitutor, installData, uninstallData, resources, rules, housekeeper, handler);
	}

	
	@Override
	protected String getUninstallName() {
		Variables variables = getInstallData().getVariables();
		// We had to override this method to remove APP_VER
		// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
		return variables.get("APP_NAME");
	}

}
