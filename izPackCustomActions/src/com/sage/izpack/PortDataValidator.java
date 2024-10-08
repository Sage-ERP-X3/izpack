package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;

public class PortDataValidator implements DataValidator {

	@Override
	public Status validateData(InstallData adata) {
		InetAddress inet = null;
		String host = "localhost";
		Status retValue = Status.ERROR;

		String value = adata.getVariable("mongodb.service.port");

		// if update mode
		// load old value

		boolean updatemode = ModifyInstallationUtil.get(adata);

		if (updatemode) {
			// load old installadata
			try {
				FileInputStream fin = new FileInputStream(new File(
						adata.getInstallPath() + File.separator + InstallData.INSTALLATION_INFORMATION));
				ObjectInputStream oin = new ObjectInputStream(fin);
				List packsinstalled = (List) oin.readObject();
				Properties variables = (Properties) oin.readObject();
				fin.close();

				String oldPort = variables.getProperty("mongodb.service.port");

				if (value.equals(oldPort))
					return Status.OK;

			} catch (Exception ex) {
				ex.printStackTrace();
				return Status.ERROR;
			}

		}

		try {
			inet = InetAddress.getByName(host);
			ServerSocket socket = new ServerSocket(Integer.parseInt(value), 0, inet);
			if (socket.getLocalPort() > 0) {
				socket.close();
				return Status.OK;
			} else {
				return Status.WARNING;
			}
		} catch (Exception ex) {
			retValue = Status.ERROR;
		}

		return retValue;

	}

	@Override
	public String getErrorMessageId() {
		return "portvalidatorerror";
	}

	@Override
	public String getWarningMessageId() {
		return "portvalidatoralreadyinuse";
	}

	@Override
	public boolean getDefaultAnswer() {
		// by default if updating ourself then the port is already in use
		return true;
	}

}
