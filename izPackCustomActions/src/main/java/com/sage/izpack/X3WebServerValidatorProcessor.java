package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

public class X3WebServerValidatorProcessor implements Validator, Processor {

	@Override
	public boolean validate(ProcessingClient client) {
		boolean bReturn = false;
		String strDataPath = getWebserverPath(client.getFieldContents(0).trim());
		if (strDataPath != null) {
			bReturn = true;
		}

		return bReturn;
	}

	@Override
	public String process(ProcessingClient client) {
		String strDataPath = getWebserverPath(client.getFieldContents(0).trim());
		return strDataPath + File.separator + "KEYSTORE" + File.separator + "SYRACUSE";
	}

	private String getWebserverPath(String x3webPath) {
		try {

			// String x3webPath =
			// adata.getVariable("syracuse.certificate.x3webserver").trim();

			File X3WebInstallInformation = new File(x3webPath + "/" + InstallData.INSTALLATION_INFORMATION);
			if (X3WebInstallInformation.exists() && X3WebInstallInformation.isFile()) {
				// we need to load it to find where is stored the data directory

				FileInputStream fin = new FileInputStream(X3WebInstallInformation);
				ObjectInputStream oin = new ObjectInputStream(fin);
				Properties variables = (Properties) oin.readObject();

				String strDataPath = variables.getProperty("webserver.dir.data");
				if (strDataPath != null) {
					return strDataPath;
				}

			}

		} catch (Exception ex) {
			// got exception
			ex.printStackTrace();
		}
		return null;
	}
}
