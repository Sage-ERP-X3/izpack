package com.sage.izpack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.util.OsVersion;

public class PreValidateServiceGUIDConfiguration implements com.izforge.izpack.data.PanelAction {

	private static final Logger logger = Logger.getLogger(PreValidateServiceGUIDConfiguration.class.getName());
	private static final String prefixLabel = "PreValidateServiceGUIDConfiguration - ";

	@Override
	public void executeAction(InstallData installData, AbstractUIHandler arg1) {

		boolean reachable = false;
		Random random = new Random();
		long msb = random.nextLong();
		long lsb = random.nextLong();
		UUID uuid = new UUID(msb, lsb);
		System.out.println(uuid); // Output: a custom-generated UUID

		// installData.setVariable("userinput.guid.clientid", uuid.toString());
		installData.setVariable("syracuse.clientid", uuid.toString());
		// 		
		GeneratePasswordHelper helper = new GeneratePasswordHelper(installData);
		String password = helper.generateStrongPassword(20);
		System.out.println(password);
		installData.setVariable("syracuse.secret", password);		
	}

	@Override
	public void initialize(PanelActionConfiguration actionConfiguration) {
		// Nothing to do
	}

}
