package com.sage.izpack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.util.OsVersion;

public class PreValidateServiceConfiguration implements com.izforge.izpack.data.PanelAction {

	private static final Logger logger = Logger.getLogger(PreValidateServiceConfiguration.class.getName());
	private static final String prefixLabel = "PreValidateServiceConfiguration - ";

	@Override
	public void executeAction(InstallData installData, AbstractUIHandler arg1) {

		if (OsVersion.IS_UNIX) {

			try {
				ProcessBuilder pb = new ProcessBuilder(new String[] { "id", "-gn" });
				Process pr = pb.start();
				// Process pr = rt.exec("id -gn");
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = input.readLine();
				installData.setVariable("GROUP_NAME", line);
				logger.log(Level.FINE, prefixLabel + "Set GROUP_NAME: " + line);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize(PanelActionConfiguration actionConfiguration) {
		// Nothing to do
	}

}
